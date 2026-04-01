package de.creditreform.crefoteam.activiti;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonNodeHelper {

    final JsonNode jsonNodeData;

    public JsonNodeHelper(JsonNode jsonNodeData) {
        this.jsonNodeData = jsonNodeData;
        JsonNode exception = this.jsonNodeData.get("exception");
        if (exception != null) {
            throw new RuntimeException(String.format("JsonNode-Exception:\n%s", exception));
        }
    }

    public JsonNode getJsonNode(String nodeName) {
        JsonNode jsonNode = jsonNodeData.get(nodeName);
        if (jsonNode == null) {
            throw new RuntimeException(String.format("JsonNode für '%s' ist nicht gefüllt!", nodeName));
        }
        return jsonNode;
    }
}
