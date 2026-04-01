# Activiti 6.0 - Lokale Installation & Migration

Migration von Activiti 5.19 auf 6.0.0 mit angepasster REST-API-Kapselung.

---

## Uebersicht

| Komponente | Activiti 5 (alt) | Activiti 6 (neu) |
|---|---|---|
| Activiti Version | 5.19.0 | **6.0.0** |
| Projekt | `activiti-process` | **`activiti-process-6`** |
| Artifact Version | 1.0.1-SNAPSHOT | **2.0.0-SNAPSHOT** |
| Tomcat Port | 9080 | **9090** |
| Java | JDK 21 | **JDK 11** (Temurin, lokal im Projekt) |
| Datenbank | `activiti` (Port 5433) | **`activiti6`** (Port 5433) |
| Web-UI | activiti-explorer | **activiti-app** |
| Jackson | 2.6.5 | **2.8.11.6** |
| H2 (Tests) | 1.3.168 | **1.4.196** |

## Zugriff

| URL | Beschreibung | Login |
|---|---|---|
| http://localhost:9090/activiti-rest/service | REST API | `kermit` / `kermit` |
| http://localhost:9090/activiti-app | Web-UI (Prozess-Designer, Task-Manager) | `kermit` / `kermit` |
| http://localhost:9090/manager/html | Tomcat Manager | `admin` / `admin` |

## Scripts

| Script | Beschreibung |
|---|---|
| `start-activiti6.cmd` | Startet lokalen Tomcat auf Port 9090 (JDK 11, PostgreSQL auf Port 5433) |
| `stop-activiti6.cmd` | Stoppt lokalen Tomcat |
| `docker/windows/start.cmd` | Startet Docker-Container (empfohlen) |
| `docker/linux/start.sh` | Startet Docker-Container auf Linux |

---

## Durchgefuehrte Migrationsschritte

### 1. pom.xml

| Aenderung | Alt | Neu |
|---|---|---|
| artifactId | `activiti-process` | `activiti-process-6` |
| version | `1.0.1-SNAPSHOT` | `2.0.0-SNAPSHOT` |
| activiti.version | `5.19.0` | `6.0.0` |
| jackson.version | `2.6.5` | `2.8.11.6` |
| h2.version | `1.3.168` | `1.4.196` |
| activiti-common-rest | vorhanden | **entfernt** (in activiti-rest integriert) |

### 2. CteActivitiServiceRestImpl.java

| Aenderung | Details |
|---|---|
| Import entfernt | `org.activiti.engine.impl.util.json.JSONObject` (interne Klasse, in 6.0 entfernt) |
| `formatJsonString()` | `JSONObject.toString(2)` durch `objectMapper.writerWithDefaultPrettyPrinter()` ersetzt |
| JsonBodyWith*-Klassen | 8 Klassen eliminiert, REST-Bodies direkt mit Jackson `ObjectNode`/`ArrayNode` gebaut |

### 3. BPMNs

**Keine Aenderungen noetig.** Die BPMN-Dateien verwenden Standard-BPMN-2.0 mit `xmlns:activiti="http://activiti.org/bpmn"`, das in Activiti 6.0 identisch bleibt.

### 4. REST-API

**Keine Aenderungen noetig.** Die REST-API Endpoints und Response-Formate sind zwischen 5.19 und 6.0 identisch:
- Gleiche URL-Pfade (`/repository/deployments`, `/runtime/process-instances`, etc.)
- Gleiche `RestUrls`-Konstanten im selben Package
- Gleiche JSON-Response-Struktur (`{"data":[...], "total":..., "start":..., "sort":..., "order":..., "size":...}`)

### 5. Tomcat-Instanz mit JDK 11

- Tomcat 9.0.115 auf Port **9090** (Shutdown-Port 8007)
- **JDK 11.0.25** (Temurin) lokal im Projektverzeichnis (`jdk-11.0.25+9/`)
- PostgreSQL JDBC-Treiber 42.2.29 in beide WARs kopiert
- Datenbank `activiti6` auf Docker-Container `activiti-db` (Port 5433)

### 6. activiti-app Fixes fuer JDK 11

Folgende JARs mussten in `activiti-app/WEB-INF/lib/` aktualisiert/hinzugefuegt werden:

