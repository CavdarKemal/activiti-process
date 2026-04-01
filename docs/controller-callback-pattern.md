# Zusammenspiel: View, Callback und Controller

## Überblick

Die drei Klassen bilden zusammen das **MVC-ähnliche Muster** für die Prozesssteuerung:

| Klasse | Rolle | Läuft auf |
|--------|-------|-----------|
| `ActivitProzessMonitorView` | **View** — zeigt Ergebnisse an, nimmt Nutzereingaben entgegen | EDT (Swing Event Dispatch Thread) |
| `ActivitiProcessCallback` | **Vertrag (Interface)** — definiert, wie der Controller mit der View kommuniziert | — |
| `ActivitiProcessController` | **Controller** — enthält die gesamte Activiti-Prozesslogik | Worker-Thread |

Das zentrale Problem, das dieses Muster löst: Der Controller läuft in einem **Worker-Thread** (damit die GUI nicht einfriert), muss aber Ergebnisse an die GUI zurückmelden — die nur vom **EDT** angefasst werden darf.

---

## Das Interface `ActivitiProcessCallback`

```java
public interface ActivitiProcessCallback {
    void onLog(String message);
    void onStatus(String message);
    void onProcessImageUpdate(Integer imageProcessId, Integer mainProcessId, String testPhase);
    int  onExistingProcessFound(int count, String meinKey, String currentTaskInfo);
}
```

Das Interface ist der **einzige Kommunikationskanal** vom Controller zur View.
Der Controller kennt die View **nicht** — er kennt nur dieses Interface.
Das ermöglicht:
- Austausch der View ohne Änderung am Controller
- Unit-Tests mit einem Mock-Callback

### Bedeutung der vier Methoden

| Methode | Wann aufgerufen | Was die View damit macht |
|---------|----------------|--------------------------|
| `onLog(message)` | Bei jedem Schritt im Prozessablauf | Zeile mit Timestamp ins `logArea` TextArea anhängen |
| `onStatus(message)` | Bei Statuswechseln | `statusLabel` in der Statusleiste aktualisieren |
| `onProcessImageUpdate(...)` | Nach Claim und nach Complete eines Tasks | Prozessdiagramm-Bild per REST laden und anzeigen |
| `onExistingProcessFound(...)` | Wenn bereits laufende Prozessinstanzen gefunden werden | Dialog anzeigen, Nutzerwahl als `int` zurückgeben |

---

## Wer kennt wen?

```
ActivitProzessMonitorView
    │
    │  implements
    ▼
ActivitiProcessCallback  ◄────────────  ActivitiProcessController
                                              (kennt nur das Interface,
                                               nicht die konkrete View)
```

- Die **View** implementiert das Callback-Interface und übergibt `this` an den Controller:
  ```java
  // in ActivitProzessMonitorView:
  private final ActivitiProcessController controller = new ActivitiProcessController(this);
  //                                                                                  ^^^^
  //                                                   "this" = die View selbst als Callback
  ```

- Der **Controller** speichert nur den Interface-Typ, niemals die konkrete View:
  ```java
  // in ActivitiProcessController:
  private final ActivitiProcessCallback callback;  // Interface, nicht ActivitProzessMonitorView!

  public ActivitiProcessController(ActivitiProcessCallback callback) {
      this.callback = callback;
  }
  ```

---

## Thread-Sicherheit: Wer darf was?

Swing ist **nicht thread-sicher**: GUI-Komponenten dürfen nur vom EDT angefasst werden.
Der Controller läuft aber im Worker-Thread.

### Lösung in der View

Jede Callback-Implementierung leitet GUI-Änderungen an den EDT weiter:

```java
@Override
public void onLog(String message) {
    String line = "[" + timestamp + "] " + message;
    SwingUtilities.invokeLater(() -> {        // ← EDT-Übergabe
        logArea.append(line + "\n");
        logArea.setCaretPosition(...);
    });
}

@Override
public void onStatus(String message) {
    SwingUtilities.invokeLater(() ->          // ← EDT-Übergabe
        statusLabel.setText(message)
    );
}
```

### Sonderfall: `onExistingProcessFound`

Diese Methode muss einen Rückgabewert liefern (die Nutzerwahl).
Daher **blockiert** sie den Worker-Thread mit `invokeAndWait` bis der Nutzer den Dialog bestätigt:

