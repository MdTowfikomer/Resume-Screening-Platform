"""
AI Talent Screening & Search Platform — FastAPI Core Engine
===========================================================
Unified backend integrating:
  - MongoDB 7.0 for operational document persistence (Source of Truth)
  - Elasticsearch 8/9 for BM25 lexical & Dense Vector kNN search
  - Reciprocal Rank Fusion (RRF) for State-of-the-Art Hybrid Search
  - AI Skill Extraction, NER, and Weighted Match Scoring
  - Interactive Web Dashboard UI for instant evaluation
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
import re
import logging
import time
import os
from pathlib import Path

from app.database.mongo_client import mongo_engine
from app.search.elastic_client import elastic_engine
from app.ai.embedder import generate_embedding
from app.services.pipeline import pipeline_service

# ── Logging setup ───────────────────────────────────────────
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="AI Talent Screening & Hybrid Search Engine",
    description="Full-stack AI platform combining MongoDB (Document Store) and Elasticsearch (Hybrid Vector Search)",
    version="2.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Skill taxonomy ──────────────────────────────────────────
TECH_SKILLS = {
    "languages": [
        "java", "python", "javascript", "typescript", "kotlin", "scala",
        "go", "golang", "rust", "c++", "c#", "ruby", "php", "swift", "r",
    ],
    "frameworks": [
        "spring boot", "spring", "django", "flask", "fastapi", "react",
        "angular", "vue", "node.js", "nodejs", "express", "nestjs",
        "hibernate", "jpa", "rails", "laravel", "next.js",
    ],
    "databases": [
        "mysql", "postgresql", "mongodb", "elasticsearch", "redis",
        "cassandra", "oracle", "dynamodb", "neo4j", "sqlite", "mariadb",
    ],
    "cloud_devops": [
        "aws", "azure", "gcp", "docker", "kubernetes", "k8s", "terraform",
        "ansible", "jenkins", "github actions", "gitlab ci", "circleci", "helm",
        "prometheus", "grafana", "elk stack",
    ],
    "messaging": [
        "kafka", "rabbitmq", "activemq", "sqs", "pubsub", "nats",
    ],
    "ai_ml": [
        "machine learning", "deep learning", "tensorflow", "pytorch",
        "scikit-learn", "nlp", "bert", "gpt", "openai", "langchain",
        "computer vision", "mlops", "mlflow", "vector search", "rag",
    ],
}
ALL_SKILLS = [s for skills in TECH_SKILLS.values() for s in skills]

SOFT_SKILLS = [
    "leadership", "communication", "teamwork", "problem solving",
    "critical thinking", "adaptability", "time management", "creativity",
    "collaboration", "mentoring", "project management", "agile", "scrum", "system design"
]

# ── Pydantic Request/Response Models ────────────────────────
class ExtractRequest(BaseModel):
    text: str = Field(..., min_length=10, description="Raw resume text")

class MatchScoreRequest(BaseModel):
    resume_text: str
    required_skills: List[str]
    preferred_skills: Optional[List[str]] = []

class Education(BaseModel):
    institution: Optional[str] = None
    degree: Optional[str] = None
    field_of_study: Optional[str] = None
    start_year: Optional[str] = None
    end_year: Optional[str] = None

class WorkExperience(BaseModel):
    company: Optional[str] = None
    title: Optional[str] = None
    start_date: Optional[str] = None
    end_date: Optional[str] = None
    is_current: Optional[bool] = False
    description: Optional[str] = None
    technologies: Optional[List[str]] = []

class ExtractResponse(BaseModel):
    technical_skills: List[str]
    soft_skills: List[str]
    certifications: List[str]
    summary: Optional[str] = None
    experience_years: Optional[float] = None
    confidence_score: float
    work_experience: List[WorkExperience] = []
    education: List[Education] = []
    processing_time_ms: int

class MatchScoreResponse(BaseModel):
    score: float
    skill_match_score: float
    semantic_score: float
    matched_skills: List[str]
    missing_skills: List[str]
    explanation: str

class ResumeCreateRequest(BaseModel):
    candidate_name: str
    candidate_email: Optional[str] = None
    raw_text: str
    technical_skills: Optional[List[str]] = []
    soft_skills: Optional[List[str]] = []
    experience_years: Optional[float] = 0.0
    summary: Optional[str] = None

class HybridSearchRequest(BaseModel):
    query: str
    mode: Optional[str] = "hybrid"  # "hybrid", "vector", "lexical"
    top_k: Optional[int] = 5

# ── Extraction helpers ───────────────────────────────────────
def extract_technical_skills(text: str) -> List[str]:
    lower = text.lower()
    found = []
    for skill in ALL_SKILLS:
        if skill.lower() in lower:
            if len(skill) <= 3:
                pattern = r'\b' + re.escape(skill) + r'\b'
                if re.search(pattern, lower):
                    found.append(skill.title())
            else:
                found.append(skill.title())
    return sorted(set(found))

def extract_soft_skills(text: str) -> List[str]:
    lower = text.lower()
    return sorted(set(
        skill.title() for skill in SOFT_SKILLS if skill in lower
    ))

def extract_certifications(text: str) -> List[str]:
    cert_patterns = [
        r'\b(AWS Certified[\w\s]+)\b',
        r'\b(Google Cloud Professional[\w\s]+)\b',
        r'\b(Microsoft Certified[\w\s]+)\b',
        r'\b(Certified Kubernetes[\w\s]+)\b',
        r'\b(CKA|CKAD|CKS)\b',
        r'\b(PMP|PRINCE2)\b',
        r'\b(Scrum Master|CSM|PSM)\b',
        r'\b(CISSP|CompTIA[\w\s]+)\b',
        r'\b(Oracle Certified[\w\s]+)\b',
    ]
    found = []
    for pattern in cert_patterns:
        matches = re.findall(pattern, text, re.IGNORECASE)
        found.extend(matches)
    return list(set(found))

def extract_experience_years(text: str) -> Optional[float]:
    patterns = [
        r'(\d+)\+?\s*years?\s*(?:of\s*)?(?:professional\s*)?experience',
        r'experience\s*(?:of\s*)?(\d+)\+?\s*years?',
        r'(\d+)\+?\s*years?\s*(?:in\s*)?(?:the\s*)?(?:industry|field)',
    ]
    max_years = 0.0
    for pattern in patterns:
        for match in re.finditer(pattern, text, re.IGNORECASE):
            try:
                years = float(match.group(1))
                max_years = max(max_years, years)
            except ValueError:
                pass
    return max_years if max_years > 0 else None

def generate_summary(text: str, skills: List[str]) -> str:
    exp_years = extract_experience_years(text)
    top_skills = skills[:5]
    exp_str = f"{int(exp_years)}+ years of experience" if exp_years else "Experience"
    skills_str = ", ".join(top_skills) if top_skills else "modern software engineering"
    return f"{exp_str} in {skills_str}."

# ── Startup Lifecycle ────────────────────────────────────────
@app.on_event("startup")
def startup_event():
    logger.info("Initializing AI Screening & Hybrid Search Platform...")
    # Seed sample candidate profiles so the application is ready-to-test immediately
    pipeline_service.seed_sample_candidates()

# ── Web UI Root ──────────────────────────────────────────────
@app.get("/", response_class=HTMLResponse)
def index_page():
    """Serves the interactive web UI dashboard."""
    template_path = Path(__file__).parent / "templates" / "index.html"
    if template_path.exists():
        return template_path.read_text(encoding="utf-8")
    return "<h1>AI Talent Screening Engine</h1><p>Visit <a href='/docs'>/docs</a> for Swagger API.</p>"

# ── Health & Diagnostics ─────────────────────────────────────
@app.get("/health")
def health():
    return {
        "status": "UP",
        "mongodb": mongo_engine.status(),
        "elasticsearch": elastic_engine.status(),
        "ai_features": {
            "nlp_skill_extraction": True,
            "vector_embedding_dims": 64,
            "hybrid_rrf_search": True
        }
    }

# ── Resume CRUD & Pipeline Sync ──────────────────────────────
@app.post("/api/v1/resumes")
def create_resume(request: ResumeCreateRequest):
    """
    Save resume to MongoDB and index into Elasticsearch vector store in real-time.
    """
    doc = request.dict()
    # Auto-extract skills if not provided
    if not doc.get("technical_skills"):
        doc["technical_skills"] = extract_technical_skills(doc["raw_text"])
    if not doc.get("experience_years"):
        doc["experience_years"] = extract_experience_years(doc["raw_text"]) or 2.0
    if not doc.get("summary"):
        doc["summary"] = generate_summary(doc["raw_text"], doc["technical_skills"])

    sync_result = pipeline_service.sync_resume(doc)
    return sync_result

@app.get("/api/v1/resumes")
def list_resumes():
    """Retrieve all candidate resumes from MongoDB."""
    return mongo_engine.list_resumes()

# ── Elasticsearch Hybrid Search (RRF) ────────────────────────
@app.post("/api/v1/search/hybrid")
def hybrid_search(request: HybridSearchRequest):
    """
    Search candidate pool using:
    - 'hybrid': Reciprocal Rank Fusion (BM25 + Dense Vector)
    - 'vector': Pure dense cosine vector search
    - 'lexical': Pure BM25 keyword search
    """
    query = request.query.strip()
    top_k = request.top_k or 5
    mode = request.mode or "hybrid"

    if not query:
        # Return recent candidates if query is blank
        resumes = mongo_engine.list_resumes(limit=top_k)
        return {
            "mode": "all",
            "query": "",
            "count": len(resumes),
            "results": resumes
        }

    query_vector = generate_embedding(query)

    if mode == "vector":
        results = elastic_engine.vector_search(query_vector, top_k=top_k)
    elif mode == "lexical":
        results = elastic_engine.lexical_search(query, top_k=top_k)
    else:
        results = elastic_engine.hybrid_search(query, query_vector, top_k=top_k)

    return {
        "mode": mode,
        "query": query,
        "count": len(results),
        "results": results
    }

# ── AI NLP & Match Scoring Endpoints ─────────────────────────
@app.post("/api/v1/extract", response_model=ExtractResponse)
def extract_skills(request: ExtractRequest):
    """Extract skills, years of experience, and entities from raw resume text."""
    start = time.time()
    text = request.text

    tech_skills = extract_technical_skills(text)
    soft_skills = extract_soft_skills(text)
    certifications = extract_certifications(text)
    exp_years = extract_experience_years(text)
    summary = generate_summary(text, tech_skills)

    confidence = min(1.0, (len(tech_skills) * 0.05) + (0.3 if exp_years else 0.0) + 0.3)
    elapsed_ms = int((time.time() - start) * 1000)

    return ExtractResponse(
        technical_skills=tech_skills,
        soft_skills=soft_skills,
        certifications=certifications,
        summary=summary,
        experience_years=exp_years,
        confidence_score=round(confidence, 2),
        work_experience=[],
        education=[],
        processing_time_ms=elapsed_ms,
    )

@app.post("/api/v1/match-score", response_model=MatchScoreResponse)
def compute_match_score(request: MatchScoreRequest):
    """Compute weighted match score between candidate resume and job requisites."""
    resume_lower = request.resume_text.lower()
    req_set = {s.lower() for s in request.required_skills}
    pref_set = {s.lower() for s in (request.preferred_skills or [])}

    matched = [s for s in req_set if s in resume_lower]
    missing = [s for s in req_set if s not in resume_lower]
    bonus = [s for s in pref_set if s in resume_lower]

    skill_score = (len(matched) / len(req_set) * 100) if req_set else 100.0
    bonus_boost = min(10.0, len(bonus) * 2.5)
    keyword_score = min(100.0, skill_score + bonus_boost)

    # Compute vector similarity
    job_text = " ".join(request.required_skills + (request.preferred_skills or []))
    v_resume = generate_embedding(request.resume_text)
    v_job = generate_embedding(job_text)
    import numpy as np
    cos_sim = float(np.dot(v_resume, v_job) / (np.linalg.norm(v_resume) * np.linalg.norm(v_job)))
    semantic_score = round(max(0.0, cos_sim * 100), 1)

    final_score = round((keyword_score * 0.6) + (semantic_score * 0.4), 1)

    return MatchScoreResponse(
        score=final_score,
        skill_match_score=round(keyword_score, 1),
        semantic_score=semantic_score,
        matched_skills=[s.title() for s in matched],
        missing_skills=[s.title() for s in missing],
        explanation=(
            f"Matched {len(matched)} of {len(request.required_skills)} required core skills. "
            f"Dense Vector Semantic Relevance: {semantic_score}%."
        )
    )

@app.post("/api/v1/seed")
def seed_candidates():
    """Seed initial sample candidates for immediate live testing."""
    ids = pipeline_service.seed_sample_candidates()
    return {"status": "success", "seeded_count": len(ids), "candidate_ids": ids}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
