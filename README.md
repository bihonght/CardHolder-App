# CardHolder-App

A simple Java web app built with **Spark Java + Gradle + PostgreSQL** that securely stores and searches cardholder information.  
Card numbers are **AES-256 encrypted** and only searchable by their last 4 digits.
---
## Quick Start (Docker)

### Clone the project
```bash
git clone https://github.com/bihonght/CardHolder-App
cd CardHolder-App

openssl rand -base64 32
# Copy the output and paste it into docker-compose.yml under:

AES_KEY_B64: "<your-generated-key>"
```

### Build
```bash
docker compose up --build

```

### App
Web app: http://localhost:8080

Health check: http://localhost:8080/health