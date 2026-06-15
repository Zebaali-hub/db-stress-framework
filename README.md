# Database Stress Testing Framework

Configurable stress testing for JDBC databases, built from Oracle-scale database performance work: concurrent workload execution, live TPS, latency percentiles, and downloadable reports.

## Origin Story

This project is a public, open-source productization of the database stress-testing workflow I used and refined while working on Oracle RDBMS engineering.

At Oracle, my day-to-day work involved validating database behavior under high-concurrency, high-volume workloads: large schemas, many sessions, multiple PDBs, RAC/Exadata environments, vector search benchmarks, AWR/SQL Monitor analysis, trace investigation, and performance reporting. I used personal helper workflows around repeatable workload execution, metrics collection, and reporting to make this work easier for myself. This project turns that personal workflow into a clean Spring Boot application that can be shown publicly without using any Oracle internal code, data, infrastructure, or proprietary tooling.

In interview terms:

> I built this side project to productize the personal stress-testing and performance analysis workflow I used to make my Oracle work easier. The public version uses Spring Boot, JDBC, HikariCP, WebSocket metrics, and PDF/JSON reporting so the same idea can run against any JDBC-accessible database.

This is not Oracle official internal software and does not contain Oracle code or data. It is my public implementation of the workflow I used personally while doing Oracle-scale engineering work.

## What This Project Does

This application answers a simple question:

> "If I put 10, 50, or 100 concurrent database clients on this database, how does it behave?"

You provide a JDBC database connection and a workload configuration. The app opens a per-test connection pool, starts worker threads, runs SQL through JDBC, measures every query, and streams live results to a dashboard.

During a test, you can see:

- **Current TPS**: how many successful database operations finished in the last second
- **p50 latency**: typical response time
- **p95 latency**: slow response time for the slowest 5% of requests
- **p99 latency**: worst-case tail latency for the slowest 1%
- **Error rate**: percentage of failed database operations
- **Active threads**: how many worker threads are currently applying load

After the test, the app saves the result and generates JSON/PDF reports.

## Highlights

- Java 17 and Spring Boot 3.2 backend
- Per-test HikariCP connection pools against the target database
- Clean lifecycle state machine: `PENDING -> RUNNING -> COMPLETED | STOPPED | FAILED`
- Strategy-based workloads: read-heavy, write-heavy, mixed, and transaction-heavy
- Dialect layer with PostgreSQL support and MySQL/Oracle extension points
- Live WebSocket metrics: TPS, p50, p95, p99, error rate, active threads
- JSON and PDF reports
- Thymeleaf + Chart.js dashboard
- OpenAPI UI at `/swagger-ui.html`

## Quick Start

### Option 1: Local Demo Without PostgreSQL

Use this when you just want to open the UI and understand the flow.

```bash
cd /Users/zebaali/Developer/zebacodes/db-stress-framework
mvn spring-boot:run -Dspring-boot.run.profiles=test -Dspring-boot.run.arguments=--server.port=8081
```

Open:

```text
http://localhost:8081
```

For this quick demo, use a custom query workload against an in-memory H2 target database:

```text
JDBC URL: jdbc:h2:mem:targetdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
Username: sa
Password:
Concurrent Threads: 5
Duration: 15
Ramp Up: 2
Workload Type: MIXED
Custom Queries:
SELECT 1
```

Click **Start Stress Test**.

Expected result:

- Dashboard opens automatically.
- TPS chart starts receiving points every second.
- Latency chart shows p50/p95/p99 lines.
- Error rate should stay near `0%`.
- After ~15 seconds, status changes from `RUNNING` to `COMPLETED`.
- JSON and PDF report download buttons become useful for reviewing the final result.

### Option 2: PostgreSQL Demo With Docker

Use this for the more realistic demo.

```bash
docker compose up --build
```

Open `http://localhost:8080`.

The bundled PostgreSQL service stores test history. The target database is supplied in the test form or REST API and is intentionally separate from the application history database.

If you run the Spring app on your laptop and PostgreSQL from Docker, use:

```text
JDBC URL: jdbc:postgresql://localhost:5432/db_stress
Username: zeba
Password: password
Concurrent Threads: 20
Duration: 30
Ramp Up: 5
Workload Type: MIXED
Custom Queries: leave empty
```

If you run both the app and PostgreSQL through `docker compose`, use this target URL from inside the app container:

```text
jdbc:postgresql://postgres:5432/db_stress
```

For PostgreSQL default workloads, the app creates this target table automatically:

```sql
CREATE TABLE IF NOT EXISTS stress_test_data (
    id SERIAL PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT NOW()
);
```

## What You Will See

### Home Page

The home page has:

- a stress test form
- workload type selector
- optional custom SQL textarea
- recent test history table

### Live Dashboard

The dashboard shows:

```text
Status: RUNNING / COMPLETED / STOPPED / FAILED
Current TPS: 120
p99 Latency: 14.82 ms
Error Rate: 0.00%
Elapsed Time: 18s
```

You will also see two live charts:

- **TPS over time**: useful for spotting throughput drops or saturation
- **Latency percentiles over time**: useful for spotting tail-latency spikes

