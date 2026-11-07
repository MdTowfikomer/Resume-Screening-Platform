# 🤖 AI-Powered Resume Screening & Job Matching Platform

[![CI — Build, Test & Docker Push](https://github.com/rahul-ghadge/ai-resume-screening-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/rahul-ghadge/ai-resume-screening-platform/actions/workflows/ci.yml)

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green)](https://www.mongodb.com/)
[![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.x-yellow)](https://www.elastic.co/)
[![Redis](https://img.shields.io/badge/Redis-7.x-red)](https://redis.io/)
[![Kafka](https://img.shields.io/badge/Kafka-7.x-black)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-compose-blue)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue)](LICENSE)

> Enterprise-grade AI-powered hiring platform that streamlines resume screening,  
> extracts skills via NLP, scores job matches, and surfaces top talent to recruiters.

---

## 📐 Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    Client (Recruiter / Candidate)               │
└──────────────────────────────┬──────────────────────────────────┘
                               │ REST / JWT
┌──────────────────────────────▼──────────────────────────────────┐
│             Spring Boot Application  :8080                      │
│                                                                 │
│  AuthController  ResumeController  JobController  MatchController│
│       ▼               ▼                ▼               ▼        │
│  AuthService    ResumeService    JobService      MatchService   │
│       ▼               ▼                ▼               ▼        │
│  UserRepo      ResumeRepo        JobRepo         MatchRepo      │
│                  (MongoDB)                       (MongoDB)      │
│                       │                                         │
│                 Kafka Producer ──► resume-uploaded-events       │
└───────────────────────────────────────────────────────────────-─┘
                               │
          ┌────────────────────┼────────────────────┐
          ▼                    ▼                    ▼
   ┌─────────────┐    ┌──────────────┐    ┌─────────────────┐
   │  MongoDB 7  │    │ Elasticsearch│    │    Redis 7      │
   │  (Primary   │    │ (Full-text   │    │  (Cache +       │
   │  Database)  │    │  Search)     │    │  Sessions)      │
   └─────────────┘    └──────────────┘    └─────────────────┘
                               │
                    ┌──────────▼──────────┐
                    │    Kafka Consumer   │
                    │  (Async Processing) │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  Python NLP Service │
                    │      :5000          │
                    │  FastAPI + spaCy +  │
                    │  sentence-BERT      │
                    └─────────────────────┘
```

---

## ✨ Features

| Feature | Description |
|---|---|
| 📄 **Resume Upload** | PDF / DOCX upload with validation (max 10 MB) |
| 🔍 **PDF Parsing** | Apache PDFBox text extraction with layout preservation |
| 🧠 **AI Skill Extraction** | Python NLP microservice with spaCy + sentence-BERT |
| 🎯 **Job Matching Score** | Weighted scoring: skills (50%) + experience (25%) + education (10%) + keywords (15%) |
| ⚡ **Async Processing** | Kafka-based pipeline — upload returns instantly, processing is async |
| 🔎 **Elasticsearch Search** | Full-text resume and job search with relevance ranking |
| 💾 **Redis Caching** | Per-entity TTL caches for resumes, jobs, and match scores |
| 🔐 **JWT Security** | Stateless authentication with role-based access (ADMIN / RECRUITER / CANDIDATE) |
| 📊 **Recruiter Dashboard** | Real-time stats: jobs, applications, shortlists, top candidates |
| 📈 **Prometheus + Grafana** | Production-ready metrics and dashboards |
| 🐳 **Docker Compose** | One-command local dev with all 10 services |
| 🚀 **CI/CD** | GitHub Actions: build → test → Docker push → SonarCloud |

---

## 🛠️ Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Spring Boot | 3.4.1 | Application framework |
| Spring Security | 6.x | JWT authentication & RBAC |
| Spring Data MongoDB | 4.x | Primary database ORM |
| Spring Data Elasticsearch | 5.x | Full-text search |
| Spring Data Redis | 3.x | Caching layer |
| Spring Kafka | 3.x | Async event streaming |
| Apache PDFBox | 3.0.3 | PDF text extraction |
| MapStruct | 1.6.3 | DTO mapping |
| Lombok | 1.18.36 | Boilerplate reduction |
| SpringDoc OpenAPI | 2.7.0 | Swagger UI |
| Micrometer Prometheus | 1.14.x | Metrics |

### AI/NLP Microservice (Python)
| Technology | Version | Purpose |
|---|---|---|
| FastAPI | 0.115.x | REST framework |
| spaCy | 3.8.x | NLP + NER |
| sentence-transformers | 3.3.x | Semantic similarity |
| Uvicorn | 0.32.x | ASGI server |

### Infrastructure
| Service | Version | Purpose |
|---|---|---|
| MongoDB | 7.0 | Primary database |
| Elasticsearch | 8.16 | Search & indexing |
| Redis | 7.4 | Cache & session |
| Apache Kafka | 7.7 (Confluent) | Event streaming |
| Prometheus | 2.55 | Metrics collection |
| Grafana | 11.4 | Dashboards |

---

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker & Docker Compose

### Option 1 — Full Docker Stack (Recommended)

```bash
# Clone
git clone https://github.com/your-username/ai-resume-screening-platform.git
cd ai-resume-screening-platform

# Start all services
docker-compose up -d

# Watch logs
docker-compose logs -f app
```

All services will be available at:

| Service | URL |
|---|---|
| Spring Boot API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Python NLP API | http://localhost:5000/docs |
| Mongo Express | http://localhost:8081 |
| Kibana | http://localhost:5601 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (admin/admin123) |

### Option 2 — Run Locally (infrastructure via Docker)

```bash
# Start only infrastructure
docker-compose up -d mongo redis elasticsearch kafka zookeeper

# Run Spring Boot
mvn spring-boot:run

# Run Python NLP (separate terminal)
cd ai-nlp-service
pip install -r requirements.txt
python -m spacy download en_core_web_sm
uvicorn app.main:app --reload --port 5000
```

---

## 🗂️ Project Structure

```
ai-resume-screening-platform/
├── src/
│   └── main/java/com/resumeai/
│       ├── AiResumeScreeningPlatformApplication.java
│       ├── config/            # Security, Redis, Kafka, OpenAPI, Async config
│       ├── constants/         # AppConstants (API paths, topics, cache keys)
│       ├── controller/        # REST controllers (Auth, Resume, Job, Match, Dashboard)
│       ├── dto/
│       │   ├── request/       # Request DTOs with validation
│       │   └── response/      # Response DTOs + generic ApiResponse<T>
│       ├── exception/         # Custom exceptions + GlobalExceptionHandler
│       ├── kafka/
│       │   ├── consumer/      # ResumeUploadedConsumer, NotificationConsumer
│       │   └── producer/      # ResumeEventProducer (events for all topics)
│       ├── model/             # MongoDB documents: Resume, JobPosting, MatchResult, User
│       ├── repository/
│       │   ├── mongo/         # MongoRepository extensions
│       │   └── elasticsearch/ # ElasticsearchRepository extensions + Documents
│       ├── security/          # JwtTokenProvider, JwtAuthFilter, UserDetailsServiceImpl
│       ├── service/           # Service interfaces
│       │   └── impl/          # Implementations: ResumeService, MatchingService, AiNlpService...
│       └── util/              # ResumeParserUtil (PDFBox), FileStorageUtil
│
├── src/test/java/com/resumeai/
│   ├── controller/            # MockMvc integration tests
│   ├── service/               # Mockito unit tests (scoring algorithm, service logic)
│   └── repository/            # Repository tests
│
├── ai-nlp-service/            # Python FastAPI microservice
│   ├── app/main.py            # FastAPI app with /extract and /match-score endpoints
│   ├── requirements.txt
│   └── Dockerfile
│
├── docker/
│   ├── mongo-init.js          # MongoDB init script (indexes)
│   └── prometheus.yml         # Prometheus scrape config
│
├── .github/workflows/ci.yml   # GitHub Actions CI/CD pipeline
├── docker-compose.yml         # Full local dev stack (10 services)
├── Dockerfile                 # Multi-stage Spring Boot Docker image
├── pom.xml                    # Maven build (Java 21, Spring Boot 3.4.1)
├── .gitignore
└── README.md
```

---

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Register candidate or recruiter |
| `POST` | `/api/v1/auth/login` | Public | Login and get JWT token |
| `GET` | `/api/v1/auth/me` | Authenticated | Get current user profile |

### Resumes
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/resumes/upload` | Authenticated | Upload PDF/DOCX resume |
| `GET` | `/api/v1/resumes` | RECRUITER/ADMIN | List all resumes (paginated) |
| `GET` | `/api/v1/resumes/{id}` | Authenticated | Get resume by ID |
| `GET` | `/api/v1/resumes/by-email/{email}` | Authenticated | Get by candidate email |
| `GET` | `/api/v1/resumes/status/{status}` | ADMIN | Filter by processing status |
| `POST` | `/api/v1/resumes/search/skills` | RECRUITER/ADMIN | Search by skill list |
| `PUT` | `/api/v1/resumes/{id}` | Authenticated | Update resume metadata |
| `DELETE` | `/api/v1/resumes/{id}` | Authenticated | Soft-delete resume |
| `POST` | `/api/v1/resumes/{id}/reprocess` | ADMIN | Re-trigger AI processing |
| `GET` | `/api/v1/resumes/stats` | RECRUITER/ADMIN | Processing statistics |

### Job Postings
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/jobs` | RECRUITER | Create job posting |
| `GET` | `/api/v1/jobs` | Public | List active jobs (paginated) |
| `GET` | `/api/v1/jobs/{id}` | Public | Get job by ID |
| `GET` | `/api/v1/jobs/search?keyword=...` | Public | Full-text search |
| `GET` | `/api/v1/jobs/recruiter/mine` | RECRUITER | My job postings |
| `PUT` | `/api/v1/jobs/{id}` | RECRUITER (owner) | Update job |
| `DELETE` | `/api/v1/jobs/{id}` | RECRUITER (owner) | Close job |
| `POST` | `/api/v1/jobs/search/skills` | Authenticated | Jobs by required skills |

### Matching
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/matches/trigger` | RECRUITER | Match resume ↔ job |
| `POST` | `/api/v1/matches/bulk/{jobId}` | RECRUITER | Bulk match all resumes [async] |
| `GET` | `/api/v1/matches/{id}` | Authenticated | Get match result |
| `GET` | `/api/v1/matches/by-job/{jobId}` | RECRUITER | All matches for a job |
| `GET` | `/api/v1/matches/by-resume/{resumeId}` | Authenticated | All matches for a resume |
| `GET` | `/api/v1/matches/top/{jobId}?threshold=70` | RECRUITER | Top candidates |
| `PATCH` | `/api/v1/matches/{id}/status` | RECRUITER | Shortlist/Reject/Hire |
| `DELETE` | `/api/v1/matches/{id}` | ADMIN | Delete match |

### Recruiter Dashboard
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/v1/recruiter/dashboard` | RECRUITER | Stats + top candidates |

---

## 🔐 Authentication

All protected endpoints require a JWT Bearer token in the Authorization header:

```bash
curl -H "Authorization: Bearer <your-jwt-token>" http://localhost:8080/api/v1/resumes
```

**Roles:**
- `ROLE_CANDIDATE` — Upload/view their own resume, see job listings
- `ROLE_RECRUITER` — Create jobs, trigger matches, view all resumes, recruiter dashboard
- `ROLE_ADMIN` — Full access including user management and force-reprocessing

---

## 🧪 Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=ResumeServiceTest

# With coverage report
mvn verify
open target/site/jacoco/index.html
```

---

## 📊 Matching Score Algorithm

```
Overall Score = (Skill Match × 50%) + (Experience × 25%) + (Education × 10%) + (Keywords × 15%)

When AI/NLP service is available:
  Final Score = (AI Score × 60%) + (Rule-based Score × 40%)

Recommendations:
  85–100 → STRONG_MATCH  ✅
  70–84  → GOOD_MATCH    👍
  55–69  → PARTIAL_MATCH ⚠️
  40–54  → WEAK_MATCH    ⛔
  0–39   → NO_MATCH      ❌
```

---

## 🔄 Kafka Event Flow

```
Resume Uploaded
      │
      ▼
[resume-uploaded-events] ── Kafka ──► ResumeUploadedConsumer
                                             │
                                    1. Extract PDF text (PDFBox)
                                    2. Call AI/NLP service
                                    3. Save enriched Resume → MongoDB
                                    4. Index → Elasticsearch
                                             │
                                             ▼
                                  [resume-processed-events]
                                             │
                                    Trigger auto-matching
                                             │
                                             ▼
                                  [job-matched-events]
                                             │
                                    Send notification
                                             │
                                             ▼
                                  [notification-events]
```

---

## 🐳 Docker Commands

```bash
# Start everything
docker-compose up -d

# Stop everything
docker-compose down

# View logs for app only
docker-compose logs -f app

# Rebuild after code changes
docker-compose up -d --build app

# Scale the NLP service
docker-compose up -d --scale ai-nlp=3

# Wipe all data (volumes)
docker-compose down -v
```

---

## ⚙️ Environment Variables

| Variable | Default | Description |
|---|---|---|
| `MONGO_HOST` | `localhost` | MongoDB host |
| `MONGO_DB` | `resume_screening_db` | Database name |
| `REDIS_HOST` | `localhost` | Redis host |
| `ES_URI` | `http://localhost:9200` | Elasticsearch URI |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka brokers |
| `AI_NLP_URL` | `http://localhost:5000` | Python NLP microservice URL |
| `AI_NLP_ENABLED` | `true` | Toggle AI service (uses fallback if false) |
| `JWT_SECRET` | *(long key)* | JWT signing secret (min 256 bits) |
| `RESUME_STORAGE_PATH` | `./uploads/resumes` | Local file storage path |

---

## 📄 License

This project is licensed under the **Apache 2.0 License** — see the [LICENSE](LICENSE) file.

---

*Built with ❤️ using Spring Boot 3.4.1, MongoDB, Elasticsearch, Kafka, Redis, and Python NLP*
---
---

<img width="1607" height="892" alt="image" src="https://github.com/user-attachments/assets/c83fa07d-1364-470d-b768-2744eeb9b197" />
<img width="1546" height="760" alt="image" src="https://github.com/user-attachments/assets/67a79e6b-4b76-4263-900a-255ed99c8b6d" />

---

<img width="1306" height="590" alt="image" src="https://github.com/user-attachments/assets/048f6871-638e-4374-8ed5-84fd3a20435d" />
<img width="1304" height="940" alt="image" src="https://github.com/user-attachments/assets/5ab7f687-e2d5-45b9-90f2-1ce6cc1863b2" />

---

<img width="1303" height="542" alt="image" src="https://github.com/user-attachments/assets/bc3efd67-cdca-449e-8f32-646974eb8b3a" />
<img width="1308" height="817" alt="image" src="https://github.com/user-attachments/assets/c7f1fac4-b586-4158-be2d-c6d37df361fa" />
<img width="1311" height="911" alt="image" src="https://github.com/user-attachments/assets/78a041f9-75bd-48ae-bfa1-5c1e42f402fc" />

---

<img width="1290" height="884" alt="image" src="https://github.com/user-attachments/assets/53682caf-37a0-4555-ba47-38109d3b2479" />
<img width="1324" height="657" alt="image" src="https://github.com/user-attachments/assets/d51d4678-bdf6-402e-9ad2-1008525335f4" />
<img width="1288" height="682" alt="image" src="https://github.com/user-attachments/assets/66a08415-a0ec-4dcd-a6df-b4444e523a85" />