| JAR | Aenderung | Grund |
|---|---|---|
| `hibernate-validator-5.4.3.Final.jar` | Upgrade von 5.0.2 | `ConfigurationImpl` initialisiert nicht unter JDK 11 |
| `javax.el-api-3.0.0.jar` | Hinzugefuegt | Hibernate Validator braucht EL-API |
| `javax.el-3.0.0.jar` | Hinzugefuegt | EL-Implementation |

---

## Was sich in Activiti 6.0 geaendert hat

### Neue Features
- Neue Job-Tabellen: `ACT_RU_TIMER_JOB`, `ACT_RU_SUSPENDED_JOB`, `ACT_RU_DEADLETTER_JOB`
- DMN Engine (Entscheidungstabellen)
- Transiente Variablen
- V5-Kompatibilitaetsmodus fuer alte Prozesse

### Entfernte Komponenten
- `activiti-explorer` -> ersetzt durch `activiti-app`
- `activiti-modeler` -> in `activiti-app` integriert
- PVM-Klassen (`org.activiti.engine.impl.pvm.*`)
- `org.activiti.engine.impl.util.json.JSONObject`

### REST-API Ergaenzungen (neu in v6)
- `management/timer-jobs` - Timer-Jobs
- `management/suspended-jobs` - Suspendierte Jobs
- `management/deadletter-jobs` - Deadletter-Jobs

---

## Warum JDK 11 statt JDK 21?

Activiti 6.0 ist offiziell fuer Java 8-11 zertifiziert. Unter Java 17+ treten folgende Probleme auf:

1. **`javax.xml.bind` (JAXB)** wurde in Java 11 als deprecated markiert und in Java 17 entfernt
2. **`MethodHandles$Lookup(Class, int)`** Konstruktor wurde in Java 17 entfernt (bricht Spring Data JPA 1.7)
3. **`--add-opens` Flags** loesen nur einen Teil der Probleme

Die **activiti-rest** WAR funktioniert unter Java 21 (kein JPA/Hibernate), aber **activiti-app** benoetigt JDK 11.

---

## Parallelbetrieb

Alle Instanzen koennen gleichzeitig laufen:

| Instanz | Port | Java | Datenbank |
|---|---|---|---|
| Activiti 5.19 | 9080 | JDK 21 | `activiti` |
| **Activiti 6.0** | **9090** | **JDK 11** | **`activiti6`** |

---

## Verzeichnisstruktur

```
activiti-process-6/
├── jdk-11.0.25+9/                  JDK 11 (Temurin, fuer lokalen Tomcat)
├── apache-tomcat-9.0.115/          Lokale Tomcat-Installation (Port 9090)
│   └── webapps/
│       ├── activiti-rest/          REST API
│       └── activiti-app/           Web-UI
├── docker/                         Docker-Setup (empfohlen!)
│   ├── Dockerfile                  Image-Definition
│   ├── docker-compose.yml          Container-Orchestrierung
│   ├── db.properties               REST-API DB-Config
│   ├── activiti-app.properties     App DB-Config
│   ├── engine.properties           Engine-Einstellungen
│   ├── init-users.sql              Automatische User-Erstellung
│   ├── startup.sh                  Container-Startscript
│   ├── README.md                   Docker-Dokumentation
│   ├── windows/                    Windows-Scripts (build, start, stop, export)
│   └── linux/                      Linux-Scripts + RHEL 8 Docker RPMs
├── src/
│   ├── main/java/
│   │   └── de/.../activiti/
│   │       ├── CteActivitiService.java          Service-Interface
│   │       ├── CteActivitiServiceRestImpl.java  REST-Implementierung
│   │       ├── ActivitiProcessGUI.java          Standalone-GUI (einfach)
│   │       └── gui/
│   │           ├── design/                      JFormDesigner-generierte Basis-Klassen
│   │           │   ├── ActivitProcessTester.java
│   │           │   └── ActivitProzessMonitor.java
│   │           └── view/                        MDI-GUI Implementierung
│   │               ├── ActivitProcessTesterMainFrame.java  MDI-Hauptfenster
│   │               └── ActivitProzessMonitorView.java      Prozess-Monitor (InternalFrame)
│   ├── main/resources/
│   │   ├── bpmns/                  Produktive BPMN-Prozesse
│   │   └── icons/                  GUI-Icons
│   └── test/                       Unit- und Integrationstests
├── docker/                         Docker-Setup (empfohlen!)
│   ├── Dockerfile                  Image-Definition (inkl. SMTP-Sink)
│   ├── docker-compose.yml          Container-Orchestrierung
│   ├── smtp-sink.py                SMTP-Sink fuer MailTask (Python3)
│   ├── db.properties               REST-API DB-Config
│   ├── activiti-app.properties     App DB-Config
│   ├── engine.properties           Engine-Einstellungen
│   ├── init-users.sql              Automatische User-Erstellung
│   ├── startup.sh                  Container-Startscript
│   ├── README.md                   Docker-Dokumentation
│   ├── windows/                    Windows-Scripts (build, start, stop, export)
│   └── linux/                      Linux-Scripts + RHEL 8 Docker RPMs
├── pom.xml                         Maven-Config (Activiti 6.0.0)
├── start-activiti6.cmd             Lokaler Tomcat Start-Script
├── stop-activiti6.cmd              Lokaler Tomcat Stop-Script
└── ACTIVITI6-SETUP.md              Diese Datei
```

