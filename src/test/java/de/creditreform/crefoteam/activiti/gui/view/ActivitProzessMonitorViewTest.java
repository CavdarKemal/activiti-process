package de.creditreform.crefoteam.activiti.gui.view;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class ActivitProzessMonitorViewTest {

    private ActivitProzessMonitorView view;

    @BeforeClass
    public static void setUpHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Before
    public void setUp() {
        view = new ActivitProzessMonitorView();
    }

    @Test
    public void testInstanziierung() {
        assertNotNull(view);
    }

    @Test
    public void testIsRunning_anfangsFalse() {
        assertFalse(view.isRunning());
    }

    @Test
    public void testShutdown_keineException() {
        view.shutdown();
        assertFalse(view.isRunning());
    }

    @Test
    public void testOnLog_keineException() {
        view.onLog("Test Nachricht");
    }

    @Test
    public void testOnStatus_keineException() {
        view.onStatus("Test Status");
    }

    @Test
    public void testOnProcessImageUpdate_nullId_keineException() {
        view.onProcessImageUpdate(null, 1, "test");
    }

    @Test
    public void testOnExistingProcessFound_headless_gibtAbbruchZurueck() {
        int result = view.onExistingProcessFound(1, "TEST-KEY", "");
        assertEquals("Headless-Modus sollte 2 (Abbrechen) liefern", 2, result);
    }
}
