/*
 * Created by JFormDesigner on Thu Mar 19 17:21:09 CET 2026
 */

package de.creditreform.crefoteam.activiti.gui.design;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author kemal
 */
public class ActivitProzessMonitor extends JPanel {
    public ActivitProzessMonitor() {
        initComponents();
    }

    public JPanel getPanelProcessControls() {
        return panelProcessControls;
    }

    public JPanel getPanelProcessMonitor() {
        return panelProcessMonitor;
    }

    public JLabel getLabelActivitiHost() {
        return labelActivitiHost;
    }

    public JComboBox getComboBoxActivitiHost() {
        return comboBoxActivitiHost;
    }

    public JButton getButtonStartProcess() {
        return buttonStartProcess;
    }

    public JButton getButtonStopUserTasksThread() {
        return buttonStopUserTasksThread;
    }

    public JComboBox getComboBoxEnvironment() {
        return comboBoxEnvironment;
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        ResourceBundle bundle = ResourceBundle.getBundle("de.cavdar.gui.design.form");
        panelProcessControls = new JPanel();
        labelEnvironment = new JLabel();
        comboBoxEnvironment = new JComboBox();
        labelActivitiHost = new JLabel();
        comboBoxActivitiHost = new JComboBox();
        buttonStartProcess = new JButton();
        buttonStopUserTasksThread = new JButton();
        panelProcessMonitor = new JPanel();

        //======== this ========
        setLayout(new BorderLayout());

        //======== panelProcessControls ========
        {
            panelProcessControls.setBorder(new EtchedBorder());
            panelProcessControls.setLayout(new GridBagLayout());
            ((GridBagLayout)panelProcessControls.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0};
            ((GridBagLayout)panelProcessControls.getLayout()).rowHeights = new int[] {0, 0, 0};
            ((GridBagLayout)panelProcessControls.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0E-4};
            ((GridBagLayout)panelProcessControls.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

            //---- labelEnvironment ----
            labelEnvironment.setText("Umgebung:");
            panelProcessControls.add(labelEnvironment, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 5), 0, 0));
            panelProcessControls.add(comboBoxEnvironment, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 7, 7), 0, 0));

            //---- labelActivitiHost ----
            labelActivitiHost.setText(bundle.getString("ActivitProzessMonitor.labelActivitiHost.text"));
            panelProcessControls.add(labelActivitiHost, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 7, 7), 0, 0));
            panelProcessControls.add(comboBoxActivitiHost, new GridBagConstraints(3, 0, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 7, 2), 0, 0));

            //---- buttonStartProcess ----
            buttonStartProcess.setText(bundle.getString("ActivitProzessMonitor.buttonStartProcess.text"));
            buttonStartProcess.setIcon(new ImageIcon(getClass().getResource("/icons/gear_run.png")));
            buttonStartProcess.setMinimumSize(new Dimension(80, 24));
            buttonStartProcess.setMaximumSize(new Dimension(120, 24));
            buttonStartProcess.setPreferredSize(new Dimension(80, 24));
            panelProcessControls.add(buttonStartProcess, new GridBagConstraints(0, 1, 4, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 7), 0, 0));

            //---- buttonStopUserTasksThread ----
            buttonStopUserTasksThread.setIcon(new ImageIcon(getClass().getResource("/icons/Stop sign.png")));
            buttonStopUserTasksThread.setActionCommand(bundle.getString("ActivitProzessMonitor.buttonStopUserTasksThread.actionCommand"));
            buttonStopUserTasksThread.setToolTipText(bundle.getString("ActivitProzessMonitor.buttonStopUserTasksThread.toolTipText"));
            buttonStopUserTasksThread.setMinimumSize(new Dimension(24, 24));
            buttonStopUserTasksThread.setMaximumSize(new Dimension(24, 24));
            buttonStopUserTasksThread.setPreferredSize(new Dimension(24, 24));
            panelProcessControls.add(buttonStopUserTasksThread, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0));
        }
        add(panelProcessControls, BorderLayout.NORTH);

        //======== panelProcessMonitor ========
        {
            panelProcessMonitor.setBorder(new EtchedBorder());
            panelProcessMonitor.setLayout(new GridBagLayout());
            ((GridBagLayout)panelProcessMonitor.getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)panelProcessMonitor.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
            ((GridBagLayout)panelProcessMonitor.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
            ((GridBagLayout)panelProcessMonitor.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
        }
        add(panelProcessMonitor, BorderLayout.CENTER);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel panelProcessControls;
    private JLabel labelEnvironment;
    private JComboBox comboBoxEnvironment;
    private JLabel labelActivitiHost;
    private JComboBox comboBoxActivitiHost;
    private JButton buttonStartProcess;
    private JButton buttonStopUserTasksThread;
    private JPanel panelProcessMonitor;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
