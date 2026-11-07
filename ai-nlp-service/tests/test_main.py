"""
Tests for the AI NLP microservice.
Run with: pytest tests/test_main.py -v
"""
import pytest
from fastapi.testclient import TestClient
import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))
from app.main import app

client = TestClient(app)

SAMPLE_RESUME = """
John Doe | john.doe@example.com | +1-555-123-4567
Senior Software Engineer with 8 years of experience.

SKILLS
Java, Python, Spring Boot, Kubernetes, AWS, Docker, Kafka, Redis, MongoDB,
PostgreSQL, Elasticsearch, React, Node.js, Machine Learning

CERTIFICATIONS
AWS Certified Solutions Architect
Certified Kubernetes Administrator (CKA)

EXPERIENCE
Senior Engineer at Acme Technologies Inc (2019-Present)
- Built microservices using Spring Boot and Kafka
- 8 years of professional experience in distributed systems

EDUCATION
Bachelor of Engineering in Computer Science - UC Berkeley 2016
"""

# ── Health endpoint ────────────────────────────────────────────

def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "UP"
    assert "spacy" in data
    assert "sentence_transformers" in data


# ── Skill extraction ───────────────────────────────────────────

def test_extract_finds_technical_skills():
    response = client.post("/api/v1/extract", json={"text": SAMPLE_RESUME})
    assert response.status_code == 200
    data = response.json()
    assert "technical_skills" in data
    assert len(data["technical_skills"]) > 0

    skill_names = [s.lower() for s in data["technical_skills"]]
    assert any("java" in s for s in skill_names), "Expected Java in skills"
    assert any("docker" in s for s in skill_names), "Expected Docker in skills"
    assert any("kafka" in s for s in skill_names), "Expected Kafka in skills"


def test_extract_finds_soft_skills():
    text = "Strong leadership, excellent communication, proven teamwork and collaboration skills."
    response = client.post("/api/v1/extract", json={"text": text})
    assert response.status_code == 200
    data = response.json()
    assert "soft_skills" in data
    assert len(data["soft_skills"]) > 0


def test_extract_detects_experience_years():
    response = client.post("/api/v1/extract", json={"text": SAMPLE_RESUME})
    assert response.status_code == 200
    data = response.json()
    assert data["experience_years"] == 8.0


def test_extract_returns_confidence_score():
    response = client.post("/api/v1/extract", json={"text": SAMPLE_RESUME})
    assert response.status_code == 200
    data = response.json()
    assert "confidence_score" in data
    assert 0.0 <= data["confidence_score"] <= 1.0


def test_extract_finds_aws_certification():
    response = client.post("/api/v1/extract", json={"text": SAMPLE_RESUME})
    assert response.status_code == 200
    data = response.json()
    certs = data.get("certifications", [])
    assert any("AWS" in c or "Kubernetes" in c for c in certs)


def test_extract_returns_processing_time():
    response = client.post("/api/v1/extract", json={"text": SAMPLE_RESUME})
    assert response.status_code == 200
    data = response.json()
    assert "processing_time_ms" in data
    assert data["processing_time_ms"] >= 0


def test_extract_short_text_fails_validation():
    response = client.post("/api/v1/extract", json={"text": "Hi"})
    assert response.status_code == 422  # FastAPI validation error


# ── Match scoring ──────────────────────────────────────────────

def test_match_score_perfect_match():
    payload = {
        "resume_text": "Expert Java developer with Spring Boot, Kubernetes, AWS, Docker, Kafka",
        "required_skills": ["java", "spring boot", "kubernetes"],
        "preferred_skills": ["docker", "kafka"]
    }
    response = client.post("/api/v1/match-score", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["score"] >= 80.0
    assert len(data["matched_skills"]) == 3
    assert len(data["missing_skills"]) == 0


def test_match_score_no_overlap():
    payload = {
        "resume_text": "PHP Laravel MySQL developer with WordPress experience",
        "required_skills": ["java", "spring boot", "kubernetes", "aws"],
        "preferred_skills": []
    }
    response = client.post("/api/v1/match-score", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["score"] < 30.0
    assert len(data["missing_skills"]) >= 3


def test_match_score_partial_match():
    payload = {
        "resume_text": "Java developer with Spring Boot and MySQL",
        "required_skills": ["java", "spring boot", "kubernetes", "aws"],
        "preferred_skills": ["docker"]
    }
    response = client.post("/api/v1/match-score", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert 20.0 <= data["score"] <= 80.0
    assert "matched_skills" in data
    assert "missing_skills" in data
    assert "explanation" in data


def test_match_score_no_required_skills():
    payload = {
        "resume_text": "Java Python developer",
        "required_skills": [],
        "preferred_skills": ["docker"]
    }
    response = client.post("/api/v1/match-score", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["score"] >= 0


def test_match_score_includes_explanation():
    payload = {
        "resume_text": "Java developer",
        "required_skills": ["java", "python"],
        "preferred_skills": []
    }
    response = client.post("/api/v1/match-score", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert len(data["explanation"]) > 0
