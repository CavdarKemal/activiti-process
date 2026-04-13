package de.creditreform.crefoteam.activiti;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests fuer CteActivitiVariableImpl.
 */
public class CteActivitiVariableImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGetName_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"name\": \"MEIN_KEY\", \"value\": \"testValue\"}");

        CteActivitiVariable variable = new CteActivitiVariableImpl(jsonNode);

        assertEquals("MEIN_KEY", variable.getName());
    }

    @Test
    public void testGetValue_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"name\": \"MEIN_KEY\", \"value\": \"testValue123\"}");

        CteActivitiVariable variable = new CteActivitiVariableImpl(jsonNode);

        assertEquals("testValue123", variable.getValue());
    }

    @Test
    public void testGetNameAndValue_withSpecialCharacters() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"name\": \"VAR_WITH_UNDERSCORE\", \"value\": \"Wert mit Leerzeichen\"}");

        CteActivitiVariable variable = new CteActivitiVariableImpl(jsonNode);

        assertEquals("VAR_WITH_UNDERSCORE", variable.getName());
        assertEquals("Wert mit Leerzeichen", variable.getValue());
    }

    @Test(expected = RuntimeException.class)
    public void testGetName_missingField_throwsException() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"value\": \"test\"}");

        CteActivitiVariable variable = new CteActivitiVariableImpl(jsonNode);
        variable.getName();
    }

    @Test(expected = RuntimeException.class)
    public void testGetValue_missingField_throwsException() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"name\": \"test\"}");

        CteActivitiVariable variable = new CteActivitiVariableImpl(jsonNode);
        variable.getValue();
    }

    @Test(expected = RuntimeException.class)
    public void testConstructor_withException_throwsException() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"exception\": \"Error from server\"}");

        new CteActivitiVariableImpl(jsonNode);
    }
}
