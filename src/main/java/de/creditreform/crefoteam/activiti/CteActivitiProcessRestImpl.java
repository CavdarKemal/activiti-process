package de.creditreform.crefoteam.activiti;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**************************************************************************************************************/
class CteActivitiProcessRestImpl implements CteActivitiProcess {
    final JsonNodeHelper jsonNodeHelper;

    private Map<String, String> variables = new HashMap<>();

    public CteActivitiProcessRestImpl(JsonNode jsonNodeData) {
        this.jsonNodeHelper = new JsonNodeHelper(jsonNodeData);
    }

    @Override
    public Integer getId() {
        JsonNode jsonNode = jsonNodeHelper.getJsonNode("id");
        Integer id = Integer.valueOf(jsonNode.textValue());
        return id;
    }

    @Override
    public boolean isSuspended() {
        return jsonNodeHelper.getJsonNode("suspended").booleanValue();
    }

    @Override
    public boolean isEnded() {
        return jsonNodeHelper.getJsonNode("ended").booleanValue();
    }

    @Override
    public String getProcessDefinitionId() {
        return jsonNodeHelper.getJsonNode("processDefinitionId").textValue();
    }

    @Override
    public String getActivityId() {
        return jsonNodeHelper.getJsonNode("activityId").textValue();
    }

    @Override
    public Map<String, String> getVariables() {
        return variables;
    }

    @Override
    public void setVariables(Map<String, String> variables) {
        this.variables.clear();
        this.variables = variables;
    }

}
