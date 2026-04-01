package de.creditreform.crefoteam.activiti.gui.view;

/**
 * Callback-Interface: Der ActivitiProcessController meldet Ereignisse
 * an die View zurueck (Logging, Status, Diagramm-Update, Benutzerentscheidung).
 */
public interface ActivitiProcessCallback {

    /**
     * Protokoll-Eintrag ausgeben.
     */
    void onLog(String message);

    /**
     * Statuszeile aktualisieren.
     */
    void onStatus(String message);

    /**
     * Prozess-Diagramm aktualisieren.
     *
     * @param imageProcessId  ID des anzuzeigenden Prozesses (Haupt- oder Sub-Prozess)
     * @param mainProcessId   ID des Hauptprozesses (um Sub-Prozess zu erkennen)
     * @param testPhase       aktuelle Test-Phase (kann null sein)
     */
    void onProcessImageUpdate(Integer imageProcessId, Integer mainProcessId, String testPhase);

    /**
     * Wird aufgerufen, wenn ein laufender Prozess gefunden wurde.
     * Die Implementierung zeigt dem Benutzer einen Dialog und gibt die Wahl zurueck.
     *
     * @param count           Anzahl laufender Instanzen
     * @param meinKey         Business-Key
     * @param currentTaskInfo Infos zum aktuellen Task (kann leer sein)
     * @return 0 = Loeschen &amp; Neu starten, 1 = Fortsetzen, 2 = Abbrechen
     */
    int onExistingProcessFound(int count, String meinKey, String currentTaskInfo);
}
