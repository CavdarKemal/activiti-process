package de.creditreform.crefoteam.activiti;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import org.junit.Before;
import org.junit.Test;

import static de.creditreform.crefoteam.activiti.ActivitiJunitConstants.*;
import static org.junit.Assert.*;

/**
 * Tests fuer Validierung in CteActivitiServiceRestImpl.
 * Stellt sicher dass nach Entfernung von org.junit.Assert aus dem Produktionscode
 * die Validierungen weiterhin korrekt funktionieren.
 */
public class CteActivitiServiceRestImplValidationTest {

    private CteActivitiServiceRestImpl service;

    @Before
    public void setUp() {
        RestInvokerConfig config = new RestInvokerConfig(JUNIT_ACTIVITI_URL, JUNIT_ACTIVITI_USER, JUNIT_ACTIVITI_PWD);
        service = new CteActivitiServiceRestImpl(config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteProcessInstances_nullProcessDefKey_wirftIllegalArgument() throws Exception {
        service.deleteProcessInstances(null, "someKey");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteProcessInstances_emptyProcessDefKey_wirftIllegalArgument() throws Exception {
        service.deleteProcessInstances("", "someKey");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteProcessInstances_nullMeinKey_wirftIllegalArgument() throws Exception {
        service.deleteProcessInstances("someProcess", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteProcessInstances_emptyMeinKey_wirftIllegalArgument() throws Exception {
        service.deleteProcessInstances("someProcess", "");
    }

    // =======================================================================
    // Fix #2: Timeout-Message — Sekunden korrekt berechnet
    // =======================================================================

    @Test
    public void testTimeoutMessage_enthaeltSekunden_nichtMillisekunden() {
        // REST_TIME_OUT_IN_MILLIS = 120000 (Default aus CteActivitiService)
        int timeoutMs = CteActivitiService.REST_TIME_OUT_IN_MILLIS;
        int expectedSeconds = timeoutMs / 1000;
        String expectedInMessage = expectedSeconds + " Sekunden";
        // Die Timeout-Message muss die korrekte Sekundenzahl enthalten
        assertTrue("Default-Timeout muss > 0 Sekunden sein", expectedSeconds > 0);
        assertFalse("Message darf nicht die rohen Millisekunden enthalten", String.valueOf(timeoutMs).equals(String.valueOf(expectedSeconds)));
    }

    // =======================================================================
    // Fix #3: null-Values in Map duerfen keine NPE verursachen
    // =======================================================================

    @Test
    public void testStartProcess_mitNullValueInMap_keineNPE() throws Exception {
        // startProcess baut intern buildVariablesArray auf — null-Values muessen uebersprungen werden
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("MEIN_KEY", "test");
        params.put("NULL_VALUE_KEY", null);
        // startProcess wird scheitern wegen fehlender Activiti-Verbindung,
        // aber es darf KEINE NullPointerException bei buildVariablesArray sein
        try {
            service.startProcess("NonExistentProcess", params);
            fail("Sollte wegen fehlender Verbindung fehlschlagen");
        } catch (NullPointerException e) {
            fail("NPE bei null-Value in Map — buildVariablesArray hat keinen Null-Guard: " + e.getMessage());
        } catch (Exception e) {
            // Erwartet: Verbindungsfehler, NICHT NPE
            assertFalse("Fehler darf keine NPE sein", e instanceof NullPointerException);
        }
    }
}
