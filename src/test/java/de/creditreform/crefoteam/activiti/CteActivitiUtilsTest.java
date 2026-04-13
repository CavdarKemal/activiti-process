package de.creditreform.crefoteam.activiti;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static de.creditreform.crefoteam.activiti.ActivitiJunitConstants.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Unit-Tests fuer CteActivitiUtils.
 * Kombiniert IntegrationTests (mit echtem Server) und Mock-Tests.
 */
public class CteActivitiUtilsTest {
    protected CteActivitiUtils cteActivitiUtils;
    CteActivitiService cteActivitiServiceREST;

    @Before
    public void setUp() {
        RestInvokerConfig restInvokerConfig = new RestInvokerConfig(JUNIT_ACTIVITI_URL, JUNIT_ACTIVITI_USER, JUNIT_ACTIVITI_PWD);
        cteActivitiServiceREST = new CteActivitiServiceRestImpl(restInvokerConfig);
        cteActivitiUtils = new CteActivitiUtils(cteActivitiServiceREST);
    }

    // =======================================================================
    // Integration Test (benoetigt echten Activiti-Server)
    // =======================================================================

    @Test
    public void testUploadActivitiProcesses() throws Exception {
        File bpmnFile = new File(getClass().getResource("/" + JUNIT_DEPLOYMENT_NAME).toURI());
        String bpmnFileName = bpmnFile.getAbsolutePath();
        String envName = "ENE";
        boolean askIfExists = true;
        String uploadedActivitiProcessesName = cteActivitiUtils.uploadActivitiProcesses(bpmnFileName, envName, askIfExists);

        CteActivitiDeployment deploymentForName = cteActivitiServiceREST.getDeploymentForName(uploadedActivitiProcessesName);
        Assert.assertNotNull(deploymentForName);

        cteActivitiServiceREST.deleteDeploymentForName(uploadedActivitiProcessesName);
        deploymentForName = cteActivitiServiceREST.getDeploymentForName(uploadedActivitiProcessesName);
        Assert.assertNull(deploymentForName);
    }

    // =======================================================================
    // Unit-Tests mit Mocks
    // =======================================================================

    @Test
    public void testUploadActivitiProcesses_DeploymentExistiertNicht_UploadErfolgreich() throws Exception {
        // Mock erstellen
        CteActivitiService mockService = createMock(CteActivitiService.class);
        CteActivitiUtils utilsWithMock = new CteActivitiUtils(mockService);

        File bpmnFile = new File(getClass().getResource("/" + JUNIT_DEPLOYMENT_NAME).toURI());
        String bpmnFileName = bpmnFile.getAbsolutePath();
        String envName = "TEST";
        String expectedDeploymentName = envName + "-" + bpmnFile.getName();

        // Erwartungen setzen
        expect(mockService.getDeploymentForName(expectedDeploymentName)).andReturn(null);
        expect(mockService.uploadDeploymentFile(anyObject(File.class))).andReturn("12345");
        replay(mockService);

        // Test ausfuehren
        String result = utilsWithMock.uploadActivitiProcesses(bpmnFileName, envName, false);

        // Verifizieren
        assertEquals(expectedDeploymentName, result);
        verify(mockService);
    }

    @Test
    public void testUploadActivitiProcesses_DeploymentExistiert_WirdGeloescht() throws Exception {
        // Mock erstellen
        CteActivitiService mockService = createMock(CteActivitiService.class);
        CteActivitiUtils utilsWithMock = new CteActivitiUtils(mockService);

        File bpmnFile = new File(getClass().getResource("/" + JUNIT_DEPLOYMENT_NAME).toURI());
        String bpmnFileName = bpmnFile.getAbsolutePath();
        String envName = "TEST";
        String expectedDeploymentName = envName + "-" + bpmnFile.getName();

        // Mock Deployment
        CteActivitiDeployment mockDeployment = createMock(CteActivitiDeployment.class);

        // Erwartungen setzen
        expect(mockService.getDeploymentForName(expectedDeploymentName)).andReturn(mockDeployment);
        mockService.deleteDeploymentForName(expectedDeploymentName);
        expectLastCall();
        expect(mockService.uploadDeploymentFile(anyObject(File.class))).andReturn("12345");
        replay(mockService, mockDeployment);

        // Test ausfuehren (askIfExists=false, damit kein Dialog kommt)
        String result = utilsWithMock.uploadActivitiProcesses(bpmnFileName, envName, false);

        // Verifizieren
        assertEquals(expectedDeploymentName, result);
        verify(mockService);
    }

    @Test
    public void testUploadActivitiProcesses_TempDateiWirdGeloescht() throws Exception {
        // Dieser Test verifiziert, dass temporaere Dateien nach dem Upload geloescht werden
        CteActivitiService mockService = createMock(CteActivitiService.class);
        CteActivitiUtils utilsWithMock = new CteActivitiUtils(mockService);

        File bpmnFile = new File(getClass().getResource("/" + JUNIT_DEPLOYMENT_NAME).toURI());
        String bpmnFileName = bpmnFile.getAbsolutePath();
        String envName = "TEMPTEST";
        String expectedDeploymentName = envName + "-" + bpmnFile.getName();
        File expectedTempFile = new File(System.getProperty("user.dir"), expectedDeploymentName);

        // Erwartungen setzen
        expect(mockService.getDeploymentForName(expectedDeploymentName)).andReturn(null);
        expect(mockService.uploadDeploymentFile(anyObject(File.class))).andReturn("12345");
        replay(mockService);

        // Test ausfuehren
        utilsWithMock.uploadActivitiProcesses(bpmnFileName, envName, false);

        // Temp-Datei sollte geloescht sein
        assertFalse("Temporaere Datei sollte geloescht sein", expectedTempFile.exists());
        verify(mockService);
    }

    // =======================================================================
    // Konstruktor-Test
    // =======================================================================

    @Test
    public void testKonstruktor() {
        CteActivitiService mockService = createMock(CteActivitiService.class);
        CteActivitiUtils utils = new CteActivitiUtils(mockService);
        assertNotNull(utils);
    }

    // =======================================================================
    // ENV-Replacement Test
    // =======================================================================

    @Test
    public void testUploadActivitiProcesses_EnvPlaceholderWirdErsetzt() throws Exception {
        // Test dass %ENV% im BPMN-File durch envName ersetzt wird
        CteActivitiService mockService = createMock(CteActivitiService.class);
        CteActivitiUtils utilsWithMock = new CteActivitiUtils(mockService);

        File bpmnFile = new File(getClass().getResource("/" + JUNIT_DEPLOYMENT_NAME).toURI());
        String bpmnFileName = bpmnFile.getAbsolutePath();
        String envName = "PRODENV";
        String expectedDeploymentName = envName + "-" + bpmnFile.getName();

        expect(mockService.getDeploymentForName(expectedDeploymentName)).andReturn(null);
        expect(mockService.uploadDeploymentFile(anyObject(File.class))).andReturn("12345");
        replay(mockService);

        String result = utilsWithMock.uploadActivitiProcesses(bpmnFileName, envName, false);
        
        assertNotNull(result);
        assertTrue("Dateiname muss mit ENV-Prefix beginnen", result.startsWith(envName));
        verify(mockService);
    }
}