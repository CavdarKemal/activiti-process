# Activiti Docker

Docker-Setup fuer **Activiti 6.0.0** (Port 9090) und **Activiti 5.19.0** (Port 9091) — beide mit eigener PostgreSQL. Datenbank ist bei jedem Start frisch, User werden automatisch angelegt.

Beide Stacks laufen parallel (Compose-Projektnamen `ACTIVITI-6` bzw. `ACTIVITI-5`), die `TestSupportViewActivitiTest`-JUnit-Tests in `testsupport_client` sprechen beide an (A5 = `configs.get(0)`, A6 = `configs.get(1)` in `ENE-config.properties`).

---

## Verzeichnisstruktur

```
docker/
├── Dockerfile                  Activiti 6 Image (Tomcat 9 + JDK 11 + Activiti 6)
├── docker-compose-a6.yml       Container-Orchestrierung Activiti 6 (+ PostgreSQL)
├── docker-compose-a5.yml       Container-Orchestrierung Activiti 5 (+ PostgreSQL)
├── db.properties               REST-API DB-Konfiguration (A6)
├── activiti-app.properties     App DB-Konfiguration (A6)
├── engine.properties           Engine-Einstellungen (A6)
├── init-users.sql              Automatische User-Erstellung beim Start
├── startup.sh                  Container-Startscript (Tomcat + User-Init)
├── activiti6.tar               Exportiertes A6-Image (plattformunabhaengig)
├── postgres-15-alpine.tar      Exportiertes PostgreSQL-Image (plattformunabhaengig)
├── README.md                   Diese Datei
│
├── activiti5/                  Activiti-5 Build-Kontext (Dockerfile + Configs)
│   ├── Dockerfile              Activiti 5 Image (Tomcat 9 + JDK 8 + Activiti 5)
│   ├── db.properties
│   ├── engine.properties
│   ├── init-users.sql
│   ├── startup.sh
│   ├── smtp-sink.py
│   └── process-explorer/
│
├── windows/                    Windows-spezifische Scripts
│   ├── README.md
│   ├── build.cmd               Activiti-6 Image bauen
│   ├── start.cmd               Activiti-6 Container starten
│   ├── stop.cmd                Activiti-6 Container stoppen
│   ├── export.cmd              A6-Images als tar exportieren
│   ├── build-a5.cmd            Activiti-5 Image bauen
│   └── start-a5.cmd            Activiti-5 Container starten
│
└── linux/                      Linux-spezifische Scripts (Activiti-6)
    ├── README.md
    ├── build.sh                Image bauen
    ├── start.sh                Container starten
    ├── stop.sh                 Container stoppen
    ├── export.sh               Images als tar exportieren
    ├── create-users.sh         User manuell anlegen (Fallback)
    └── rhel-rpms/              Docker Offline-Installation fuer RHEL 8
        ├── install-docker.sh
        ├── containerd.io-*.rpm
        ├── docker-ce-*.rpm
        ├── docker-ce-cli-*.rpm
        └── docker-compose-plugin-*.rpm
```

---

## Zugriff

### Activiti 6 (Port 9090)

| URL | Beschreibung | Login |
|---|---|---|
| http://HOST:9090/ | Startseite mit Links | - |
| http://HOST:9090/activiti-app | Web-UI (Prozess-Designer, Tasks) | `kermit` / `kermit` |
| http://HOST:9090/activiti-rest/service | REST API | `kermit` / `kermit` |

### Activiti 5 (Port 9091)

| URL | Beschreibung | Login |
|---|---|---|
| http://HOST:9091/activiti-rest/service | REST API | `kermit` / `kermit` |
| http://HOST:9091/process-explorer | Process Explorer | - |

Activiti 5 hat **keine** Activiti-App (Web-UI), nur REST-API + Process-Explorer.

### Container- und DB-Ports

| Compose-Stack | Activiti (Host → Container) | PostgreSQL (Host → Container) | SMTP-Sink |
|---|---|---|---|
| `ACTIVITI-6` | `9090:8080` | `5433:5432` | `2525:25` |
| `ACTIVITI-5` | `9091:8080` | `5434:5432` | `2526:25` |

## Automatisch angelegte User

| User-ID | Passwort | Name |
|---|---|---|
| kermit | kermit | Demo-User (Activiti Standard) |
| CAVDARK-ENE | cavdark | Kemal Cavdar (ENE) |
| CAVDARK-ABE | cavdark | Kemal Cavdar (ABE) |
| CAVDARK-GEE | cavdark | Kemal Cavdar (GEE) |
| CAVDARK-PRE | cavdark | Kemal Cavdar (PRE) |
| NELLENN-ENE | nellenn | Norbert Nellen (ENE) |
| NELLENN-ABE | nellenn | Norbert Nellen (ABE) |
| NELLENN-GEE | nellenn | Norbert Nellen (GEE) |
| admin | admin | Administrator |

User werden automatisch beim Container-Start angelegt (via `init-users.sql`). Neue User koennen dort ergaenzt werden.

---

## Schnellstart

### Aus dem `docker/`-Verzeichnis

```bash
# Activiti 6 starten (Port 9090)
docker compose -f docker-compose-a6.yml up -d

# Activiti 5 starten (Port 9091)
docker compose -f docker-compose-a5.yml up -d

# Status pruefen
docker ps --format "{{.Names}}\t{{.Ports}}"
```

