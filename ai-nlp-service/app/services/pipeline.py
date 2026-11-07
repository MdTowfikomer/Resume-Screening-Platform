"""
Real-Time MongoDB to Elasticsearch Sync Pipeline
=================================================
Ensures MongoDB acts as the Primary System of Record while Elasticsearch
is continuously updated with dense vector embeddings and full-text indexes.
"""

import logging
from typing import Dict, Any, List
from app.database.mongo_client import mongo_engine
from app.search.elastic_client import elastic_engine
from app.ai.embedder import generate_embedding

logger = logging.getLogger(__name__)

class PipelineService:
    def sync_resume(self, resume_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        1. Persist transactional document to MongoDB.
        2. Generate semantic vector embedding.
        3. Index document + vector into Elasticsearch.
        """
        # Step 1: Save to MongoDB (Source of Truth)
        resume_id = mongo_engine.insert_resume(resume_data)

        # Step 2: Generate Vector
        searchable_text = f"{resume_data.get('candidate_name', '')} {resume_data.get('summary', '')} {' '.join(resume_data.get('technical_skills', []))} {resume_data.get('raw_text', '')}"
        vector = generate_embedding(searchable_text)

        # Step 3: Index to Elasticsearch
        elastic_engine.index_document(resume_id, resume_data, vector)

        logger.info(f"Pipeline synchronized Resume [{resume_id}] MongoDB -> Elasticsearch")
        return {
            "id": resume_id,
            "status": "synchronized",
            "mongo_stored": True,
            "elasticsearch_indexed": True,
            "vector_dims": len(vector)
        }

    def seed_sample_candidates(self) -> List[str]:
        """Seed initial realistic candidate profiles into MongoDB + Elasticsearch."""
        samples = [
            {
                "id": "cand-001",
                "candidate_name": "Aarav Sharma",
                "candidate_email": "aarav.sharma@example.com",
                "experience_years": 7.5,
                "technical_skills": ["Python", "FastAPI", "MongoDB", "Elasticsearch", "Docker", "Kubernetes", "Redis", "Kafka"],
                "soft_skills": ["Leadership", "Communication", "System Design"],
                "summary": "Senior Backend Architect with 7+ years building distributed search and event-driven systems using FastAPI, MongoDB, and Elasticsearch.",
                "raw_text": "Experienced Senior Backend Engineer specializing in Python, FastAPI, MongoDB, Elasticsearch, and Kubernetes. Architected high-throughput data synchronization pipelines and hybrid search services with Redis caching."
            },
            {
                "id": "cand-002",
                "candidate_name": "Sophia Chen",
                "candidate_email": "sophia.chen@example.com",
                "experience_years": 5.0,
                "technical_skills": ["Python", "PyTorch", "NLP", "Machine Learning", "RAG", "Vector Search", "LangChain", "OpenAI"],
                "soft_skills": ["Problem Solving", "Collaboration", "Research"],
                "summary": "AI/ML Engineer focused on NLP, Semantic Vector Search, and Retrieval-Augmented Generation (RAG) applications.",
                "raw_text": "Machine Learning Engineer with 5 years experience in natural language processing (NLP), PyTorch, vector databases, LangChain, and fine-tuning embeddings for semantic talent search."
            },
            {
                "id": "cand-003",
                "candidate_name": "Marcus Vance",
                "candidate_email": "marcus.v@example.com",
                "experience_years": 9.0,
                "technical_skills": ["AWS", "Terraform", "Kubernetes", "Docker", "CI/CD", "Prometheus", "Grafana", "Go"],
                "soft_skills": ["Mentoring", "Agile", "DevOps Culture"],
                "summary": "Lead Cloud Infrastructure & Platform Engineer specializing in multi-region Kubernetes clusters and AWS infrastructure as code.",
                "raw_text": "Staff DevOps Engineer with 9 years managing AWS, GCP, Docker, Kubernetes clusters, Helm charts, and automated GitHub Actions CI/CD pipelines."
            },
            {
                "id": "cand-004",
                "candidate_name": "Priya Patel",
                "candidate_email": "priya.patel@example.com",
                "experience_years": 4.5,
                "technical_skills": ["React", "TypeScript", "Node.js", "Express", "TailwindCSS", "Next.js", "MongoDB"],
                "soft_skills": ["UI/UX Design", "Communication", "Teamwork"],
                "summary": "Full-Stack Web Developer skilled in React, TypeScript, Next.js, and Node.js REST APIs with MongoDB.",
                "raw_text": "Frontend and Full-Stack Developer with 4.5 years creating responsive, high-performance web applications using React, TypeScript, Next.js, and MongoDB backends."
            }
        ]

        ids = []
        for cand in samples:
            self.sync_resume(cand)
            ids.append(cand["id"])

        logger.info(f"Seeded {len(ids)} candidate profiles into MongoDB & Elasticsearch")
        return ids

pipeline_service = PipelineService()
