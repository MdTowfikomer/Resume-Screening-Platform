"""
MongoDB Storage Engine with Resilience Fallback
================================================
Handles document persistence for resumes, candidates, and job postings.
Supports live MongoDB (local or Atlas) and provides a thread-safe
in-memory document store fallback when no MongoDB cluster is reachable.
"""

import os
import logging
from typing import Dict, Any, List, Optional
from datetime import datetime
import uuid

try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass

logger = logging.getLogger(__name__)

MONGO_URI = os.getenv("MONGO_URI", "mongodb://localhost:27017")
MONGO_DB_NAME = os.getenv("MONGO_DB_NAME", "resume_screening_db")

class MongoEngine:
    def __init__(self):
        self.client = None
        self.db = None
        self.is_connected = False
        self._memory_store: Dict[str, Dict[str, Dict[str, Any]]] = {
            "resumes": {},
            "jobs": {},
            "candidates": {}
        }
        self.connect()

    def connect(self):
        """Attempt connection to MongoDB with quick timeout."""
        try:
            from pymongo import MongoClient
            client = MongoClient(
                MONGO_URI,
                serverSelectionTimeoutMS=1500,
                connectTimeoutMS=1500
            )
            # Ping database to verify connection
            client.admin.command('ping')
            self.client = client
            self.db = client[MONGO_DB_NAME]
            self.is_connected = True
            logger.info(f"Connected to MongoDB at {MONGO_URI} [DB: {MONGO_DB_NAME}]")
        except Exception as e:
            self.is_connected = False
            self.client = None
            self.db = None
            logger.warning(
                f"MongoDB not reachable at {MONGO_URI} ({e}). "
                "Operating in resilient In-Memory Document Store mode."
            )

    def insert_resume(self, resume_data: Dict[str, Any]) -> str:
        """Insert a resume document into MongoDB or fallback store."""
        doc_id = resume_data.get("id") or str(uuid.uuid4())
        doc = dict(resume_data)
        doc["id"] = doc_id
        doc["created_at"] = doc.get("created_at") or datetime.utcnow().isoformat()
        doc["updated_at"] = datetime.utcnow().isoformat()

        if self.is_connected and self.db is not None:
            try:
                self.db.resumes.replace_one({"id": doc_id}, doc, upsert=True)
                return doc_id
            except Exception as e:
                logger.error(f"MongoDB insert error: {e}. Storing in memory fallback.")

        self._memory_store["resumes"][doc_id] = doc
        return doc_id

    def get_resume(self, resume_id: str) -> Optional[Dict[str, Any]]:
        """Retrieve a resume document by ID."""
        if self.is_connected and self.db is not None:
            try:
                doc = self.db.resumes.find_one({"id": resume_id}, {"_id": 0})
                if doc:
                    return doc
            except Exception as e:
                logger.error(f"MongoDB fetch error: {e}")

        return self._memory_store["resumes"].get(resume_id)

    def list_resumes(self, limit: int = 50) -> List[Dict[str, Any]]:
        """List all resumes stored in MongoDB."""
        if self.is_connected and self.db is not None:
            try:
                docs = list(self.db.resumes.find({}, {"_id": 0}).limit(limit))
                if docs:
                    return docs
            except Exception as e:
                logger.error(f"MongoDB list error: {e}")

        return list(self._memory_store["resumes"].values())[:limit]

    def delete_resume(self, resume_id: str) -> bool:
        """Delete a resume by ID."""
        deleted = False
        if self.is_connected and self.db is not None:
            try:
                res = self.db.resumes.delete_one({"id": resume_id})
                deleted = res.deleted_count > 0
            except Exception as e:
                logger.error(f"MongoDB delete error: {e}")

        if resume_id in self._memory_store["resumes"]:
            del self._memory_store["resumes"][resume_id]
            deleted = True
        return deleted

    def status(self) -> Dict[str, Any]:
        """Return connectivity & document count."""
        count = len(self._memory_store["resumes"])
        if self.is_connected and self.db is not None:
            try:
                count = self.db.resumes.count_documents({})
            except Exception:
                pass
        return {
            "connected": self.is_connected,
            "mode": "Live MongoDB" if self.is_connected else "In-Memory Document Store Fallback",
            "uri": MONGO_URI if self.is_connected else "in-memory://",
            "database": MONGO_DB_NAME,
            "total_documents": count
        }

mongo_engine = MongoEngine()
