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

/*
* curl -X POST -u CAVDARK-ENE:cavdark -H "Content-Type: application/json" -d "{\"processDefinitionKey\":\"JUNIT-TEST\", \"variables\":[{\"name\":\"MEIN_KEY\",\"value\":\"JUNIT-KEY-1\"}]}" http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/process-instances
  http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/process-instances/269881/variables
* */
public class CteActivitiRestServiceIntegration3Test extends RestIntegrationTestBase {
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
    public void testSignalReceived() throws Exception {
        String meinKey = "testSignalReceived";
        Integer processInstanceID = cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, meinKey);
        int statusCode = cteActivitiServiceREST.signalEventReceived("ENE" + "cancelProcessSignal");
        Assert.assertEquals(204, statusCode);
        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, meinKey);
    }

    @Test
    public void testClaimUnclaimTask() throws Exception {
        String meinKey = "testClaimUnclaimTask";
        Integer processInstanceID = cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, meinKey);

        // Hole Tasks des Prozesses <processInstanceID> ...
        Map<String, Object> myVariablesMap = new HashMap<>();
        myVariablesMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey);
        List<CteActivitiTask> activitiTaskList = cteActivitiServiceREST.queryTasksForTaskVariables(myVariablesMap);
        List<CteActivitiTask> myActivitiTaskList = new ArrayList<CteActivitiTask>();
        for (CteActivitiTask cteActivitiTask : activitiTaskList) {
            String meinKeyX = cteActivitiTask.getVariables().get(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY);
            if (meinKey.equals(meinKeyX)) {
                myActivitiTaskList.add(cteActivitiTask);
            }
        }
        // ... und check, dass die Tasks noch keinem User zugeordnet (claimed) sind
        for (CteActivitiTask cteActivitiTask : myActivitiTaskList) {
            Assert.assertNull(cteActivitiTask.getAssignee());
            cteActivitiServiceREST.checkTaskVariables(cteActivitiTask, meinKey);
        }
        // Claim die Tasks des Prozesses <processInstanceID> für <ActivitiTestsBase.JUNIT_ACTIVITI_USER>
        for (CteActivitiTask cteActivitiTask : myActivitiTaskList) {
            cteActivitiServiceREST.claimTask(cteActivitiTask, restInvokerConfig.getServiceUser());
        }
        // Hole Tasks des Prozesses <processInstanceID> erneut
        activitiTaskList = cteActivitiServiceREST.queryTasksForTaskVariables(myVariablesMap);
        // ... und check...
        String kermitUser = "kermit";
        for (CteActivitiTask cteActivitiTask : activitiTaskList) {
            // ...dass die Tasks dem User <ActivitiTestsBase.JUNIT_ACTIVITI_USER> zugeordnet (claimed) sind
            Assert.assertEquals(restInvokerConfig.getServiceUser(), cteActivitiTask.getAssignee());
            // Un-Claim die Tasks...
            cteActivitiServiceREST.unclaimTask(cteActivitiTask);
            // ... und claim die Tasks des Prozesses <processInstanceID> für <kermitUser>
            cteActivitiServiceREST.claimTask(cteActivitiTask, kermitUser);
        }
        // Hole Tasks des Prozesses <processInstanceID> erneut...
        activitiTaskList = cteActivitiServiceREST.queryTasksForTaskVariables(myVariablesMap);
        // ... und check, dass die Tasks nun dem User <kermitUser> zugeordnet (claimed) sind
        for (CteActivitiTask cteActivitiTask : activitiTaskList) {
            Assert.assertEquals(cteActivitiTask.getAssignee(), kermitUser);
        }
        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, meinKey);
    }

    /*** Geht nicht!!!
     @Test public void testSignalEventReceived() throws Exception
     {
     processInstanceID = startProcess(ActivitiTestsBase.JUNIT_ACTIVITI_USER);
     CteActivitiTask cteActivitiTask = cteActivitiServiceREST.findNextTaskForGroup( UserTaskThread.CTE_TESTER_GROUP );
     Assert.assertNotNull( cteActivitiTask );
     cteActivitiServiceREST.claimTask( cteActivitiTask, restServiceActiviti.getServiceUser() );
     String signalName = "testSignal1";
     cteActivitiServiceREST.signalEventReceived( signalName );
     Map<String, Object> taskParamsMap = new HashMap<>();
     taskParamsMap.put( "signalType", "SignalType1");
     cteActivitiServiceREST.completeTask( cteActivitiTask, taskParamsMap );
     }
     */
}
