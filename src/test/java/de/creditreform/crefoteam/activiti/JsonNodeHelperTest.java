package de.creditreform.crefoteam.activiti;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests fuer JsonNodeHelper.
 */
public class JsonNodeHelperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGetJsonNode_existingNode() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"name\": \"testValue\", \"id\": 123}");
        JsonNodeHelper helper = new JsonNodeHelper(jsonNode);

        JsonNode result = helper.getJsonNode("name");

        assertNotNull(result);
        assertEquals("testValue", result.textValue());
    }

    @Test
    public void testGetJsonNode_numericValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"id\": 42}");
        JsonNodeHelper helper = new JsonNodeHelper(jsonNode);

        JsonNode result = helper.getJsonNode("id");

        assertNotNull(result);
        assertEquals(42, result.intValue());
    }

    @Test(expected = RuntimeException.class)
    public void testGetJsonNode_nonExistingNode_throwsException() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"name\": \"test\"}");
        JsonNodeHelper helper = new JsonNodeHelper(jsonNode);

        helper.getJsonNode("nonExisting");
    }

    @Test(expected = RuntimeException.class)
    public void testConstructor_withException_throwsRuntimeException() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"exception\": \"Server error occurred\"}");

        new JsonNodeHelper(jsonNode);
    }

    @Test
    public void testGetJsonNodeData_returnsOriginalNode() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"key\": \"value\"}");
        JsonNodeHelper helper = new JsonNodeHelper(jsonNode);

        JsonNode result = helper.getJsonNodeData();

        assertSame(jsonNode, result);
    }

    @Test
    public void testConstructor_withoutExceptionField_noThrow() throws Exception {
        // exception-Feld ist nicht vorhanden, kein Fehler
        JsonNode jsonNode = objectMapper.readTree("{\"data\": \"ok\"}");

        JsonNodeHelper helper = new JsonNodeHelper(jsonNode);

        assertNotNull(helper.getJsonNodeData());
    }

    @Test(expected = RuntimeException.class)
    public void testConstructor_withNullException_throwsException() throws Exception {
        // exception-Feld ist vorhanden (auch mit null-Wert), wirft Exception
        JsonNode jsonNode = objectMapper.readTree("{\"data\": \"ok\", \"exception\": null}");

        new JsonNodeHelper(jsonNode);
    }
}
