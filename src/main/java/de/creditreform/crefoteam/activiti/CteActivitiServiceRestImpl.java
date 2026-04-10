package de.creditreform.crefoteam.activiti;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.creditreform.crefoteam.cte.rest.RestInvoker;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.rest.RestInvokerResponse;
import org.activiti.rest.service.api.RestUrls;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class CteActivitiServiceRestImpl implements CteActivitiService {
    protected final static Logger LOGGER = LoggerFactory.getLogger(CteActivitiServiceRestImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestInvokerConfig activitiRestInvokerConfig;
    private final RestInvokerActiviti restServiceInvoker;
    private final int restTimeoutInMillis;

    public CteActivitiServiceRestImpl(RestInvokerConfig activitiRestInvokerConfig) {
        this(activitiRestInvokerConfig, REST_TIME_OUT_IN_MILLIS);
    }

    public CteActivitiServiceRestImpl(RestInvokerConfig activitiRestInvokerConfig, int restTimeoutInMillis) {
        this.activitiRestInvokerConfig = activitiRestInvokerConfig;
        this.restTimeoutInMillis = restTimeoutInMillis;
        this.restServiceInvoker = new RestInvokerActiviti(activitiRestInvokerConfig.getServiceURL(), activitiRestInvokerConfig.getServiceUser(), activitiRestInvokerConfig.getServicePassword());
    }

    public static String extendsRestUrls(String[] restUrlArray) {
        String[] resultAry = new String[restUrlArray.length + 1];
        resultAry[0] = SERVICE_PATH;
        System.arraycopy(restUrlArray, 0, resultAry, 1, restUrlArray.length);
        return StringUtils.join(resultAry, '/');
    }

    @Override
    public RestInvokerConfig getActivitiRestInvokerConfig() {
        return activitiRestInvokerConfig;
    }

    @Override
    public RestInvokerActiviti getRestServiceInvoker() {
        return restServiceInvoker;
    }

    @Override
    public int getRestTimeoutInMillis() {
        return restTimeoutInMillis;
    }

    @Override
    public InputStream getProcessImage(Integer processInstanceId) throws Exception {
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_PROCESS_INSTANCE_DIAGRAM), processInstanceId.toString());
        LOGGER.debug(formatRequest("getProcessImage", "GET", restServiceInvoker, ""));
        InputStream inputStream = restServiceInvoker.invokeGetInputStream();
        LOGGER.debug(formatResponseBody(null, "{<<<BitMap>>>}"));
        return inputStream;
    }

    @Override
    public int signalEventReceived(final String signalName) throws Exception {
        restServiceInvoker.init(restTimeoutInMillis);
        String appendPath = extendsRestUrls(RestUrls.URL_EXECUTION_COLLECTION);
        restServiceInvoker.appendPath(appendPath);

        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("action", "signalEventReceived");
        requestNode.put("signalName", signalName);
        String stringEntity = requestNode.toString();

        LOGGER.debug(formatRequest("signalEventReceived", "PUT", restServiceInvoker, stringEntity));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokePut(stringEntity, RestInvoker.CONTENT_TYPE_JSON);
        LOGGER.debug(formatResponseBody(null, restInvokerResponse.getResponseBody()));
        int statusCode = restInvokerResponse.getStatusCode();
        return statusCode;
    }


    /*****************************************    ProcessDefinitions   *****************************************/
    private List<CteActivitiProcessDefinition> listProcessDefinitions() throws Exception {
        List<CteActivitiProcessDefinition> cteActivitiProcessDefinitionList = new ArrayList<>();
        // KEIN restServiceInvoker.init hier!!!
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_PROCESS_DEFINITION_COLLECTION));
        LOGGER.debug(formatRequest("listProcessDefinitions", "GET", restServiceInvoker, ""));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_JSON);
        String responseBody = restInvokerResponse.expectStatusOK().getResponseBody();
        LOGGER.debug(formatResponseBody(null, responseBody));
        JsonNode jsonNodeFromResponse = restInvokerResponse.decodeResponse(objectMapper::readTree);
        JsonNode jsonNodeData = jsonNodeFromResponse.get("data");
        Iterator<JsonNode> jsonNodeDataIterator = jsonNodeData.iterator();
        while (jsonNodeDataIterator.hasNext()) {
            jsonNodeData = jsonNodeDataIterator.next();
            CteActivitiProcessDefinition cteActivitiProcessDefinition = new CteActivitiProcessDefinitionRestImpl(jsonNodeData);
            cteActivitiProcessDefinitionList.add(cteActivitiProcessDefinition);
        }
        return cteActivitiProcessDefinitionList;
    }

    public List<CteActivitiProcessDefinition> listProcessDefinitionsLike(String processNameLike) throws Exception {
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.queryParam("nameLike", (processNameLike + "%"));
        return listProcessDefinitions();
    }

    /**
     * GET repository/process-definitions
     * http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/repository/process-definitions?name=JUNITTest1Name
     */
    public CteActivitiProcessDefinition getProcessDefinitionForName(String processName) throws Exception {
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.queryParam("name", processName);
        List<CteActivitiProcessDefinition> cteActivitiProcessDefinitions = listProcessDefinitions();
        if (cteActivitiProcessDefinitions.size() == 1) {
            return cteActivitiProcessDefinitions.get(0);
        } else if (cteActivitiProcessDefinitions.size() > 1) {
            throw new IllegalStateException("ACTIVITI liefert für '" + processName + "' mehrere Prozesse!");
        }
        return null;
    }

    /**
     * GET repository/process-definitions/{processDefinitionId}
     * http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/repository/process-definitions?key=JUNITTest1Key
     */
    public CteActivitiProcessDefinition getProcessDefinitionForKey(String processDefKey) throws Exception {
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_PROCESS_DEFINITION_COLLECTION));
        restServiceInvoker.queryParam("key", processDefKey);
        LOGGER.debug(formatRequest("getProcessDefinitionForKey", "GET", restServiceInvoker, ""));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_JSON);
        String responseBody = restInvokerResponse.expectStatusOK().getResponseBody();
        LOGGER.debug(formatResponseBody(null, responseBody));
        JsonNode jsonNodeFromResponse = restInvokerResponse.decodeResponse(objectMapper::readTree);
        JsonNode jsonNodeData = jsonNodeFromResponse.get("data");
        Iterator<JsonNode> jsonNodeDataIterator = jsonNodeData.iterator();
        if (jsonNodeDataIterator.hasNext()) {
            jsonNodeData = jsonNodeDataIterator.next();
            CteActivitiProcessDefinition cteActivitiProcessDefinition = new CteActivitiProcessDefinitionRestImpl(jsonNodeData);
            return cteActivitiProcessDefinition;
        }
        return null;
    }
    /*----------------------------------------    ProcessDefinitions   ----------------------------------------*/

    /*****************************************    ProcessInstances   *****************************************/
    @Override
    public List<CteActivitiProcess> queryProcessInstances(String processDefinitionKey, Map<String, Object> paramsMap) throws Exception {
        // GET runtime/process-instances?processDefinitionKey=xxx
        // Schneller als POST /query/process-instances, da kein JOIN gegen act_ru_variable.
        // createCteActivitiProcessImpl lädt Prozess-Variablen bereits — client-seitiger Filter möglich.
        List<CteActivitiProcess> cteActivitiProcessInstanceList = new ArrayList<>();
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_PROCESS_INSTANCE_COLLECTION));
        restServiceInvoker.queryParam("processDefinitionKey", processDefinitionKey);
        LOGGER.debug(formatRequest("queryProcessInstances", "GET", restServiceInvoker, ""));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_JSON);
        String responseBody = restInvokerResponse.expectStatusOK().getResponseBody();
        LOGGER.debug(formatResponseBody(null, responseBody));
        JsonNode jsonNodeFromResponse = restInvokerResponse.decodeResponse(objectMapper::readTree);
        JsonNode jsonNodeData = jsonNodeFromResponse.get("data");
        Iterator<JsonNode> jsonNodeDataIterator = jsonNodeData.iterator();
        while (jsonNodeDataIterator.hasNext()) {
            jsonNodeData = jsonNodeDataIterator.next();
            CteActivitiProcess cteActivitiProcessInstance = createCteActivitiProcessImpl(jsonNodeData);
            cteActivitiProcessInstanceList.add(cteActivitiProcessInstance);
        }
        // Client-seitiger Variablen-Filter (Variablen wurden bereits von createCteActivitiProcessImpl geladen)
        if (paramsMap != null && !paramsMap.isEmpty()) {
            cteActivitiProcessInstanceList.removeIf(proc -> {
                Map<String, String> vars = proc.getVariables();
                for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                    if (!entry.getValue().toString().equals(vars.get(entry.getKey()))) {
                        return true;
                    }
                }
                return false;
            });
        }
        return cteActivitiProcessInstanceList;
    }

    public void deleteProcessInstances(String processDefinitionKey, String meinKey) throws Exception {
        Map<String, Object> paramsMap = new HashMap<>();
        if (processDefinitionKey == null || processDefinitionKey.isEmpty()) {
            throw new IllegalArgumentException("Parameter <processDefinitionKey> darf nicht leer sein!");
        }
        if (meinKey == null || meinKey.isEmpty()) {
            throw new IllegalArgumentException("Parameter <meinKey> darf nicht leer sein!");
        }
        paramsMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey);
        List<CteActivitiProcess> processInstancesList = queryProcessInstances(processDefinitionKey, paramsMap);
        for (CteActivitiProcess processInstance : processInstancesList) {
            deleteProcessInstance(processInstance.getId());
        }
    }

    public void deleteProcessInstance(Integer processInstanceID) throws Exception {
        // DELETE runtime/process-instances/{processInstanceId}
        // DELETE http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/process-instances/248014
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_PROCESS_INSTANCE), processInstanceID.toString());
        LOGGER.debug(formatRequest("deleteProcessInstance", "DELETE", restServiceInvoker, ""));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokeDelete(RestInvoker.CONTENT_TYPE_JSON);
        LOGGER.debug(formatResponseBody(null, restInvokerResponse.getResponseBody()));
        restInvokerResponse.expectStatusNoContent();
    }
    /*--------------------------------    ProcessInstances   *****************************************/

    public List<CteActivitiExecution> listExecutions() throws Exception {
        // GET runtime/executions
        return getExecutions(null);
    }

    public List<CteActivitiExecution> getExecutions(Integer processInstanceID) throws Exception {
        // GET runtime/executions
        List<CteActivitiExecution> cteActivitiExecutionList = new ArrayList<CteActivitiExecution>();
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_EXECUTION_COLLECTION));
        if (processInstanceID != null) {
            restServiceInvoker.queryParam("processInstanceId", processInstanceID.toString());
        }
        LOGGER.debug(formatRequest("getExecutions", "GET", restServiceInvoker, "processInstanceId = " + processInstanceID));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_JSON);
        String responseBody = restInvokerResponse.expectStatusOK().getResponseBody();
        LOGGER.debug(formatResponseBody(null, responseBody));
        JsonNode jsonNodeFromResponse = restInvokerResponse.decodeResponse(objectMapper::readTree);
        JsonNode jsonNodeData = jsonNodeFromResponse.get("data");
        Iterator<JsonNode> jsonNodeDataIterator = jsonNodeData.iterator();
        while (jsonNodeDataIterator.hasNext()) {
            jsonNodeData = jsonNodeDataIterator.next();
            CteActivitiExecution cteActivitiExecution = new CteActivitiExecutionRestImpl(jsonNodeData);
            cteActivitiExecutionList.add(cteActivitiExecution);
        }
        return cteActivitiExecutionList;
    }

    protected Integer startProcess(String processDefinitionKey, String meinKey) throws Exception {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey);
        CteActivitiProcess processInstance = startProcess(processDefinitionKey, paramsMap);
        Objects.requireNonNull(processInstance, "startProcess() darf nicht null liefern");
        Map<String, String> variables = processInstance.getVariables();
        String meinKeyX = variables.get(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY);
        if (!meinKey.equals(meinKeyX)) {
            throw new IllegalStateException("MEIN_KEY stimmt nicht ueberein: erwartet=" + meinKey + ", erhalten=" + meinKeyX);
        }

        Integer pInstanceID = processInstance.getId();
        CteActivitiProcess cteActivitiProcessInstance2 = getProcessInstanceByID(pInstanceID);
        Objects.requireNonNull(cteActivitiProcessInstance2, "getProcessInstanceByID() darf nicht null liefern fuer ID=" + pInstanceID);
        return pInstanceID;
    }

    @Override
    public CteActivitiProcess startProcess(String processDefinitionKey, Map<String, Object> paramsMap) throws Exception {
        // POST runtime/process-instances
        if (processDefinitionKey == null) {
            throw new RuntimeException("Für Parameter <processDefinitionKey> muss ein gültiger String-Wert übergeben werden!");
        }
        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("processDefinitionKey", processDefinitionKey);
        requestNode.set("variables", buildVariablesArray(paramsMap));
        String stringEntity = requestNode.toString();

        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_PROCESS_INSTANCE_COLLECTION));
        LOGGER.debug(formatRequest("startProcess", "POST", restServiceInvoker, stringEntity));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokePost(stringEntity, RestInvoker.CONTENT_TYPE_JSON);
        String responseBody = restInvokerResponse.getResponseBody();
        LOGGER.debug(formatResponseBody(null, responseBody));
        JsonNode jsonNodeFromResponse = restInvokerResponse.decodeResponse(objectMapper::readTree);
        CteActivitiProcess cteActivitiProcessInstance = createCteActivitiProcessImpl(jsonNodeFromResponse);
        return cteActivitiProcessInstance;
    }

    @Override
    public CteActivitiProcess getProcessInstanceByID(Integer processInstanceID) throws Exception {
        // GET runtime/process-instances
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_PROCESS_INSTANCE), processInstanceID.toString());
        LOGGER.debug(formatRequest("getProcessInstanceByID", "GET", restServiceInvoker, "processInstanceId = \" + processInstanceId"));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_JSON);
        String responseBody = restInvokerResponse.expectStatusOK().getResponseBody();
        LOGGER.debug(formatResponseBody(null, responseBody));
        JsonNode jsonNodeFromResponse = restInvokerResponse.decodeResponse(objectMapper::readTree);
        CteActivitiProcess cteActivitiProcessInstance = createCteActivitiProcessImpl(jsonNodeFromResponse);
        return cteActivitiProcessInstance;
    }

    @Override
    public List<CteActivitiTask> listTasks(Map<String, Object> paramsMap) throws Exception {
        List<CteActivitiTask> cteActivitiTasksList = new ArrayList<>();
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("includeProcessVariables", true);
        requestBody.put("includeTaskLocalVariables", true);
        if (paramsMap != null) {
            ArrayNode processVarsArray = null;
            for (Map.Entry<String, Object> paramEntry : paramsMap.entrySet()) {
                String key = paramEntry.getKey();
                if (!key.isEmpty() && Character.isLowerCase(key.charAt(0))) {
                    // Gültige Activiti-Felder (beginnen mit Kleinbuchstabe, z.B. processInstanceId)
                    requestBody.put(key, paramEntry.getValue().toString());
                } else if (key.equals("ACTIVITI_PROCESS_NAME")) {
                    // ACTIVITI_PROCESS_NAME ist der Process-Definition-Key, kein Prozessvariable
                    requestBody.put("processDefinitionKey", paramEntry.getValue().toString());
                } else if (!key.isEmpty()) {
                    // Echte Prozessvariablen (z.B. MEIN_KEY) → processInstanceVariables
                    if (processVarsArray == null) {
                        processVarsArray = objectMapper.createArrayNode();
                    }
                    ObjectNode varNode = objectMapper.createObjectNode();
                    varNode.put("name", key);
                    varNode.put("value", paramEntry.getValue().toString());
                    varNode.put("operation", "equals");
                    varNode.put("type", "string");
                    processVarsArray.add(varNode);
                }
            }
            if (processVarsArray != null) {
                requestBody.set("processInstanceVariables", processVarsArray);
            }
        }
        JsonNode jsonNodeData = executeTaskQuery(requestBody);
        Iterator<JsonNode> jsonNodeDataIterator = jsonNodeData.iterator();
        while (jsonNodeDataIterator.hasNext()) {
            JsonNode taskNode = jsonNodeDataIterator.next();
            try {
                CteActivitiTaskRestImpl cteActivitiTask = createCteActivitiTaskRestImpl(taskNode);
                cteActivitiTasksList.add(cteActivitiTask);
            } catch (Exception ex) {
                CteActivitiTaskRestImpl cteActivitiTask = new CteActivitiTaskRestImpl(taskNode);
                LOGGER.warn("CteActivitiServiceRestImpl#listTasks(): createCteActivitiTaskRestImpl() für {} führt zur Exception:", cteActivitiTask.getId(), ex);
            }
        }
        return cteActivitiTasksList;
    }

    protected CteActivitiTask queryNextTaskForTaskVariables(Map<String, Object> paramsMap) throws Exception {
        List<CteActivitiTask> tasksFor = queryTasksForTaskVariables(paramsMap);
        if (tasksFor.size() > 1) {
            throw new IllegalStateException("queryNextTaskForTaskVariables(): queryTasksForTaskVariables() liefert mehrere Treffer für Parameter " + paramsMap);
        }
        if (tasksFor.size() == 1) {
            return tasksFor.get(0);
        }
        return null;
    }

    protected List<CteActivitiTask> queryTasksForTaskVariables(Map<String, Object> myVariablesMap) throws Exception {
        List<CteActivitiTask> cteActivitiTaskList = new ArrayList<>();
        ObjectNode requestBody = buildTaskQueryBody(myVariablesMap);
        JsonNode jsonNodeData = executeTaskQuery(requestBody);
        Iterator<JsonNode> jsonNodeDataIterator = jsonNodeData.iterator();
        while (jsonNodeDataIterator.hasNext()) {
            JsonNode taskNode = jsonNodeDataIterator.next();
            try {
                CteActivitiTaskRestImpl cteActivitiTaskRest = createCteActivitiTaskRestImpl(taskNode);
                cteActivitiTaskList.add(cteActivitiTaskRest);
            } catch (Exception ex) {
                CteActivitiTaskRestImpl cteActivitiTask = new CteActivitiTaskRestImpl(taskNode);
                LOGGER.warn("CteActivitiServiceRestImpl#queryTasksForTaskVariables(): createCteActivitiTaskRestImpl() für {} führt zur Exception:", cteActivitiTask.getId(), ex);
            }
        }
        return cteActivitiTaskList;
    }

    @Override
    public void deleteTask(Integer taskID) throws Exception {
        // DELETE runtime/tasks/{taskId}?cascadeHistory={cascadeHistory}&deleteReason={deleteReason}
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_TASK), taskID.toString());
        restServiceInvoker.queryParam("cascadeHistory", "true");
        LOGGER.debug(formatRequest("deleteTask", "DELETE", restServiceInvoker, ""));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokeDelete(RestInvoker.CONTENT_TYPE_JSON);
        LOGGER.debug(formatResponseBody(null, restInvokerResponse.getResponseBody()));
        restInvokerResponse.expectStatusNoContent();
    }

    @Override
    public CteActivitiTask selectTaskForBusinessKey(Integer processInstanceID, String meinKey) throws Exception {
        LOGGER.info("selectTaskForBusinessKey: meinKey={}, processInstanceID={}", meinKey, processInstanceID);
        Map<String, Object> filter = new HashMap<>();
        filter.put(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY, meinKey);
        final long timeMillis = System.currentTimeMillis();
        CteActivitiTask theUserTask = null;
        while (theUserTask == null) {
            theUserTask = queryNextTaskForTaskVariables(filter);
            if (theUserTask != null) {
                break;
            }
            Thread.sleep(1000);
            LOGGER.info(".");
            if (System.currentTimeMillis() > timeMillis + restTimeoutInMillis) {
                throw new TimeoutException("\n\tqueryNextTaskForTaskVariables() liefert keine Nachfolge-Task innerhalb von " + (restTimeoutInMillis / 1000) + " Sekunden!");
            }
        }
        LOGGER.info("selectTaskForBusinessKey: Task gefunden: ID={}, ProcessInstanceId={}, TaskDefinitionKey={}, Variablen={}",
                theUserTask.getId(), theUserTask.getProcessInstanceId(), theUserTask.getTaskDefinitionKey(), theUserTask.getVariables());
        return theUserTask;
    }

    @Override
    public void claimTask(CteActivitiTask cteActivitiTask, String userID) throws Exception {
        Integer id = cteActivitiTask.getId();
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_TASK), id.toString());

        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("action", "claim");
        requestNode.put("assignee", userID);
        String stringEntity = requestNode.toString();
        LOGGER.debug(formatRequest("claimTask", "POST", restServiceInvoker, stringEntity));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokePost(stringEntity, RestInvoker.CONTENT_TYPE_JSON);
        LOGGER.debug(formatResponseBody(null, restInvokerResponse.getResponseBody()));
        // stelle sicher, dass der Status-Code der Response 200 ist
        restInvokerResponse.expectStatusOK();
    }

    @Override
    public void unclaimTask(CteActivitiTask cteActivitiTask) throws Exception {
        Integer id = cteActivitiTask.getId();
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_TASK), id.toString());

        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("action", "claim");
        String stringEntity = requestNode.toString();
        LOGGER.debug(formatRequest("unclaimTask", "POST", restServiceInvoker, stringEntity));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokePost(stringEntity, RestInvoker.CONTENT_TYPE_JSON);
        LOGGER.debug(formatResponseBody(null, restInvokerResponse.getResponseBody()));
        // stelle sicher, dass der Status-Code der Response 200 ist
        restInvokerResponse.expectStatusOK();
    }

    @Override
    public void completeTask(CteActivitiTask cteActivitiTask, Map<String, Object> taskParams) throws Exception {
        Integer id = cteActivitiTask.getId();
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_TASK), id.toString());
        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("action", "complete");
        requestNode.set("variables", buildVariablesArray(taskParams));
        String stringEntity = requestNode.toString();
        LOGGER.debug(formatRequest("completeTask", "POST", restServiceInvoker, stringEntity));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokePost(stringEntity, RestInvoker.CONTENT_TYPE_JSON);
        LOGGER.debug(formatResponseBody(null, restInvokerResponse.getResponseBody()));
        // stelle sicher, dass der Status-Code der Response 201 ist
        restInvokerResponse.expectStatusOK();
    }

    protected Map<String, String> getProcessVariables(Integer processInstanceId) throws Exception {
        // http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/process-instances/269881/variables
        Map<String, String> processVars = new HashMap<>();
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_COLLECTION), processInstanceId.toString());
        LOGGER.debug(formatRequest("getProcessVariables", "GET", restServiceInvoker, "processInstanceId = " + processInstanceId));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_JSON);
        String responseBody = restInvokerResponse.expectStatusOK().getResponseBody();
        LOGGER.debug(formatResponseBody(null, "\"variables\":" + responseBody));
        JsonNode jsonNodeFromResponse = restInvokerResponse.decodeResponse(objectMapper::readTree);
        Iterator<JsonNode> jsonNodeDataIterator = jsonNodeFromResponse.iterator();
        while (jsonNodeDataIterator.hasNext()) {
            jsonNodeFromResponse = jsonNodeDataIterator.next();
            CteActivitiVariable cteActivitiVariable = new CteActivitiVariableImpl(jsonNodeFromResponse);
            processVars.put(cteActivitiVariable.getName(), cteActivitiVariable.getValue());
        }
        return processVars;
    }

    protected Map<String, String> getTaskVariables(Integer taskId) throws Exception {
        // GET runtime/tasks/{taskId}/variables?scope={scope}
        Map<String, String> processVars = new HashMap<>();
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_TASK_VARIABLES_COLLECTION), taskId.toString());
        LOGGER.debug(formatRequest("getTaskVariables", "GET", restServiceInvoker, "taskId = " + taskId));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_JSON);
        String responseBody = restInvokerResponse.expectStatusOK().getResponseBody();
        LOGGER.debug(formatResponseBody(null, "\"variables\":" + responseBody));
        JsonNode jsonNodeFromResponse = restInvokerResponse.decodeResponse(objectMapper::readTree);
        Iterator<JsonNode> jsonNodeDataIterator = jsonNodeFromResponse.iterator();
        while (jsonNodeDataIterator.hasNext()) {
            jsonNodeFromResponse = jsonNodeDataIterator.next();
            CteActivitiVariable cteActivitiVariable = new CteActivitiVariableImpl(jsonNodeFromResponse);
            processVars.put(cteActivitiVariable.getName(), cteActivitiVariable.getValue());
        }
        return processVars;
    }

    protected void checkTaskVariables(CteActivitiTask cteActivitiTask, String meinKey2) {
        Map<String, String> variables = cteActivitiTask.getVariables();
        String meinKeyX = variables.get(ActivitProcessConstants.UT_TASK_PARAM_NAME_MEIN_KEY);
        if (!Objects.equals(meinKeyX, meinKey2)) {
            throw new IllegalStateException("MEIN_KEY stimmt nicht ueberein: erwartet=" + meinKey2 + ", erhalten=" + meinKeyX);
        }
        // ????? warum fehlt die denn manchmal??? Assert.assertNotNull(variables.get("initiator"));
        logUserTask(cteActivitiTask);
    }

    private JsonNode executeTaskQuery(ObjectNode requestBody) throws Exception {
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_TASK_QUERY));
        String stringEntity = requestBody.toString();
        LOGGER.debug(formatRequest("executeTaskQuery", "POST", restServiceInvoker, stringEntity));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokePost(stringEntity, RestInvoker.CONTENT_TYPE_JSON);
        String responseBody = restInvokerResponse.expectStatusOK().getResponseBody();
        LOGGER.debug(formatResponseBody(null, responseBody));
        return restInvokerResponse.decodeResponse(objectMapper::readTree).get("data");
    }

    protected CteActivitiTaskRestImpl createCteActivitiTaskRestImpl(JsonNode jsonNodeData) throws Exception {
        CteActivitiTaskRestImpl cteActivitiTask = new CteActivitiTaskRestImpl(jsonNodeData);
        Map<String, String> allVariables = new HashMap<>();
        // Activiti 6: variables enthält task-lokale + Prozess-Variablen zusammen
        // Activiti 5.19: includeProcessVariables liefert Prozess-Variablen in "processVariables", task-lokale in "variables"
        JsonNode variablesNode = jsonNodeData.get("variables");
        if (variablesNode != null && variablesNode.isArray() && variablesNode.size() > 0) {
            allVariables.putAll(parseVariablesFromJsonNode(variablesNode));
        }
        JsonNode processVariablesNode = jsonNodeData.get("processVariables");
        if (processVariablesNode != null && processVariablesNode.isArray() && processVariablesNode.size() > 0) {
            allVariables.putAll(parseVariablesFromJsonNode(processVariablesNode));
        }
        if (allVariables.isEmpty()) {
            allVariables = getTaskVariables(cteActivitiTask.getId());
        }
        cteActivitiTask.setVariables(allVariables);
        return cteActivitiTask;
    }

    protected CteActivitiProcess createCteActivitiProcessImpl(JsonNode jsonNodeData) throws Exception {
        CteActivitiProcess cteActivitiProcessInstance = new CteActivitiProcessRestImpl(jsonNodeData);
        // die Variablen des Prozesses müssen explizit abgefragt werden!
        Map<String, String> processVariables = getProcessVariables(cteActivitiProcessInstance.getId());
        cteActivitiProcessInstance.setVariables(processVariables);
        return cteActivitiProcessInstance;
    }

    private String formatJsonString(String jsonString) {
        if ((jsonString == null) || (jsonString.length() < 1)) {
            jsonString = "{}";
        }
        if (!jsonString.startsWith("{")) {
            jsonString = "{\n" + jsonString;
        }
        if (!jsonString.endsWith("}")) {
            jsonString += "\n}";
        }
        try {
            Object json = objectMapper.readValue(jsonString, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception ex) {
            LOGGER.trace("formatJsonString: konnte nicht als JSON parsen, gebe Original zurueck: {}", ex.getMessage());
        }
        return jsonString;
    }

    private String formatResponseBody(String methodName, String responseBody) {
        String replacedResponseBody = formatJsonString(responseBody);
        if (methodName != null) {
            replacedResponseBody = String.format("\n<<%s>>\nResponse:%s", methodName, replacedResponseBody);
        } else {
            replacedResponseBody = String.format("\nResponse:%s", replacedResponseBody);
        }
        return replacedResponseBody;
    }

    private String formatRequest(String methodName, String httpMethod, RestInvokerActiviti restServiceInvoker, String stringEntity) {
        String line = "===============================================================================================================";
        String formattedJsonString = formatJsonString(stringEntity);
        String formattedRequest = String.format("\n%s\n<<%s>>\nReguest:\n\tMethod:\t%s\n\tURL:\t%s\n\tParams:\t%s",
                line, methodName, httpMethod, restServiceInvoker.buildURI(), formattedJsonString);
        return formattedRequest;
    }

    public File prepareBpmnFileForEnvironment(String bpmnFileName, String envName) throws Exception {
        File srcFile = new File(bpmnFileName);
        File dstFile = new File(System.getProperty("user.dir"), String.format("%s-%s", envName, srcFile.getName()));
        String oldContent = FileUtils.readFileToString(srcFile, java.nio.charset.StandardCharsets.UTF_8);
        String newContent = oldContent.replaceAll("%ENV%", envName);
        FileUtils.writeStringToFile(dstFile, newContent, java.nio.charset.StandardCharsets.UTF_8);
        return dstFile;
    }

    protected void logUserTask(CteActivitiTask cteActivitiTask) {
        String stringBuffer = "::UserTask::" +
                "\tTaskDefinitionKey: " + cteActivitiTask.getTaskDefinitionKey() +
                "\tID: " + cteActivitiTask.getId() +
                "\tName: " + cteActivitiTask.getName() +
                "\tVariables: " + cteActivitiTask.getVariables() +
                "\n";
        LOGGER.debug(stringBuffer);
    }


    private ArrayNode buildVariablesArray(Map<String, Object> paramsMap) {
        ArrayNode array = objectMapper.createArrayNode();
        if (paramsMap != null) {
            for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    LOGGER.warn("buildVariablesArray: null-Wert fuer Key '{}' uebersprungen", entry.getKey());
                    continue;
                }
                ObjectNode varNode = objectMapper.createObjectNode();
                varNode.put("name", entry.getKey());
                varNode.put("value", value.toString());
                array.add(varNode);
            }
        }
        return array;
    }

    private ArrayNode buildProcessVariablesArrayWithAutoOperation(Map<String, Object> paramsMap) {
        ArrayNode array = objectMapper.createArrayNode();
        if (paramsMap != null) {
            for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                Object rawValue = entry.getValue();
                if (rawValue == null) {
                    LOGGER.warn("buildProcessVariablesArrayWithAutoOperation: null-Wert fuer Key '{}' uebersprungen", entry.getKey());
                    continue;
                }
                String value = rawValue.toString();
                String operation = value.contains("%") ? "like" : "equals";
                ObjectNode varNode = objectMapper.createObjectNode();
                varNode.put("name", entry.getKey());
                varNode.put("value", value);
                varNode.put("operation", operation);
                varNode.put("type", "string");
                array.add(varNode);
            }
        }
        return array;
    }

    private ObjectNode buildTaskQueryBody(Map<String, Object> processVariablesFilter) {
        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("includeProcessVariables", true);
        requestNode.put("includeTaskLocalVariables", true);
        if (processVariablesFilter != null && !processVariablesFilter.isEmpty()) {
            requestNode.set("processInstanceVariables", buildProcessVariablesArrayEquals(processVariablesFilter));
        }
        return requestNode;
    }

    private ArrayNode buildProcessVariablesArrayEquals(Map<String, Object> paramsMap) {
        ArrayNode array = objectMapper.createArrayNode();
        if (paramsMap != null) {
            for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    LOGGER.warn("buildProcessVariablesArrayEquals: null-Wert fuer Key '{}' uebersprungen", entry.getKey());
                    continue;
                }
                ObjectNode varNode = objectMapper.createObjectNode();
                varNode.put("name", entry.getKey());
                varNode.put("value", value.toString());
                varNode.put("operation", "equals");
                varNode.put("type", "string");
                array.add(varNode);
            }
        }
        return array;
    }

    private Map<String, String> parseVariablesFromJsonNode(JsonNode variablesArray) {
        Map<String, String> variables = new HashMap<>();
        if (variablesArray != null && variablesArray.isArray()) {
            for (JsonNode varNode : variablesArray) {
                CteActivitiVariable cteActivitiVariable = new CteActivitiVariableImpl(varNode);
                variables.put(cteActivitiVariable.getName(), cteActivitiVariable.getValue());
            }
        }
        return variables;
    }

    /*****************************************    Deployments   *****************************************/
    private List<CteActivitiDeployment> listDeploymentsInternal() throws Exception {
        List<CteActivitiDeployment> cteActivitiDeploymentList = new ArrayList<>();
        // KEIN restServiceInvoker.init hier!!!
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_DEPLOYMENT_COLLECTION));
        LOGGER.debug(formatRequest("listDeploymentsInternal", "GET", restServiceInvoker, ""));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_JSON);
        String responseBody = restInvokerResponse.expectStatusOK().getResponseBody();
        LOGGER.debug(formatResponseBody(null, responseBody));
        JsonNode jsonNodeFromResponse = restInvokerResponse.decodeResponse(objectMapper::readTree);
        JsonNode jsonNodeData = jsonNodeFromResponse.get("data");
        Iterator<JsonNode> jsonNodeDataIterator = jsonNodeData.iterator();
        while (jsonNodeDataIterator.hasNext()) {
            jsonNodeData = jsonNodeDataIterator.next();
            CteActivitiDeployment cteActivitiDeployment = new CteActivitiDeploymentRestImpl(jsonNodeData);
            cteActivitiDeploymentList.add(cteActivitiDeployment);
        }
        return cteActivitiDeploymentList;
    }


    public List<CteActivitiDeployment> listDeploymentsForNameLike(String deploymentNameLike) throws Exception {
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.queryParam("nameLike", (deploymentNameLike + "%"));
        return listDeploymentsInternal();
    }

    public CteActivitiDeployment getDeploymentForName(String deploymentName) throws Exception {
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.queryParam("name", deploymentName);
        List<CteActivitiDeployment> cteActivitiDeployments = listDeploymentsInternal();
        if (cteActivitiDeployments.size() == 1) {
            return cteActivitiDeployments.get(0);
        } else if (cteActivitiDeployments.size() > 1) {
            throw new IllegalStateException("ACTIVITI liefert für '" + deploymentName + "' mehrere Deployments!");
        }
        return null;
    }

    public String uploadDeploymentFile(File deploymentFile) throws Exception {
        // POST repository/deployments
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_DEPLOYMENT_COLLECTION));
        LOGGER.debug(formatRequest("uploadDeployment", "POST", restServiceInvoker, ""));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokePostMP(deploymentFile, RestInvoker.CONTENT_TYPE_JSON);
        String responseBody = restInvokerResponse.expectStatusCreated().getResponseBody();
        LOGGER.debug(formatResponseBody(null, responseBody));
        JsonNode jsonNodeFromResponse = restInvokerResponse.decodeResponse(objectMapper::readTree);
        String deploymentID = jsonNodeFromResponse.get("id").textValue();
        return deploymentID;
    }

    public void deleteDeploymentForName(String deploymentName) throws Exception {
        CteActivitiDeployment cteActivitiDeploymentForName = getDeploymentForName(deploymentName);
        if (cteActivitiDeploymentForName == null) {
            throw new RuntimeException("Deployment '" + deploymentName + "' existiert nicht!");
        }
        // DELETE repository/deployments/{deploymentId}?cascade=true
        String deploymentId = cteActivitiDeploymentForName.getId();
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_DEPLOYMENT), deploymentId);
        restServiceInvoker.queryParam("cascade", "true");
        LOGGER.debug(formatRequest("deleteDeployment", "DELETE", restServiceInvoker, ""));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokeDelete(RestInvoker.CONTENT_TYPE_JSON);
        LOGGER.debug(formatResponseBody(null, restInvokerResponse.getResponseBody()));
        restInvokerResponse.expectStatusNoContent();
    }

    public void deleteCteActivitiDeployment(CteActivitiDeployment cteActivitiDeployment) throws Exception {
        // DELETE repository/deployments/{deploymentId}?cascade=true
        String deploymentId = cteActivitiDeployment.getId();
        restServiceInvoker.init(restTimeoutInMillis);
        restServiceInvoker.appendPath(extendsRestUrls(RestUrls.URL_DEPLOYMENT), deploymentId);
        restServiceInvoker.queryParam("cascade", "true");
        LOGGER.debug(formatRequest("deleteDeployment", "DELETE", restServiceInvoker, ""));
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokeDelete(RestInvoker.CONTENT_TYPE_JSON);
        LOGGER.debug(formatResponseBody(null, restInvokerResponse.getResponseBody()));
        restInvokerResponse.expectStatusNoContent();
    }
    /*----------------------------------------    Deployments   ----------------------------------------*/


}
