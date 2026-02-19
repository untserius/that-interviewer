from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
from numpy import dot
from numpy.linalg import norm
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Embedding Service", version="1.0.0")

# Loaded once at startup — stays in memory
MODEL_NAME = "all-MiniLM-L6-v2"
model: SentenceTransformer | None = None


@app.on_event("startup")
def load_model():
    global model
    logger.info(f"Loading model: {MODEL_NAME}")
    model = SentenceTransformer(MODEL_NAME)
    logger.info("Model loaded and ready")


# ── Request / Response schemas ────────────────────────────────────────────────

class SimilarityRequest(BaseModel):
    user_answer: str
    ideal_answer: str


class SimilarityResponse(BaseModel):
    similarity: float          # 0.0 – 1.0 cosine similarity
    user_answer: str
    ideal_answer: str


class EmbedRequest(BaseModel):
    text: str


class EmbedResponse(BaseModel):
    embedding: list[float]


# ── Endpoints ─────────────────────────────────────────────────────────────────

@app.get("/health")
def health():
    return {"status": "ok", "model": MODEL_NAME, "ready": model is not None}


@app.post("/similarity", response_model=SimilarityResponse)
def compute_similarity(req: SimilarityRequest):
    """
    Compute cosine similarity between user_answer and ideal_answer.
    Returns a float 0.0–1.0.
    """
    if model is None:
        raise HTTPException(status_code=503, detail="Model not loaded yet")

    if not req.user_answer.strip() or not req.ideal_answer.strip():
        raise HTTPException(status_code=400, detail="Both texts must be non-empty")

    embeddings = model.encode([req.user_answer, req.ideal_answer])
    user_vec, ideal_vec = embeddings[0], embeddings[1]

    similarity = cosine_similarity(user_vec, ideal_vec)

    logger.info(f"Similarity computed: {similarity:.4f}")

    return SimilarityResponse(
        similarity=round(float(similarity), 4),
        user_answer=req.user_answer,
        ideal_answer=req.ideal_answer
    )


@app.post("/embed", response_model=EmbedResponse)
def embed_text(req: EmbedRequest):
    """
    Return the raw embedding vector for a single text.
    Useful for debugging or caching embeddings externally.
    """
    if model is None:
        raise HTTPException(status_code=503, detail="Model not loaded yet")

    embedding = model.encode(req.text)
    return EmbedResponse(embedding=embedding.tolist())


# ── Helpers ───────────────────────────────────────────────────────────────────

def cosine_similarity(a, b) -> float:
    denom = norm(a) * norm(b)
    if denom == 0:
        return 0.0
    return float(dot(a, b) / denom)