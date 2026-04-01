# Activiti 6.0 Docker - Linux

## Voraussetzung

- Docker Engine + Docker Compose Plugin
- Falls nicht installiert: siehe `rhel-rpms/install-docker.sh` (Offline-Installation fuer RHEL 8)

## Docker installieren (RHEL 8, offline)

```bash
cd rhel-rpms
sh install-docker.sh
# Danach abmelden + anmelden (Docker-Gruppe)
```

## Schnellstart

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

## Zugriff

- **Activiti App:** http://HOSTNAME:9090/activiti-app (kermit/kermit)
- **REST API:** http://HOSTNAME:9090/activiti-rest/service (kermit/kermit)

## Port aendern

```bash
ACTIVITI_PORT=8080 ./start.sh
```

## Firewall (falls aktiv)

```bash
sudo firewall-cmd --permanent --add-port=9090/tcp
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