```java
@Override
public int onExistingProcessFound(int count, String meinKey, String currentTaskInfo) {
    int[] result = {2};  // Standardwert = Abbrechen
    SwingUtilities.invokeAndWait(() ->       // ← blockiert Worker-Thread!
        result[0] = JOptionPane.showOptionDialog(...)
    );
    return result[0];    // Nutzerwahl wird an Controller zurückgegeben
}
```

`invokeLater` vs. `invokeAndWait`:

| | `invokeLater` | `invokeAndWait` |
|--|--|--|
| Wartet auf EDT? | Nein (fire & forget) | Ja (blockiert) |
| Rückgabewert möglich? | Nein | Ja (über Array-Trick) |
| Verwendet bei | `onLog`, `onStatus`, `onProcessImageUpdate` | `onExistingProcessFound` |

---

## Ablauf: Start eines Prozesses

```
EDT (Swing)                          Worker-Thread
───────────────────────────────      ──────────────────────────────────────────

User klickt "Start"
  │
  ▼
onStart() [View, EDT]
  ├─ meinKey = currentEnvironment.getMeinKey()
  ├─ setInputEnabled(false)
  ├─ progressBar.setIndeterminate(true)
  └─ new Thread(() → {
                          service = createService()
                          controller.run(service, meinKey, user, envName)
                            │
                            ├─ callback.onLog("Prüfe ob Prozess läuft...")
                            │     └──────────────────────────────────────►  invokeLater → logArea.append(...)
                            │
                            ├─ service.queryProcessInstances(...)
                            │
                            ├─ [falls vorhanden] callback.onExistingProcessFound(...)
                            │     └──────────────────────────────────────►  invokeAndWait → Dialog anzeigen
                            │     ◄──────────────────────────────────────  Nutzerwahl (0/1/2)
                            │
                            ├─ [NEW] deployBpmns(...)
                            │     ├─ callback.onLog("Deploye BPMNs...")
                            │     └─ ...
                            │
                            ├─ service.startProcess(...)
                            ├─ callback.onLog("Prozess gestartet: ID = ...")
                            ├─ callback.onProcessImageUpdate(...)
                            │     └──────────────────────────────────────►  invokeLater → Bild laden + anzeigen
                            │
                            └─ runTaskLoop()
                                 └─ [loop]
                                      ├─ service.selectTaskForBusinessKey(...)
                                      ├─ callback.onLog("Task N: ...")
                                      ├─ callback.onStatus("Task N: ...")
                                      ├─ service.claimTask(...)
                                      ├─ callback.onProcessImageUpdate(...)
                                      ├─ service.completeTask(...)
                                      └─ callback.onProcessImageUpdate(...)

                      }) [finally-Block]
                          ├─ invokeLater → setInputEnabled(true)
                          ├─ invokeLater → stopButton.setEnabled(false)
                          └─ invokeLater → progressBar.setIndeterminate(false)
```

---

## Stopp-Mechanismus

Der Stopp funktioniert über ein `volatile boolean`-Flag im Controller:

```java
// in ActivitiProcessController:
private volatile boolean running = false;
```

`volatile` sorgt dafür, dass das Flag **sofort** aus allen Threads sichtbar ist
(kein CPU-Cache-Problem zwischen EDT und Worker-Thread).

```
EDT                                  Worker-Thread
───────────────────                  ──────────────────────────
User klickt "Stop"
  │
  ▼
controller.stop()
  └─ running = false                 while (running) {   ← merkt beim nächsten
     onLog("Unterbrochen...")            ...               Schleifendurchlauf
     onStatus("Unterbrochen...")     }
```

Der Worker-Thread prüft `running` an mehreren Stellen in der Task-Schleife —
u.a. nach dem Claim, damit ein bereits geclaimter Task korrekt **freigegeben**
(`unclaimTask`) wird, bevor der Thread endet.

---

## Zusammenfassung: Warum dieses Muster?

| Problem | Lösung |
|---------|--------|
| GUI friert ein, wenn Activiti-REST-Calls laufen | Controller im Worker-Thread |
| Worker-Thread darf Swing nicht direkt anfassen | Alle GUI-Updates über `SwingUtilities.invokeLater` |
| Controller soll nicht von der konkreten View abhängen | Kommunikation nur über `ActivitiProcessCallback`-Interface |
| Dialog braucht Nutzereingabe als Rückgabewert | `invokeAndWait` blockiert Worker-Thread bis Antwort da |
| Stopp muss sofort wirken, auch threadübergreifend | `volatile boolean running` im Controller |
