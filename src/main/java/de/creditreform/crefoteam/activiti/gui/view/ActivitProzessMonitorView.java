package de.creditreform.crefoteam.activiti.gui.view;

import de.creditreform.crefoteam.activiti.CteActivitiServiceRestImpl;
import de.creditreform.crefoteam.activiti.config.ActivitiEnvironment;
import de.creditreform.crefoteam.activiti.config.ActivitiEnvironmentManager;
import de.creditreform.crefoteam.activiti.gui.design.ActivitProzessMonitor;
import de.creditreform.crefoteam.cte.rest.RestInvokerConfig;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Prozess-Monitor: Zeigt ein Activiti-Prozess-Diagramm und steuert den Prozess.
 * Jede Instanz laeuft mit eigenem Worker-Thread (multithreaded MDI).
 * Jede Instanz hat ihre eigene Umgebungsauswahl (comboBoxEnvironment + comboBoxActivitiHost).
 * <p>
 * Die gesamte Prozess-Logik ist in {@link ActivitiProcessController} ausgelagert.
 */
public class ActivitProzessMonitorView extends ActivitProzessMonitor implements ActivitiProcessCallback {

    // GUI-Komponenten
    private final JLabel processImageLabel = new JLabel();
    private final JTextArea logArea = new JTextArea();
    private final JLabel statusLabel = new JLabel("Bereit");
    private final JProgressBar progressBar = new JProgressBar();
    private JPanel diagramPanel;

    private ActivitiEnvironment currentEnvironment;
    private final ActivitiProcessController controller = new ActivitiProcessController(this);
    private Thread workerThread;

    public ActivitProzessMonitorView() {
        super();
        initControls();
        initListeners();
    }

    private void initControls() {
        initEnvironmentSelector();

        JPanel monitorPanel = getPanelProcessMonitor();
        monitorPanel.setLayout(new BorderLayout(5, 5));

        diagramPanel = new JPanel(new BorderLayout());
        diagramPanel.setBorder(new TitledBorder("Prozess-Diagramm"));
        processImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        processImageLabel.setText("Kein Prozess aktiv");
        JScrollPane imageScroll = new JScrollPane(processImageLabel);
        diagramPanel.add(imageScroll, BorderLayout.CENTER);
        monitorPanel.add(diagramPanel, BorderLayout.CENTER);

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(new TitledBorder("Protokoll"));
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        logArea.setRows(8);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(0, 180));
        logPanel.add(logScroll, BorderLayout.CENTER);
        monitorPanel.add(logPanel, BorderLayout.SOUTH);

        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        progressBar.setPreferredSize(new Dimension(150, 16));
        progressBar.setIndeterminate(false);
        statusBar.add(statusLabel, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);
        monitorPanel.add(statusBar, BorderLayout.PAGE_END);

