# 🎯 AI Talent Screening & Hybrid Search Platform
### Fusing MongoDB Atlas & Elasticsearch Cloud with Reciprocal Rank Fusion (RRF)

[![Live Web Application](https://img.shields.io/badge/Live%20Demo-ai--talent--search.vercel.app-2563eb?style=for-the-badge&logo=vercel)](https://ai-talent-search.vercel.app/)
[![Medium Technical Deep Dive](https://img.shields.io/badge/Medium-Read%20Article-black?style=for-the-badge&logo=medium)](https://medium.com/@mdtowfikomer/building-an-ai-powered-talent-search-engine-with-mongodb-elasticsearch-and-vector-hybrid-search-2731de6a4fc7)
[![Python](https://img.shields.io/badge/Python-3.12-blue?style=for-the-badge&logo=python)](https://python.org)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.110+-009688?style=for-the-badge&logo=fastapi)](https://fastapi.tiangolo.com)
[![MongoDB](https://img.shields.io/badge/MongoDB%20Atlas-Online-47A248?style=for-the-badge&logo=mongodb)](https://www.mongodb.com/)
[![Elasticsearch](https://img.shields.io/badge/Elasticsearch-Serverless%209.7-005571?style=for-the-badge&logo=elasticsearch)](https://www.elastic.co/)

---

## 📌 Executive Summary

Traditional Applicant Tracking Systems (ATS) struggle with two extremes:
1. **Lexical Keyword Matching (BM25)** is brittle — it misses qualified candidates whose resumes describe identical proficiencies with different wording.
2. **Dense Vector Semantic Search** lacks precision — it often hallucinates conceptual matches while ignoring hard technical prerequisites.

This platform implements the **Dual-Store Architecture** to bridge this gap:
- **MongoDB Atlas** serves as the transactional **Source of Truth** for candidate resumes and job postings.
- **Elasticsearch Cloud (Serverless v9.7)** indexes **64-dimensional dense semantic vectors** alongside BM25 inverted text indices.
- **Reciprocal Rank Fusion (RRF)** combines both rankings with a smoothing constant ($k=60$) to surface candidates with both exact technical skills and contextual relevance.

---

## 🏛️ System Architecture

```
                       ┌──────────────────────────────────────────────┐
                       │          Client Web UI (Vercel Edge)         │
                       │        https://ai-talent-search.vercel.app   │
                       └──────────────────────┬───────────────────────┘
                                              │ HTTP / JSON
                       ┌──────────────────────▼───────────────────────┐
                       │        FastAPI Unified ASGI Service          │
                       │   • NLP Skill Extraction & NER               │
                       │   • 64-dim Dense Vector Embeddings           │
                       │   • Reciprocal Rank Fusion Engine            │
                       └──────────────┬───────────────────────┬───────┘
                                      │                       │
                       ┌──────────────▼──────┐ ┌──────────────▼───────────┐
                       │   MongoDB Atlas     │ │   Elasticsearch Cloud    │
                       │  (Source of Truth)  │ │   (Vector + BM25 Engine) │
                       │                     │ │                          │
                       │ • Resume Documents  │ │ • BM25 Inverted Index    │
                       │ • Candidate History │ │ • Dense Cosine kNN       │
                       │ • Flexible JSON/BSON│ │ • Reciprocal Rank Fusion │
                       └─────────────────────┘ └──────────────────────────┘
```

---

## 🔬 The Math: Reciprocal Rank Fusion (RRF)

Score normalization between unbounded BM25 scores and bounded cosine similarities often produces distorted rankings. **RRF** overcomes this by evaluating rank positions:

$$\text{RRF Score}(d) = \sum_{m \in M} \frac{1}{k + r_m(d)}$$

Where:
- $M$ is the set of retrieval modalities (BM25 Lexical + Dense Vector).
- $r_m(d)$ is the 1-based rank position of document $d$ in modality $m$.
- $k = 60$ is the standard smoothing parameter that balances high-rank precision with long-tail recall.

---

## 🚀 Live Demo & Endpoints

- **Live Web Application**: [https://ai-talent-search.vercel.app/](https://ai-talent-search.vercel.app/)
- **Technical Deep-Dive Article**: [Read on Medium](https://medium.com/@mdtowfikomer/building-an-ai-powered-talent-search-engine-with-mongodb-elasticsearch-and-vector-hybrid-search-2731de6a4fc7)

### REST API Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/health` | Cluster health status for MongoDB and Elasticsearch |
| `POST` | `/api/v1/search/hybrid` | Fused RRF hybrid candidate search (`mode: hybrid/vector/lexical`) |
| `POST` | `/api/v1/resumes` | Dual-write pipeline: stores in Mongo & indexes in Elastic |
| `POST` | `/api/v1/extract` | AI NLP technical skill & entity extraction |
| `POST` | `/api/v1/match-score` | Weighted composite job-resume score breakdown |
| `POST` | `/api/v1/seed` | Seeds verified benchmark candidate profiles |

---

## 🛠️ Local Development & Quickstart

```powershell
# 1. Clone repository
git clone https://github.com/MdTowfikomer/Resume-Screening-Platform.git
cd Resume-Screening-Platform

# 2. Setup Python environment
cd ai-nlp-service
python -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt

# 3. Configure credentials (optional, resilient in-memory fallback included)
copy .env.example .env
# Fill in MONGO_URI and ELASTICSEARCH_URL

# 4. Launch backend & UI
python run.py
```
Open `http://localhost:8000` to access the candidate ledger and interactive search workbench.

---

## 📄 License
This project is open-source under the [MIT License](LICENSE).
