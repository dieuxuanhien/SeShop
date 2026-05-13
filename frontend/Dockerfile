FROM node:22-alpine AS build
WORKDIR /workspace
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ARG VITE_STRIPE_PUBLISHABLE_KEY
ENV VITE_STRIPE_PUBLISHABLE_KEY=$VITE_STRIPE_PUBLISHABLE_KEY
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
ENV BACKEND_UPSTREAM=http://backend:8080
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /workspace/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -qO- http://127.0.0.1/healthz >/dev/null || exit 1
