#!/bin/bash
# Docker Offline-Installation fuer RHEL 8 / CentOS 8
# Alle RPMs muessen im selben Verzeichnis wie dieses Script liegen

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== Docker Offline-Installation fuer RHEL 8 ==="
echo ""

# Alte Versionen entfernen
echo "[1/4] Entferne alte Docker/Podman-Versionen..."
sudo yum remove -y docker docker-client docker-client-latest docker-common \
    docker-latest docker-latest-logrotate docker-logrotate docker-engine \
    podman runc buildah 2>/dev/null

# RPMs installieren
echo ""
echo "[2/4] Installiere Docker RPMs..."
sudo yum install -y \
    ./containerd.io-*.rpm \
    ./docker-ce-cli-*.rpm \
    ./docker-ce-*.rpm \
    ./docker-compose-plugin-*.rpm

# Docker starten und aktivieren
echo ""
echo "[3/4] Starte Docker..."
sudo systemctl start docker
sudo systemctl enable docker

# User zur Docker-Gruppe hinzufuegen
echo ""
echo "[4/4] Fuege $USER zur Docker-Gruppe hinzu..."
sudo usermod -aG docker $USER

echo ""
echo "=== Installation abgeschlossen ==="
echo ""
docker --version
docker compose version
echo ""
echo "WICHTIG: Abmelden und neu anmelden, damit die Docker-Gruppe wirkt!"
echo "         Oder: newgrp docker"
echo ""
echo "Test: docker run hello-world"
