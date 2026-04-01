package de.creditreform.crefoteam.activiti;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface CteActivitiService {
    String SERVICE_PATH = "activiti-rest/service";
    int REST_TIME_OUT_IN_MILLIS = 1 * 60 * 1000; // 1 Minuten

    RestInvokerConfig getActivitiRestInvokerConfig();
    RestInvokerActiviti getRestServiceInvoker();
    int getRestTimeoutInMillis();
    List<CteActivitiTask> listTasks(Map<String, Object> paramsMap) throws Exception;

    void deleteTask(Integer taskID) throws Exception;

    InputStream getProcessImage(Integer processInstanceId) throws Exception;

    CteActivitiTask selectTaskForBusinessKey(Integer processInstanceID, String meinKey) throws Exception;

    void claimTask(final CteActivitiTask cteActivitiTask, final String userID) throws Exception;

    void unclaimTask(final CteActivitiTask cteActivitiTask) throws Exception;

    void completeTask(final CteActivitiTask cteActivitiTask, Map<String, Object> paramsMap) throws Exception;

    int signalEventReceived(String signalName) throws Exception;

    CteActivitiDeployment getDeploymentForName(String name) throws Exception;

    void deleteDeploymentForName(String name) throws Exception;

    String uploadDeploymentFile(File deploymentFile) throws Exception;

    List<CteActivitiProcess> queryProcessInstances(String processDefinitionKey, Map<String, Object> paramsMap) throws Exception;

    void deleteProcessInstance(Integer id) throws Exception;

    CteActivitiProcess startProcess(String processDefinitionKey, Map<String, Object> paramsMap) throws Exception;

    CteActivitiProcess getProcessInstanceByID(Integer processInstanceID) throws Exception;

}
