package de.creditreform.crefoteam.activiti;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static de.creditreform.crefoteam.activiti.ActivitiJunitConstants.AUTOMATED_SUB_RT2_DEPLOYMENT_NAME;
import static de.creditreform.crefoteam.activiti.ActivitiJunitConstants.AUTOMATED_SUB_RT2_SUB_DEPLOYMENT_NAME;

public class CteActivitiRestServiceIntegration4Test extends RestIntegrationTestBase {

    final String PROCESS_DEF_KEY = "JUnitTestAutomationProcessRT2";
    String meinKey = "JUnitX";

    @Before
    public void setUp() {
        super.setUp();
        try {
            cteActivitiServiceREST.uploadDeploymentFile(new File(getClass().getResource("/" + AUTOMATED_SUB_RT2_DEPLOYMENT_NAME).toURI()));
            cteActivitiServiceREST.uploadDeploymentFile(new File(getClass().getResource("/" + AUTOMATED_SUB_RT2_SUB_DEPLOYMENT_NAME).toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testClaimUnclaimTask() throws Exception {
        Integer processInstanceID = cteActivitiServiceREST.startProcess(PROCESS_DEF_KEY, meinKey);

        Map<String, Object> myVariablesMap = new HashMap<>();
        myVariablesMap.put("TIME_BEFORE_NEXT_TASTK", "PT1S");

        CteActivitiTask nextTask;

        nextTask = claimAndCompleteUserTask(processInstanceID, meinKey, "UserTaskGeneratePseudoCrefos", myVariablesMap, true);
        Assert.assertEquals("Folge-UserTask stimmt nicht!", "UserTaskStartUploads", nextTask.getTaskDefinitionKey());

        startSubProcess(processInstanceID, myVariablesMap);

        nextTask = claimAndCompleteUserTask(processInstanceID, meinKey, "UserTaskModifyPseudoCrefos", myVariablesMap, true);
        System.out.println(nextTask.getTaskDefinitionKey());
        Assert.assertEquals("Folge-UserTask stimmt nicht!", "UserTaskStartUploads", nextTask.getTaskDefinitionKey());

        nextTask = claimAndCompleteUserTask(processInstanceID, meinKey, "UserTaskStartUploads", myVariablesMap, true);
        System.out.println(nextTask.getTaskDefinitionKey());

        nextTask = claimAndCompleteUserTask(processInstanceID, meinKey, "UserTaskStartExports", myVariablesMap, true);
        System.out.println(nextTask.getTaskDefinitionKey());

        nextTask = claimAndCompleteUserTask(processInstanceID, meinKey, "UserTaskWaitForExports", myVariablesMap, true);
        System.out.println(nextTask.getTaskDefinitionKey());

        nextTask = claimAndCompleteUserTask(processInstanceID, meinKey, "UserTaskStartCollect", myVariablesMap, true);
        System.out.println(nextTask.getTaskDefinitionKey());

        nextTask = claimAndCompleteUserTask(processInstanceID, meinKey, "UserTaskCheckCollects", myVariablesMap, true);
        System.out.println(nextTask.getTaskDefinitionKey());

        nextTask = claimAndCompleteUserTask(processInstanceID, meinKey, "UserTaskStartRestore", myVariablesMap, true);
        System.out.println(nextTask.getTaskDefinitionKey());

        nextTask = claimAndCompleteUserTask(processInstanceID, meinKey, "UserTaskStartCheck", myVariablesMap, false);
        Assert.assertNull(nextTask);

        cteActivitiServiceREST.deleteProcessInstances(PROCESS_DEF_KEY, meinKey);
    }

    private CteActivitiTask startSubProcess(Integer processInstanceID, Map<String, Object> myVariablesMap) throws Exception {
        CteActivitiTask nextTask;

        nextTask = claimAndCompleteUserTask(processInstanceID, meinKey, "UserTaskStartUploads", myVariablesMap, true);
        Assert.assertEquals("Folge-UserTask stimmt nicht!", "UserTaskStartExports", nextTask.getTaskDefinitionKey());

        nextTask = claimAndCompleteUserTask(processInstanceID, meinKey, "UserTaskStartExports", myVariablesMap, true);
        Assert.assertEquals("Folge-UserTask stimmt nicht!", "UserTaskWaitForExports", nextTask.getTaskDefinitionKey());

        nextTask = claimAndCompleteUserTask(processInstanceID, meinKey, "UserTaskWaitForExports", myVariablesMap, true);
        Assert.assertEquals("Folge-UserTask stimmt nicht!", "UserTaskStartCollect", nextTask.getTaskDefinitionKey());

        nextTask = claimAndCompleteUserTask(processInstanceID, meinKey, "UserTaskStartCollect", myVariablesMap, true);

        return nextTask;
    }

}
