# Activiti Docker - Windows

## Voraussetzung

- Docker Desktop fuer Windows

## Schnellstart

### Activiti 6 (Port 9090)

```cmd
build.cmd        REM Image bauen (einmalig, braucht Internet)
start.cmd        REM Container starten (frische DB + automatische User)
stop.cmd         REM Container stoppen
```

### Activiti 5 (Port 9091)

```cmd
build-a5.cmd     REM Image bauen (einmalig, braucht Internet)
start-a5.cmd     REM Container starten
```

Zum Stoppen von Activiti 5 (kein eigenes Script):

```cmd
cd ..
docker compose -f docker-compose-a5.yml down
```

## Zugriff

- **A6 Web-UI:**  http://localhost:9090/activiti-app (kermit/kermit)
- **A6 REST API:** http://localhost:9090/activiti-rest/service (kermit/kermit)
- **A5 REST API:** http://localhost:9091/activiti-rest/service (kermit/kermit)

## Image exportieren (fuer Rechner ohne Internet)

```cmd
export.cmd
```

Erzeugt `activiti6.tar` und `postgres-15-alpine.tar` im Elternverzeichnis (`docker/`). Ein Export-Script fuer Activiti 5 existiert aktuell nicht; bei Bedarf manuell:

```cmd
docker save -o activiti5.tar activiti5:latest
```

### Import auf dem Zielrechner

```cmd
docker load -i activiti6.tar
docker load -i postgres-15-alpine.tar
```

Dann `docker-compose-a6.yml` (aus dem Elternverzeichnis) und `start.cmd` kopieren.

## Hinweis: BuildKit

Das `build.cmd` setzt `DOCKER_BUILDKIT=0`, damit das exportierte Image auch auf aelteren Linux Docker-Versionen funktioniert.
