#!/bin/bash
# Activiti 6.0 Docker - Images exportieren (Linux)
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$SCRIPT_DIR"

echo "Exportiere Docker-Images..."
echo ""

echo "[1/2] activiti6:latest ..."
docker save activiti6:latest -o activiti6.tar
echo "       -> activiti6.tar"

echo "[2/2] postgres:15-alpine ..."
docker save postgres:15-alpine -o postgres-15-alpine.tar
echo "       -> postgres-15-alpine.tar"

echo ""
echo "Fertig! Zum Importieren auf dem Zielrechner:"
echo "  docker load -i activiti6.tar"
echo "  docker load -i postgres-15-alpine.tar"
