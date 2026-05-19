# discovery-portal-flow

Sanitized Spring Boot/Kotlin service for running recommender generation flows.

The repository intentionally contains only the flow orchestration API, task runtime, recommender generation services, persistence schema, and publication-safe placeholders for integrations with internal systems.

## Local Build

```bash
./gradlew.bat compileKotlin
```

On Unix-like environments use:

```bash
./gradlew compileKotlin
```

## Local Run

```bash
docker compose up --build
```

The default app port is `8080`; management endpoints are exposed on `8081`.