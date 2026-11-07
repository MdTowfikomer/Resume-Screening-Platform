"""
Integration test for AI Screening & Hybrid Search Engine
"""
import sys
from fastapi.testclient import TestClient
from app.main import app

def test_full_pipeline():
    client = TestClient(app)

    # 1. Test Health Endpoint
    print("\n[1] Testing /health endpoint...")
    res = client.get("/health")
    assert res.status_code == 200, f"Health check failed: {res.text}"
    health = res.json()
    print("Health response:", health)
    assert "mongodb" in health
    assert "elasticsearch" in health

    # 2. Test Resume Sync (MongoDB + Elasticsearch)
    print("\n[2] Testing /api/v1/resumes sync...")
    resume_payload = {
        "candidate_name": "Devin AI",
        "candidate_email": "devin@example.com",
        "raw_text": "Experienced software architect with 8 years building cloud applications in Python, FastAPI, Docker, and Elasticsearch.",
        "technical_skills": ["Python", "FastAPI", "Docker", "Elasticsearch"],
        "soft_skills": ["Problem Solving"],
        "experience_years": 8.0,
        "summary": "Software Architect with 8 years experience."
    }
    res = client.post("/api/v1/resumes", json=resume_payload)
    assert res.status_code == 200, f"Resume insert failed: {res.text}"
    sync_data = res.json()
    print("Sync response:", sync_data)
    assert sync_data["status"] == "synchronized"
    doc_id = sync_data["id"]

    # 3. Test Hybrid Search (RRF)
    print("\n[3] Testing /api/v1/search/hybrid (RRF Fused Search)...")
    res = client.post("/api/v1/search/hybrid", json={
        "query": "FastAPI architect with Elasticsearch",
        "mode": "hybrid",
        "top_k": 3
    })
    assert res.status_code == 200, f"Hybrid search failed: {res.text}"
    search_data = res.json()
    print(f"Hybrid search returned {search_data['count']} candidates:")
    for r in search_data["results"]:
        print(f" - {r['candidate_name']} | RRF Score: {r.get('rrf_score')} | Skills: {r.get('skills')}")
    assert len(search_data["results"]) > 0

    # 4. Test Skill Extraction
    print("\n[4] Testing /api/v1/extract...")
    res = client.post("/api/v1/extract", json={
        "text": "Certified AWS Solutions Architect with 6 years experience in Java, Spring Boot, MongoDB, and Redis."
    })
    assert res.status_code == 200, f"Extraction failed: {res.text}"
    extract_data = res.json()
    print("Extracted skills:", extract_data["technical_skills"])
    assert "Mongodb" in extract_data["technical_skills"]
    assert "Java" in extract_data["technical_skills"]

    # 5. Test Match Score
    print("\n[5] Testing /api/v1/match-score...")
    res = client.post("/api/v1/match-score", json={
        "resume_text": "Backend Engineer with 5 years in Python, FastAPI, MongoDB, and Docker.",
        "required_skills": ["Python", "FastAPI", "MongoDB"],
        "preferred_skills": ["Kubernetes", "Redis"]
    })
    assert res.status_code == 200, f"Match score failed: {res.text}"
    match_data = res.json()
    print("Match score:", match_data["score"], "% |", match_data["explanation"])
    assert match_data["score"] > 80.0

    print("\nALL 5 INTEGRATION TESTS PASSED SUCCESSFULLY! 🚀")

if __name__ == "__main__":
    test_full_pipeline()
