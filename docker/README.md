# Activiti 6.0 Docker

Docker-Setup fuer Activiti 6.0.0 mit PostgreSQL. Die Datenbank ist bei jedem Start frisch. User werden automatisch angelegt.

---

## Verzeichnisstruktur

```
docker/
├── Dockerfile                  Image-Definition (Tomcat 9 + JDK 11 + Activiti 6)
├── docker-compose.yml          Container-Orchestrierung (Activiti + PostgreSQL)
├── db.properties               REST-API DB-Konfiguration
├── activiti-app.properties     App DB-Konfiguration
├── engine.properties           Engine-Einstellungen
├── init-users.sql              Automatische User-Erstellung beim Start
├── startup.sh                  Container-Startscript (Tomcat + User-Init)
├── activiti6.tar               Exportiertes Activiti-Image (plattformunabhaengig)
├── postgres-15-alpine.tar      Exportiertes PostgreSQL-Image (plattformunabhaengig)
├── README.md                   Diese Datei
│
├── windows/                    Windows-spezifische Scripts
│   ├── README.md
│   ├── build.cmd               Image bauen
│   ├── start.cmd               Container starten
│   ├── stop.cmd                Container stoppen
│   └── export.cmd              Images als tar exportieren
│
└── linux/                      Linux-spezifische Scripts
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

| URL | Beschreibung | Login |
|---|---|---|
| http://HOST:9090/ | Startseite mit Links | - |
| http://HOST:9090/activiti-app | Web-UI (Prozess-Designer, Tasks) | `kermit` / `kermit` |
| http://HOST:9090/activiti-rest/service | REST API | `kermit` / `kermit` |

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

## Frische DB bei jedem Start

Die PostgreSQL-Datenbank laeuft auf `tmpfs` (RAM-Disk). Bei jedem `stop` + `start` wird die DB komplett neu erstellt:
- Activiti-Schema wird automatisch angelegt
- Demo-User und -Prozesse werden deployed
- Eigene User werden aus `init-users.sql` angelegt
- Eigene Deployments gehen beim Stop verloren

---

## Container-Architektur

```
┌─────────────────────────────────────────────────┐
│  activiti (Port 9090 -> 8080)                   │
│  ├── Tomcat 9.0                                 │
│  ├── JDK 11 (Temurin)                           │
│  ├── activiti-rest.war   (REST API)             │
│  ├── activiti-app.war    (Web-UI)               │
│  ├── postgresql-client   (fuer User-Init)       │
│  ├── startup.sh          (Tomcat + User-Init)   │
│  └── init-users.sql      (User-Definitionen)    │
├─────────────────────────────────────────────────┤
│  activiti-db                                    │
│  ├── PostgreSQL 15 (Alpine)                     │
│  ├── DB: activiti                               │
│  ├── User: postgres / postgres                  │
│  └── tmpfs (RAM-Disk, keine Persistenz)         │
└─────────────────────────────────────────────────┘
```

## Port aendern

Falls Port 9090 belegt ist:
- **Windows:** `set ACTIVITI_PORT=8080` vor `start.cmd`
- **Linux:** `ACTIVITI_PORT=8080 linux/start.sh`

---

## Neue User hinzufuegen

1. `init-users.sql` bearbeiten (INSERT-Statements ergaenzen)
2. Image neu bauen: `windows\build.cmd` bzw. `linux/build.sh`
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

---

Erstellt: 18. Maerz 2026
