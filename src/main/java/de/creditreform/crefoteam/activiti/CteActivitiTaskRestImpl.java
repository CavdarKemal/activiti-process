package de.creditreform.crefoteam.activiti;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**************************************************************************************************************/
class CteActivitiTaskRestImpl implements CteActivitiTask {
    final JsonNodeHelper jsonNodeHelper;

    private Map<String, String> variables = new HashMap<>();

    public CteActivitiTaskRestImpl(JsonNode jsonNodeData) {
        this.jsonNodeHelper = new JsonNodeHelper(jsonNodeData);
    }

    @Override
    public String getName() {
        return jsonNodeHelper.getJsonNode("name").textValue();
    }

    @Override
    public String getAssignee() {
        return jsonNodeHelper.getJsonNode("assignee").textValue();
    }

    @Override
    public Integer getId() {
        return getIntegerValue("id");
    }

    @Override
    public String getTaskDefinitionKey() {
        return jsonNodeHelper.getJsonNode("taskDefinitionKey").textValue();
    }

    @Override
    public String getProcessDefinitionId() {
        return jsonNodeHelper.getJsonNode("processDefinitionId").textValue();
    }

    @Override
    public Integer getProcessInstanceId() {
        return getIntegerValue("processInstanceId");
    }

    @Override
    public String getDelegationState() {
        return jsonNodeHelper.getJsonNode("delegationState").textValue();
    }

    @Override
    public boolean isSuspended() {
        return jsonNodeHelper.getJsonNode("suspended").booleanValue();
    }

    @Override
    public Integer getExecutionId() {
        return getIntegerValue("executionId");
    }

    @Override
    public Integer getParentTaskId() {
        return getIntegerValue("parentTaskId");
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

    @Override
    public String toString() {
        String stringBuffer = "{ Id: " + getId() +
                ", TaskDefinitionKey: " + getTaskDefinitionKey() +
                ", ProcessInstanceId: " + getProcessInstanceId() +
                ", ExecutionId: " + getExecutionId() +
//    stringBuffer.append(", ParentTaskId: " + getParentTaskId());
//    stringBuffer.append(", Assignee: " + getAssignee());
                " }";
        return stringBuffer;
    }

    private Integer getIntegerValue(String fieldName) {
        JsonNode jsonValue = jsonNodeHelper.getJsonNode(fieldName);
        if ((jsonValue != null) && !jsonValue.isNull()) {
            Integer intValue = Integer.valueOf(jsonValue.textValue());
            return intValue;
        }
        return Integer.valueOf(-1);
    }

}
