#!/bin/bash
# Activiti 6.0 Docker - Image bauen (Linux)
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$SCRIPT_DIR"

echo "Baue Activiti 6 Docker-Image..."
docker compose build
echo ""
echo "Fertig! Starten mit: linux/start.sh"
