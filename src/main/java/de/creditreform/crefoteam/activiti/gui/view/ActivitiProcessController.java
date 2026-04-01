package de.creditreform.crefoteam.activiti.gui.view;

import de.creditreform.crefoteam.activiti.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Kapselt die gesamte Activiti-Prozess-Logik (Deployment, Start, Task-Schleife).
 * Kommuniziert mit der View ausschliesslich ueber {@link ActivitiProcessCallback}.
 */
public class ActivitiProcessController {

    private static final String MAIN_BPMN = "bpmns/CteAutomatedTestProcess.bpmn";
    private static final String SUB_BPMN = "bpmns/CteAutomatedTestProcessSUB.bpmn";

    private final ActivitiProcessCallback callback;

    private Integer processInstanceID;
    private volatile boolean running = false;
    private int taskCount = 0;
    private String envName = "LOCAL";
    private String processDefKey = envName + "-TestAutomationProcess";

    private enum StartAction { NEW, RESUME, CANCEL }

    public ActivitiProcessController(ActivitiProcessCallback callback) {
        this.callback = callback;
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        if (running) {
            running = false;
            callback.onLog(String.format("Task-Verarbeitung unterbrochen. Prozess %d laeuft weiter auf Activiti.", processInstanceID));
            callback.onStatus(String.format("Unterbrochen (Prozess %d aktiv)", processInstanceID));
        }
    }

    /**
     * Startet den Prozess-Ablauf. Muss in einem Worker-Thread aufgerufen werden.
     *
     * @param service   Activiti-Service
     * @param meinKey   Business-Key
     * @param userName  Benutzername fuer Task-Claim
     * @param envName   Umgebungsname (z.B. LOCAL, DEV) — bestimmt den BPMN-Deployment-Prefix
     */
    public void run(CteActivitiServiceRestImpl service, String meinKey, String userName, String envName) throws Exception {
        this.envName = envName;
        this.processDefKey = envName + "-TestAutomationProcess";
        callback.onLog(String.format("Pruefe ob Prozess '%s' mit Key '%s' bereits laeuft...", processDefKey, meinKey));
        StartAction action = checkExistingProcess(service, meinKey);

        switch (action) {
            case CANCEL:
                callback.onLog("Vorgang abgebrochen durch Benutzer.");
                return;

            case RESUME:
                callback.onLog(String.format("Setze laufenden Prozess fort (ID = %d)...", processInstanceID));
                callback.onProcessImageUpdate(processInstanceID, processInstanceID, null);
                break;

            case NEW:
                callback.onLog("Deploye BPMNs...");
                deployBpmns(service);
                callback.onLog(String.format("Starte Prozess '%s'...", processDefKey));
                Map<String, Object> startParams = buildProcessParams(meinKey);
                CteActivitiProcess process = service.startProcess(processDefKey, startParams);
                processInstanceID = process.getId();
                taskCount = 0;
                callback.onLog(String.format("Prozess gestartet: ID = %d", processInstanceID));
                callback.onProcessImageUpdate(processInstanceID, processInstanceID, null);
                break;
        }

        running = true;
        Map<String, Object> taskParams = buildProcessParams(meinKey);
        runTaskLoop(service, userName, meinKey, taskParams);
    }

    // ============================================= Prozess-Logik =============================================

