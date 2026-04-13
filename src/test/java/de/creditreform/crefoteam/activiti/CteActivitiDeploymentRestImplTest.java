package de.creditreform.crefoteam.activiti;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests fuer CteActivitiDeploymentRestImpl.
 */
public class CteActivitiDeploymentRestImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGetName_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(
                "{\"name\": \"TestDeployment.bpmn\", \"id\": \"12345\", \"url\": \"http://localhost:8080/deployment/12345\"}");

        CteActivitiDeployment deployment = new CteActivitiDeploymentRestImpl(jsonNode);

        assertEquals("TestDeployment.bpmn", deployment.getName());
    }

    @Test
    public void testGetId_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(
                "{\"name\": \"Test.bpmn\", \"id\": \"deploy-001\", \"url\": \"http://localhost/api\"}");

        CteActivitiDeployment deployment = new CteActivitiDeploymentRestImpl(jsonNode);

        assertEquals("deploy-001", deployment.getId());
    }

    @Test
    public void testGetUrl_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(
                "{\"name\": \"Test.bpmn\", \"id\": \"123\", \"url\": \"http://activiti:9090/service/repository/deployments/123\"}");

        CteActivitiDeployment deployment = new CteActivitiDeploymentRestImpl(jsonNode);

        assertEquals("http://activiti:9090/service/repository/deployments/123", deployment.getUrl());
    }

    @Test(expected = RuntimeException.class)
    public void testMissingField_throwsException() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"name\": \"Test.bpmn\"}");

        CteActivitiDeployment deployment = new CteActivitiDeploymentRestImpl(jsonNode);
        deployment.getId(); // id fehlt
    }

    @Test(expected = RuntimeException.class)
    public void testWithException_throwsRuntimeException() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"exception\": \"Server Error\"}");

        new CteActivitiDeploymentRestImpl(jsonNode);
    }
}
