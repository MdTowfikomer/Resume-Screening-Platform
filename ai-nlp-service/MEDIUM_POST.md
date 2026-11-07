# Building an AI-Powered Talent Search Engine with MongoDB, Elasticsearch, and Vector Hybrid Search

*How we unified document persistence, Lucene full-text indexing, and dense vector embeddings using Reciprocal Rank Fusion (RRF) and FastAPI.*

---

![Architecture Overview](medium-hero.png)

In today's hyper-competitive hiring landscape, traditional applicant tracking systems (ATS) face a fundamental dilemma: **keyword matching is brittle, while pure vector semantic search often misses exact technical requirements.**

If a job posting requires `PostgreSQL`, `FastAPI`, and `AWS Certified Solutions Architect`, a traditional BM25 search will fail to recognize that a candidate with "Deep experience scaling relational databases on AWS" has relevant skills. Conversely, a pure vector embedding search might retrieve a candidate who writes eloquently about backend architecture but lacks experience with PostgreSQL.

To solve this, we architected and deployed a **production-grade AI Talent Screening & Search Platform**. In this article, we'll break down how we combined **MongoDB** (as the transactional document store), **Elasticsearch** (for lexical BM25 + dense vector similarity), and an **AI extraction pipeline** using **Reciprocal Rank Fusion (RRF)**.

---

## 1. System Architecture: The Dual-Store Pattern

A common anti-pattern in search-heavy architectures is forcing a single database to serve as both the transactional source of truth and the vector search engine. 

Instead, we adopted the **Dual-Store Pattern**:

```
                       ┌──────────────────────────────────────────────┐
                       │           Recruiter / Client Web UI          │
                       └──────────────────────┬───────────────────────┘
                                              │ HTTP / REST
                       ┌──────────────────────▼───────────────────────┐
                       │          FastAPI Orchestration Layer         │
                       └──────────────┬───────────────────────┬───────┘
                                      │                       │
                       ┌──────────────▼──────┐ ┌──────────────▼───────────┐
                       │      MongoDB        │ │      Elasticsearch       │
                       │  (Source of Truth)  │ │   (Vector + BM25 Search) │
                       │                     │ │                          │
                       │ • Resumes & Profile │ │ • Inverted Text Indices  │
                       │ • Job Requisitions  │ │ • 64-dim Dense Vectors   │
                       │ • Candidate History │ │ • Hybrid RRF Search      │
                       └─────────────────────┘ └──────────────────────────┘
```

### Why MongoDB as the Document Store?
Resumes are inherently semi-structured. Work experience, certifications, educational timelines, and varying skill taxonomies map naturally to JSON/BSON documents. MongoDB provides:
- **Flexible Schema Evolution**: New extracted NLP fields can be added without database migrations.
- **Transactional Integrity**: Ensures candidate records and applications are committed reliably.

### Why Elasticsearch for AI Search?
Elasticsearch 8+ excels at combining **Lucene BM25 inverted indexes** with **dense vector kNN nearest neighbor search**. This gives us:
- Tokenized fuzzy keyword matching for acronyms (`k8s`, `AWS`, `CI/CD`).
- HNSW dense vector indexing for conceptual matching ("distributed cloud computing").

---

## 2. Bridging the Gap: Real-Time Synchronization Pipeline

Every time a candidate uploads a resume or updates their profile, our pipeline handles ingestion in three distinct stages:

```python
class PipelineService:
    def sync_resume(self, resume_data: Dict[str, Any]) -> Dict[str, Any]:
        # Step 1: Persist transactional document in MongoDB
        resume_id = mongo_engine.insert_resume(resume_data)

        # Step 2: Generate 64-dimensional semantic embedding
        searchable_text = f"{resume_data.get('candidate_name')} {resume_data.get('summary')} {' '.join(resume_data.get('technical_skills', []))} {resume_data.get('raw_text', '')}"
        vector = generate_embedding(searchable_text)

        # Step 3: Index document and vector into Elasticsearch
        elastic_engine.index_document(resume_id, resume_data, vector)

        return {
            "id": resume_id,
            "status": "synchronized",
            "mongo_stored": True,
            "elasticsearch_indexed": True
        }
```

This guarantees that MongoDB remains the durable system of record, while Elasticsearch is continuously hydrated for search queries.

---

## 3. The Secret Sauce: Hybrid Search & Reciprocal Rank Fusion (RRF)

The biggest challenge in hybrid retrieval is **score normalization**. BM25 scores are unbounded positive numbers (e.g., $1.2$ to $18.5$), whereas cosine similarity scores are bounded between $-1.0$ and $+1.0$. Simply adding them together creates skewed results.

To solve this, we implemented **Reciprocal Rank Fusion (RRF)**. RRF relies on the *rank position* rather than the raw score:

$$RRF\_Score(d) = \sum_{m \in M} \frac{1}{k + r_m(d)}$$

