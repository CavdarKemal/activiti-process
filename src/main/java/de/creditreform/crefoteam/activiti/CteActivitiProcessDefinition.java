package de.creditreform.crefoteam.activiti;

/**
 * Process-Definition
 * {
 * "id":"testAutomationProcess:2:16722"
 * "url":"http://rhsctew002.ecofis.de:8080/activiti-rest/service/repository/process-definitions/testAutomationProcess:2:16722"
 * "key":"testAutomationProcess"
 * "version":2
 * "name":"CTE Test-Automatisierung"
 * "description":null
 * "tenantId":""
 * "deploymentId":"16719"
 * "deploymentUrl":"http://rhsctew002.ecofis.de:8080/activiti-rest/service/repository/deployments/16719"
 * "resource":"http://rhsctew002.ecofis.de:8080/activiti-rest/service/repository/deployments/16719/resources/JUNITTestProcess.bpmn"
 * "diagramResource":"http://rhsctew002.ecofis.de:8080/activiti-rest/service/repository/deployments/16719/resources/JUNITTestProcess.testAutomationProcess.png"
 * "category":"http://www.activiti.org/test"
 * "graphicalNotationDefined":true
 * "suspended":false
 * "startFormDefined":true
 * }
 */
public interface CteActivitiProcessDefinition {
    String getId();

    String getKey();

    String getName();

    String getUrl();

}
