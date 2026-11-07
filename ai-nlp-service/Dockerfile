FROM python:3.12-slim

WORKDIR /app

# Install curl and uv for rapid package installations
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN pip install --no-cache-dir uv

# Copy dependencies
COPY requirements.txt .
RUN uv pip install --system --no-cache -r requirements.txt

# Copy application source
COPY . .

# Expose web service port
EXPOSE 8000

ENV PORT=8000
ENV PYTHONUNBUFFERED=1

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
