package de.creditreform.crefoteam.activiti.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests fuer ActivitiEnvironmentManager.
 */
public class ActivitiEnvironmentManagerTest {

    private File testPropertiesFile;
    private File testPropertiesFile2;

    @Before
    public void setUp() {
        // Aufräumen vor dem Test
        cleanupTestFiles();
    }

    @After
    public void tearDown() {
        cleanupTestFiles();
    }

    private void cleanupTestFiles() {
        testPropertiesFile = new File(System.getProperty("user.dir"), "junit-activiti.properties");
        testPropertiesFile2 = new File(System.getProperty("user.dir"), "junit2-activiti.properties");
        if (testPropertiesFile.exists()) {
            testPropertiesFile.delete();
        }
        if (testPropertiesFile2.exists()) {
            testPropertiesFile2.delete();
        }
    }

    @Test
    public void testGetDefault_returnsLocalEnvironment() {
        ActivitiEnvironment defaultEnv = ActivitiEnvironmentManager.getDefault();

        assertNotNull(defaultEnv);
        assertEquals("local", defaultEnv.getName());
        assertEquals("http://localhost:9091", defaultEnv.getUrl());
        assertEquals("kermit", defaultEnv.getUser());
        assertEquals("kermit", defaultEnv.getPassword());
    }

    @Test
    public void testLoad_nonExistingFile_returnsDefault() {
        ActivitiEnvironment env = ActivitiEnvironmentManager.load("nicht-vorhanden");

        assertEquals(ActivitiEnvironmentManager.getDefault().getUrl(), env.getUrl());
    }

    @Test
    public void testLoad_existingFile_returnsConfiguredEnvironment() throws IOException {
        // Properties-Datei erstellen
        try (FileWriter writer = new FileWriter(testPropertiesFile)) {
            writer.write("activiti.url=http://testserver:9090\n");
            writer.write("activiti.user=testuser\n");
            writer.write("activiti.password=testpass\n");
        }

        ActivitiEnvironment env = ActivitiEnvironmentManager.load("junit");

        assertEquals("junit", env.getName());
        assertEquals("http://testserver:9090", env.getUrl());
        assertEquals("testuser", env.getUser());
        assertEquals("testpass", env.getPassword());
        assertEquals("JUNIT", env.getEnvName());
    }

    @Test
    public void testLoad_multipleUrls_parsesCorrectly() throws IOException {
        try (FileWriter writer = new FileWriter(testPropertiesFile)) {
            writer.write("activiti.url=http://server1:8080;;http://server2:8080;;http://server3:8080\n");
            writer.write("activiti.user=user\n");
            writer.write("activiti.password=pass\n");
        }

        ActivitiEnvironment env = ActivitiEnvironmentManager.load("junit");

        assertEquals(3, env.getUrls().size());
        assertEquals("http://server1:8080", env.getUrls().get(0));
        assertEquals("http://server2:8080", env.getUrls().get(1));
        assertEquals("http://server3:8080", env.getUrls().get(2));
    }

    @Test
    public void testLoad_partialConfig_usesDefaults() throws IOException {
        try (FileWriter writer = new FileWriter(testPropertiesFile)) {
            writer.write("activiti.url=http://custom:8080\n");
            // user und password fehlen
        }

        ActivitiEnvironment env = ActivitiEnvironmentManager.load("junit");

        assertEquals("http://custom:8080", env.getUrl());
        assertEquals("kermit", env.getUser()); // Default
        assertEquals("kermit", env.getPassword()); // Default
    }

    @Test
    public void testFindEnvironmentNames_findsPropertiesFiles() throws IOException {
        // Zwei Test-Dateien erstellen
        try (FileWriter writer = new FileWriter(testPropertiesFile)) {
            writer.write("activiti.url=http://localhost:8080\n");
        }
        try (FileWriter writer = new FileWriter(testPropertiesFile2)) {
            writer.write("activiti.url=http://localhost:8081\n");
        }

        List<String> names = ActivitiEnvironmentManager.findEnvironmentNames();

        assertTrue("junit sollte in der Liste sein", names.contains("junit"));
        assertTrue("junit2 sollte in der Liste sein", names.contains("junit2"));
    }

    @Test
    public void testLoad_urlsWithWhitespace_trimmed() throws IOException {
        try (FileWriter writer = new FileWriter(testPropertiesFile)) {
            writer.write("activiti.url=  http://server1:8080  ;;  http://server2:8080  \n");
            writer.write("activiti.user=user\n");
            writer.write("activiti.password=pass\n");
        }

        ActivitiEnvironment env = ActivitiEnvironmentManager.load("junit");

        assertEquals(2, env.getUrls().size());
        assertEquals("http://server1:8080", env.getUrls().get(0));
        assertEquals("http://server2:8080", env.getUrls().get(1));
    }

    @Test
    public void testLoad_emptyUrl_handledGracefully() throws IOException {
        try (FileWriter writer = new FileWriter(testPropertiesFile)) {
            writer.write("activiti.url=\n");
            writer.write("activiti.user=user\n");
            writer.write("activiti.password=pass\n");
        }

        ActivitiEnvironment env = ActivitiEnvironmentManager.load("junit");

        assertNotNull(env.getUrls());
        assertFalse(env.getUrls().isEmpty());
    }
}
