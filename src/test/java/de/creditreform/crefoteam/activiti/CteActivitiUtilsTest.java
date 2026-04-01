package de.creditreform.crefoteam.activiti;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static de.creditreform.crefoteam.activiti.ActivitiJunitConstants.*;

public class CteActivitiUtilsTest extends TestCase {
    protected final Logger LOGGER = LoggerFactory.getLogger(CteActivitiUtilsTest.class);
    protected CteActivitiUtils cteActivitiUtils;
    CteActivitiService cteActivitiServiceREST;

    @Before
    public void setUp() {
        RestInvokerConfig restInvokerConfig = new RestInvokerConfig(JUNIT_ACTIVITI_URL, JUNIT_ACTIVITI_USER, JUNIT_ACTIVITI_PWD);
        cteActivitiServiceREST = new CteActivitiServiceRestImpl(restInvokerConfig);
        cteActivitiUtils = new CteActivitiUtils(cteActivitiServiceREST);
    }

    @Test
    public void testUploadActivitiProcesses() throws Exception {
        File bpmnFile = new File(getClass().getResource("/" + JUNIT_DEPLOYMENT_NAME).toURI());
        String bpmnFileName = bpmnFile.getAbsolutePath();
        String envName = "ENE";
        boolean askIfExists = true;
        String uploadedActivitiProcessesName = cteActivitiUtils.uploadActivitiProcesses(bpmnFileName, envName, askIfExists);

        CteActivitiDeployment deploymentForName = cteActivitiServiceREST.getDeploymentForName(uploadedActivitiProcessesName);
        Assert.assertNotNull(deploymentForName);

        cteActivitiServiceREST.deleteDeploymentForName(uploadedActivitiProcessesName);
        deploymentForName = cteActivitiServiceREST.getDeploymentForName(uploadedActivitiProcessesName);
        Assert.assertNull(deploymentForName);
    }

/*    public void testUploadActivitiProcessesFromClassPath() throws Exception {
        String envName = "ENE";
        List<String> uploadedBpmnsList = cteActivitiUtils.uploadActivitiProcessesFromClassPath(envName);
        Assert.assertFalse("Keine BPMN's im Class-Path gefunden!", uploadedBpmnsList.isEmpty());

        uploadedBpmnsList.forEach(uploadedBpmn -> {
            try {
                cteActivitiServiceREST.deleteDeploymentForName(uploadedBpmn);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }*/
}