Where:
- $M$ is the set of search modalities (BM25 Lexical + Dense Vector).
- $r_m(d)$ is the 1-based rank of document $d$ in system $m$.
- $k$ is a smoothing constant (standard value: $60$) to prevent top-ranked outliers from overpowering the fused score.

Here is the exact implementation in our Elasticsearch engine:

```python
def hybrid_search(self, query: str, query_vector: List[float], top_k: int = 10, rrf_k: int = 60) -> List[Dict[str, Any]]:
    # Retrieve top candidates from both channels
    lexical_hits = self.lexical_search(query, top_k=top_k * 2)
    vector_hits = self.vector_search(query_vector, top_k=top_k * 2)

    rrf_scores: Dict[str, float] = {}
    doc_lookup: Dict[str, Dict[str, Any]] = {}
    lexical_ranks: Dict[str, int] = {}
    vector_ranks: Dict[str, int] = {}

    # 1. Accumulate Lexical Ranks
    for rank, hit in enumerate(lexical_hits, start=1):
        did = hit["id"]
        doc_lookup[did] = hit
        lexical_ranks[did] = rank
        rrf_scores[did] = rrf_scores.get(did, 0.0) + (1.0 / (rrf_k + rank))

    # 2. Accumulate Dense Vector Ranks
    for rank, hit in enumerate(vector_hits, start=1):
        did = hit["id"]
        doc_lookup[did] = hit
        vector_ranks[did] = rank
        rrf_scores[did] = rrf_scores.get(did, 0.0) + (1.0 / (rrf_k + rank))

    # 3. Sort by Fused Reciprocal Rank Score
    sorted_docs = sorted(rrf_scores.items(), key=lambda x: x[1], reverse=True)[:top_k]
    
    return [
        {
            **doc_lookup[did],
            "rrf_score": round(score, 5),
            "lexical_rank": lexical_ranks.get(did),
            "vector_rank": vector_ranks.get(did)
        }
        for did, score in sorted_docs
    ]
```

---

## 4. Benchmark: Lexical vs. Vector vs. Hybrid RRF

To validate our approach, we tested search relevance across tricky candidate search scenarios:

| Query | Pure BM25 Keyword | Pure Vector Search | Hybrid (RRF) |
|---|---|---|---|
| *"FastAPI backend engineer with MongoDB"* | Finds exact keyword hits, misses candidates with "Python async web frameworks" | Captures general Python devs, but might surface Flask or Django devs higher | **Finds candidates with exact FastAPI/MongoDB experience ranked at #1** |
| *"AI engineer fine-tuning embeddings"* | Misses candidates unless exact term "fine-tuning" is present | High semantic recall, but misses specific vector DB proficiencies | **Surfaces PyTorch/RAG developers with vector database experience** |
| *"Cloud SRE with Kubernetes and Terraform"* | Strong on tech keywords, misses leadership context | Captures general sysadmins | **Perfect balance of platform tooling + domain seniority** |

---

## 5. Running & Deploying the Project

We designed the service to run out of the box with zero setup friction:

### Local Execution (FastAPI + Embedded Resilience)
```bash
# Clone the repository
git clone https://github.com/rahul-ghadge/ai-resume-screening-platform.git
cd ai-resume-screening-platform/ai-nlp-service

# Install dependencies (blazing fast with uv)
uv venv .venv
.\.venv\Scripts\activate
uv pip install -r requirements.txt

# Run the server
python run.py
```
Visit `http://localhost:8000` to interact with the Candidate Ledger & Evaluation Workbench or `http://localhost:8000/docs` for the interactive Swagger API.

### Live Production Deployment (Vercel Full-Stack)
The platform is deployed live as a unified Serverless ASGI application on Vercel:
- **Live Demo**: [https://ai-talent-search-frontend.vercel.app](https://ai-talent-search-frontend.vercel.app)
- **API Health**: [https://ai-talent-search-frontend.vercel.app/health](https://ai-talent-search-frontend.vercel.app/health)

---

## Key Takeaways

1. **Don't force one database to do everything**: Use MongoDB Atlas for agile, document-based operational storage, and Elasticsearch for low-latency retrieval.
2. **Hybrid Search is the new standard**: Pure keyword search is too rigid; pure vector search is too fuzzy. **Reciprocal Rank Fusion (RRF)** bridges the gap without requiring complex score normalization.
3. **Resilience matters**: By adding fallback stores, local development and automated testing can proceed uninterrupted even before full cloud clusters are provisioned.

---

### Resources & Source Code
- **Live Application**: [https://ai-talent-search-frontend.vercel.app](https://ai-talent-search-frontend.vercel.app)
- **GitHub Repository**: [MdTowfikomer/Resume-Screening-Platform](https://github.com/MdTowfikomer/Resume-Screening-Platform)
- **Tech Stack**: FastAPI, MongoDB Atlas, Elasticsearch Cloud Serverless 9.7, Reciprocal Rank Fusion (RRF), Python 3.12, Tailwind CSS.

*If you found this architecture breakdown helpful, leave a few claps 👏 and share your thoughts in the comments!*
