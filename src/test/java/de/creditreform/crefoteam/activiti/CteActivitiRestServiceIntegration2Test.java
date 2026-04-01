package de.creditreform.crefoteam.activiti;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.creditreform.crefoteam.activiti.ActivitiJunitConstants.JUNIT_DEPLOYMENT_NAME;
import static de.creditreform.crefoteam.activiti.ActivitiJunitConstants.JUNIT_PROCESS_DEFINITION_KEY;

public class CteActivitiRestServiceIntegration2Test extends RestIntegrationTestBase {

    @Override
    @Before
    public void setUp() {
        super.setUp();
        try {
            cteActivitiServiceREST.uploadDeploymentFile(new File(getClass().getResource("/" + JUNIT_DEPLOYMENT_NAME).toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testListExecutions() throws Exception {
        String meinKey = "testListExecutions";
        Integer processInstanceID = cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, meinKey);

        List<CteActivitiExecution> cteActivitiExecutionList = cteActivitiServiceREST.listExecutions();
        Assert.assertNotNull(cteActivitiExecutionList);
        Assert.assertFalse(cteActivitiExecutionList.isEmpty());
        for (CteActivitiExecution cteActivitiExecution : cteActivitiExecutionList) {
            Integer processInstanceId = cteActivitiExecution.getProcessInstanceId();
            Assert.assertNotNull(processInstanceId);
            List<CteActivitiExecution> cteActivitiExecutionListForID = cteActivitiServiceREST.getExecutions(processInstanceId);
            Assert.assertNotNull(cteActivitiExecutionListForID);
            Assert.assertTrue(cteActivitiExecutionListForID.size() > 0);
        }
        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, meinKey);
    }

    @Test
    public void testQueryStartDeleteProcessInstances() throws Exception {
        Map<String, Object> paramsMap = new HashMap<>();

        List<CteActivitiProcess> activitiProcessList = cteActivitiServiceREST.queryProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, paramsMap);
        Assert.assertTrue(activitiProcessList.isEmpty());

        String meinKey = "MEIN-KEY-";
        List<Integer> processInstanceIDsList = new ArrayList<Integer>();
        for (int i = 0; i < 3; i++) {
            // Starte Prozess-Instanz mit <"JUNIT-TEST"> und <"MEIN-KEY-"+i>
            paramsMap.clear();
            paramsMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, (meinKey + i));
            CteActivitiProcess processInstance = cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, paramsMap);
            Assert.assertNotNull(processInstance);
            processInstanceIDsList.add(processInstance.getId());
        }

