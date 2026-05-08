# SeShop Monitoring and Observability

This monitoring setup extends the production deployment with Prometheus metrics, Grafana dashboards, container health checks, and trace-aware logs.

## Signals

| Signal | Source | Use |
|---|---|---|
| Liveness/readiness | `/actuator/health/liveness`, `/actuator/health/readiness`, frontend `/healthz` | Container orchestration and release verification |
| Metrics | `/actuator/prometheus` | API availability, latency, error rate, JVM, and HikariCP database pool monitoring |
| Logs | Container stdout | Operational diagnosis; backend log lines include `traceId=` |
| Trace correlation | `X-Trace-Id` response/request header | Tie API responses, errors, and logs together |

## Start Monitoring

Use the monitoring profile with the production Compose stack:

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml --profile monitoring up -d
```

Prometheus is exposed on `PROMETHEUS_PORT` and Grafana on `GRAFANA_PORT`. Restrict both ports with host firewall rules or a private network when deploying outside a trusted environment.

## Dashboards

Grafana is provisioned from:

- `monitoring/grafana/provisioning/datasources/prometheus.yml`
- `monitoring/grafana/provisioning/dashboards/seshop.yml`
- `monitoring/grafana/dashboards/seshop-overview.json`

The default dashboard covers backend availability, request rate, server error rate, average HTTP latency, HikariCP pool usage, and JVM heap usage.

## Alerts

Prometheus loads alert rules from `monitoring/prometheus/alerts.yml`.

Initial rules cover:

- Backend scrape/down state for more than 2 minutes.
- Any sustained backend 5xx responses.
- Average HTTP latency above 1 second for 10 minutes.
- HikariCP active connections above 80 percent of max for 10 minutes.

Wire these alerts to an Alertmanager or managed alerting service before production go-live.

## Runbook Checks

```bash
curl -fsS http://localhost/healthz
curl -fsS http://localhost/api/v1/products
curl -fsS http://localhost:9090/-/ready
```

Use a browser to open Grafana and confirm the `SeShop Overview` dashboard is populated after the backend receives traffic.
