package de.creditreform.crefoteam.activiti;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests fuer RestInvokerActiviti — insbesondere Sicherheits-Aspekte.
 */
public class RestInvokerActivitiTest {

    @Test
    public void testToString_enthaeltKeinPasswort() {
        String secretPassword = "MySuperSecretPassword123!";
        RestInvokerActiviti invoker = new RestInvokerActiviti("http://localhost:9090", "kermit", secretPassword);
        String str = invoker.toString();
        assertFalse("toString() darf das Passwort nicht im Klartext enthalten", str.contains(secretPassword));
    }

    @Test
    public void testToString_enthaeltMaskierung() {
        RestInvokerActiviti invoker = new RestInvokerActiviti("http://localhost:9090", "kermit", "geheim");
        String str = invoker.toString();
        assertTrue("toString() muss die Maskierung *** enthalten", str.contains("***"));
    }

    @Test
    public void testToString_enthaeltUserNameUndUrl() {
        RestInvokerActiviti invoker = new RestInvokerActiviti("http://localhost:9090", "kermit", "geheim");
        String str = invoker.toString();
        assertTrue("toString() muss den Username enthalten", str.contains("kermit"));
        assertTrue("toString() muss die URL enthalten", str.contains("localhost:9090"));
    }
}
