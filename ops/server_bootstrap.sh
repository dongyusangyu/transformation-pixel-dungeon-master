#!/usr/bin/env bash
set -euo pipefail

# Baseline server stack for this project:
# - Docker + Docker Compose
# - PostgreSQL + Redis
# - Nginx reverse proxy placeholder
# - UFW + fail2ban
# - Prometheus + Grafana + node-exporter + cAdvisor
#
# Run as root on a Debian/Ubuntu server:
#   bash server_bootstrap.sh

APP_DIR="${APP_DIR:-/opt/dungeon}"
APP_USER="${APP_USER:-dungeon}"
SSH_PORT="${SSH_PORT:-22}"
BACKEND_PORT="${BACKEND_PORT:-8080}"

if [ "$(id -u)" -ne 0 ]; then
  echo "Please run as root."
  exit 1
fi

if ! command -v apt-get >/dev/null 2>&1; then
  echo "This script supports Debian/Ubuntu servers with apt-get."
  exit 1
fi

export DEBIAN_FRONTEND=noninteractive

echo "[1/8] Installing system packages..."
apt-get update
apt-get install -y \
  ca-certificates \
  curl \
  fail2ban \
  gnupg \
  nginx \
  openssl \
  ufw \
  docker.io

if ! docker compose version >/dev/null 2>&1; then
  if apt-cache show docker-compose-plugin >/dev/null 2>&1; then
    apt-get install -y docker-compose-plugin
  else
    apt-get install -y docker-compose
  fi
fi

if docker compose version >/dev/null 2>&1; then
  COMPOSE=(docker compose)
else
  COMPOSE=(docker-compose)
fi

echo "[2/8] Enabling services..."
systemctl enable --now docker
systemctl enable --now nginx
systemctl enable --now fail2ban

echo "[3/8] Creating app user and directories..."
if ! id "${APP_USER}" >/dev/null 2>&1; then
  useradd -m -s /bin/bash "${APP_USER}"
fi
usermod -aG docker "${APP_USER}" || true

mkdir -p \
  "${APP_DIR}/monitoring/prometheus" \
  "${APP_DIR}/monitoring/grafana/provisioning/datasources" \
  "${APP_DIR}/nginx" \
  "${APP_DIR}/data"

echo "[4/8] Writing environment file..."
if [ ! -f "${APP_DIR}/.env" ]; then
  POSTGRES_PASSWORD="$(openssl rand -base64 48 | tr -dc 'A-Za-z0-9' | head -c 32)"
  REDIS_PASSWORD="$(openssl rand -base64 48 | tr -dc 'A-Za-z0-9' | head -c 32)"
  GRAFANA_ADMIN_PASSWORD="$(openssl rand -base64 48 | tr -dc 'A-Za-z0-9' | head -c 24)"
  cat > "${APP_DIR}/.env" <<EOF
POSTGRES_DB=dungeon
POSTGRES_USER=dungeon
POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
REDIS_PASSWORD=${REDIS_PASSWORD}
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD}
BACKEND_PORT=${BACKEND_PORT}
EOF
  chmod 600 "${APP_DIR}/.env"
else
  echo "Existing ${APP_DIR}/.env kept."
fi

