FROM python:3.12-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app/ ./app/
COPY run.py .

ENV EMBED_HOST=0.0.0.0
ENV EMBED_PORT=8000
ENV EMBED_PERSIST_DIRECTORY=/data/chroma

EXPOSE 8000

VOLUME /data/chroma

CMD ["python", "run.py"]
