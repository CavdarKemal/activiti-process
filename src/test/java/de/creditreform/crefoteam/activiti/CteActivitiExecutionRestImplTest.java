package de.creditreform.crefoteam.activiti;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests fuer CteActivitiExecutionRestImpl.
 */
public class CteActivitiExecutionRestImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String FULL_EXECUTION_JSON = "{"
            + "\"id\": \"200\","
            + "\"suspended\": false,"
            + "\"ended\": false,"
            + "\"processInstanceId\": \"100\","
            + "\"activityId\": \"serviceTask1\""
            + "}";

    @Test
    public void testGetId_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_EXECUTION_JSON);

        CteActivitiExecution execution = new CteActivitiExecutionRestImpl(jsonNode);

        assertEquals(Integer.valueOf(200), execution.getId());
    }

    @Test
    public void testIsSuspended_returnsFalse() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_EXECUTION_JSON);

        CteActivitiExecution execution = new CteActivitiExecutionRestImpl(jsonNode);

        assertFalse(execution.isSuspended());
    }

    @Test
    public void testIsSuspended_returnsTrue() throws Exception {
        String json = FULL_EXECUTION_JSON.replace("\"suspended\": false", "\"suspended\": true");
        JsonNode jsonNode = objectMapper.readTree(json);

        CteActivitiExecution execution = new CteActivitiExecutionRestImpl(jsonNode);

        assertTrue(execution.isSuspended());
    }

    @Test
    public void testIsEnded_returnsFalse() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_EXECUTION_JSON);

        CteActivitiExecution execution = new CteActivitiExecutionRestImpl(jsonNode);

        assertFalse(execution.isEnded());
    }

    @Test
    public void testIsEnded_returnsTrue() throws Exception {
        String json = FULL_EXECUTION_JSON.replace("\"ended\": false", "\"ended\": true");
        JsonNode jsonNode = objectMapper.readTree(json);

        CteActivitiExecution execution = new CteActivitiExecutionRestImpl(jsonNode);

        assertTrue(execution.isEnded());
    }

    @Test
    public void testGetProcessInstanceId_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_EXECUTION_JSON);

        CteActivitiExecution execution = new CteActivitiExecutionRestImpl(jsonNode);

        assertEquals(Integer.valueOf(100), execution.getProcessInstanceId());
    }

    @Test
    public void testGetActivityId_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_EXECUTION_JSON);

        CteActivitiExecution execution = new CteActivitiExecutionRestImpl(jsonNode);

        assertEquals("serviceTask1", execution.getActivityId());
    }

    @Test(expected = RuntimeException.class)
    public void testMissingField_throwsException() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"suspended\": false}");

        CteActivitiExecution execution = new CteActivitiExecutionRestImpl(jsonNode);
        execution.getId();  // id fehlt
    }

    @Test(expected = RuntimeException.class)
    public void testWithException_throwsRuntimeException() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"exception\": \"Execution not found\"}");

        new CteActivitiExecutionRestImpl(jsonNode);
    }

    @Test
    public void testSuspendedAndEnded_bothTrue() throws Exception {
        String json = "{\"id\": \"300\", \"suspended\": true, \"ended\": true, \"processInstanceId\": \"150\", \"activityId\": \"endEvent\"}";
        JsonNode jsonNode = objectMapper.readTree(json);

        CteActivitiExecution execution = new CteActivitiExecutionRestImpl(jsonNode);

        assertTrue(execution.isSuspended());
        assertTrue(execution.isEnded());
        assertEquals("endEvent", execution.getActivityId());
    }
}