---

## Docker-Betrieb (empfohlen)

Fuer Details siehe `docker/README.md`. Kurzfassung:

**Windows:**
```cmd
cd docker
windows\build.cmd
windows\start.cmd
```

**Linux:**
```bash
cd docker
linux/build.sh        # oder: docker load -i activiti6.tar
linux/start.sh
```

Features:
- Frische DB bei jedem Start (tmpfs)
- User werden automatisch angelegt (init-users.sql)
- SMTP-Sink fuer MailTask (Activiti 6 MailTask laeuft synchron, braucht SMTP-Server)
- Port konfigurierbar (Standard: 9090)

---

## GUI - Prozess-Tester

### Standalone-GUI (einfach)
```
mvn exec:java -Dexec.mainClass="de.creditreform.crefoteam.activiti.ActivitiProcessGUI"
```

### MDI-GUI (Multi-Prozess)
```
mvn exec:java -Dexec.mainClass="de.creditreform.crefoteam.activiti.gui.ActivitProcessTesterMainFrame"
```

Funktionen:
- **MDI-Architektur**: Mehrere Prozess-Monitore gleichzeitig (JDesktopPane + JInternalFrames)
- **Multithreaded**: Jeder Monitor laeuft mit eigenem Worker-Thread
- **Starten / Fortsetzen / Unterbrechen**: Prozesse sind wiederanlauffaehig
- **Prozess-Diagramm**: Zeigt aktuelles BPMN-Diagramm inkl. Sub-Prozess und TEST_PHASE
- **Fenster-Anordnung**: Horizontal, Vertikal, Kaskade

Workflow:
1. "Neuer Monitor" oeffnet ein InternalFrame
2. Activiti-URL und Business-Key eingeben
3. "Starten" prueft ob ein laufender Prozess existiert:
   - **Loeschen & Neu**: Bestehende Prozesse loeschen, BPMNs deployen, neu starten
   - **Fortsetzen**: Am aktuellen UserTask weitermachen
   - **Abbrechen**: Nichts tun
4. "Unterbrechen" stoppt die Task-Verarbeitung, Prozess bleibt auf Activiti erhalten

---

## Tests

Alle Tests gegen die Docker-Instanz (`localhost:9090`, User `kermit`/`kermit`):

| Testklasse | Beschreibung |
|---|---|
| `CteActivitiRestInvokerIntegrationTest` | REST-Invoker: GET, POST, PUT, DELETE |
| `CteActivitiRestInvokerMockTest` | REST-Invoker mit WireMock |
| `CteActivitiRestServiceIntegration1Test` | Deployments, Prozess-Definitionen |
| `CteActivitiRestServiceIntegration2Test` | Prozess-Start, Task-Abfragen |
| `CteActivitiRestServiceIntegration3Test` | Claim/Unclaim, Signals |
| `CteActivitiRestServiceIntegration4Test` | Automatisierter Prozess mit Sub-Prozess (Junit-BPMNs) |
| `CteActivitiRestServiceIntegrationTest` | Parallele Prozesse mit Signals |
| `CteActivitiUtilsTest` | Upload-Utilities |
| `CteAutomatedProcessIntegrationTest` | Produktiver CteAutomatedTestProcess: 44 Tasks (Haupt + 2x Sub) |

Tests ausfuehren:
```bash
# Docker starten
cd docker && windows\start.cmd

# Alle Tests (inkl. Integrationstests)
mvn test -Pitest

# Nur den produktiven Prozess-Test
mvn test -Pitest -Dtest=CteAutomatedProcessIntegrationTest
```

---

Erstellt: 17. Maerz 2026
Aktualisiert: 19. Maerz 2026
