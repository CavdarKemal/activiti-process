package de.creditreform.crefoteam.activiti;

import java.util.Map;

/**
 * Process-Instance
 * {
 * "id":"269872",
 * "url":"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/process-instances/269872",
 * "businessKey":null,
 * "suspended":false,
 * "ended":false,
 * "processDefinitionId":"JUNIT-TEST:1:269790",
 * "processDefinitionUrl":"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/repository/process-definitions/JUNIT-TEST:1:269790",
 * "activityId":"UserTask0",
 * "variables":[],
 * "tenantId":"",
 * "completed":false
 * }
 */
public interface CteActivitiProcess {
    Integer getId();

    boolean isSuspended();

    boolean isEnded();

    String getProcessDefinitionId();

    String getActivityId();

    Map<String, String> getVariables();

    void setVariables(Map<String, String> variables);

}
