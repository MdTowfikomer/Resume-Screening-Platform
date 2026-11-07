// MongoDB initialization script
// Creates the database, collections, and indexes

db = db.getSiblingDB('resume_screening_db');

// ── Collections ──────────────────────────────────────────────
db.createCollection('users');
db.createCollection('resumes');
db.createCollection('job_postings');
db.createCollection('match_results');

// ── Indexes ───────────────────────────────────────────────────
db.users.createIndex({ "email": 1 }, { unique: true });
db.users.createIndex({ "username": 1 }, { unique: true });

db.resumes.createIndex({ "candidate_email": 1, "is_active": 1 });
db.resumes.createIndex({ "candidate_id": 1 });
db.resumes.createIndex({ "processing_status": 1 });
db.resumes.createIndex({ "technical_skills": 1 });
db.resumes.createIndex({ "created_at": -1 });

db.job_postings.createIndex({ "recruiter_id": 1 });
db.job_postings.createIndex({ "status": 1 });
db.job_postings.createIndex({ "required_skills": 1 });
db.job_postings.createIndex({ "expires_at": 1 });
db.job_postings.createIndex({ "title": "text", "description": "text" });

db.match_results.createIndex({ "resume_id": 1, "job_id": 1 }, { unique: true });
db.match_results.createIndex({ "job_id": 1, "overall_score": -1 });
db.match_results.createIndex({ "recruiter_id": 1 });
db.match_results.createIndex({ "status": 1 });

print('✅ MongoDB initialized: resume_screening_db with all indexes');
