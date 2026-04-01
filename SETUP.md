# activiti-process – Setup & Betrieb

REST-API-Kapselung fuer Activiti 5.19 und Activiti 6.0 in einem Projekt.

---

## Uebersicht

| | Activiti 5.19 | Activiti 6.0 |
|---|---|---|
| Docker-Port | **9091** | **9090** |
| SMTP-Sink-Port | 2526 | 2525 |
| Web-UI | – | activiti-app |
| Process Explorer | http://localhost:9091/process-explorer | http://localhost:9090/process-explorer |
| REST API | http://localhost:9091/activiti-rest/service | http://localhost:9090/activiti-rest/service |
| Login | `kermit` / `kermit` | `kermit` / `kermit` |

---

## Erstinstallation (einmalig)

### Voraussetzungen

- **Docker Desktop** installiert und gestartet
- **Git** installiert
- **JDK 11** fuer den Maven-Build (z.B. Temurin 11)
- Internetzugang (Docker-Build laedt Activiti von GitHub)

### Repository klonen

```cmd
cd E:\Projekte\ClaudeCode
git clone https://github.com/CavdarKemal/activiti-process.git
cd activiti-process
```

### Docker-Images bauen

```cmd
cd docker\windows

build.cmd       ← Activiti 6 Image bauen  (ca. 5 min, einmalig)
build-a5.cmd    ← Activiti 5 Image bauen  (ca. 5 min, einmalig)
```

> Die Images (`activiti6:latest` und `activiti5:latest`) bleiben lokal gespeichert.
> Nur bei Code-Aenderungen in `docker/` muss neu gebaut werden.

---

## Taeglicher Betrieb

### Container starten

Jedes `start.cmd` oeffnet die Container im Vordergrund (Logs sichtbar).
Am besten je in einem eigenen Terminal-Fenster starten:

```cmd
cd activiti-process\docker\windows

start.cmd       ← Activiti 6 auf Port 9090
start-a5.cmd    ← Activiti 5 auf Port 9091
```

> **Hinweis:** Jeder Start beginnt mit **frischer Datenbank** (tmpfs).
> Deployments, Prozesse und Historien sind nach einem Neustart weg.

### Container stoppen

```cmd
stop.cmd        ← Activiti 6 stoppen
```

Activiti 5: einfach das Terminal-Fenster schliessen oder `Ctrl+C`.

---

## URLs im Ueberblick

| URL | Beschreibung |
|---|---|
| http://localhost:9090/process-explorer | **Process Explorer Activiti 6** |
| http://localhost:9091/process-explorer | **Process Explorer Activiti 5** |
| http://localhost:9090/activiti-rest/service | REST API Activiti 6 |
| http://localhost:9091/activiti-rest/service | REST API Activiti 5 |
| http://localhost:9090/activiti-app | Web-UI Activiti 6 (Prozess-Designer, Task-Manager) |

---

## Maven-Build

Tests laufen gegen Activiti 6 (Port 9090) oder Activiti 5 (Port 9091),
konfiguriert in `src/test/resources/activiti-test.local-cfg.xml`.

```cmd
REM Nur kompilieren (ohne Tests)
ci.cmd 11

REM Kompilieren + alle Integrationstests
cit.cmd 11
```

Die Scripts `ci.cmd` / `cit.cmd` liegen global im PATH und rufen Maven mit JDK 11 auf.

### Aktiviti-Version fuer Tests umschalten

In `src/test/resources/activiti-test.local-cfg.xml` den Host anpassen:

```xml
<!-- Activiti 6 -->
<property name="serviceURL" value="http://localhost:9090/activiti-rest/service"/>

<!-- Activiti 5 -->
<property name="serviceURL" value="http://localhost:9091/activiti-rest/service"/>
```

---

## Verzeichnisstruktur

