package de.creditreform.crefoteam.activiti;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Integrations-Test fuer den produktiven CteAutomatedTestProcess.
 * Deployt Haupt- und Sub-Prozess, startet den Hauptprozess und
 * durchlaeuft alle UserTasks in einer Schleife bis der Prozess endet.
 */
public class CteAutomatedProcessIntegrationTest extends RestIntegrationTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(CteAutomatedProcessIntegrationTest.class);

    private static final String ENV_NAME = "JUNIT";
    private static final String PROCESS_DEF_KEY = ENV_NAME + "-TestAutomationProcess";
    private static final String MEIN_KEY = "JUNIT-CteAutomated";

    private static final String MAIN_BPMN = "bpmns/CteAutomatedTestProcess.bpmn";
    private static final String SUB_BPMN = "bpmns/CteAutomatedTestProcessSUB.bpmn";

    // Erwartete Task-Reihenfolge im Sub-Prozess
    private static final String[] SUB_PROCESS_TASKS = {
            "UserTaskStartUploads",
            "UserTaskWaitBeforBeteiligtenImport",
            "UserTaskStartBeteiligtenImport",
            "UserTaskWaitForBeteiligtenImport",
            "UserTaskStartEntgBerechnung",
            "UserTaskWaitForEntgBerechnung",
            "UserTaskStartBtlgAktualisierung",
            "UserTaskWaitForBtlgAktualisierung",
            "UserTaskWaitBeforeCtImport",
            "UserTaskStartCtImport",
            "UserTaskWaitForCtImport",
            "UserTaskWaitBeforeExport",
            "UserTaskStartExports",
            "UserTaskStartCollect",
            "UserTaskCheckCollects",
            "UserTaskStartRestore",
            "UserTaskCheckRefExports",
            "UserTaskCheckExportProtokoll",
            "UserTaskStartSftpUploads",
            "UserTaskCheckSftpUploads"
    };

    @Override
    @Before
    public void setUp() {
        super.setUp();
        // Zusaetzlich JUNIT-* Deployments aufraeumen (super.setUp raeumt nur "Junit*" auf, case-sensitiv!)
        cleanupDeployments("JUNIT");
        try {
            // BPMNs mit %ENV% -> JUNIT ersetzen und deployen
            File mainBpmn = cteActivitiServiceREST.prepareBpmnFileForEnvironment(
                    getClass().getClassLoader().getResource(MAIN_BPMN).getPath(), ENV_NAME);
            cteActivitiServiceREST.uploadDeploymentFile(mainBpmn);
            mainBpmn.delete();

            File subBpmn = cteActivitiServiceREST.prepareBpmnFileForEnvironment(
                    getClass().getClassLoader().getResource(SUB_BPMN).getPath(), ENV_NAME);
            cteActivitiServiceREST.uploadDeploymentFile(subBpmn);
            subBpmn.delete();
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Deployen der BPMNs", e);
        }
    }

    /**
     * Startet den Hauptprozess und durchlaeuft alle UserTasks in einer Schleife,
     * analog zum UserTaskThread: naechsten Task holen, claimen, (Handler ausfuehren), completen.
     * Wiederholt bis kein weiterer Task mehr kommt (Prozess beendet).
     */
    @Test
    public void testCompleteProcessLoop() throws Exception {
        // Prozess starten
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, MEIN_KEY);
        paramsMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_TEST_TYPE, "PHASE1_AND_PHASE2");
        paramsMap.put("TIME_BEFORE_BTLG_IMPORT", "0");
        paramsMap.put("TIME_BEFORE_CT_IMPORT", "0");
        paramsMap.put("TIME_BEFORE_EXPORT", "0");

        CteActivitiProcess process = cteActivitiServiceREST.startProcess(PROCESS_DEF_KEY, paramsMap);
        Assert.assertNotNull("Prozess konnte nicht gestartet werden!", process);
        Integer processInstanceID = process.getId();
        LOG.info("Prozess gestartet: ID={}, Key={}", processInstanceID, PROCESS_DEF_KEY);

        // Task-Schleife (analog UserTaskThread.run())
        List<String> completedTasks = new ArrayList<>();
        int taskCount = 0;
        final int MAX_TASKS = 50; // Sicherheitsgrenze

        while (taskCount < MAX_TASKS) {
            CteActivitiTask currentTask = selectNextTask(processInstanceID);
            if (currentTask == null) {
                LOG.info("Kein weiterer Task gefunden - Prozess beendet nach {} Tasks", taskCount);
                break;
            }

            String taskDefKey = currentTask.getTaskDefinitionKey();
            String taskName = currentTask.getName();
            LOG.info("Task {}: {} ({})", ++taskCount, taskDefKey, taskName);

            // Handler-Klassennamen aus dem Task-Namen extrahieren (Text in eckigen Klammern)
            String handlerName = extractHandlerName(taskName);
            if (handlerName != null) {
                LOG.info("  Handler-Klasse: {}", handlerName);
            }

            // Task claimen und completen
            cteActivitiServiceREST.claimTask(currentTask, restInvokerConfig.getServiceUser());
            cteActivitiServiceREST.completeTask(currentTask, paramsMap);
            completedTasks.add(taskDefKey);
        }

        Assert.assertTrue("Prozess sollte mindestens 1 Task haben!", taskCount > 0);
        Assert.assertTrue("Prozess sollte beendet sein (< MAX_TASKS)!", taskCount < MAX_TASKS);

        // Erwartete Haupt-Tasks pruefen
        Assert.assertEquals("Erster Task muss UserTaskPrepareTestSystem sein",
                "UserTaskPrepareTestSystem", completedTasks.get(0));
        Assert.assertEquals("Zweiter Task muss UserTaskGeneratePseudoCrefos sein",
                "UserTaskGeneratePseudoCrefos", completedTasks.get(1));
        Assert.assertEquals("Vorletzter Task muss UserTaskSuccessMail sein",
                "UserTaskSuccessMail", completedTasks.get(completedTasks.size() - 2));
        Assert.assertEquals("Letzter Task muss UserTaskRestoreTestSystem sein",
                "UserTaskRestoreTestSystem", completedTasks.get(completedTasks.size() - 1));

        // Sub-Prozess-Tasks pruefen (Phase-1 und Phase-2)
        verifySubProcessTasks(completedTasks, 2, "Phase-1");
        verifySubProcessTasks(completedTasks, 2 + SUB_PROCESS_TASKS.length, "Phase-2");

        // Erwartete Gesamtzahl: 2 (Haupt-Anfang) + 2x20 (Sub) + 2 (Haupt-Ende) = 44
        int expectedTotal = 2 + (2 * SUB_PROCESS_TASKS.length) + 2;
        Assert.assertEquals("Gesamtzahl der Tasks stimmt nicht!", expectedTotal, completedTasks.size());

        LOG.info("Alle {} Tasks erfolgreich durchlaufen: {}", completedTasks.size(), completedTasks);

        // Aufraeumen
        cteActivitiServiceREST.deleteProcessInstances(PROCESS_DEF_KEY, MEIN_KEY);
    }

    /**
     * Holt den naechsten Task fuer den BusinessKey.
     * Prueft zuerst ob der Prozess noch laeuft, bevor auf den Task gewartet wird.
     * Gibt null zurueck wenn der Prozess beendet ist.
     */
    private CteActivitiTask selectNextTask(Integer processInstanceID) throws Exception {
        // Prozess-Status pruefen
        if (isProcessEnded(processInstanceID)) {
            LOG.info("Prozess {} ist beendet", processInstanceID);
            return null;
        }

        // Task abholen via MEIN_KEY-Variable (funktioniert auch in Sub-Prozessen,
        // die eine eigene processInstanceId haben)
        for (int i = 0; i < 10; i++) {
            CteActivitiTask task = cteActivitiServiceREST.selectTaskForBusinessKey(processInstanceID, MEIN_KEY);
            if (task != null) {
                return task;
            }
            Thread.sleep(500);

            if (isProcessEnded(processInstanceID)) {
                LOG.info("Prozess {} ist beendet (nach Polling)", processInstanceID);
                return null;
            }
        }
        return null;
    }

    /**
     * Raeumt Deployments und deren laufende Prozess-Instanzen auf.
     */
    private void cleanupDeployments(String nameLike) {
        try {
            List<CteActivitiDeployment> deployments = cteActivitiServiceREST.listDeploymentsForNameLike(nameLike);
            for (CteActivitiDeployment deployment : deployments) {
                LOG.info("Raeume Deployment '{}' auf", deployment.getName());
                // Zuerst laufende Prozess-Instanzen loeschen
                try {
                    List<CteActivitiProcess> processes = cteActivitiServiceREST.queryProcessInstances(
                            PROCESS_DEF_KEY, new HashMap<>());
                    for (CteActivitiProcess proc : processes) {
                        cteActivitiServiceREST.deleteProcessInstance(proc.getId());
                    }
                } catch (Exception ignore) {
                }
                cteActivitiServiceREST.deleteCteActivitiDeployment(deployment);
            }
        } catch (Exception e) {
            LOG.warn("Cleanup fehlgeschlagen: {}", e.getMessage());
        }
    }

    /**
     * Prueft ob die Prozess-Instanz beendet ist.
     * Gibt true zurueck wenn der Prozess ended==true hat oder nicht mehr existiert (404).
     */
    private boolean isProcessEnded(Integer processInstanceID) {
        try {
            CteActivitiProcess process = cteActivitiServiceREST.getProcessInstanceByID(processInstanceID);
            return process.isEnded();
        } catch (Exception e) {
            // 404 oder andere Fehler = Prozess existiert nicht mehr
            return true;
        }
    }

    /**
     * Extrahiert den Handler-Klassennamen aus dem Task-Namen.
     * Beispiel: "Pseudo Crefos erzeugen [GeneratePseudoCrefos]" -> "GeneratePseudoCrefos"
     */
    private String extractHandlerName(String taskName) {
        if (taskName == null) return null;
        int start = taskName.lastIndexOf('[');
        int end = taskName.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return taskName.substring(start + 1, end);
        }
        return null;
    }

    /**
     * Prueft, ob die Sub-Prozess-Tasks an der erwarteten Position in der richtigen Reihenfolge stehen.
     */
    private void verifySubProcessTasks(List<String> completedTasks, int startIndex, String phaseName) {
        for (int i = 0; i < SUB_PROCESS_TASKS.length; i++) {
            int idx = startIndex + i;
            Assert.assertTrue(phaseName + ": Index " + idx + " ausserhalb der Task-Liste (size=" + completedTasks.size() + ")",
                    idx < completedTasks.size());
            Assert.assertEquals(phaseName + ": Task " + (i + 1) + " stimmt nicht!",
                    SUB_PROCESS_TASKS[i], completedTasks.get(idx));
        }
    }
}
