import os
import numpy as np
from fastapi import FastAPI
from pydantic import BaseModel
import httpx
import logging

logging.basicConfig(level=logging.INFO)
log = logging.getLogger(__name__)

app = FastAPI()

HF_API_URL = "https://api-inference.huggingface.co/models/sentence-transformers/all-MiniLM-L6-v2"
HF_TOKEN = os.getenv("HF_TOKEN", "")  # optional, increases rate limit

class SimilarityRequest(BaseModel):
    text1: str
    text2: str

def cosine_similarity(a, b):
    a, b = np.array(a), np.array(b)
    return float(np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b) + 1e-10))

async def get_embedding(text: str) -> list:
    headers = {}
    if HF_TOKEN:
        headers["Authorization"] = f"Bearer {HF_TOKEN}"
    
    async with httpx.AsyncClient(timeout=30) as client:
        response = await client.post(
            HF_API_URL,
            headers=headers,
            json={"inputs": text}
        )
        response.raise_for_status()
        result = response.json()
        # HF returns list of embeddings for list of inputs, or single embedding
        if isinstance(result[0], list):
            return result[0]
        return result

@app.get("/health")
async def health():
    return {"status": "ok", "ready": True, "mode": "huggingface-api"}

@app.post("/similarity")
async def similarity(req: SimilarityRequest):
    try:
        emb1 = await get_embedding(req.text1)
        emb2 = await get_embedding(req.text2)
        score = cosine_similarity(emb1, emb2)
        return {"similarity": round(score, 4)}
    except Exception as e:
        log.error(f"Embedding error: {e}")
        # Return 0 so Spring Boot falls back to keyword scoring
        return {"similarity": 0.0}