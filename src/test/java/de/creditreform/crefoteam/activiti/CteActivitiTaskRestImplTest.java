package de.creditreform.crefoteam.activiti;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests fuer CteActivitiTaskRestImpl.
 */
public class CteActivitiTaskRestImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String FULL_TASK_JSON = "{"
            + "\"name\": \"User Task 1\","
            + "\"assignee\": \"kermit\","
            + "\"id\": \"42\","
            + "\"taskDefinitionKey\": \"userTask1\","
            + "\"processDefinitionId\": \"myProcess:1:4\","
            + "\"processInstanceId\": \"100\","
            + "\"delegationState\": \"pending\","
            + "\"suspended\": false,"
            + "\"executionId\": \"101\","
            + "\"parentTaskId\": \"99\""
            + "}";

    @Test
    public void testGetName_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_TASK_JSON);

        CteActivitiTask task = new CteActivitiTaskRestImpl(jsonNode);

        assertEquals("User Task 1", task.getName());
    }

    @Test
    public void testGetAssignee_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_TASK_JSON);

        CteActivitiTask task = new CteActivitiTaskRestImpl(jsonNode);

        assertEquals("kermit", task.getAssignee());
    }

    @Test
    public void testGetId_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_TASK_JSON);

        CteActivitiTask task = new CteActivitiTaskRestImpl(jsonNode);

        assertEquals(Integer.valueOf(42), task.getId());
    }

    @Test
    public void testGetTaskDefinitionKey_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_TASK_JSON);

        CteActivitiTask task = new CteActivitiTaskRestImpl(jsonNode);

        assertEquals("userTask1", task.getTaskDefinitionKey());
    }

    @Test
    public void testGetProcessDefinitionId_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_TASK_JSON);

        CteActivitiTask task = new CteActivitiTaskRestImpl(jsonNode);

        assertEquals("myProcess:1:4", task.getProcessDefinitionId());
    }

    @Test
    public void testGetProcessInstanceId_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_TASK_JSON);

        CteActivitiTask task = new CteActivitiTaskRestImpl(jsonNode);

        assertEquals(Integer.valueOf(100), task.getProcessInstanceId());
    }

    @Test
    public void testGetDelegationState_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_TASK_JSON);

        CteActivitiTask task = new CteActivitiTaskRestImpl(jsonNode);

        assertEquals("pending", task.getDelegationState());
    }

    @Test
    public void testIsSuspended_returnsFalse() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_TASK_JSON);

        CteActivitiTask task = new CteActivitiTaskRestImpl(jsonNode);

        assertFalse(task.isSuspended());
    }

    @Test
    public void testIsSuspended_returnsTrue() throws Exception {
        String json = FULL_TASK_JSON.replace("\"suspended\": false", "\"suspended\": true");
        JsonNode jsonNode = objectMapper.readTree(json);

        CteActivitiTask task = new CteActivitiTaskRestImpl(jsonNode);

        assertTrue(task.isSuspended());
    }

    @Test
    public void testGetExecutionId_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_TASK_JSON);

        CteActivitiTask task = new CteActivitiTaskRestImpl(jsonNode);

        assertEquals(Integer.valueOf(101), task.getExecutionId());
    }

    @Test
    public void testGetParentTaskId_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_TASK_JSON);

        CteActivitiTask task = new CteActivitiTaskRestImpl(jsonNode);

        assertEquals(Integer.valueOf(99), task.getParentTaskId());
    }

    @Test
    public void testGetVariables_initiallyEmpty() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_TASK_JSON);

        CteActivitiTask task = new CteActivitiTaskRestImpl(jsonNode);

        assertNotNull(task.getVariables());
        assertTrue(task.getVariables().isEmpty());
    }

    @Test
    public void testSetVariables_setsCorrectly() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_TASK_JSON);
        CteActivitiTask task = new CteActivitiTaskRestImpl(jsonNode);

        Map<String, String> variables = new HashMap<>();
        variables.put("key1", "value1");
        variables.put("key2", "value2");
        task.setVariables(variables);

        assertEquals(2, task.getVariables().size());
        assertEquals("value1", task.getVariables().get("key1"));
        assertEquals("value2", task.getVariables().get("key2"));
    }

    @Test
    public void testToString_containsRelevantInfo() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_TASK_JSON);

        CteActivitiTask task = new CteActivitiTaskRestImpl(jsonNode);
        String str = task.toString();

        assertTrue(str.contains("42"));  // Id
        assertTrue(str.contains("userTask1"));  // TaskDefinitionKey
        assertTrue(str.contains("100"));  // ProcessInstanceId
    }

    @Test(expected = RuntimeException.class)
    public void testMissingField_throwsException() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"name\": \"Task\"}");

        CteActivitiTask task = new CteActivitiTaskRestImpl(jsonNode);
        task.getId();  // id fehlt
    }
}
