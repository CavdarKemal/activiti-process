package de.creditreform.crefoteam.activiti.config;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests fuer ActivitiEnvironment.
 */
public class ActivitiEnvironmentTest {

    @Test
    public void testGetters_returnCorrectValues() {
        List<String> urls = Arrays.asList("http://server1:8080", "http://server2:8080");
        ActivitiEnvironment env = new ActivitiEnvironment("ene", urls, "user1", "pass1");

        assertEquals("ene", env.getName());
        assertEquals(urls, env.getUrls());
        assertEquals("user1", env.getUser());
        assertEquals("pass1", env.getPassword());
    }

    @Test
    public void testGetUrl_returnsFirstUrl() {
        List<String> urls = Arrays.asList("http://primary:8080", "http://secondary:8080");
        ActivitiEnvironment env = new ActivitiEnvironment("test", urls, "user", "pass");

        assertEquals("http://primary:8080", env.getUrl());
    }

    @Test
    public void testGetUrl_emptyList_returnsEmptyString() {
        ActivitiEnvironment env = new ActivitiEnvironment("test", Collections.emptyList(), "user", "pass");

        assertEquals("", env.getUrl());
    }

    @Test
    public void testGetEnvName_returnsUpperCase() {
        ActivitiEnvironment env = new ActivitiEnvironment("ene", Collections.singletonList("http://localhost"), "user", "pass");

        assertEquals("ENE", env.getEnvName());
    }

    @Test
    public void testGetMeinKey_returnsUpperCase() {
        ActivitiEnvironment env = new ActivitiEnvironment("gee", Collections.singletonList("http://localhost"), "user", "pass");

        assertEquals("GEE", env.getMeinKey());
    }

    @Test
    public void testToString_returnsName() {
        ActivitiEnvironment env = new ActivitiEnvironment("abe", Collections.singletonList("http://localhost"), "user", "pass");

        assertEquals("abe", env.toString());
    }

    @Test
    public void testUrls_areUnmodifiable() {
        List<String> urls = Arrays.asList("http://server1:8080", "http://server2:8080");
        ActivitiEnvironment env = new ActivitiEnvironment("test", urls, "user", "pass");

        try {
            env.getUrls().add("http://newserver:8080");
            fail("URLs sollten unveraenderlich sein");
        } catch (UnsupportedOperationException e) {
            // Erwartet
        }
    }

    @Test
    public void testSingleUrl() {
        ActivitiEnvironment env = new ActivitiEnvironment("local", Collections.singletonList("http://localhost:9090"), "kermit", "kermit");

        assertEquals(1, env.getUrls().size());
        assertEquals("http://localhost:9090", env.getUrl());
        assertEquals("LOCAL", env.getEnvName());
    }
}
