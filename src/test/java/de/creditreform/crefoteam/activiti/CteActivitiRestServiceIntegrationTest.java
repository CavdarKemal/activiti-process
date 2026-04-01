package de.creditreform.crefoteam.activiti;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static de.creditreform.crefoteam.activiti.ActivitiJunitConstants.JUNIT_DEPLOYMENT_NAME;
import static de.creditreform.crefoteam.activiti.ActivitiJunitConstants.JUNIT_PROCESS_DEFINITION_KEY;

public class CteActivitiRestServiceIntegrationTest extends RestIntegrationTestBase {

    String JUNIT_KEY_1 = "JUNIT-KEY-1";
    String JUNIT_KEY_2 = "JUNIT-KEY-2";
    String JUNIT_KEY_3 = "JUNIT-KEY-3";

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
    public void testProcessTwoExecutionParallelSameSignal() throws Exception {

        Integer processInstance1ID = cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, JUNIT_KEY_1);
        Integer processInstance2ID = cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, JUNIT_KEY_2);

        CteActivitiTask nextUserTask;
        Map<String, Object> paramsMap = new HashMap<>();

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-1", Path for signal 0
        paramsMap.put("signalType", "signalType0");
        nextUserTask = claimAndCompleteUserTask(processInstance1ID, JUNIT_KEY_1, "UserTask0", paramsMap, true);
        cteActivitiServiceREST.checkTaskVariables(nextUserTask, JUNIT_KEY_1);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-2", Path for signal 0
        paramsMap.put("signalType", "signalType0");
        nextUserTask = claimAndCompleteUserTask(processInstance2ID, JUNIT_KEY_2, "UserTask0", paramsMap, true);
        cteActivitiServiceREST.checkTaskVariables(nextUserTask, JUNIT_KEY_2);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-1"
        nextUserTask = claimAndCompleteUserTask(processInstance1ID, JUNIT_KEY_1, "UserTask0.0", paramsMap, true);
        cteActivitiServiceREST.checkTaskVariables(nextUserTask, JUNIT_KEY_1);
        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-1"
        nextUserTask = claimAndCompleteUserTask(processInstance1ID, JUNIT_KEY_1, "UserTask0.1", paramsMap, true);
        cteActivitiServiceREST.checkTaskVariables(nextUserTask, JUNIT_KEY_1);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-2"
        nextUserTask = claimAndCompleteUserTask(processInstance2ID, JUNIT_KEY_2, "UserTask0.0", paramsMap, true);
        cteActivitiServiceREST.checkTaskVariables(nextUserTask, JUNIT_KEY_2);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-1"
        nextUserTask = claimAndCompleteUserTask(processInstance1ID, JUNIT_KEY_1, "UserTask0.2", paramsMap, false);
        Assert.assertNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-2"
        nextUserTask = claimAndCompleteUserTask(processInstance2ID, JUNIT_KEY_2, "UserTask0.1", paramsMap, true);
        cteActivitiServiceREST.checkTaskVariables(nextUserTask, JUNIT_KEY_2);
        // claim und complete next UserTask for prozess with businessKey = "JUNIT-BUSINESS-KEY-2"
        nextUserTask = claimAndCompleteUserTask(processInstance2ID, JUNIT_KEY_2, "UserTask0.2", paramsMap, false);
        Assert.assertNull(nextUserTask);

        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, JUNIT_KEY_1);
        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, JUNIT_KEY_2);
    }

    @Test
    public void testProcessTwoExecutionParallelDifferentSignal() throws Exception {
        Integer processInstance1ID = cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, JUNIT_KEY_1);
        Integer processInstance2ID = cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, JUNIT_KEY_2);
        Integer processInstance3ID = cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, JUNIT_KEY_3);

        CteActivitiTask nextUserTask;
        Map<String, Object> paramsMap = new HashMap<>();

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-KEY-1", Path for signal 0
        paramsMap.put("signalType", "signalType0");
        nextUserTask = claimAndCompleteUserTask(processInstance1ID, JUNIT_KEY_1, "UserTask0", paramsMap, true);
        cteActivitiServiceREST.checkTaskVariables(nextUserTask, JUNIT_KEY_1);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-KEY-2", Path for signal 0
        paramsMap.put("signalType", "signalType1");
        nextUserTask = claimAndCompleteUserTask(processInstance2ID, JUNIT_KEY_2, "UserTask0", paramsMap, true);
        cteActivitiServiceREST.checkTaskVariables(nextUserTask, JUNIT_KEY_2);

        paramsMap.put("signalType", "signalType2");
        nextUserTask = claimAndCompleteUserTask(processInstance3ID, JUNIT_KEY_3, "UserTask0", paramsMap, true);
        cteActivitiServiceREST.checkTaskVariables(nextUserTask, JUNIT_KEY_3);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-KEY-1"
        nextUserTask = claimAndCompleteUserTask(processInstance1ID, JUNIT_KEY_1, "UserTask0.0", paramsMap, true);
        cteActivitiServiceREST.checkTaskVariables(nextUserTask, JUNIT_KEY_1);
        // claim und complete next UserTask for prozess with businessKey = "JUNIT-KEY-1"
        nextUserTask = claimAndCompleteUserTask(processInstance1ID, JUNIT_KEY_1, "UserTask0.1", paramsMap, true);
        cteActivitiServiceREST.checkTaskVariables(nextUserTask, JUNIT_KEY_1);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-KEY-2"
        nextUserTask = claimAndCompleteUserTask(processInstance2ID, JUNIT_KEY_2, "UserTask1.0", paramsMap, true);
        cteActivitiServiceREST.checkTaskVariables(nextUserTask, JUNIT_KEY_2);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-KEY-3"
        nextUserTask = claimAndCompleteUserTask(processInstance3ID, JUNIT_KEY_3, "UserTask2.0", paramsMap, false);
        Assert.assertNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-KEY-2"
        nextUserTask = claimAndCompleteUserTask(processInstance2ID, JUNIT_KEY_2, "UserTask1.1", paramsMap, false);
        Assert.assertNull(nextUserTask);

        // claim und complete next UserTask for prozess with businessKey = "JUNIT-KEY-1"
        nextUserTask = claimAndCompleteUserTask(processInstance1ID, JUNIT_KEY_1, "UserTask0.2", paramsMap, false);
        Assert.assertNull(nextUserTask);

        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, JUNIT_KEY_1);
        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, JUNIT_KEY_2);
        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, JUNIT_KEY_3);
    }

}
