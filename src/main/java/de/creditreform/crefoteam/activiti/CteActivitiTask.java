package de.creditreform.crefoteam.activiti;

import java.util.Map;

/**
 * {
 * "id":"269817",
 * "url":"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/tasks/269817",
 * "owner":null,
 * "assignee":null,
 * "delegationState":null,
 * "name":"UserTask #0",
 * "description":null,
 * "createTime":"2017-07-20T15:09:13.501+02:00",
 * "dueDate":null,
 * "priority":50,
 * "suspended":false,
 * "taskDefinitionKey":"UserTask0",
 * "tenantId":"",
 * "category":null,
 * "formKey":null,
 * "parentTaskId":null,
 * "parentTaskUrl":null,
 * "executionId":"269809",
 * "executionUrl":"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/executions/269809",
 * "processInstanceId":"269809",
 * "processInstanceUrl":"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/process-instances/269809",
 * "processDefinitionId":"JUNIT-TEST:1:269790",
 * "processDefinitionUrl":"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/repository/process-definitions/JUNIT-TEST:1:269790",
 * "variables":[]
 * }
 */

public interface CteActivitiTask {
    Integer getId();

    String getName();

    String getAssignee();

    String getTaskDefinitionKey();

    String getProcessDefinitionId();

    Integer getProcessInstanceId();

    String getDelegationState();

    boolean isSuspended();

    Integer getExecutionId();

    Integer getParentTaskId();

    Map<String, String> getVariables();

    void setVariables(Map<String, String> variables);
}
