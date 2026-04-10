package de.creditreform.crefoteam.activiti.gui.view;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.*;

/**
 * Tests fuer ActivitiProcessController.
 * Verifiziert Fix #8: kein leerer catch-Block in checkExistingProcess.
 */
public class ActivitiProcessControllerTest {

    // =======================================================================
    // Fix #8: Leerer catch-Block in checkExistingProcess
    // =======================================================================

    @Test
    public void testCheckExistingProcess_keinLeererCatchBlock() throws Exception {
        File src = new File("src/main/java/de/creditreform/crefoteam/activiti/gui/view/ActivitiProcessController.java");
        String content = new String(Files.readAllBytes(src.toPath()));
        assertFalse("checkExistingProcess darf keinen leeren catch (Exception ignore) mehr haben",
                content.contains("} catch (Exception ignore) {"));
    }
}
