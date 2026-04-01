package de.creditreform.crefoteam.activiti;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CteActivitiUtils {

    protected final Logger LOGGER = LoggerFactory.getLogger(CteActivitiUtils.class);
    private final CteActivitiService activitiRestService;

    public CteActivitiUtils(CteActivitiService activitiRestService) {
        this.activitiRestService = activitiRestService;
    }

    public String uploadActivitiProcesses(String bpmnFileName, String envName, boolean askIfExists) throws Exception {
        if (!checkIfBpmnFileExists(envName, bpmnFileName, askIfExists)) {
            return null;
        }
        File newBpmnFile = prepareBpmnFileForEnvironment(bpmnFileName, envName);
        String deploymentID = activitiRestService.uploadDeploymentFile(new File(newBpmnFile.getAbsolutePath()));
        boolean delete = newBpmnFile.delete();
        if (!delete) {
            throw new RuntimeException(String.format("\nTemporäre ACTIVITI-Prozess-Definitionsdatei \n  '%s'\nkonnte nicht gelöscht werden!", newBpmnFile.getAbsolutePath()));
        }
        return newBpmnFile.getName();
    }

    public List<String> uploadActivitiProcessesFromClassPath(String envName) throws Exception {
        URL diagramsURL = getClass().getResource("/");
        if(diagramsURL == null) {
            throw new RuntimeException("!!!! getClass().getResource('/') liefert null!");
        }
        LOGGER.info("Path von DiagramsURL : " + diagramsURL.getPath());
        LOGGER.info("diagramsURL.getFile() : " + diagramsURL.getFile());
        File diagramsFile = new File(diagramsURL.getFile());
        FileFilter bpmnFilter = theFile -> theFile.getName().endsWith(".bpmn");
        List<String> uploadedBpmnList = new ArrayList<>();
        File[] bpmnFiles = diagramsFile.listFiles(bpmnFilter);
        if(bpmnFiles == null) {
            LOGGER.warn(" Keine BPMN's im Class-Path '" + diagramsURL.getFile() + "' gefunden!");
            return uploadedBpmnList;
        }
        for (File bpmnFile : bpmnFiles) {
            String processName = uploadActivitiProcesses(bpmnFile.getAbsolutePath(), envName, false);
            if (processName == null) {
                throw new RuntimeException("Der ACTIVITI-Prozess" + diagramsFile.getAbsolutePath() + " konnte nicht deployed werden!");
            }
            uploadedBpmnList.add(processName);
        }
        return uploadedBpmnList;
    }

    private File prepareBpmnFileForEnvironment(String bpmnFileName, String envName) throws Exception {
        File srcFile = new File(bpmnFileName);
        File dstFile = new File(System.getProperty("user.dir"), String.format("%s-%s", envName, srcFile.getName()));
        String oldContent = FileUtils.readFileToString(srcFile);
        String newContent = oldContent.replaceAll("%ENV%", envName);
        FileUtils.writeStringToFile(dstFile, newContent);
        return dstFile;
    }

    private boolean checkIfBpmnFileExists(String envName, String bpmnFileName, boolean askIfExists) throws Exception {
        File bpmnFile = new File(bpmnFileName);
        String envBpmnFileName = String.format("%s-%s", envName, bpmnFile.getName());
        CteActivitiDeployment cteActivitiDeployment = activitiRestService.getDeploymentForName(envBpmnFileName);
        if (cteActivitiDeployment != null) {
            if (askIfExists) {
                String questionMsg = "Das Deployment " + envBpmnFileName + " existiert bereits! Soll es ersetzt werden?";
                int option = JOptionPane.showConfirmDialog(null, questionMsg, "ACTIVITI-Prozess-Definitionsdatei deployen", JOptionPane.YES_NO_OPTION);
                if (option != JOptionPane.OK_OPTION) {
                    return false;
                }
            }
            activitiRestService.deleteDeploymentForName(envBpmnFileName);
        }
        return true;
    }

}
