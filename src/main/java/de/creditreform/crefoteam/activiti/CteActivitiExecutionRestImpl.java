package de.creditreform.crefoteam.activiti;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by CavdarK on 25.01.2016.
 */
public class CteActivitiExecutionRestImpl implements CteActivitiExecution {
    final JsonNodeHelper jsonNodeHelper;

    public CteActivitiExecutionRestImpl(JsonNode jsonNodeData) {
        this.jsonNodeHelper = new JsonNodeHelper(jsonNodeData);
    }

    @Override
    public Integer getId() {
        Integer id = Integer.valueOf(jsonNodeHelper.getJsonNode("id").textValue());
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
    public Integer getProcessInstanceId() {
        Integer id = Integer.valueOf(jsonNodeHelper.getJsonNode("processInstanceId").textValue());
        return id;
    }

    @Override
    public String getActivityId() {
        return jsonNodeHelper.getJsonNode("activityId").textValue();
    }

}
