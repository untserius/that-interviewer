# ── Stage 1: Build React ──────────────────────────────────────────────────────
FROM node:24-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm ci --silent

COPY public ./public
COPY src ./src

# REACT_APP_API_URL is passed as a build arg so it gets baked into the bundle
ARG REACT_APP_API_URL
ENV REACT_APP_API_URL=$REACT_APP_API_URL

RUN npm run build

# ── Stage 2: Serve with Nginx ─────────────────────────────────────────────────
FROM nginx:alpine

RUN rm /etc/nginx/conf.d/default.conf

# Use envsubst to replace $PORT at container startup
COPY nginx.conf /etc/nginx/templates/default.conf.template

COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 8080

CMD ["/bin/sh", "-c", "export PORT=${PORT:-8080} && envsubst '$PORT' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]