```
activiti-process/
├── src/
│   ├── main/java/de/.../activiti/
│   │   ├── CteActivitiService.java           Service-Interface
│   │   ├── CteActivitiServiceRestImpl.java   REST-Implementierung (5.19 + 6.0 kompatibel)
│   │   ├── config/ActivitiEnvironmentManager.java  ENV-Prefix-Verwaltung (ENE/GEE/ABE/JUNIT)
│   │   └── gui/                              MDI-GUI (Prozess-Tester)
│   ├── main/resources/
│   │   └── bpmns/                            Produktive BPMN-Prozesse
│   │       ├── CteAutomatedTestProcess.bpmn  Haupt-Testprozess
│   │       └── CteAutomatedTestProcessSUB.bpmn  Sub-Prozess
│   └── test/resources/
│       └── bpmns/                            Test-BPMNs (identisch zu main/resources/bpmns!)
├── docker/
│   ├── Dockerfile                            Activiti 6 Image
│   ├── docker-compose.yml                    Activiti 6 Stack (Port 9090)
│   ├── process-explorer/                     Process Explorer (A6-Version, Default: 9090)
│   ├── activiti5/
│   │   ├── Dockerfile                        Activiti 5 Image
│   │   └── process-explorer/                 Process Explorer (A5-Version, Default: 9091)
│   ├── docker-compose-a5.yml                 Activiti 5 Stack (Port 9091)
│   ├── windows/
│   │   ├── build.cmd                         A6 Image bauen
│   │   ├── build-a5.cmd                      A5 Image bauen
│   │   ├── start.cmd                         A6 starten
│   │   ├── start-a5.cmd                      A5 starten (frische DB)
│   │   └── stop.cmd                          A6 stoppen
│   └── linux/
│       ├── build.sh
│       └── start.sh
├── process-explorer/
│   ├── index.html                            Process Explorer Quellcode (A6, Default: 9090)
│   └── index-a5.html                        Process Explorer Quellcode (A5, Default: 9091)
├── ene-activiti.properties                   Verbindungseinstellungen ENE
├── gee-activiti.properties                   Verbindungseinstellungen GEE
├── abe-activiti.properties                   Verbindungseinstellungen ABE
└── pom.xml                                   Maven-Config (Activiti 6.0.0, Java 11)
```

---

## Bekannte Unterschiede Activiti 5.19 vs. 6.0

### activiti:inheritVariables

In Activiti 5.19 wird `activiti:inheritVariables="true"` in CallActivities **ignoriert** (erst ab 5.22 unterstuetzt).
Prozess-Variablen muessen deshalb explizit mit `<activiti:in source="..." target="...">` weitergegeben werden.

In den BPMNs (`CteAutomatedTestProcess.bpmn`) ist `MEIN_KEY` daher explizit in beide CallActivities eingetragen:

```xml
<callActivity ... activiti:inheritVariables="true">
  <extensionElements>
    <activiti:in sourceExpression="${'PHASE_1'}" target="TEST_PHASE"></activiti:in>
    <activiti:in source="MEIN_KEY" target="MEIN_KEY"></activiti:in>
  </extensionElements>
</callActivity>
```

### Task-Query mit Prozess-Variablen

In Activiti 6 werden bei `includeProcessVariables=true` alle Variablen im `variables`-Array zusammengefasst.
In Activiti 5.19 koennen Prozess-Variablen im separaten Feld `processVariables` zurueckkommen.
`CteActivitiServiceRestImpl#createCteActivitiTaskRestImpl` merged daher beide Felder.

### BPMN-Sync-Regel

Die BPMNs in `src/main/resources/bpmns/` und `src/test/resources/bpmns/` muessen
**identisch** sein. Aenderungen immer in **beiden** Verzeichnissen vornehmen.

---

## Tests

Alle Integrationstests gegen Docker-Instanz (Port konfigurierbar, Standard: 9090):

| Testklasse | Beschreibung |
|---|---|
| `CteActivitiRestInvokerMockTest` | REST-Invoker mit WireMock (kein Docker noetig) |
| `CteActivitiRestInvokerIntegrationTest` | REST-Invoker: GET, POST, PUT, DELETE |
| `CteActivitiRestServiceIntegration1Test` | Deployments, Prozess-Definitionen |
| `CteActivitiRestServiceIntegration2Test` | Prozess-Start, Task-Abfragen, Variablen |
| `CteActivitiRestServiceIntegration3Test` | Claim/Unclaim, Signals |
| `CteActivitiRestServiceIntegration4Test` | Automatisierter Prozess mit Sub-Prozess |
| `CteActivitiRestServiceIntegrationTest` | Parallele Prozesse mit Signals |
| `CteActivitiUtilsTest` | Upload-Utilities |
| `CteAutomatedProcessIntegrationTest` | Produktiver CteAutomatedTestProcess (44 Tasks) |

---

## Linux-Installation (RHEL 8 / Produktionsserver)

```bash
cd activiti-process/docker/linux

# Docker installieren (RPM-Pakete liegen im Repo)
sudo bash rhel-rpms/install-docker.sh

# Image bauen
bash build.sh

# Starten
bash start.sh

# Stoppen
bash stop.sh
```

---

Erstellt: April 2026
