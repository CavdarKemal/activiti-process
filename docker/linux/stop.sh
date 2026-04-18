#!/bin/bash
# Activiti 6.0 Docker - Stop (Linux)
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$SCRIPT_DIR"

docker compose -f docker-compose-a6.yml down
echo "Activiti 6.0 gestoppt."
