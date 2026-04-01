# Activiti 6.0 Docker - Windows

## Voraussetzung

- Docker Desktop fuer Windows

## Schnellstart

```cmd
build.cmd        REM Image bauen (einmalig, braucht Internet)
start.cmd        REM Container starten (frische DB + automatische User)
stop.cmd         REM Container stoppen
```

## Zugriff

- **Activiti App:** http://localhost:9090/activiti-app (kermit/kermit)
- **REST API:** http://localhost:9090/activiti-rest/service (kermit/kermit)

## Image exportieren (fuer Rechner ohne Internet)

```cmd
export.cmd
```

Erzeugt `activiti6.tar` und `postgres-15-alpine.tar` im Elternverzeichnis (`docker/`).

### Import auf dem Zielrechner

```cmd
docker load -i activiti6.tar
docker load -i postgres-15-alpine.tar
```

Dann `docker-compose.yml` (aus dem Elternverzeichnis) und `start.cmd` kopieren.

## Hinweis: BuildKit

Das `build.cmd` setzt `DOCKER_BUILDKIT=0`, damit das exportierte Image auch auf aelteren Linux Docker-Versionen funktioniert.
