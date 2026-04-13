package de.creditreform.crefoteam.activiti;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests fuer CteActivitiProcessDefinitionRestImpl.
 */
public class CteActivitiProcessDefinitionRestImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String FULL_DEFINITION_JSON = "{"
            + "\"name\": \"My Process\","
            + "\"id\": \"myProcess:1:4\","
            + "\"key\": \"myProcess\","
            + "\"url\": \"http://localhost:8080/service/repository/process-definitions/myProcess:1:4\""
            + "}";

    @Test
    public void testGetName_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_DEFINITION_JSON);

        CteActivitiProcessDefinition definition = new CteActivitiProcessDefinitionRestImpl(jsonNode);

        assertEquals("My Process", definition.getName());
    }

    @Test
    public void testGetId_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_DEFINITION_JSON);

        CteActivitiProcessDefinition definition = new CteActivitiProcessDefinitionRestImpl(jsonNode);

        assertEquals("myProcess:1:4", definition.getId());
    }

    @Test
    public void testGetKey_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_DEFINITION_JSON);

        CteActivitiProcessDefinition definition = new CteActivitiProcessDefinitionRestImpl(jsonNode);

        assertEquals("myProcess", definition.getKey());
    }

    @Test
    public void testGetUrl_returnsCorrectValue() throws Exception {
        JsonNode jsonNode = objectMapper.readTree(FULL_DEFINITION_JSON);

        CteActivitiProcessDefinition definition = new CteActivitiProcessDefinitionRestImpl(jsonNode);

        assertEquals("http://localhost:8080/service/repository/process-definitions/myProcess:1:4", definition.getUrl());
    }

    @Test(expected = RuntimeException.class)
    public void testMissingField_throwsException() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"name\": \"Test\"}");

        CteActivitiProcessDefinition definition = new CteActivitiProcessDefinitionRestImpl(jsonNode);
        definition.getId();  // id fehlt
    }

    @Test(expected = RuntimeException.class)
    public void testWithException_throwsRuntimeException() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"exception\": \"Not Found\"}");

        new CteActivitiProcessDefinitionRestImpl(jsonNode);
    }

    @Test
    public void testWithSpecialCharactersInName() throws Exception {
        String json = "{\"name\": \"ENE-Prozess für Tests\", \"id\": \"ene:2:10\", \"key\": \"ene\", \"url\": \"http://localhost/api\"}";
        JsonNode jsonNode = objectMapper.readTree(json);

        CteActivitiProcessDefinition definition = new CteActivitiProcessDefinitionRestImpl(jsonNode);

        assertEquals("ENE-Prozess für Tests", definition.getName());
        assertEquals("ene", definition.getKey());
    }
}