echo "[5/8] Writing Docker Compose stack..."
cat > "${APP_DIR}/docker-compose.yml" <<'EOF'
services:
  postgres:
    image: postgres:16-alpine
    restart: unless-stopped
    env_file: .env
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "127.0.0.1:5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    restart: unless-stopped
    env_file: .env
    command: ["sh", "-c", "redis-server --appendonly yes --requirepass \"$${REDIS_PASSWORD}\""]
    ports:
      - "127.0.0.1:6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD-SHELL", "redis-cli -a \"$${REDIS_PASSWORD}\" ping | grep PONG"]
      interval: 10s
      timeout: 5s
      retries: 5

  prometheus:
    image: prom/prometheus:v2.53.4
    restart: unless-stopped
    command:
      - "--config.file=/etc/prometheus/prometheus.yml"
      - "--storage.tsdb.retention.time=15d"
    ports:
      - "127.0.0.1:9090:9090"
    volumes:
      - ./monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus_data:/prometheus
    depends_on:
      - node-exporter
      - cadvisor

  grafana:
    image: grafana/grafana:11.1.4
    restart: unless-stopped
    env_file: .env
    environment:
      GF_SECURITY_ADMIN_USER: ${GRAFANA_ADMIN_USER}
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_ADMIN_PASSWORD}
      GF_USERS_ALLOW_SIGN_UP: "false"
    ports:
      - "127.0.0.1:3000:3000"
    volumes:
      - grafana_data:/var/lib/grafana
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning:ro
    depends_on:
      - prometheus

  node-exporter:
    image: prom/node-exporter:v1.8.2
    restart: unless-stopped
    pid: host
    command:
      - "--path.rootfs=/host"
    volumes:
      - "/:/host:ro,rslave"

  cadvisor:
    image: gcr.io/cadvisor/cadvisor:v0.49.1
    restart: unless-stopped
    privileged: true
    devices:
      - "/dev/kmsg:/dev/kmsg"
    volumes:
      - "/:/rootfs:ro"
      - "/var/run:/var/run:ro"
      - "/sys:/sys:ro"
      - "/var/lib/docker/:/var/lib/docker:ro"
      - "/dev/disk/:/dev/disk:ro"

volumes:
  postgres_data:
  redis_data:
  prometheus_data:
  grafana_data:
EOF

cat > "${APP_DIR}/monitoring/prometheus/prometheus.yml" <<'EOF'
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: prometheus
    static_configs:
      - targets: ["prometheus:9090"]

  - job_name: node
    static_configs:
      - targets: ["node-exporter:9100"]

  - job_name: cadvisor
    static_configs:
      - targets: ["cadvisor:8080"]
EOF

cat > "${APP_DIR}/monitoring/grafana/provisioning/datasources/prometheus.yml" <<'EOF'
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
EOF

echo "[6/8] Writing Nginx reverse proxy placeholder..."
cat > /etc/nginx/sites-available/dungeon <<EOF
server {
    listen 80 default_server;
    server_name _;

    client_max_body_size 20m;

    location = /health {
        access_log off;
        return 200 "ok\n";
        add_header Content-Type text/plain;
    }

    location / {
        proxy_pass http://127.0.0.1:${BACKEND_PORT};
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
EOF

rm -f /etc/nginx/sites-enabled/default
ln -sfn /etc/nginx/sites-available/dungeon /etc/nginx/sites-enabled/dungeon
nginx -t
systemctl reload nginx

echo "[7/8] Configuring firewall and fail2ban..."
ufw allow "${SSH_PORT}/tcp"
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

cat > /etc/fail2ban/jail.d/sshd.local <<EOF
[sshd]
enabled = true
port = ${SSH_PORT}
maxretry = 5
findtime = 10m
bantime = 1h
EOF
systemctl restart fail2ban

echo "[8/8] Starting data and monitoring stack..."
chown -R "${APP_USER}:${APP_USER}" "${APP_DIR}"
cd "${APP_DIR}"
"${COMPOSE[@]}" pull
"${COMPOSE[@]}" up -d

echo
echo "Server baseline is ready."
echo "App dir: ${APP_DIR}"
echo "Environment file: ${APP_DIR}/.env"
echo "Nginx health check: http://SERVER_IP/health"
echo
echo "Database is bound to 127.0.0.1:5432."
echo "Redis is bound to 127.0.0.1:6379."
echo "Grafana is bound to 127.0.0.1:3000."
echo "Prometheus is bound to 127.0.0.1:9090."
echo
echo "To view Grafana from your local machine, use an SSH tunnel:"
echo "  ssh -L 3000:127.0.0.1:3000 ${APP_USER}@SERVER_IP"
echo
echo "IMPORTANT: rotate any shared root password and prefer SSH keys after setup."