    private StartAction checkExistingProcess(CteActivitiServiceRestImpl service, String meinKey) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey);
        List<CteActivitiProcess> existing = service.queryProcessInstances(processDefKey, params);

        if (existing.isEmpty()) {
            callback.onLog("Kein laufender Prozess gefunden.");
            return StartAction.NEW;
        }

        callback.onLog(String.format("Es laufen %d Prozess-Instanz(en) mit Key '%s'.", existing.size(), meinKey));

        CteActivitiProcess firstProcess = existing.get(0);
        String currentTaskInfo = "";
        try {
            CteActivitiTask currentTask = service.selectTaskForBusinessKey(firstProcess.getId(), meinKey);
            if (currentTask != null) {
                String phase = currentTask.getVariables().get("TEST_PHASE");
                currentTaskInfo = String.format("\n\nAktueller Task: %s (%s)%s",
                        currentTask.getTaskDefinitionKey(), currentTask.getName(),
                        phase != null ? "\nPhase: " + phase : "");
            }
        } catch (Exception ignore) {
        }

        int choice = callback.onExistingProcessFound(existing.size(), meinKey, currentTaskInfo);

        switch (choice) {
            case 0:
                for (CteActivitiProcess proc : existing) {
                    callback.onLog(String.format("Loesche Prozess-Instanz %d...", proc.getId()));
                    service.deleteProcessInstance(proc.getId());
                }
                callback.onLog("Alle bestehenden Prozess-Instanzen geloescht.");
                return StartAction.NEW;
            case 1:
                processInstanceID = firstProcess.getId();
                return StartAction.RESUME;
            default:
                return StartAction.CANCEL;
        }
    }

    private void runTaskLoop(CteActivitiServiceRestImpl service, String userName,
                             String meinKey, Map<String, Object> taskParams) throws Exception {
        while (running) {
            if (isProcessEnded(service, processInstanceID)) {
                callback.onLog(String.format("Prozess %d ist beendet nach %d Tasks.", processInstanceID, taskCount));
                callback.onStatus(String.format("Prozess beendet (%d Tasks)", taskCount));
                break;
            }

            CteActivitiTask task;
            try {
                task = service.selectTaskForBusinessKey(processInstanceID, meinKey);
            } catch (TimeoutException e) {
                if (isProcessEnded(service, processInstanceID)) {
                    callback.onLog(String.format("Prozess %d ist beendet nach %d Tasks.", processInstanceID, taskCount));
                    callback.onStatus(String.format("Prozess beendet (%d Tasks)", taskCount));
                } else {
                    callback.onLog("Timeout bei Task-Abfrage. Prozess laeuft noch.");
                    callback.onStatus(String.format("Timeout - Prozess %d aktiv", processInstanceID));
                }
                break;
            }
            if (task == null || !running) break;

            taskCount++;
            String taskDefKey = task.getTaskDefinitionKey();
            String taskName = task.getName();
            Integer taskProcessId = task.getProcessInstanceId();
            boolean inSubProcess = !taskProcessId.equals(processInstanceID);
            String testPhase = task.getVariables().get("TEST_PHASE");

            if (inSubProcess) {
                callback.onLog(String.format("Task %d: %s (%s) [Sub-Prozess %d, %s]", taskCount, taskDefKey, taskName, taskProcessId, testPhase));
            } else {
                callback.onLog(String.format("Task %d: %s (%s)", taskCount, taskDefKey, taskName));
            }
            callback.onStatus(String.format("Task %d: %s%s", taskCount, taskDefKey, testPhase != null ? " [" + testPhase + "]" : ""));

            service.claimTask(task, userName);
            callback.onProcessImageUpdate(taskProcessId, processInstanceID, testPhase);

            if (!running) {
                callback.onLog(String.format("Unterbrochen nach Claim von '%s'. Task wird freigegeben.", taskDefKey));
                service.unclaimTask(task);
                break;
            }

            service.completeTask(task, taskParams);
            callback.onProcessImageUpdate(taskProcessId, processInstanceID, testPhase);
        }

        if (!running && !isProcessEnded(service, processInstanceID)) {
            callback.onLog(String.format("Task-Verarbeitung unterbrochen bei Task %d. Prozess %d kann fortgesetzt werden.",
                    taskCount, processInstanceID));
        }
    }

    // ============================================= Hilfsmethoden =============================================

    private Map<String, Object> buildProcessParams(String meinKey) {
        Map<String, Object> params = new HashMap<>();
        params.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey);
        params.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_TEST_TYPE, "PHASE1_AND_PHASE2");
        params.put("TIME_BEFORE_BTLG_IMPORT", "0");
        params.put("TIME_BEFORE_CT_IMPORT", "0");
        params.put("TIME_BEFORE_EXPORT", "0");
        return params;
    }

    private void deployBpmns(CteActivitiServiceRestImpl service) throws Exception {
        List<CteActivitiDeployment> oldDeployments = service.listDeploymentsForNameLike(envName);
        for (CteActivitiDeployment d : oldDeployments) {
            callback.onLog(String.format("Loesche altes Deployment '%s'...", d.getName()));
            try {
                service.deleteCteActivitiDeployment(d);
            } catch (Exception e) {
                callback.onLog(String.format("  Warnung: %s", e.getMessage()));
            }
        }

        String mainBpmnPath = getClass().getClassLoader().getResource(MAIN_BPMN).getPath();
        File mainFile = service.prepareBpmnFileForEnvironment(mainBpmnPath, envName);
        service.uploadDeploymentFile(mainFile);
        mainFile.delete();
        callback.onLog(String.format("Deployed: %s", MAIN_BPMN));

        String subBpmnPath = getClass().getClassLoader().getResource(SUB_BPMN).getPath();
        File subFile = service.prepareBpmnFileForEnvironment(subBpmnPath, envName);
        service.uploadDeploymentFile(subFile);
        subFile.delete();
        callback.onLog(String.format("Deployed: %s", SUB_BPMN));
    }

    private boolean isProcessEnded(CteActivitiServiceRestImpl service, Integer processInstanceID) {
        try {
            CteActivitiProcess process = service.getProcessInstanceByID(processInstanceID);
            return process.isEnded();
        } catch (Exception e) {
            return true;
        }
    }
}
