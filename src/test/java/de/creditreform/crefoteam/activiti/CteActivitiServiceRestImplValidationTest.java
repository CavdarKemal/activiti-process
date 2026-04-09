package de.creditreform.crefoteam.activiti;

import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;
import org.junit.Before;
import org.junit.Test;

import static de.creditreform.crefoteam.activiti.ActivitiJunitConstants.*;

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
}
