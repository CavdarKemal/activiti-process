package de.creditreform.crefoteam.activiti;

/**
 * Created by CavdarK on 25.01.2016.
 */

/*
Execution
{
     "id":"5",
     "url":"http://localhost:8182/runtime/executions/5",
     "parentId":null,
     "parentUrl":null,
     "processInstanceId":"5",
     "processInstanceUrl":"http://localhost:8182/runtime/process-instances/5",
     "suspended":false,
     "activityId":null,
     "tenantId":null
}
*/
public interface CteActivitiExecution {
    Integer getId();

    boolean isSuspended();

    boolean isEnded();

    Integer getProcessInstanceId();

    String getActivityId();

}
