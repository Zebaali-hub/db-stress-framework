# Deployment Guide

## Why This App Should Not Run On Vercel

This project is a Spring Boot backend application. It needs:

- a long-running Java process
- WebSocket/STOMP connections
- background worker threads for stress tests
- JDBC connection pools
- PostgreSQL for test history

Vercel is optimized for frontend apps and serverless functions. It is not the right production host for this Spring Boot server.

Use Vercel later for a portfolio landing page if needed, but deploy this backend to a container-friendly platform such as Render, Railway, Fly.io, or AWS.

## Recommended: Render

The repository includes `render.yaml`, so Render can deploy the app as a Docker web service with a managed PostgreSQL database.

### Steps

1. Push this repo to GitHub.
2. Go to Render.
3. Create a new Blueprint.
4. Select this repository.
5. Render will read `render.yaml`.
6. Deploy.

Render creates:

- `db-stress-framework`: Spring Boot web service
- `db-stress-history`: PostgreSQL history database

The app will expose:

```text
/
/api/stress/history
/swagger-ui.html
/actuator/health
```

## Important Demo Note

The deployed app stores history in PostgreSQL. The **target database** you stress-test is still supplied in the UI form.

For a public demo, do not stress-test private or production databases.

Use either:

- a disposable PostgreSQL database
- a demo database created only for this app
- custom queries like `SELECT 1` for a safe smoke test

## Local Production-Like Run

```bash
docker compose up --build
```

Open:

```text
http://localhost:8080
```

## Vercel Option

Use Vercel only for a separate static portfolio page that links to:

- GitHub repo
- deployed backend URL
- screenshots
- demo video

Do not use Vercel for the Spring Boot backend itself.
