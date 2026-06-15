# Interview Story

## Short Pitch

I built a Database Stress Testing Framework as a side project to productize the personal stress-testing workflow I used to make my Oracle RDBMS engineering work easier.

At Oracle, I worked on validating database behavior under large-scale concurrent workloads: high row counts, multiple sessions, PDB/RAC-style environments, performance metrics, trace analysis, AWR/SQL Monitor review, and production-defect reproduction. To make my own work more repeatable, I used helper workflows around running stress workloads, collecting metrics, and producing reports. This project recreates that personal workflow as a public Spring Boot application using JDBC, HikariCP, ExecutorService, WebSocket metrics, and report generation.

## What Problem It Solves

Database teams often need to answer:

- How much concurrent load can this database handle?
- What happens when thread count increases?
- Are p95/p99 latencies stable?
- Do errors appear under write-heavy or transaction-heavy workloads?
- Can we generate a repeatable report after each test?

This project gives a simple UI and REST API for running those tests.

## How It Connects To Oracle Work

At Oracle, this was not an official company-wide product. The real work involved Oracle environments, internal frameworks, large schemas, PDBs, RAC/Exadata systems, SQL workloads, trace files, AWR reports, SQL Monitor, and bug reproduction.

This project is the public version of the personal workflow I used to make that work easier:

- Oracle work: connect to target database services/PDBs.
- This project: connect through a JDBC URL, username, and password.
- Oracle work: run concurrent sessions and workloads.
- This project: run configurable worker threads through ExecutorService.
- Oracle work: measure TPS, latency, failures, waits, regressions.
- This project: measures TPS, p50, p95, p99, error rate, and active threads.
- Oracle work: prepare findings for engineers/directors.
- This project: generates JSON and PDF reports.

## Safe Resume Wording

Use:

> Built an open-source Spring Boot database stress-testing framework productizing Oracle-scale workload validation experience: per-test HikariCP pools, concurrent JDBC execution, live TPS/p50/p95/p99 metrics, WebSocket dashboard, and PDF/JSON reports.

Or, if you want the personal-tool angle stronger:

> Built a public Spring Boot version of the personal database stress-testing workflow I used at Oracle to streamline workload execution, TPS/latency tracking, and report generation across high-concurrency database validation tasks.

Avoid:

> Oracle officially used/adopted this exact public project.

Unless the exact public project was actually used there, that wording is risky.

## Interview Answer

If asked, "Did you use this at Oracle?", say:

> I used this kind of personal workflow to make my Oracle stress-testing work more repeatable: connecting to target database environments, running concurrent SQL workloads, tracking TPS and latency, and preparing reports. This public repo is the clean shareable version of that workflow, built without Oracle internal code, data, or infrastructure.

If you did build personal helper scripts or side automation during Oracle work, say:

> I had personal helper workflows/scripts for repeatability and reporting, but this repo is a clean public implementation without Oracle code or data.

## Why It Stands Out

Most backend portfolio projects are CRUD apps. This one shows:

- Java backend engineering
- database internals awareness
- concurrency and thread pools
- JDBC and connection pooling
- performance metrics
- real-time WebSocket updates
- report generation
- production-style lifecycle and cancellation handling

That makes it directly aligned with backend, database platform, infra, and AI/data infrastructure roles.
