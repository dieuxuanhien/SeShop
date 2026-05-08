# SeShop Production Deployment

This runbook implements the deployment shape described in [SESHOP SAD](../3.Design/SESHOP%20SAD.md#33-deployment-view) and [SESHOP ADD](../3.Design/SESHOP%20ADD.md#33-deployment-view): a React SPA container, stateless Spring Boot API container, PostgreSQL, and Redis.

## Prerequisites

- A Linux host with Docker Engine and Docker Compose v2.
- GHCR access to the published `seshop-backend` and `seshop-frontend` images.
- DNS and TLS termination in front of the frontend container.
- A secret store or host-local `.env.production` file outside source control.

## Publish Images

Images are built and pushed by `.github/workflows/docker-publish.yml`.

1. Set the GitHub Actions repository variable `VITE_API_BASE_URL` to `/api/v1` for same-origin deployment.
2. Push to `main`, push a `v*` tag, or run the workflow manually.
3. Confirm these images exist in GHCR:
   - `ghcr.io/<owner>/<repo>/seshop-backend:<tag>`
   - `ghcr.io/<owner>/<repo>/seshop-frontend:<tag>`

## Configure Host

Create the production environment file from the tracked example:

```bash
cp .env.production.example .env.production
```

Set at minimum:

- `SESHOP_BACKEND_IMAGE`
- `SESHOP_FRONTEND_IMAGE`
- `PUBLIC_ORIGIN`
- `POSTGRES_PASSWORD`
- `JWT_SECRET`

Enable and configure external providers only when credentials are approved for production.

## Deploy

```bash
docker login ghcr.io
docker compose --env-file .env.production -f docker-compose.prod.yml pull
docker compose --env-file .env.production -f docker-compose.prod.yml up -d
```

Verify health:

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml ps
curl -fsS http://localhost/healthz
curl -fsS http://localhost/api/v1/products
```

The frontend container proxies `/api/*` to the backend container, so the browser can use same-origin `/api/v1` requests.

## Monitoring

After the core stack is healthy, start the monitoring profile:

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml --profile monitoring up -d
```

See [Monitoring Observability](Monitoring%20Observability.md) for dashboards, alert rules, and operational checks.

## Rollback

1. Set `SESHOP_BACKEND_IMAGE` and `SESHOP_FRONTEND_IMAGE` to the previous known-good tags.
2. Pull and restart:

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml pull
docker compose --env-file .env.production -f docker-compose.prod.yml up -d
```

Keep PostgreSQL backups aligned with the approved production backup policy before applying schema-changing releases.
