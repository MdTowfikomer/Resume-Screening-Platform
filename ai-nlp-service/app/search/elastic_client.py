"""
Elasticsearch & Hybrid Vector Search Engine
============================================
Combines traditional BM25 Lexical Keyword Search with Dense Vector
Semantic Search using Reciprocal Rank Fusion (RRF).
Supports live Elasticsearch 8/9 and includes a deterministic in-memory
Vector + Lexical simulation for standalone execution.
"""

import os
import math
import logging
from typing import Dict, Any, List, Optional
import numpy as np

logger = logging.getLogger(__name__)

ELASTIC_URL = os.getenv("ELASTICSEARCH_URL", "http://localhost:9200")
ELASTIC_USER = os.getenv("ELASTIC_USER", "elastic")
ELASTIC_PASSWORD = os.getenv("ELASTIC_PASSWORD", "password123")
INDEX_NAME = os.getenv("ELASTIC_INDEX", "resumes-vector-idx")

class ElasticsearchEngine:
    def __init__(self):
        self.client = None
        self.is_connected = False
        self._memory_index: Dict[str, Dict[str, Any]] = {}
        self.connect()

    def connect(self):
        """Attempt connection to Elasticsearch cluster."""
        try:
            from elasticsearch import Elasticsearch
            es = Elasticsearch(
                ELASTIC_URL,
                basic_auth=(ELASTIC_USER, ELASTIC_PASSWORD) if ELASTIC_PASSWORD else None,
                request_timeout=1.5,
                verify_certs=False
            )
            if es.ping():
                self.client = es
                self.is_connected = True
                self._ensure_index()
                logger.info(f"Connected to Elasticsearch at {ELASTIC_URL} [Index: {INDEX_NAME}]")
            else:
                self._fallback()
        except Exception as e:
            logger.warning(
                f"Elasticsearch not reachable at {ELASTIC_URL} ({e}). "
                "Operating in resilient Hybrid Vector Search In-Memory mode."
            )
            self._fallback()

    def _fallback(self):
        self.is_connected = False
        self.client = None

    def _ensure_index(self):
        """Ensure vector search mapping exists in Elasticsearch 8/9."""
        if not self.is_connected or not self.client:
            return
        try:
            if not self.client.indices.exists(index=INDEX_NAME):
                mapping = {
                    "mappings": {
                        "properties": {
                            "id": {"type": "keyword"},
                            "candidate_name": {"type": "text"},
                            "skills": {"type": "keyword"},
                            "resume_text": {"type": "text"},
                            "experience_years": {"type": "float"},
                            "embedding": {
                                "type": "dense_vector",
                                "dims": 64,
                                "index": True,
                                "similarity": "cosine"
                            }
                        }
                    }
                }
                self.client.indices.create(index=INDEX_NAME, body=mapping)
                logger.info(f"Created Elasticsearch vector index: {INDEX_NAME}")
        except Exception as e:
            logger.error(f"Error ensuring index in Elasticsearch: {e}")

    def index_document(self, doc_id: str, document: Dict[str, Any], vector: List[float]) -> bool:
        """Index a document with text and semantic vector representation."""
        indexed_doc = {
            "id": doc_id,
            "candidate_name": document.get("candidate_name", "Anonymous Candidate"),
            "skills": document.get("technical_skills", []),
            "soft_skills": document.get("soft_skills", []),
            "experience_years": document.get("experience_years", 0.0),
            "summary": document.get("summary", ""),
            "resume_text": document.get("raw_text", ""),
            "embedding": vector
        }

        # Index in live Elasticsearch if active
        if self.is_connected and self.client:
            try:
                self.client.index(index=INDEX_NAME, id=doc_id, document=indexed_doc)
                return True
            except Exception as e:
                logger.error(f"Elasticsearch index error: {e}. Storing in memory.")

        # Always maintain in memory index
        self._memory_index[doc_id] = indexed_doc
        return True

    def lexical_search(self, query: str, top_k: int = 10) -> List[Dict[str, Any]]:
        """BM25 Full-text keyword search."""
        if self.is_connected and self.client:
            try:
                res = self.client.search(
                    index=INDEX_NAME,
                    body={
                        "size": top_k,
                        "query": {
                            "multi_match": {
                                "query": query,
                                "fields": ["resume_text^2", "skills^3", "candidate_name", "summary"]
                            }
                        }
                    }
                )
                hits = []
                for h in res["hits"]["hits"]:
                    src = h["_source"]
                    hits.append({
                        "id": src["id"],
                        "candidate_name": src["candidate_name"],
                        "skills": src["skills"],
                        "experience_years": src["experience_years"],
                        "summary": src["summary"],
                        "score": round(float(h["_score"]), 4)
                    })
                return hits
            except Exception as e:
                logger.error(f"Elasticsearch lexical search error: {e}")

        # In-memory BM25 approximation
        terms = [t.lower() for t in query.split() if len(t) > 1]
        results = []
        for doc_id, doc in self._memory_index.items():
            text = (doc["resume_text"] + " " + " ".join(doc["skills"]) + " " + doc["candidate_name"]).lower()
            score = 0.0
            for term in terms:
                count = text.count(term)
                if count > 0:
                    score += (count * 1.5) / (count + 1.2)
            if score > 0:
                results.append({
                    "id": doc_id,
                    "candidate_name": doc["candidate_name"],
                    "skills": doc["skills"],
                    "experience_years": doc["experience_years"],
                    "summary": doc["summary"],
                    "score": round(score, 4)
                })
        results.sort(key=lambda x: x["score"], reverse=True)
        return results[:top_k]

    def vector_search(self, query_vector: List[float], top_k: int = 10) -> List[Dict[str, Any]]:
        """Dense vector semantic kNN similarity search."""
        if self.is_connected and self.client:
            try:
                res = self.client.search(
                    index=INDEX_NAME,
                    body={
                        "size": top_k,
                        "knn": {
                            "field": "embedding",
                            "query_vector": query_vector,
                            "k": top_k,
                            "num_candidates": 50
                        }
                    }
                )
                hits = []
                for h in res["hits"]["hits"]:
                    src = h["_source"]
                    hits.append({
                        "id": src["id"],
                        "candidate_name": src["candidate_name"],
                        "skills": src["skills"],
                        "experience_years": src["experience_years"],
                        "summary": src["summary"],
                        "score": round(float(h["_score"]), 4)
                    })
                return hits
            except Exception as e:
                logger.error(f"Elasticsearch vector search error: {e}")

        # In-memory cosine similarity
        q_vec = np.array(query_vector, dtype=np.float32)
        q_norm = np.linalg.norm(q_vec)
        if q_norm == 0:
            q_norm = 1.0

        results = []
        for doc_id, doc in self._memory_index.items():
            d_vec = np.array(doc["embedding"], dtype=np.float32)
            d_norm = np.linalg.norm(d_vec)
            if d_norm > 0:
                similarity = float(np.dot(q_vec, d_vec) / (q_norm * d_norm))
            else:
                similarity = 0.0

            results.append({
                "id": doc_id,
                "candidate_name": doc["candidate_name"],
                "skills": doc["skills"],
                "experience_years": doc["experience_years"],
                "summary": doc["summary"],
                "score": round(similarity, 4)
            })

        results.sort(key=lambda x: x["score"], reverse=True)
        return results[:top_k]

    def hybrid_search(self, query: str, query_vector: List[float], top_k: int = 10, rrf_k: int = 60) -> List[Dict[str, Any]]:
        """
        State-of-the-art Hybrid Search using Reciprocal Rank Fusion (RRF).
        Score(d) = 1/(k + rank_bm25) + 1/(k + rank_vector)
        """
        lexical_hits = self.lexical_search(query, top_k=top_k * 2)
        vector_hits = self.vector_search(query_vector, top_k=top_k * 2)

        rrf_scores: Dict[str, float] = {}
        doc_lookup: Dict[str, Dict[str, Any]] = {}
        lexical_ranks: Dict[str, int] = {}
        vector_ranks: Dict[str, int] = {}

        # Record lexical ranks
        for rank, hit in enumerate(lexical_hits, start=1):
            did = hit["id"]
            doc_lookup[did] = hit
            lexical_ranks[did] = rank
            rrf_scores[did] = rrf_scores.get(did, 0.0) + (1.0 / (rrf_k + rank))

        # Record vector ranks
        for rank, hit in enumerate(vector_hits, start=1):
            did = hit["id"]
            doc_lookup[did] = hit
            vector_ranks[did] = rank
            rrf_scores[did] = rrf_scores.get(did, 0.0) + (1.0 / (rrf_k + rank))

        # Format hybrid fused results
        fused = []
        for did, score in sorted(rrf_scores.items(), key=lambda item: item[1], reverse=True)[:top_k]:
            doc_info = dict(doc_lookup[did])
            doc_info["rrf_score"] = round(score, 5)
            doc_info["lexical_rank"] = lexical_ranks.get(did, None)
            doc_info["vector_rank"] = vector_ranks.get(did, None)
            fused.append(doc_info)

        return fused

    def status(self) -> Dict[str, Any]:
        """Status information for monitoring & dashboard."""
        count = len(self._memory_index)
        if self.is_connected and self.client:
            try:
                count = self.client.count(index=INDEX_NAME)["count"]
            except Exception:
                pass

        return {
            "connected": self.is_connected,
            "mode": "Live Elasticsearch 8/9" if self.is_connected else "In-Memory Hybrid Vector Engine Fallback",
            "cluster_url": ELASTIC_URL if self.is_connected else "in-memory://",
            "index_name": INDEX_NAME,
            "total_indexed": count,
            "features": [
                "BM25 Lexical Keyword Search",
                "Dense Vector Cosine kNN Search",
                "Reciprocal Rank Fusion (RRF) Hybrid Search"
            ]
        }

elastic_engine = ElasticsearchEngine()
