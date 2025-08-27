#  Perf Starter (Persona-based JMeter + WireMock + Docker)

This repo gives you a **sample microservice** that calls **WireMock** downstreams and a **persona-based load test** using **JMeter (Java DSL)**. It generates an HTML report, lets you **save a baseline**, and includes a **GitHub Actions** workflow to run smoke on PRs and a baseline nightly.

## Prereqs (local)
- Docker / Docker Compose
- Java 21 + Maven (only to *generate* the JMX file)
- Python 3 (to compare baseline vs current)

## 1) Start the service and WireMock
```bash
docker compose up -d app wiremock
```

## 2) Generate the JMX file from Java DSL
```bash
cd perf/dsl
mvn -q -Dtest=perf.BaselineTest test -DVUS=100 -DRUN_ID=baseline-v1
# This writes: perf/jmeter/personas.jmx
```

## 3) Run JMeter in Docker and build the HTML report
```bash
cd ../..   # back to repo root
RUN_ID=baseline-v1 docker compose run --rm jmeter
# Open perf/jmeter/reports/index.html in your browser
```

## 4) Save a baseline snapshot (first run only)
```bash
python3 perf/scripts/extract_stats.py perf/jmeter/reports/statistics.json > baseline.json
```

## 5) Compare future runs to the baseline
```bash
python3 perf/scripts/extract_stats.py perf/jmeter/reports/statistics.json > current.json
python3 perf/scripts/compare_perf.py current.json
```

## 6) Push to GitHub
- Commit the whole folder to a new repo.
- The workflow `.github/workflows/perf.yml` runs:
  - **Smoke test on PRs** (30 VUs, quick)
  - **Baseline nightly** (100 VUs)
- It will **skip compare** automatically if `baseline.json` isn’t present yet. Commit your `baseline.json` to set the guardrail.

## Tuning
- Change persona weights/think time in `perf/personas/*.yaml` (or adjust directly in `BaselineTest.java`).
- Change WireMock delays in `wiremock/mappings/*.json` to simulate slower/failing dependencies.
- Increase total VUs by passing `-DVUS=200` when generating the JMX.

## Endpoints under test
- `GET /menu`
- `POST /orders` (calls WireMock `/payments` and `/rewards/earn` with realistic delays)

Have fun benchmarking! 🚀