        for (int i = 0; i < processInstanceIDsList.size(); i++) {
            paramsMap.clear();
            // Liste Prozess-Instanzen mit <"JUNIT-TEST"> und <"MEIN-KEY-"+i>
            paramsMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey + i);
            activitiProcessList = cteActivitiServiceREST.queryProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, paramsMap);
            Assert.assertEquals(("Es dürfte nur 1 Prozess mit 'MEIN_KEY = " + (meinKey + i) + " gefunden werden!"), 1, activitiProcessList.size());
        }

        paramsMap.clear();
        // Suche alle Prozess-Instanzen mit <"JUNIT-TEST"> und <"MEIN-KEY-">
        paramsMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey + "%");
        activitiProcessList = cteActivitiServiceREST.queryProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, paramsMap);
        for (CteActivitiProcess activitiProcess : activitiProcessList) {
            CteActivitiProcess processInstance = cteActivitiServiceREST.getProcessInstanceByID(activitiProcess.getId());
            Assert.assertNotNull(processInstance);
            cteActivitiServiceREST.deleteProcessInstance(activitiProcess.getId());
        }
    }

    @Test
    public void testListExecutionsForProcessInstanceId() throws Exception {
        String meinKey = "testListTasks";
        Integer processInstanceID = cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, meinKey);

        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("processInstanceId", processInstanceID.toString());
        List<CteActivitiTask> cteActivitiTaskList = cteActivitiServiceREST.listTasks(paramsMap);
        Assert.assertNotNull(cteActivitiTaskList);
        Assert.assertFalse(cteActivitiTaskList.isEmpty());

        for (CteActivitiTask cteActivitiTask : cteActivitiTaskList) {
            Integer processInstanceId = cteActivitiTask.getProcessInstanceId();
            Assert.assertNotNull(processInstanceId);
            cteActivitiServiceREST.checkTaskVariables(cteActivitiTask, meinKey);
            List<CteActivitiExecution> cteActivitiExecutionListForID = cteActivitiServiceREST.getExecutions(processInstanceId);
            Assert.assertNotNull(cteActivitiExecutionListForID);
            Assert.assertTrue(cteActivitiExecutionListForID.size() > 0);
        }
        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, meinKey);
    }

    @Test
    public void testQueryTasksForTaskVariables() throws Exception {
        String meinKey1 = "testQueryTasksForTaskVariables-1";
        String meinKey2 = "testQueryTasksForTaskVariables-2";

        Integer processInstance1ID = cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, meinKey1);
        Integer processInstance2ID = cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, meinKey2);

        Map<String, Object> myVariablesMap = new HashMap<>();
        List<CteActivitiTask> cteActivitiTaskList = cteActivitiServiceREST.queryTasksForTaskVariables(myVariablesMap);
        Assert.assertNotNull(cteActivitiTaskList);
        for (CteActivitiTask cteActivitiTask : cteActivitiTaskList) {
            Map<String, String> variables = cteActivitiTask.getVariables();
            // Nur prüfen dass MEIN_KEY in den Variablen vorhanden ist (andere Tasks auf dem Server ignorieren)
            String meinKeyX = variables.get(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY);
            Assert.assertNotNull("MEIN_KEY fehlt in den Task-Variablen für Task " + cteActivitiTask.getId(), meinKeyX);
            cteActivitiServiceREST.logUserTask(cteActivitiTask);
        }
        myVariablesMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey1);
        List<CteActivitiTask> cteActivitiTaskListPID1 = cteActivitiServiceREST.queryTasksForTaskVariables(myVariablesMap);
        for (CteActivitiTask cteActivitiTask : cteActivitiTaskListPID1) {
            cteActivitiServiceREST.checkTaskVariables(cteActivitiTask, meinKey1);
        }
        myVariablesMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey2);
        List<CteActivitiTask> cteActivitiTaskListPID2 = cteActivitiServiceREST.queryTasksForTaskVariables(myVariablesMap);
        for (CteActivitiTask cteActivitiTask : cteActivitiTaskListPID2) {
            cteActivitiServiceREST.checkTaskVariables(cteActivitiTask, meinKey2);
        }

        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, meinKey1);
        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, meinKey2);
    }

    @Test
    public void testQueryNextTaskForTaskVariables() throws Exception {
        String meinKey1 = "testQueryNextTaskForTaskVariables-1";
        String meinKey2 = "testQueryNextTaskForTaskVariables-2";
        // eventuelle Liechen entfernen...
        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, meinKey1);
        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, meinKey2);

        Integer processInstance1ID = cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, meinKey1);
        Integer processInstance2ID = cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, meinKey2);

        Map<String, Object> myVariablesMap = new HashMap<>();
        myVariablesMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey1);
        CteActivitiTask cteActivitiTaskPID1 = cteActivitiServiceREST.queryNextTaskForTaskVariables(myVariablesMap);
        cteActivitiServiceREST.checkTaskVariables(cteActivitiTaskPID1, meinKey1);

        myVariablesMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey2);
        CteActivitiTask cteActivitiTaskPID2 = cteActivitiServiceREST.queryNextTaskForTaskVariables(myVariablesMap);
        cteActivitiServiceREST.checkTaskVariables(cteActivitiTaskPID2, meinKey2);

        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, meinKey1);
        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, meinKey2);
    }

    @Test
    public void testQueryTasksServerSideFilteringByMeinKey() throws Exception {
        String meinKey1 = "testServerSideFilter-1";
        String meinKey2 = "testServerSideFilter-2";
        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, meinKey1);
        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, meinKey2);

        cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, meinKey1);
        cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, meinKey2);

        Map<String, Object> filter1 = new HashMap<>();
        filter1.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey1);
        List<CteActivitiTask> tasks1 = cteActivitiServiceREST.queryTasksForTaskVariables(filter1);
        Assert.assertEquals("Genau 1 Task für meinKey1 erwartet", 1, tasks1.size());
        Assert.assertEquals(meinKey1, tasks1.get(0).getVariables().get(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY));

        Map<String, Object> filter2 = new HashMap<>();
        filter2.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey2);
        List<CteActivitiTask> tasks2 = cteActivitiServiceREST.queryTasksForTaskVariables(filter2);
        Assert.assertEquals("Genau 1 Task für meinKey2 erwartet", 1, tasks2.size());
        Assert.assertEquals(meinKey2, tasks2.get(0).getVariables().get(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY));

        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, meinKey1);
        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, meinKey2);
    }

}