        getButtonStopUserTasksThread().setEnabled(false);
    }

    private void initEnvironmentSelector() {
        JComboBox<String> envCombo = getComboBoxEnvironment();
        List<String> names = ActivitiEnvironmentManager.findEnvironmentNames();
        envCombo.removeAllItems();
        if (names.isEmpty()) {
            currentEnvironment = ActivitiEnvironmentManager.getDefault();
            envCombo.addItem(currentEnvironment.getName());
        } else {
            for (String name : names) {
                envCombo.addItem(name);
            }
            currentEnvironment = ActivitiEnvironmentManager.load(names.get(0));
        }
        refreshHostCombo(currentEnvironment);

        envCombo.addActionListener(e -> {
            String selected = (String) envCombo.getSelectedItem();
            if (selected != null) {
                currentEnvironment = ActivitiEnvironmentManager.load(selected);
                refreshHostCombo(currentEnvironment);
            }
        });
    }

    private void refreshHostCombo(ActivitiEnvironment env) {
        JComboBox hostCombo = getComboBoxActivitiHost();
        hostCombo.removeAllItems();
        for (String url : env.getUrls()) {
            hostCombo.addItem(url);
        }
    }

    private void initListeners() {
        getButtonStartProcess().addActionListener(e -> onStart());
        getButtonStopUserTasksThread().addActionListener(e -> controller.stop());
    }

    // ============================================= Aktionen =============================================

    public boolean isRunning() {
        return controller.isRunning();
    }

    public void shutdown() {
        controller.stop();
    }

    private CteActivitiServiceRestImpl createService() {
        String selectedUrl = (String) getComboBoxActivitiHost().getSelectedItem();
        String url = (selectedUrl != null && !selectedUrl.isEmpty()) ? selectedUrl : currentEnvironment.getUrl();
        RestInvokerConfig config = new RestInvokerConfig(url, currentEnvironment.getUser(), currentEnvironment.getPassword());
        return new CteActivitiServiceRestImpl(config);
    }

    private void onStart() {
        String meinKey = currentEnvironment.getMeinKey();

        setInputEnabled(false);
        getButtonStopUserTasksThread().setEnabled(true);
        progressBar.setIndeterminate(true);

        workerThread = new Thread(() -> {
            try {
                CteActivitiServiceRestImpl service = createService();
                controller.run(service, meinKey, currentEnvironment.getUser(), currentEnvironment.getEnvName());
            } catch (Exception ex) {
                if (controller.isRunning()) {
                    onLog("FEHLER: " + ex.getMessage());
                }
            } finally {
                SwingUtilities.invokeLater(() -> {
                    setInputEnabled(true);
                    getButtonStopUserTasksThread().setEnabled(false);
                    progressBar.setIndeterminate(false);
                });
            }
        }, "MonitorWorker-" + meinKey);
        workerThread.setDaemon(true);
        workerThread.start();
    }

    // ============================================= ActivitiProcessCallback =============================================

    @Override
    public void onLog(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String line = String.format("[%s] %s", timestamp, message);
        SwingUtilities.invokeLater(() -> {
            logArea.append(line + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    @Override
    public void onStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    @Override
    public void onProcessImageUpdate(Integer imageProcessId, Integer mainProcessId, String testPhase) {
        if (imageProcessId == null) return;
        try {
            CteActivitiServiceRestImpl service = createService();
            InputStream imageStream = service.getProcessImage(imageProcessId);
            if (imageStream != null) {
                BufferedImage image = ImageIO.read(imageStream);
                imageStream.close();
                if (image != null) {
                    boolean isSub = !imageProcessId.equals(mainProcessId);
                    SwingUtilities.invokeLater(() -> {
                        processImageLabel.setIcon(new ImageIcon(image));
                        processImageLabel.setText(null);
                        if (diagramPanel.getBorder() instanceof TitledBorder) {
                            String title;
                            if (isSub && testPhase != null) {
                                title = String.format("Prozess-Diagramm (Sub-Prozess %d - %s)", imageProcessId, testPhase);
                            } else if (isSub) {
                                title = String.format("Prozess-Diagramm (Sub-Prozess %d)", imageProcessId);
                            } else {
                                title = "Prozess-Diagramm (Hauptprozess)";
                            }
                            ((TitledBorder) diagramPanel.getBorder()).setTitle(title);
                            diagramPanel.repaint();
                        }
                    });
                }
            }
        } catch (Exception e) {
            // Bild nicht verfuegbar
        }
    }

    @Override
    public int onExistingProcessFound(int count, String meinKey, String currentTaskInfo) {
        Object[] options = {"Loeschen & Neu starten", "Fortsetzen", "Abbrechen"};
        int[] result = {2};
        try {
            SwingUtilities.invokeAndWait(() ->
                result[0] = JOptionPane.showOptionDialog(
                        SwingUtilities.getWindowAncestor(this),
                        String.format("Es laufen %d Prozess-Instanz(en) mit Key '%s'.%s\n\nWas moechten Sie tun?",
                                count, meinKey, currentTaskInfo),
                        "Laufender Prozess gefunden",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, options, options[1])
            );
        } catch (Exception e) {
            // Abbruch bei Fehler
        }
        return result[0];
    }

    // ============================================= GUI-Hilfsmethoden =============================================

    private void setInputEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            getComboBoxEnvironment().setEnabled(enabled);
            getComboBoxActivitiHost().setEnabled(enabled);
            getButtonStartProcess().setEnabled(enabled);
        });
    }
}
