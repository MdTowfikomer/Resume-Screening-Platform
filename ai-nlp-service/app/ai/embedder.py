"""
Semantic Embedding Generator
============================
Generates normalized dense vectors representing semantic skill concepts
and resume descriptions for vector indexing and similarity comparison.
"""

import re
import math
import hashlib
from typing import List
import numpy as np

DIMS = 64

# Canonical skill dimension anchors to ensure rich semantic topology
SKILL_TAXONOMY_ANCHORS = [
    "python", "java", "golang", "c++", "c#", "typescript", "javascript", "rust",
    "spring boot", "fastapi", "django", "express", "react", "angular", "vue", "nodejs",
    "mongodb", "elasticsearch", "postgresql", "mysql", "redis", "cassandra", "dynamodb",
    "aws", "gcp", "azure", "docker", "kubernetes", "terraform", "ci/cd", "devops",
    "kafka", "rabbitmq", "microservices", "event-driven", "rest api", "graphql", "grpc",
    "machine learning", "deep learning", "nlp", "llm", "rag", "vector search", "pytorch",
    "system design", "distributed systems", "high availability", "scalability", "caching",
    "leadership", "teamwork", "agile", "scrum", "communication", "problem solving"
]

def generate_embedding(text: str) -> List[float]:
    """
    Generate a 64-dimensional dense semantic vector from resume or query text.
    Uses anchor-based projection with semantic hash distribution,
    producing unit-normalized vectors suitable for Cosine / Dot Product search.
    """
    vec = np.zeros(DIMS, dtype=np.float32)
    clean_text = text.lower()
    words = re.findall(r'\b[a-z0-9_+#.-]+\b', clean_text)

    if not words:
        vec[0] = 1.0
        return vec.tolist()

    # 1. Project against taxonomy anchors
    for i, anchor in enumerate(SKILL_TAXONOMY_ANCHORS[:DIMS]):
        if anchor in clean_text:
            vec[i] += 2.5

    # 2. Distribute word hashes across dimensions
    for word in words:
        h = int(hashlib.md5(word.encode('utf-8')).hexdigest()[:8], 16)
        idx = h % DIMS
        weight = 1.0 / (1.0 + math.log(1 + words.count(word)))
        vec[idx] += weight

    # 3. Normalize vector to unit length (L2 norm)
    norm = np.linalg.norm(vec)
    if norm > 0:
        vec = vec / norm
    else:
        vec[0] = 1.0

    return [round(float(v), 5) for v in vec]
