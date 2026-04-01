package de.creditreform.crefoteam.activiti;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.creditreform.crefoteam.activiti.ActivitiJunitConstants.*;

public class CteActivitiRestServiceIntegration1Test extends RestIntegrationTestBase {
    @Test
    public void testListCteActivitiDeployments() throws Exception {
        String deploymentNameLike = "JunitAutomated";
        List<CteActivitiDeployment> cteActivitiDeploymentList = cteActivitiServiceREST.listDeploymentsForNameLike(deploymentNameLike);
        Assert.assertNotNull(cteActivitiDeploymentList);
        Assert.assertTrue(cteActivitiDeploymentList.isEmpty());

        cteActivitiServiceREST.uploadDeploymentFile(new File(getClass().getResource("/" + AUTOMATED_MAIN_DEPLOYMENT_NAME).toURI()));
        cteActivitiServiceREST.uploadDeploymentFile(new File(getClass().getResource("/" + AUTOMATED_SUB_RT_DEPLOYMENT_NAME).toURI()));

        cteActivitiDeploymentList = cteActivitiServiceREST.listDeploymentsForNameLike(deploymentNameLike);
        Assert.assertNotNull(cteActivitiDeploymentList);
        Assert.assertFalse(cteActivitiDeploymentList.isEmpty());

        cteActivitiServiceREST.deleteDeploymentForName(AUTOMATED_MAIN_DEPLOYMENT_NAME);
        cteActivitiServiceREST.deleteDeploymentForName(AUTOMATED_SUB_RT_DEPLOYMENT_NAME);
        cteActivitiDeploymentList = cteActivitiServiceREST.listDeploymentsForNameLike(deploymentNameLike);
        Assert.assertNotNull(cteActivitiDeploymentList);
        Assert.assertTrue(cteActivitiDeploymentList.isEmpty());
    }

    @Test
    public void testGetCteActivitiDeploymentForName() throws Exception {
        CteActivitiDeployment cteActivitiDeployment = cteActivitiServiceREST.getDeploymentForName("NonExistent!");
        Assert.assertNull(cteActivitiDeployment);

        cteActivitiServiceREST.uploadDeploymentFile(new File(getClass().getResource("/" + JUNIT_DEPLOYMENT_NAME).toURI()));
        cteActivitiDeployment = cteActivitiServiceREST.getDeploymentForName(JUNIT_DEPLOYMENT_NAME);
        Assert.assertNotNull(cteActivitiDeployment);
        Assert.assertEquals(JUNIT_DEPLOYMENT_NAME, cteActivitiDeployment.getName());

        cteActivitiServiceREST.deleteDeploymentForName(JUNIT_DEPLOYMENT_NAME);
        cteActivitiDeployment = cteActivitiServiceREST.getDeploymentForName(JUNIT_DEPLOYMENT_NAME);
        Assert.assertNull(cteActivitiDeployment);
    }

    @Test
    public void testGetCteActivitiProcessDefinitionForName() throws Exception {
        CteActivitiProcessDefinition cteActivitiProcessDefinition = cteActivitiServiceREST.getProcessDefinitionForName("NonExistent!");
        Assert.assertNull(cteActivitiProcessDefinition);

        cteActivitiServiceREST.uploadDeploymentFile(new File(getClass().getResource("/" + JUNIT_DEPLOYMENT_NAME).toURI()));
        cteActivitiProcessDefinition = cteActivitiServiceREST.getProcessDefinitionForName(JUNIT_PROCESS_NAME);
        Assert.assertNotNull(cteActivitiProcessDefinition);
        Assert.assertEquals(JUNIT_PROCESS_DEFINITION_KEY, cteActivitiProcessDefinition.getKey());
        Assert.assertEquals(JUNIT_PROCESS_NAME, cteActivitiProcessDefinition.getName());

        cteActivitiServiceREST.deleteDeploymentForName(JUNIT_DEPLOYMENT_NAME);
        cteActivitiProcessDefinition = cteActivitiServiceREST.getProcessDefinitionForName(JUNIT_PROCESS_NAME);
        Assert.assertNull(cteActivitiProcessDefinition);
    }

