FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --upgrade pip --quiet && \
    pip install --no-cache-dir -r requirements.txt --quiet

COPY main.py .

RUN useradd -m appuser && chown -R appuser /app
USER appuser

EXPOSE 8000

# Model downloads on first request, cached in ~/.cache/huggingface
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]