package de.creditreform.crefoteam.activiti;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import de.creditreform.crefoteam.cte.rest.RestInvoker;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import de.creditreform.crefoteam.cte.rest.RestInvokerResponse;
import org.activiti.rest.service.api.RestUrls;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import static de.creditreform.crefoteam.activiti.ActivitiJunitConstants.*;



public class CteActivitiRestInvokerMockTest {
    protected static RestInvokerConfig restInvokerConfig = new RestInvokerConfig("http://127.0.0.1:8090/activiti-rest/service", JUNIT_ACTIVITI_USER, JUNIT_ACTIVITI_PWD);
    protected static RestInvokerActiviti restServiceInvoker = new RestInvokerActiviti(restInvokerConfig.getServiceURL(), restInvokerConfig.getServiceUser(), restInvokerConfig.getServicePassword());

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig().port(8090));

    @Test
    public void testInvokeGet() {
        String path = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_DEFINITION_COLLECTION);
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(restInvokerConfig.getServicePath() + "/" + path)).willReturn(WireMock.aResponse().withStatus(200).withBody("blabla")));

        restServiceInvoker.init(Integer.valueOf(1000));
        restServiceInvoker.appendPath(RestUrls.URL_PROCESS_DEFINITION_COLLECTION);

        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokeGet(RestInvoker.CONTENT_TYPE_JSON);
        Assert.assertNotNull(restInvokerResponse);
        Assert.assertEquals("blabla", restInvokerResponse.getResponseBody());
        Assert.assertEquals(200, restInvokerResponse.getStatusCode());
    }

    @Test
    public void testInvokePut() {
        String path = RestUrls.createRelativeResourceUrl(RestUrls.URL_USER_COLLECTION) + "/" + JUNIT_ACTIVITI_USER;
        WireMock.stubFor(WireMock.put(WireMock.urlEqualTo(restInvokerConfig.getServicePath() + "/" + path)).willReturn(WireMock.aResponse().withStatus(200).withBody("erledigt")));

        restServiceInvoker.init(Integer.valueOf(1000));
        restServiceInvoker.appendPath(RestUrls.URL_USER, JUNIT_ACTIVITI_USER);
        String stringEntity = "{\"firstName\":\"Kemal\"}";
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokePut(stringEntity, RestInvoker.CONTENT_TYPE_JSON);
        Assert.assertNotNull(restInvokerResponse);
        Assert.assertEquals("erledigt", restInvokerResponse.getResponseBody());
        Assert.assertEquals(200, restInvokerResponse.getStatusCode());
    }

    @Test
    public void testInvokePost() {
        String path = RestUrls.createRelativeResourceUrl(RestUrls.URL_PROCESS_INSTANCE_QUERY);
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo(restInvokerConfig.getServicePath() + "/" + path)).willReturn(WireMock.aResponse().withStatus(200).withBody("aha!")));

        restServiceInvoker.init(Integer.valueOf(1000));
        restServiceInvoker.appendPath(RestUrls.URL_PROCESS_INSTANCE_QUERY);
        String stringEntity = "{\"processDefinitionKey\":\"oneTaskProcess\"}";
        RestInvokerResponse restInvokerResponse = restServiceInvoker.invokePost(stringEntity, RestInvoker.CONTENT_TYPE_JSON);
        Assert.assertNotNull(restInvokerResponse);
        Assert.assertEquals("aha!", restInvokerResponse.getResponseBody());
        Assert.assertEquals(200, restInvokerResponse.getStatusCode());
    }
}
