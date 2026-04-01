package de.creditreform.crefoteam.activiti;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by CavdarK on 20.07.2017.
 */
public class CteActivitiVariableImpl implements CteActivitiVariable {

    final JsonNodeHelper jsonNodeHelper;

    public CteActivitiVariableImpl(JsonNode jsonNodeData) {
        this.jsonNodeHelper = new JsonNodeHelper(jsonNodeData);
    }

    @Override
    public String getName() {
        return getJsonNode("name").textValue();
    }

    @Override
    public String getValue() {
        return getJsonNode("value").textValue();
    }

    private JsonNode getJsonNode(String nodeName) {
        JsonNode jsonNode = jsonNodeHelper.jsonNodeData.get(nodeName);
        if (jsonNode == null) {
            throw new RuntimeException(String.format("JsonNode für '%s' ist nicht gefüllt!", nodeName));
        }
        return jsonNode;
    }
}