### Final Reports

The JSON/PDF report includes:

- test ID
- target database URL
- thread count, duration, ramp-up, workload type
- total transactions
- total errors
- average TPS
- peak TPS
- p50/p95/p99 latency
- performance grade
- recommendations

Example interpretation:

```text
Total Transactions: 3,420
Total Errors: 0
Average TPS: 114.00
Peak TPS: 146.00
P99 Latency: 38.40 ms
Grade: GOOD
```

Meaning:

- The database handled the configured workload successfully.
- Tail latency stayed below 50 ms.
- There were no failed operations.
- You can try increasing thread count to find the saturation point.

## REST API

```text
POST  /api/stress/start
GET   /api/stress/{id}/status
POST  /api/stress/{id}/stop
GET   /api/stress/{id}/report/pdf
GET   /api/stress/{id}/report/json
GET   /api/stress/history
```

Start a test:

```bash
curl -X POST http://localhost:8081/api/stress/start \
  -H "Content-Type: application/json" \
  -d '{
    "jdbcUrl": "jdbc:h2:mem:targetdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
    "username": "sa",
    "password": "",
    "concurrentThreads": 5,
    "durationSeconds": 15,
    "rampUpSeconds": 2,
    "workloadType": "MIXED",
    "customQueries": ["SELECT 1"]
  }'
```

Response:

```json
{
  "dashboardUrl": "/dashboard/6bbf44f7-7f11-4959-b6c7-2a836c9d6317",
  "testId": "6bbf44f7-7f11-4959-b6c7-2a836c9d6317"
}
```

Check status:

```bash
curl http://localhost:8081/api/stress/6bbf44f7-7f11-4959-b6c7-2a836c9d6317/status
```

Example status:

```json
{
  "testId": "6bbf44f7-7f11-4959-b6c7-2a836c9d6317",
  "currentTps": 128.0,
  "errorRate": 0.0,
  "p50LatencyMs": 1.32,
  "p95LatencyMs": 3.91,
  "p99LatencyMs": 5.44,
  "totalTransactions": 1480,
  "totalErrors": 0,
  "activeThreads": 5,
  "elapsedSeconds": 12,
  "status": "RUNNING"
}
```

Stop a running test:

```bash
curl -X POST http://localhost:8081/api/stress/{testId}/stop
```

Download reports:

```bash
curl -o report.json http://localhost:8081/api/stress/{testId}/report/json
curl -o report.pdf http://localhost:8081/api/stress/{testId}/report/pdf
```

## Architecture

```text
REST API / Web UI
      |
StressTestService
      |
WorkloadExecutor -> per-test HikariCP -> Target JDBC Database
      |
MetricsCollector -> WebSocketPublisher -> Live Dashboard
      |
ReportGeneratorService -> JSON/PDF
      |
PostgreSQL history database
```

## Interview Notes

The backend is intentionally designed around production concerns:

- target database isolation through per-test pools
- clean cancellation and executor shutdown
- rolling one-second metric windows
- bounded final latency samples
- workload strategy pattern
- dialect abstraction instead of hard-coded PostgreSQL-only SQL

How to explain this project in an interview:

> I built a database-specific stress testing framework in Java/Spring Boot. It accepts a JDBC target, creates an isolated HikariCP pool per test, ramps worker threads through ExecutorService, executes configurable read/write/transaction workloads, collects TPS and p50/p95/p99 latency every second, streams metrics over WebSocket to a dashboard, and generates PDF/JSON reports. It productizes the kind of workload execution and performance reporting I did at Oracle.

Good technical points to mention:

- App history DB and target DB are intentionally separate.
- Each stress test has its own connection pool, so tests are isolated.
- Metrics use thread-safe counters and rolling one-second latency windows.
- Workloads use a strategy pattern, so new workload types can be added cleanly.
- SQL is behind a dialect layer, so PostgreSQL/MySQL/Oracle can differ safely.
- Stop/cancel is handled through a shared stop signal and graceful executor shutdown.

## Troubleshooting

### Port 8080 Is Already Used

Run on another port:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test -Dspring-boot.run.arguments=--server.port=8081
```

Then open:

```text
http://localhost:8081
```

### PostgreSQL Is Not Running

Use the test profile demo:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test -Dspring-boot.run.arguments=--server.port=8081
```

Or start PostgreSQL:

```bash
docker compose up postgres
```

### Default Workloads Fail On H2

H2 is only for the quick demo. Use **Custom Queries** with:

```sql
SELECT 1
```

For full default workloads, use PostgreSQL.

## Deployment

This Spring Boot backend should be deployed to a container-friendly platform, not Vercel.

Recommended production-style deployment:

- Render, Railway, Fly.io, or AWS for the Spring Boot backend
- PostgreSQL for test history
- Vercel only for a separate static portfolio/demo page

See [Deployment Guide](docs/DEPLOYMENT.md).

## Author

Zeba Ali, ex-Oracle MTS IC2. Built from real concurrent execution and database performance engineering experience across Oracle RDBMS, Exadata, Exascale, and RAC.