    @Test
    public void testGetCteActivitiProcessDefinitionForKey() throws Exception {
        CteActivitiProcessDefinition cteActivitiProcessDefinition = cteActivitiServiceREST.getProcessDefinitionForKey("NonExistent!");
        Assert.assertNull(cteActivitiProcessDefinition);

        cteActivitiServiceREST.uploadDeploymentFile(new File(getClass().getResource("/" + JUNIT_DEPLOYMENT_NAME).toURI()));
        cteActivitiProcessDefinition = cteActivitiServiceREST.getProcessDefinitionForKey(JUNIT_PROCESS_DEFINITION_KEY);
        Assert.assertNotNull(cteActivitiProcessDefinition);
        Assert.assertEquals(JUNIT_PROCESS_DEFINITION_KEY, cteActivitiProcessDefinition.getKey());
        Assert.assertEquals(JUNIT_PROCESS_NAME, cteActivitiProcessDefinition.getName());

        cteActivitiServiceREST.deleteDeploymentForName(JUNIT_DEPLOYMENT_NAME);
        cteActivitiProcessDefinition = cteActivitiServiceREST.getProcessDefinitionForKey(JUNIT_PROCESS_DEFINITION_KEY);
        Assert.assertNull(cteActivitiProcessDefinition);
    }

    @Test
    public void testListCteActivitiProcessesDefinitions() throws Exception {
        String processNameLike = "Junit";
        List<CteActivitiProcessDefinition> cteActivitiProcessDefinitionList = cteActivitiServiceREST.listProcessDefinitionsLike(processNameLike);
        Assert.assertNotNull(cteActivitiProcessDefinitionList);
        Assert.assertTrue(cteActivitiProcessDefinitionList.isEmpty());

        cteActivitiServiceREST.uploadDeploymentFile(new File(getClass().getResource("/" + JUNIT_DEPLOYMENT_NAME).toURI()));
        cteActivitiProcessDefinitionList = cteActivitiServiceREST.listProcessDefinitionsLike(processNameLike);
        Assert.assertNotNull(cteActivitiProcessDefinitionList);
        Assert.assertFalse(cteActivitiProcessDefinitionList.isEmpty());

        boolean found = false;
        for (CteActivitiProcessDefinition cteActivitiProcessDefinition : cteActivitiProcessDefinitionList) {
            String theKey = cteActivitiProcessDefinition.getKey();
            System.out.println(theKey);
            found = cteActivitiProcessDefinition.getKey().equals(JUNIT_PROCESS_DEFINITION_KEY);
            if (found) {
                break;
            }
        }
        Assert.assertTrue("ProcessesDefinition nicht gefunden!", found);

        cteActivitiServiceREST.deleteDeploymentForName(JUNIT_DEPLOYMENT_NAME);
        cteActivitiProcessDefinitionList = cteActivitiServiceREST.listProcessDefinitionsLike(processNameLike);
        Assert.assertNotNull(cteActivitiProcessDefinitionList);
        Assert.assertTrue(cteActivitiProcessDefinitionList.isEmpty());
    }

    /**********************************************************************/
    @Test
    public void testGetProcessDigram() throws Exception {
        String meinKey = "JUNIT-BUSINESS-KEY";
        cteActivitiServiceREST.uploadDeploymentFile(new File(getClass().getResource("/" + JUNIT_DEPLOYMENT_NAME).toURI()));
        Integer processInstanceID = cteActivitiServiceREST.startProcess(JUNIT_PROCESS_DEFINITION_KEY, meinKey);
        InputStream processImage = cteActivitiServiceREST.getProcessImage(processInstanceID);
        writeProcessImageToFile(processImage, "c:/Temp/process-0.jpg");

        Map<String, Object> myVariablesMap = new HashMap<>();
        myVariablesMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey);
        CteActivitiTask cteActivitiTask = cteActivitiServiceREST.queryNextTaskForTaskVariables(myVariablesMap);
        Assert.assertNotNull(cteActivitiTask);
        cteActivitiServiceREST.logUserTask(cteActivitiTask);

        cteActivitiServiceREST.claimTask(cteActivitiTask, restInvokerConfig.getServiceUser());
        writeProcessImageToFile(cteActivitiServiceREST.getProcessImage(processInstanceID), "c:/Temp/process-1.jpg");

        Map<String, Object> taskParams = new HashMap<>();
        taskParams.put("signalType", "signalType1");
        cteActivitiServiceREST.completeTask(cteActivitiTask, taskParams);

        cteActivitiServiceREST.deleteProcessInstances(JUNIT_PROCESS_DEFINITION_KEY, meinKey);
        processImage = cteActivitiServiceREST.getProcessImage(processInstanceID);
        Assert.assertNull(processImage);
        cteActivitiServiceREST.deleteDeploymentForName(JUNIT_DEPLOYMENT_NAME);
    }

    private void writeProcessImageToFile(InputStream processImageIS, String fileName) throws Exception {
        File targetFile = new File(fileName);
        FileUtils.copyInputStreamToFile(processImageIS, targetFile);
    }

}
