# Activiti Docker - Linux

Die Scripts in diesem Verzeichnis decken aktuell nur **Activiti 6** (Port 9090) ab.
Fuer Activiti 5 gibt es auf Linux keine eigenen Scripts — siehe „Activiti 5 auf Linux" unten.

## Voraussetzung

- Docker Engine + Docker Compose Plugin
- Falls nicht installiert: siehe `rhel-rpms/install-docker.sh` (Offline-Installation fuer RHEL 8)

## Docker installieren (RHEL 8, offline)

```bash
cd rhel-rpms
sh install-docker.sh
# Danach abmelden + anmelden (Docker-Gruppe)
```

## Schnellstart (Activiti 6)

### Variante A: Image auf dem Server bauen (braucht Internet)

```bash
./build.sh          # Image bauen
./start.sh          # Container starten
./stop.sh           # Container stoppen
```

### Variante B: Image von Windows importieren (kein Internet noetig)

Auf Windows: `windows\export.cmd` ausfuehren.
Dann die tar-Dateien auf den Server kopieren:

```bash
docker load -i activiti6.tar
docker load -i postgres-15-alpine.tar
./start.sh
```

## Activiti 5 auf Linux

Keine fertigen Scripts — direkt mit dem Compose-Plugin arbeiten:

```bash
cd ..                                          # ins docker/-Verzeichnis
docker compose -f docker-compose-a5.yml up -d
docker compose -f docker-compose-a5.yml down
```

Das Activiti-5-Image wird beim ersten `up -d` automatisch gebaut (braucht Internet). Fuer Offline-Installation manuell exportieren/importieren:

```bash
# Auf dem Bau-Rechner:
docker save -o activiti5.tar activiti5:latest
# Auf dem Ziel-Server:
docker load -i activiti5.tar
```

## Zugriff

- **A6 Web-UI:**  http://HOSTNAME:9090/activiti-app (kermit/kermit)
- **A6 REST API:** http://HOSTNAME:9090/activiti-rest/service (kermit/kermit)
- **A5 REST API:** http://HOSTNAME:9091/activiti-rest/service (kermit/kermit)

## Port aendern

```bash
ACTIVITI_PORT=8080 ./start.sh                                          # A6
ACTIVITI5_PORT=8081 docker compose -f ../docker-compose-a5.yml up -d   # A5
```

## Firewall (falls aktiv)

```bash
sudo firewall-cmd --permanent --add-port=9090/tcp   # A6
sudo firewall-cmd --permanent --add-port=9091/tcp   # A5 (optional)
sudo firewall-cmd --reload
```

## User manuell anlegen (Fallback)

Falls die automatische User-Erstellung nicht funktioniert:

```bash
./create-users.sh
```

## Windows-Zeilenenden fixen

Falls Scripts mit `\r: command not found` fehlschlagen:

```bash
sed -i 's/\r$//' *.sh
sed -i 's/\r$//' rhel-rpms/*.sh
```
