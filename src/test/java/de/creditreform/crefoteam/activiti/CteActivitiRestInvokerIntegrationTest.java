package de.creditreform.crefoteam.activiti;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.creditreform.crefoteam.cte.rest.RestInvoker;
import de.creditreform.crefoteam.cte.rest.RestInvokerResponse;
import org.activiti.rest.service.api.RestUrls;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.Iterator;

public class CteActivitiRestInvokerIntegrationTest extends RestIntegrationTestBase {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        super.setUp();
        cteActivitiServiceREST.getRestServiceInvoker().init(Integer.valueOf(1000));
    }

    @Test
    public void testBuildURI() throws Exception {
        try {
            cteActivitiServiceREST.getRestServiceInvoker().appendPath(new String[]{""});
            Assert.fail("IllegalArgumentException expected!");
        } catch (IllegalArgumentException ex) {
            // OK
        }
        URI buildURI = cteActivitiServiceREST.getRestServiceInvoker().buildURI();
        Assert.assertNotNull(buildURI);
        Assert.assertTrue(buildURI.toString().startsWith(restInvokerConfig.getServiceURL()));

        cteActivitiServiceREST.getRestServiceInvoker().appendPath(new String[]{"subservice"});
        buildURI = cteActivitiServiceREST.getRestServiceInvoker().buildURI();
        Assert.assertNotNull(buildURI);
        Assert.assertTrue(buildURI.toString().startsWith(restInvokerConfig.getServiceURL() + "/subservice"));

        cteActivitiServiceREST.getRestServiceInvoker().appendPath(CteActivitiServiceRestImpl.extendsRestUrls(RestUrls.URL_PROCESS_DEFINITION_COLLECTION));
        cteActivitiServiceREST.getRestServiceInvoker().queryParam("name", "test");
        buildURI = cteActivitiServiceREST.getRestServiceInvoker().buildURI();
        Assert.assertNotNull(buildURI);
        Assert.assertTrue(buildURI.toString().startsWith(restInvokerConfig.getServiceURL() + "/activiti-rest/service/repository/process-definitions?name=test"));

        cteActivitiServiceREST.getRestServiceInvoker().appendPath(CteActivitiServiceRestImpl.extendsRestUrls(RestUrls.URL_PROCESS_DEFINITION_COLLECTION));
        cteActivitiServiceREST.getRestServiceInvoker().queryParam("name", "test");
        cteActivitiServiceREST.getRestServiceInvoker().queryParam("resourceNameLike", "resource[a-z]{5}");
        buildURI = cteActivitiServiceREST.getRestServiceInvoker().buildURI();
        Assert.assertNotNull(buildURI);
        Assert.assertTrue(buildURI.toString().contains("?name=test&name=test&resourceNameLike=resource%5Ba-z%5D%7B5%7D"));
    }

    @Test
    public void testInvokeGet() throws Exception {
        cteActivitiServiceREST.getRestServiceInvoker().appendPath(CteActivitiServiceRestImpl.extendsRestUrls(RestUrls.URL_PROCESS_DEFINITION_COLLECTION));
        RestInvokerResponse restInvokerResponse = cteActivitiServiceREST.getRestServiceInvoker().invokeGet(RestInvoker.CONTENT_TYPE_JSON);
        Assert.assertNotNull(restInvokerResponse);
        Assert.assertEquals(200, restInvokerResponse.getStatusCode());
        Assert.assertTrue(restInvokerResponse.getResponseBody().startsWith("{\"data\":[{\"id\":\""));
    }

    @Test
    public void testInvokePost() throws Exception {
        cteActivitiServiceREST.getRestServiceInvoker().appendPath(CteActivitiServiceRestImpl.extendsRestUrls(RestUrls.URL_PROCESS_INSTANCE_QUERY));
        String stringEntity = "{\"processDefinitionKey\":\"oneTaskProcess\"}";
        RestInvokerResponse restInvokerResponse = cteActivitiServiceREST.getRestServiceInvoker().invokePost(stringEntity, RestInvoker.CONTENT_TYPE_JSON);
        Assert.assertNotNull(restInvokerResponse);
        Assert.assertEquals(200, restInvokerResponse.getStatusCode());
        Assert.assertTrue(restInvokerResponse.getResponseBody().startsWith("{\"data\":[]"));
    }

    @Test
    public void testInvokePut() throws Exception {
        cteActivitiServiceREST.getRestServiceInvoker().appendPath(CteActivitiServiceRestImpl.extendsRestUrls(RestUrls.URL_USER), restInvokerConfig.getServiceUser());
        String stringEntity = "{\"firstName\":\"Kemal\"}";
        RestInvokerResponse restInvokerResponse = cteActivitiServiceREST.getRestServiceInvoker().invokePut(stringEntity, RestInvoker.CONTENT_TYPE_JSON);
        Assert.assertNotNull(restInvokerResponse);
        Assert.assertEquals(200, restInvokerResponse.getStatusCode());
        Assert.assertTrue(restInvokerResponse.getResponseBody().startsWith("{\"id\":\"kermit\",\"firstName\":\"Kemal\""));
    }

    @Test
    public void testInvokeDelete() throws Exception {
        // zuerst ein Deployment erzeugen...
        String JUNIT_DEPLOYMENT_NAME = "JunitTestEmpty.bpmn";
        String deploymentPath = this.getClass().getResource("/" + JUNIT_DEPLOYMENT_NAME).getPath();
        cteActivitiServiceREST.getRestServiceInvoker().appendPath(CteActivitiServiceRestImpl.extendsRestUrls(RestUrls.URL_DEPLOYMENT_COLLECTION));
        File deployFile = new File(deploymentPath);
        RestInvokerResponse restInvokerResponse = cteActivitiServiceREST.getRestServiceInvoker().invokePostMP(deployFile, RestInvoker.CONTENT_TYPE_JSON);
        Assert.assertNotNull(restInvokerResponse);
        Assert.assertEquals(201, restInvokerResponse.getStatusCode()); // Created!

        // ... und Deployments abrufen...
        cteActivitiServiceREST.getRestServiceInvoker().appendPath(CteActivitiServiceRestImpl.extendsRestUrls(RestUrls.URL_DEPLOYMENT_COLLECTION));
        cteActivitiServiceREST.getRestServiceInvoker().queryParam("name", JUNIT_DEPLOYMENT_NAME);
        restInvokerResponse = cteActivitiServiceREST.getRestServiceInvoker().invokeGet(RestInvoker.CONTENT_TYPE_JSON);
        String responseBody = restInvokerResponse.expectStatusOK().getResponseBody();
        JsonNode jsonNodeFromResponse = objectMapper.readTree(responseBody);
        JsonNode jsonNodeData = jsonNodeFromResponse.get("data");
        Iterator<JsonNode> jsonNodeDataIterator = jsonNodeData.iterator();
        Assert.assertTrue(jsonNodeDataIterator.hasNext());
        jsonNodeData = jsonNodeDataIterator.next();
        Assert.assertNotNull(jsonNodeData);

        // ... und nun dieses Deployment löschen...
        String deploymentId = jsonNodeData.get("id").textValue();
        cteActivitiServiceREST.getRestServiceInvoker().appendPath(CteActivitiServiceRestImpl.extendsRestUrls(RestUrls.URL_DEPLOYMENT), deploymentId);
        // !!!cteActivitiServiceREST.getRestServiceInvoker().queryParam( "deploymentId", deploymentId );
        restInvokerResponse = cteActivitiServiceREST.getRestServiceInvoker().invokeDelete(RestInvoker.CONTENT_TYPE_JSON);
        Assert.assertNotNull(restInvokerResponse);
        Assert.assertEquals(HttpStatus.SC_NO_CONTENT, restInvokerResponse.getStatusCode());
    }


    @Test
    public void testFormatResponseBody() {
        doFormatResponse("{\"data\":[],\"total\":1,\"start\":0,\"sort\":\"id\",\"order\":\"asc\",\"size\":1}");
        doFormatResponse("{\"data\":[{\"id\":\"250717\",\"variables\":[]}],\"total\":1,\"start\":0,\"sort\":\"id\",\"order\":\"asc\",\"size\":1}");
        doFormatResponse("{\"data\":[{\"id\":\"250717\",\"url\":\"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/tasks/250717\",\"owner\":null,\"assignee\":null,\"delegationState\":null,\"name\":\"Pausiert alle JVM-Jobs, die sich in der Engine befinden\",\"description\":null,\"createTime\":\"2017-07-11T17:48:28.229+02:00\",\"dueDate\":null,\"priority\":50,\"suspended\":false,\"taskDefinitionKey\":\"UserTaskPauseAllJobs\",\"tenantId\":\"\",\"category\":null,\"formKey\":null,\"parentTaskId\":null,\"parentTaskUrl\":null,\"executionId\":\"250707\",\"executionUrl\":\"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/executions/250707\",\"processInstanceId\":\"250707\",\"processInstanceUrl\":\"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/process-instances/250707\",\"processDefinitionId\":\"testAutomationProcess:1:217288\",\"processDefinitionUrl\":\"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/repository/process-definitions/testAutomationProcess:1:217288\",\"variables\":[]}],\"total\":1,\"start\":0,\"sort\":\"id\",\"order\":\"asc\",\"size\":1}");
        doFormatResponse("{\"data\":[{\"id\":\"238535\",\"url\":\"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/tasks/238535\",\"owner\":null,\"assignee\":null,\"delegationState\":null,\"name\":\"Test-Art und -FÃ¤lle angeben\",\"description\":null,\"createTime\":\"2017-06-04T13:22:06.145+02:00\",\"dueDate\":null,\"priority\":50,\"suspended\":false,\"taskDefinitionKey\":\"UserTaskEnterTestData\",\"tenantId\":\"\",\"category\":null,\"formKey\":null,\"parentTaskId\":null,\"parentTaskUrl\":null,\"executionId\":\"250093\",\"executionUrl\":\"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/executions/250093\",\"processInstanceId\":\"250093\",\"processInstanceUrl\":\"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/process-instances/250093\",\"processDefinitionId\":\"testAutomationProcess:1:217288\",\"processDefinitionUrl\":\"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/repository/process-definitions/testAutomationProcess:1:217288\",\"variables\":[]},{\"id\":\"251310\",\"url\":\"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/tasks/251310\",\"owner\":null,\"assignee\":null,\"delegationState\":null,\"name\":\"Auf Testsystem warten [UT-WaitForTestSystem]\",\"description\":null,\"createTime\":\"2017-07-12T12:38:35.756+02:00\",\"dueDate\":null,\"priority\":50,\"suspended\":false,\"taskDefinitionKey\":\"UserTaskWaitForTestSystem\",\"tenantId\":\"\",\"category\":null,\"formKey\":null,\"parentTaskId\":null,\"parentTaskUrl\":null,\"executionId\":\"251299\",\"executionUrl\":\"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/executions/251299\",\"processInstanceId\":\"251299\",\"processInstanceUrl\":\"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/process-instances/251299\",\"processDefinitionId\":\"testAutomationProcessIT:1:217298\",\"processDefinitionUrl\":\"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/repository/process-definitions/testAutomationProcessIT:1:217298\",\"variables\":[]}],\"total\":2,\"start\":0,\"sort\":\"id\",\"order\":\"asc\",\"size\":2}");
        doFormatResponse("{\"id\":\"252362\",\"url\":\"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/runtime/process-instances/252362\",\"businessKey\":null,\"suspended\":false,\"ended\":false,\"processDefinitionId\":\"testAutomationProcess:1:217288\",\"processDefinitionUrl\":\"http://pc10003926.verband.creditreform.de:8080/activiti-rest/service/repository/process-definitions/testAutomationProcess:1:217288\",\"activityId\":\"UserTaskPauseAllJobs\",\"variables\":[],\"tenantId\":\"\",\"completed\":false}");
    }

    private void doFormatResponse(String responseBody) {
        System.out.println("===========================================================================================");
        System.out.println(responseBody);
        System.out.println("-------------------------------------------------------------------------------------------");
        String replacedResponseBody = responseBody.replaceFirst("\\{", "\n\\{\n\t");
        replacedResponseBody = replacedResponseBody.replaceAll("\"", "");
        replacedResponseBody = replacedResponseBody.replaceAll("data:\\[],", "data:\\[],\n\t");
        replacedResponseBody = replacedResponseBody.replaceAll("data:\\[\\{", "data:\n\t\\[\n\t\t\\{\n\t\t\t");
        replacedResponseBody = replacedResponseBody.replaceAll("\\},\\{", "\n\t\t\\},\n\t\t\\{\n\t\t\t");
        replacedResponseBody = replacedResponseBody.replaceAll("\\}\\]", "\n\t\t\\}\n\t\\]\n\t");
        replacedResponseBody = replacedResponseBody.substring(0, replacedResponseBody.length() - 1);
        replacedResponseBody = String.format("Response:%s\n}", replacedResponseBody);
        System.out.println(replacedResponseBody);
        System.out.println("-------------------------------------------------------------------------------------------");
        System.out.println();
    }

    @Test
    public void testFormatRequest() {
        doFormatRequest("GET", "{\"executionId\":\"252386\"}");
    }

    private void doFormatRequest(String httpMethod, String stringEntity) {
        String methodName = "doFormatRequest";
        System.out.println("===========================================================================================");
        System.out.println("-------------------------------------------------------------------------------------------");
        String formattedRequest = String.format("\n::%s::\nReguest:\n\tMethod:\t%s\n\tURL:\t%s\n\tParams:\t%s", methodName, httpMethod, cteActivitiServiceREST.getRestServiceInvoker().buildURI(), stringEntity);
        System.out.println(formattedRequest);
        System.out.println("-------------------------------------------------------------------------------------------");
        System.out.println();
    }
}
