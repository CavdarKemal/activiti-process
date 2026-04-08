#!/bin/bash
# Activiti 6.0 Docker - Start (Linux)
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$SCRIPT_DIR"

export ACTIVITI_PORT="${ACTIVITI_PORT:-9090}"

echo "Starte Activiti 6.0 (frische DB bei jedem Start)..."
echo ""
echo "  Activiti App:  http://$(hostname):${ACTIVITI_PORT}/activiti-app  (kermit/kermit)"
echo "  REST API:      http://$(hostname):${ACTIVITI_PORT}/activiti-rest/service  (kermit/kermit)"
echo "  User werden automatisch angelegt (init-users.sql)"
echo ""

docker compose up "$@"
