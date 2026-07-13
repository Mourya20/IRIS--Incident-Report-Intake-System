# IRIS - Incident Report Intake System (Stage 1 Backend)

IRIS is a Spring Boot + PostgreSQL backend for incident intake, triage, auditing, and IOC enrichment.

## Stage 1 implemented
- Spring Boot backend with Flyway migrations
- PostgreSQL-ready configuration
- JWT auth + RBAC (`REPORTER`, `ANALYST`, `ADMIN`)
- Incident APIs with role-aware visibility
- Hash-chained incident audit log + verification endpoint
- Severity scoring foundation using DB rules
- IOC enrichment abstraction with VirusTotal + AbuseIPDB providers (graceful fallback)
- MITRE ATT&CK curated seed + keyword suggestion mapping

## Local setup
### Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL 16+ (or Docker)

### Environment variables
- `DB_URL` (default: `jdbc:postgresql://localhost:5432/iris`)
- `DB_USERNAME` (default: `iris`)
- `DB_PASSWORD` (default: `iris`)
- `JWT_SECRET` (must be >= 32 chars)
- `JWT_EXPIRATION_MINUTES` (default: `120`)
- `VT_ENABLED`, `VT_API_KEY`
- `ABUSEIPDB_ENABLED`, `ABUSEIPDB_API_KEY`

### Run with Maven
```bash
mvn spring-boot:run
```

Flyway migrations run automatically at startup.

### Run with Docker Compose
```bash
mvn clean package
docker compose up --build
```

## Auth flow
1. Register reporter user:
   - `POST /api/auth/register`
2. Login:
   - `POST /api/auth/login`
3. Use bearer token:
   - Authorization header with ****** from login response

## Core Stage 1 APIs
- `POST /api/incidents`
- `GET /api/incidents/{id}`
- `GET /api/incidents?status=&severity=&assigneeId=`
- `PATCH /api/incidents/{id}/status`
- `POST /api/incidents/{id}/iocs`
- `GET /api/incidents/{id}/audit/verify`
- Admin severity rule management:
  - `GET /api/admin/severity-rules`
  - `PUT /api/admin/severity-rules/{id}`

## Testing
```bash
mvn test
```

Included baseline tests:
- JWT auth issuance
- Audit hash-chain integrity verification
- Incident create + role authorization