Erwartetes Ergebnis:
```
ACTIVITI-5      0.0.0.0:2526->25/tcp, 0.0.0.0:9091->8080/tcp
ACTIVITI-5-db   0.0.0.0:5434->5432/tcp
ACTIVITI-6      0.0.0.0:2525->25/tcp, 0.0.0.0:9090->8080/tcp
ACTIVITI-6-db   0.0.0.0:5433->5432/tcp
```

### Windows-Scripts

```cmd
windows\build.cmd        REM A6 Image bauen (einmalig)
windows\start.cmd        REM A6 Container starten
windows\stop.cmd         REM A6 Container stoppen
windows\build-a5.cmd     REM A5 Image bauen (einmalig)
windows\start-a5.cmd     REM A5 Container starten
```

A5 hat aktuell kein `stop-a5.cmd`; zum Stoppen:
```cmd
docker compose -f docker-compose-a5.yml down
```

### Linux-Scripts (Activiti 6)

```bash
linux/build.sh           # Image bauen
linux/start.sh           # Container starten
linux/stop.sh            # Container stoppen
```

Fuer Activiti 5 gibt es keine Linux-Scripts — direkt mit `docker compose -f docker-compose-a5.yml {up -d,down}` arbeiten.

---

## Frische DB bei jedem Start

Beide PostgreSQL-Datenbanken laufen auf `tmpfs` (RAM-Disk). Bei jedem `down` + `up` wird die DB komplett neu erstellt:
- Activiti-Schema wird automatisch angelegt
- Demo-User und -Prozesse werden deployed
- Eigene User werden aus `init-users.sql` angelegt
- Eigene Deployments gehen beim Stop verloren

---

## Container-Architektur

Zwei unabhaengige Compose-Stacks. Jeder besteht aus einem Activiti- und einem Postgres-Container.

```
ACTIVITI-6 (docker-compose-a6.yml)
┌─────────────────────────────────────────────────┐
│  ACTIVITI-6 (Port 9090 -> 8080)                 │
│  ├── Tomcat 9.0                                 │
│  ├── JDK 11 (Temurin)                           │
│  ├── activiti-rest.war   (REST API)             │
│  ├── activiti-app.war    (Web-UI)               │
│  ├── postgresql-client   (fuer User-Init)       │
│  ├── startup.sh          (Tomcat + User-Init)   │
│  └── init-users.sql      (User-Definitionen)    │
├─────────────────────────────────────────────────┤
│  ACTIVITI-6-db (Host-Port 5433)                 │
│  ├── PostgreSQL 15 (Alpine)                     │
│  ├── DB: activiti                               │
│  ├── User: postgres / postgres                  │
│  └── tmpfs (RAM-Disk, keine Persistenz)         │
└─────────────────────────────────────────────────┘

ACTIVITI-5 (docker-compose-a5.yml)
┌─────────────────────────────────────────────────┐
│  ACTIVITI-5 (Port 9091 -> 8080)                 │
│  ├── Tomcat 9.0                                 │
│  ├── JDK 8                                      │
│  ├── activiti-rest.war   (REST API, keine App)  │
│  └── process-explorer                           │
├─────────────────────────────────────────────────┤
│  ACTIVITI-5-db (Host-Port 5434)                 │
│  ├── PostgreSQL 15 (Alpine)                     │
│  ├── DB: activiti                               │
│  └── tmpfs (RAM-Disk)                           │
└─────────────────────────────────────────────────┘
```

## Port aendern

- **Activiti 6**, Windows: `set ACTIVITI_PORT=8080` vor `start.cmd`
- **Activiti 6**, Linux: `ACTIVITI_PORT=8080 linux/start.sh`
- **Activiti 5**, Windows: `set ACTIVITI5_PORT=8081` vor `start-a5.cmd`
- **Activiti 5**, Linux: `ACTIVITI5_PORT=8081 docker compose -f docker-compose-a5.yml up -d`

---

## Neue User hinzufuegen

1. `init-users.sql` (A6) bzw. `activiti5/init-users.sql` (A5) bearbeiten (INSERT-Statements ergaenzen)
2. Image neu bauen (`build.cmd`/`build-a5.cmd` bzw. `linux/build.sh`)
3. Container neu starten

---

## Bekannte Probleme und Loesungen

### Windows-Zeilenenden in Shell-Scripts

Das Dockerfile enthaelt `sed -i 's/\r$//'` fuer `startup.sh`, damit es unter Linux ausfuehrbar ist. Falls weitere Scripts betroffen sind, auf dem Linux-Server ausfuehren:

```bash
sed -i 's/\r$//' *.sh
```

### BuildKit-Kompatibilitaet

`windows\build.cmd` setzt `DOCKER_BUILDKIT=0`, da BuildKit-Attestations von aelteren Docker-Versionen auf Linux nicht gelesen werden koennen (`unexpected EOF` beim `docker load`).

### Image-Export

Die tar-Dateien (`activiti6.tar`, `postgres-15-alpine.tar`) liegen im `docker/`-Hauptverzeichnis und sind **plattformunabhaengig** (Linux amd64). Sie koennen sowohl auf Windows als auch auf Linux importiert werden.

### Stale Container nach Umbenennung

Falls Container mit alten Namen (`docker-activiti-1`, `TestSupportClient-activiti6` etc.) existieren und Port 9090/9091 belegen, zuerst aufraeumen:

```powershell
docker rm -f docker-activiti-1 docker-activiti-db-1 docker-activiti5-1 docker-activiti5-db-1 TestSupportClient-activiti6 TestSupportClient-activiti6-db TestSupportClient-activiti5 TestSupportClient-activiti5-db my_activiti 2>$null
```

---

Zuletzt aktualisiert: 19. April 2026
