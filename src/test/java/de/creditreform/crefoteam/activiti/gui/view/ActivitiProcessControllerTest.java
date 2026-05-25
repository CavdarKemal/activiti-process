package de.creditreform.crefoteam.activiti.gui.view;

import de.creditreform.crefoteam.activiti.*;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class ActivitiProcessControllerTest {

    private TestCallback callback;
    private CteActivitiServiceRestImpl service;
    private ActivitiProcessController controller;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private static final String MEIN_KEY = "TEST-KEY";
    private static final String USER_NAME = "testuser";
    private static final String ENV_NAME = "LOCAL";
    private static final String PROCESS_DEF_KEY = ENV_NAME + "-TestAutomationProcess";

    @Before
    public void setUp() {
        callback = new TestCallback();
        service = createMock(CteActivitiServiceRestImpl.class);
        controller = new ActivitiProcessController(callback);
    }

    // ============================================= Zustand =============================================

    @Test
    public void testIsRunning_anfangsFalse() {
        assertFalse(controller.isRunning());
    }

    @Test
    public void testStop_wennNichtLaeuft_keinEffekt() {
        controller.stop();
        assertFalse(controller.isRunning());
        assertTrue(callback.logMessages.isEmpty());
    }

    // ============================================= Cancel =============================================

    @Test
    public void testRun_abbruch_wennBenutzerAbbricht() throws Exception {
        CteActivitiProcess existing = createMock(CteActivitiProcess.class);
        expect(service.queryProcessInstances(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(Collections.singletonList(existing));
        expect(existing.getId()).andReturn(42).anyTimes();
        expect(service.selectTaskForBusinessKey(42, MEIN_KEY))
                .andThrow(new RuntimeException("test"));

        callback.existingProcessChoice = 2;

        replay(service, existing);
        controller.run(service, MEIN_KEY, USER_NAME, ENV_NAME);

        assertFalse(controller.isRunning());
        assertTrue(containsLog("abgebrochen"));
        verify(service, existing);
    }

    // ============================================= Resume =============================================

    @Test
    public void testRun_fortsetzen_prozessSofortBeendet() throws Exception {
        CteActivitiProcess existing = createMock(CteActivitiProcess.class);
        expect(service.queryProcessInstances(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(Collections.singletonList(existing));
        expect(existing.getId()).andReturn(100).anyTimes();
        expect(service.selectTaskForBusinessKey(100, MEIN_KEY)).andReturn(null);

        callback.existingProcessChoice = 1;

        CteActivitiProcess ended = createMock(CteActivitiProcess.class);
        expect(service.getProcessInstanceByID(100)).andReturn(ended);
        expect(ended.isEnded()).andReturn(true);

        replay(service, existing, ended);
        controller.run(service, MEIN_KEY, USER_NAME, ENV_NAME);

        assertTrue(containsLog("Setze laufenden Prozess fort"));
        assertTrue(containsLog("beendet nach 0 Tasks"));
        verify(service, existing, ended);
    }

    // ============================================= Neuer Prozess =============================================

    @Test
    public void testRun_neuerProzess_keinBestehender() throws Exception {
        expect(service.queryProcessInstances(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(Collections.emptyList());

        expectDeployBpmns();

        CteActivitiProcess newProcess = createMock(CteActivitiProcess.class);
        expect(service.startProcess(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(newProcess);
        expect(newProcess.getId()).andReturn(200).anyTimes();

        CteActivitiProcess ended = createMock(CteActivitiProcess.class);
        expect(service.getProcessInstanceByID(200)).andReturn(ended);
        expect(ended.isEnded()).andReturn(true);

        replay(service, newProcess, ended);
        controller.run(service, MEIN_KEY, USER_NAME, ENV_NAME);

        assertTrue(containsLog("Kein laufender Prozess gefunden"));
        assertTrue(containsLog("Prozess gestartet: ID = 200"));
        assertTrue(containsLog("beendet nach 0 Tasks"));
        verify(service, newProcess, ended);
    }

    // ============================================= Loeschen & Neu =============================================

    @Test
    public void testRun_loeschenUndNeu() throws Exception {
        CteActivitiProcess existing = createMock(CteActivitiProcess.class);
        expect(service.queryProcessInstances(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(Collections.singletonList(existing));
        expect(existing.getId()).andReturn(50).anyTimes();
        expect(service.selectTaskForBusinessKey(50, MEIN_KEY))
                .andThrow(new RuntimeException("test"));

        callback.existingProcessChoice = 0;

        service.deleteProcessInstance(50);
        expectLastCall();

        expectDeployBpmns();

        CteActivitiProcess newProcess = createMock(CteActivitiProcess.class);
        expect(service.startProcess(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(newProcess);
        expect(newProcess.getId()).andReturn(300).anyTimes();

        CteActivitiProcess ended = createMock(CteActivitiProcess.class);
        expect(service.getProcessInstanceByID(300)).andReturn(ended);
        expect(ended.isEnded()).andReturn(true);

        replay(service, existing, newProcess, ended);
        controller.run(service, MEIN_KEY, USER_NAME, ENV_NAME);

        assertTrue(containsLog("Loesche Prozess-Instanz 50"));
        assertTrue(containsLog("geloescht"));
        assertTrue(containsLog("Prozess gestartet"));
        verify(service, existing, newProcess, ended);
    }

    @Test
    public void testRun_mehrereBestehendeLoeschen() throws Exception {
        CteActivitiProcess proc1 = createMock(CteActivitiProcess.class);
        CteActivitiProcess proc2 = createMock(CteActivitiProcess.class);

        expect(service.queryProcessInstances(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(Arrays.asList(proc1, proc2));
        expect(proc1.getId()).andReturn(10).anyTimes();
        expect(proc2.getId()).andReturn(20).anyTimes();
        expect(service.selectTaskForBusinessKey(10, MEIN_KEY))
                .andThrow(new RuntimeException("test"));

        callback.existingProcessChoice = 0;

        service.deleteProcessInstance(10);
        expectLastCall();
        service.deleteProcessInstance(20);
        expectLastCall();

        expectDeployBpmns();

        CteActivitiProcess newProcess = createMock(CteActivitiProcess.class);
        expect(service.startProcess(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(newProcess);
        expect(newProcess.getId()).andReturn(30).anyTimes();

        CteActivitiProcess ended = createMock(CteActivitiProcess.class);
        expect(service.getProcessInstanceByID(30)).andReturn(ended);
        expect(ended.isEnded()).andReturn(true);

        replay(service, proc1, proc2, newProcess, ended);
        controller.run(service, MEIN_KEY, USER_NAME, ENV_NAME);

        assertTrue(containsLog("Loesche Prozess-Instanz 10"));
        assertTrue(containsLog("Loesche Prozess-Instanz 20"));
        verify(service, proc1, proc2, newProcess, ended);
    }

    // ============================================= Task-Schleife =============================================

    @Test
    public void testRunTaskLoop_einTask() throws Exception {
        expect(service.queryProcessInstances(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(Collections.emptyList());
        expectDeployBpmns();

        CteActivitiProcess newProcess = createMock(CteActivitiProcess.class);
        expect(service.startProcess(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(newProcess);
        expect(newProcess.getId()).andReturn(500).anyTimes();

        CteActivitiProcess runningProc = createMock(CteActivitiProcess.class);
        expect(service.getProcessInstanceByID(500)).andReturn(runningProc);
        expect(runningProc.isEnded()).andReturn(false);

        CteActivitiTask task = createTaskMock(500, "UserTask1", "Erster Task", null);
        expect(service.selectTaskForBusinessKey(500, MEIN_KEY)).andReturn(task);

        service.claimTask(task, USER_NAME);
        expectLastCall();
        service.completeTask(eq(task), anyObject(Map.class));
        expectLastCall();

        CteActivitiProcess ended = createMock(CteActivitiProcess.class);
        expect(service.getProcessInstanceByID(500)).andReturn(ended);
        expect(ended.isEnded()).andReturn(true);

        replay(service, newProcess, runningProc, task, ended);
        controller.run(service, MEIN_KEY, USER_NAME, ENV_NAME);

        assertTrue(containsLog("Task 1: UserTask1 (Erster Task)"));
        assertTrue(containsLog("beendet nach 1 Tasks"));
        verify(service, newProcess, runningProc, task, ended);
    }

    @Test
    public void testRunTaskLoop_subProzessTask() throws Exception {
        expect(service.queryProcessInstances(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(Collections.emptyList());
        expectDeployBpmns();

        CteActivitiProcess newProcess = createMock(CteActivitiProcess.class);
        expect(service.startProcess(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(newProcess);
        expect(newProcess.getId()).andReturn(600).anyTimes();

        CteActivitiProcess runningProc = createMock(CteActivitiProcess.class);
        expect(service.getProcessInstanceByID(600)).andReturn(runningProc);
        expect(runningProc.isEnded()).andReturn(false);

        CteActivitiTask subTask = createTaskMock(601, "SubTask1", "Sub Task", "EXECUTION");
        expect(service.selectTaskForBusinessKey(600, MEIN_KEY)).andReturn(subTask);

        service.claimTask(subTask, USER_NAME);
        expectLastCall();
        service.completeTask(eq(subTask), anyObject(Map.class));
        expectLastCall();

        CteActivitiProcess ended = createMock(CteActivitiProcess.class);
        expect(service.getProcessInstanceByID(600)).andReturn(ended);
        expect(ended.isEnded()).andReturn(true);

        replay(service, newProcess, runningProc, subTask, ended);
        controller.run(service, MEIN_KEY, USER_NAME, ENV_NAME);

        assertTrue(containsLog("Sub-Prozess 601"));
        assertTrue(containsLog("EXECUTION"));
        verify(service, newProcess, runningProc, subTask, ended);
    }

    @Test
    public void testRunTaskLoop_nullTask_brichtAb() throws Exception {
        CteActivitiProcess existing = createMock(CteActivitiProcess.class);
        expect(service.queryProcessInstances(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(Collections.singletonList(existing));
        expect(existing.getId()).andReturn(800).anyTimes();
        expect(service.selectTaskForBusinessKey(800, MEIN_KEY))
                .andThrow(new RuntimeException("test"));

        callback.existingProcessChoice = 1;

        CteActivitiProcess runningProc = createMock(CteActivitiProcess.class);
        expect(service.getProcessInstanceByID(800)).andReturn(runningProc);
        expect(runningProc.isEnded()).andReturn(false);

        expect(service.selectTaskForBusinessKey(800, MEIN_KEY)).andReturn(null);

        replay(service, existing, runningProc);
        controller.run(service, MEIN_KEY, USER_NAME, ENV_NAME);

        assertFalse(containsLog("beendet"));
        verify(service, existing, runningProc);
    }

    // ============================================= Timeout =============================================

    @Test
    public void testRunTaskLoop_timeout_prozessBeendet() throws Exception {
        CteActivitiProcess existing = createMock(CteActivitiProcess.class);
        expect(service.queryProcessInstances(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(Collections.singletonList(existing));
        expect(existing.getId()).andReturn(700).anyTimes();
        expect(service.selectTaskForBusinessKey(700, MEIN_KEY))
                .andThrow(new RuntimeException("test"));

        callback.existingProcessChoice = 1;

        CteActivitiProcess runningProc = createMock(CteActivitiProcess.class);
        expect(service.getProcessInstanceByID(700)).andReturn(runningProc);
        expect(runningProc.isEnded()).andReturn(false);

        expect(service.selectTaskForBusinessKey(700, MEIN_KEY))
                .andThrow(new TimeoutException("timeout"));

        CteActivitiProcess ended = createMock(CteActivitiProcess.class);
        expect(service.getProcessInstanceByID(700)).andReturn(ended);
        expect(ended.isEnded()).andReturn(true);

        replay(service, existing, runningProc, ended);
        controller.run(service, MEIN_KEY, USER_NAME, ENV_NAME);

        assertTrue(containsLog("beendet"));
        verify(service, existing, runningProc, ended);
    }

    @Test
    public void testRunTaskLoop_timeout_prozessLaeuftWeiter() throws Exception {
        CteActivitiProcess existing = createMock(CteActivitiProcess.class);
        expect(service.queryProcessInstances(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(Collections.singletonList(existing));
        expect(existing.getId()).andReturn(750).anyTimes();
        expect(service.selectTaskForBusinessKey(750, MEIN_KEY))
                .andThrow(new RuntimeException("test"));

        callback.existingProcessChoice = 1;

        CteActivitiProcess runningProc = createMock(CteActivitiProcess.class);
        expect(service.getProcessInstanceByID(750)).andReturn(runningProc);
        expect(runningProc.isEnded()).andReturn(false);

        expect(service.selectTaskForBusinessKey(750, MEIN_KEY))
                .andThrow(new TimeoutException("timeout"));

        CteActivitiProcess stillRunning = createMock(CteActivitiProcess.class);
        expect(service.getProcessInstanceByID(750)).andReturn(stillRunning);
        expect(stillRunning.isEnded()).andReturn(false);

        replay(service, existing, runningProc, stillRunning);
        controller.run(service, MEIN_KEY, USER_NAME, ENV_NAME);

        assertTrue(containsLog("Timeout"));
        assertTrue(callback.statusMessages.stream().anyMatch(m -> m.contains("Timeout")));
        verify(service, existing, runningProc, stillRunning);
    }

    // ============================================= Stop waehrend Verarbeitung =============================================

    @Test
    public void testRunTaskLoop_stopWaehrendClaim_taskWirdFreigegeben() throws Exception {
        expect(service.queryProcessInstances(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(Collections.emptyList());
        expectDeployBpmns();

        CteActivitiProcess newProcess = createMock(CteActivitiProcess.class);
        expect(service.startProcess(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(newProcess);
        expect(newProcess.getId()).andReturn(900).anyTimes();

        CteActivitiProcess runningProc = createMock(CteActivitiProcess.class);
        expect(service.getProcessInstanceByID(900)).andReturn(runningProc).anyTimes();
        expect(runningProc.isEnded()).andReturn(false).anyTimes();

        CteActivitiTask task = createTaskMock(900, "StopTask", "Wird gestoppt", null);
        expect(service.selectTaskForBusinessKey(900, MEIN_KEY)).andReturn(task);

        service.claimTask(task, USER_NAME);
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                controller.stop();
                return null;
            }
        });

        service.unclaimTask(task);
        expectLastCall();

        replay(service, newProcess, runningProc, task);
        controller.run(service, MEIN_KEY, USER_NAME, ENV_NAME);

        assertFalse(controller.isRunning());
        assertTrue(containsLog("Unterbrochen nach Claim"));
        assertTrue(containsLog("fortgesetzt werden"));
        verify(service, newProcess, runningProc, task);
    }

    // ============================================= Task-Info =============================================

    @Test
    public void testCheckExistingProcess_mitTaskInfo() throws Exception {
        CteActivitiProcess existing = createMock(CteActivitiProcess.class);
        CteActivitiTask currentTask = createMock(CteActivitiTask.class);

        expect(service.queryProcessInstances(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(Collections.singletonList(existing));
        expect(existing.getId()).andReturn(150).anyTimes();

        expect(service.selectTaskForBusinessKey(150, MEIN_KEY)).andReturn(currentTask);
        expect(currentTask.getTaskDefinitionKey()).andReturn("PrepareData");
        expect(currentTask.getName()).andReturn("Daten vorbereiten");
        Map<String, String> taskVars = new HashMap<>();
        taskVars.put("TEST_PHASE", "SETUP");
        expect(currentTask.getVariables()).andReturn(taskVars);

        callback.existingProcessChoice = 2;

        replay(service, existing, currentTask);
        controller.run(service, MEIN_KEY, USER_NAME, ENV_NAME);

        assertTrue(callback.lastExistingProcessTaskInfo.contains("PrepareData"));
        assertTrue(callback.lastExistingProcessTaskInfo.contains("SETUP"));
        verify(service, existing, currentTask);
    }

    // ============================================= isProcessEnded Exception =============================================

    @Test
    public void testRunTaskLoop_isProcessEndedException_giltAlsBeendet() throws Exception {
        CteActivitiProcess existing = createMock(CteActivitiProcess.class);
        expect(service.queryProcessInstances(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(Collections.singletonList(existing));
        expect(existing.getId()).andReturn(999).anyTimes();
        expect(service.selectTaskForBusinessKey(999, MEIN_KEY))
                .andThrow(new RuntimeException("test"));

        callback.existingProcessChoice = 1;

        expect(service.getProcessInstanceByID(999))
                .andThrow(new RuntimeException("process not found"));

        replay(service, existing);
        controller.run(service, MEIN_KEY, USER_NAME, ENV_NAME);

        assertTrue(containsLog("beendet"));
        verify(service, existing);
    }

    // ============================================= Deployment mit alten Deployments =============================================

    @Test
    public void testDeployBpmns_altesDeploymentLoeschenFehlschlaegt_faehrtFort() throws Exception {
        expect(service.queryProcessInstances(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(Collections.emptyList());

        CteActivitiDeployment oldDeploy = createMock(CteActivitiDeployment.class);
        expect(service.listDeploymentsForNameLike(ENV_NAME))
                .andReturn(Collections.singletonList(oldDeploy));
        expect(oldDeploy.getName()).andReturn("OLD-Deploy");

        service.deleteCteActivitiDeployment(oldDeploy);
        expectLastCall().andThrow(new RuntimeException("Delete fehlgeschlagen"));

        File tempFile1 = tempFolder.newFile("main.bpmn");
        File tempFile2 = tempFolder.newFile("sub.bpmn");
        expect(service.prepareBpmnFileForEnvironment(anyObject(String.class), eq(ENV_NAME)))
                .andReturn(tempFile1);
        expect(service.uploadDeploymentFile(tempFile1)).andReturn("d1");
        expect(service.prepareBpmnFileForEnvironment(anyObject(String.class), eq(ENV_NAME)))
                .andReturn(tempFile2);
        expect(service.uploadDeploymentFile(tempFile2)).andReturn("d2");

        CteActivitiProcess newProcess = createMock(CteActivitiProcess.class);
        expect(service.startProcess(eq(PROCESS_DEF_KEY), anyObject(Map.class)))
                .andReturn(newProcess);
        expect(newProcess.getId()).andReturn(400).anyTimes();

        CteActivitiProcess ended = createMock(CteActivitiProcess.class);
        expect(service.getProcessInstanceByID(400)).andReturn(ended);
        expect(ended.isEnded()).andReturn(true);

        replay(service, oldDeploy, newProcess, ended);
        controller.run(service, MEIN_KEY, USER_NAME, ENV_NAME);

        assertTrue(containsLog("Warnung: Delete fehlgeschlagen"));
        assertTrue(containsLog("Prozess gestartet"));
        verify(service, oldDeploy, newProcess, ended);
    }

    // ============================================= Hilfsmethoden =============================================

    private void expectDeployBpmns() throws Exception {
        expect(service.listDeploymentsForNameLike(ENV_NAME))
                .andReturn(Collections.emptyList());

        File tempFile1 = tempFolder.newFile("main.bpmn");
        File tempFile2 = tempFolder.newFile("sub.bpmn");
        expect(service.prepareBpmnFileForEnvironment(anyObject(String.class), eq(ENV_NAME)))
                .andReturn(tempFile1);
        expect(service.uploadDeploymentFile(tempFile1)).andReturn("deploy-1");
        expect(service.prepareBpmnFileForEnvironment(anyObject(String.class), eq(ENV_NAME)))
                .andReturn(tempFile2);
        expect(service.uploadDeploymentFile(tempFile2)).andReturn("deploy-2");
    }

    private CteActivitiTask createTaskMock(int processInstanceId, String taskDefKey, String name, String testPhase) {
        CteActivitiTask task = createMock(CteActivitiTask.class);
        expect(task.getTaskDefinitionKey()).andReturn(taskDefKey).anyTimes();
        expect(task.getName()).andReturn(name).anyTimes();
        expect(task.getProcessInstanceId()).andReturn(processInstanceId).anyTimes();
        Map<String, String> vars = new HashMap<>();
        if (testPhase != null) {
            vars.put("TEST_PHASE", testPhase);
        }
        expect(task.getVariables()).andReturn(vars).anyTimes();
        return task;
    }

    private boolean containsLog(String substring) {
        return callback.logMessages.stream().anyMatch(m -> m.contains(substring));
    }

    // ============================================= Test-Callback =============================================

    private static class TestCallback implements ActivitiProcessCallback {
        final List<String> logMessages = new ArrayList<>();
        final List<String> statusMessages = new ArrayList<>();
        int existingProcessChoice = 2;
        String lastExistingProcessTaskInfo = "";

        @Override
        public void onLog(String message) {
            logMessages.add(message);
        }

        @Override
        public void onStatus(String message) {
            statusMessages.add(message);
        }

        @Override
        public void onProcessImageUpdate(Integer imageProcessId, Integer mainProcessId, String testPhase) {
        }

        @Override
        public int onExistingProcessFound(int count, String meinKey, String currentTaskInfo) {
            lastExistingProcessTaskInfo = currentTaskInfo;
            return existingProcessChoice;
        }
    }
}
