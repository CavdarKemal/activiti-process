package de.creditreform.crefoteam.activiti;

import com.fasterxml.jackson.databind.JsonNode;

/**************************************************************************************************************/
class CteActivitiDeploymentRestImpl implements CteActivitiDeployment {
    final JsonNodeHelper jsonNodeHelper;

    public CteActivitiDeploymentRestImpl(JsonNode jsonNodeData) {
        this.jsonNodeHelper = new JsonNodeHelper(jsonNodeData);
    }

    @Override
    public String getName() {
        return jsonNodeHelper.getJsonNode("name").textValue();
    }

    @Override
    public String getId() {
        return jsonNodeHelper.getJsonNode("id").textValue();
    }

    @Override
    public String getUrl() {
        return jsonNodeHelper.getJsonNode("url").textValue();
    }

}
