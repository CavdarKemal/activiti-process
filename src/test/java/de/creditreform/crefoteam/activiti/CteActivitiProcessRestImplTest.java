package de.creditreform.crefoteam.activiti;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests fuer CteActivitiProcessRestImpl.
 */
public class CteActivitiProcessRestImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String FULL_PROCESS_JSON = "{"
            + "\"id\": \"500\","
            + "\"suspended\": false,"
            + "\"ended\": false,"
            + "\"processDefinitionId\": \"myProcess:1:4\","
            + "\"activityId\": \"userTask1\""
            + "}";

    @Test
    public void testGetId_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_PROCESS_JSON);

        CteActivitiProcess process = new CteActivitiProcessRestImpl(jsonNode);

        assertEquals(Integer.valueOf(500), process.getId());
    }

    @Test
    public void testIsSuspended_returnsFalse() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_PROCESS_JSON);

        CteActivitiProcess process = new CteActivitiProcessRestImpl(jsonNode);

        assertFalse(process.isSuspended());
    }

    @Test
    public void testIsSuspended_returnsTrue() throws Exception {
        String json = FULL_PROCESS_JSON.replace("\"suspended\": false", "\"suspended\": true");
        JsonNode jsonNode = objectMapper.readTree(json);

        CteActivitiProcess process = new CteActivitiProcessRestImpl(jsonNode);

        assertTrue(process.isSuspended());
    }

    @Test
    public void testIsEnded_returnsFalse() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_PROCESS_JSON);

        CteActivitiProcess process = new CteActivitiProcessRestImpl(jsonNode);

        assertFalse(process.isEnded());
    }

    @Test
    public void testIsEnded_returnsTrue() throws Exception {
        String json = FULL_PROCESS_JSON.replace("\"ended\": false", "\"ended\": true");
        JsonNode jsonNode = objectMapper.readTree(json);

        CteActivitiProcess process = new CteActivitiProcessRestImpl(jsonNode);

        assertTrue(process.isEnded());
    }

    @Test
    public void testGetProcessDefinitionId_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_PROCESS_JSON);

        CteActivitiProcess process = new CteActivitiProcessRestImpl(jsonNode);

        assertEquals("myProcess:1:4", process.getProcessDefinitionId());
    }

    @Test
    public void testGetActivityId_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_PROCESS_JSON);

        CteActivitiProcess process = new CteActivitiProcessRestImpl(jsonNode);

        assertEquals("userTask1", process.getActivityId());
    }

    @Test
    public void testGetVariables_initiallyEmpty() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_PROCESS_JSON);

        CteActivitiProcess process = new CteActivitiProcessRestImpl(jsonNode);

        assertNotNull(process.getVariables());
        assertTrue(process.getVariables().isEmpty());
    }

    @Test
    public void testSetVariables_setsCorrectly() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_PROCESS_JSON);
        CteActivitiProcess process = new CteActivitiProcessRestImpl(jsonNode);

        Map<String, String> variables = new HashMap<>();
        variables.put("MEIN_KEY", "ENE");
        variables.put("TEST_TYPE", "PHASE2_ONLY");
        process.setVariables(variables);

        assertEquals(2, process.getVariables().size());
        assertEquals("ENE", process.getVariables().get("MEIN_KEY"));
    }

    @Test(expected = RuntimeException.class)
    public void testMissingField_throwsException() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"suspended\": false}");

        CteActivitiProcess process = new CteActivitiProcessRestImpl(jsonNode);
        process.getId();  // id fehlt
    }
}
