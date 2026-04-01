package de.creditreform.crefoteam.activiti.gui;

import de.creditreform.crefoteam.activiti.gui.design.ActivitProcessTester;
import de.creditreform.crefoteam.activiti.gui.view.ActivitProzessMonitorView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * MDI-Hauptfenster: Verwaltet mehrere ActivitProzessMonitorView-InternalFrames.
 * Jedes InternalFrame laeuft mit eigenem Worker-Thread und eigener Umgebungsauswahl.
 */
public class ActivitProcessTesterMainFrame extends ActivitProcessTester {

    private final JDesktopPane desktopPane = new JDesktopPane();
    private final List<JInternalFrame> internalFrames = new ArrayList<>();
    private int frameCounter = 0;

    // Toolbar-Buttons
    private JButton buttonNewMonitor;
    private JButton buttonTileHorizontal;
    private JButton buttonTileVertical;
    private JButton buttonCascade;

    public ActivitProcessTesterMainFrame() {
        super();
        initControls();
        initListeners();
    }

    private void initControls() {
        setTitle("Activiti 6 - Prozess-Tester (MDI)");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        buttonNewMonitor = new JButton("Neuer Monitor");
        buttonNewMonitor.setIcon(loadIcon("/icons/book_open.png"));
        toolBar.add(buttonNewMonitor);
        toolBar.addSeparator();

        buttonTileHorizontal = new JButton("Horizontal");
        buttonTileHorizontal.setToolTipText("Fenster horizontal anordnen");
        toolBar.add(buttonTileHorizontal);

        buttonTileVertical = new JButton("Vertikal");
        buttonTileVertical.setToolTipText("Fenster vertikal anordnen");
        toolBar.add(buttonTileVertical);

        buttonCascade = new JButton("Kaskade");
        buttonCascade.setToolTipText("Fenster kaskadieren");
        toolBar.add(buttonCascade);

        // Layout
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolBar, BorderLayout.NORTH);

        desktopPane.setBackground(new Color(60, 63, 65));
        getContentPane().add(desktopPane, BorderLayout.CENTER);

        // Ersten Monitor automatisch oeffnen
        addNewMonitor();
    }

    private void initListeners() {
        buttonNewMonitor.addActionListener(e -> addNewMonitor());
        buttonTileHorizontal.addActionListener(e -> tileHorizontal());
        buttonTileVertical.addActionListener(e -> tileVertical());
        buttonCascade.addActionListener(e -> cascade());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });
    }

    /**
     * Fuegt ein neues Monitor-InternalFrame hinzu.
     */
    public void addNewMonitor() {
        frameCounter++;
        String title = "Prozess-Monitor #" + frameCounter;

        ActivitProzessMonitorView monitorView = new ActivitProzessMonitorView();

        JInternalFrame internalFrame = new JInternalFrame(title, true, true, true, true);
        internalFrame.setContentPane(monitorView);
        internalFrame.setSize(1100, 750);
        internalFrame.setVisible(true);

        // Kaskadiert positionieren
        int offset = (frameCounter - 1) * 30;
        internalFrame.setLocation(offset % 200, offset % 150);

        internalFrame.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent e) {
                monitorView.shutdown();
                internalFrames.remove(internalFrame);
            }
        });

        desktopPane.add(internalFrame);
        internalFrames.add(internalFrame);

        try {
            internalFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException ex) {
            // ignorieren
        }
    }

    private void onExit() {
        boolean hasRunning = internalFrames.stream()
                .map(f -> (ActivitProzessMonitorView) f.getContentPane())
                .anyMatch(ActivitProzessMonitorView::isRunning);

        if (hasRunning) {
            int option = JOptionPane.showConfirmDialog(this,
                    "Es laufen noch Prozess-Monitore.\n" +
                    "Die Prozesse bleiben auf Activiti erhalten.\n\n" +
                    "Wirklich beenden?",
                    "Beenden", JOptionPane.YES_NO_OPTION);
            if (option != JOptionPane.YES_OPTION) return;
        }

        for (JInternalFrame frame : new ArrayList<>(internalFrames)) {
            ActivitProzessMonitorView view = (ActivitProzessMonitorView) frame.getContentPane();
            view.shutdown();
        }

        dispose();
        System.exit(0);
    }

    // ============================================= Fenster-Anordnung =============================================

    private void tileHorizontal() {
        JInternalFrame[] frames = getVisibleFrames();
        if (frames.length == 0) return;
        int height = desktopPane.getHeight() / frames.length;
        int y = 0;
        for (JInternalFrame frame : frames) {
            try { frame.setMaximum(false); } catch (Exception e) { }
            frame.setBounds(0, y, desktopPane.getWidth(), height);
            y += height;
        }
    }

    private void tileVertical() {
        JInternalFrame[] frames = getVisibleFrames();
        if (frames.length == 0) return;
        int width = desktopPane.getWidth() / frames.length;
        int x = 0;
        for (JInternalFrame frame : frames) {
            try { frame.setMaximum(false); } catch (Exception e) { }
            frame.setBounds(x, 0, width, desktopPane.getHeight());
            x += width;
        }
    }

    private void cascade() {
        JInternalFrame[] frames = getVisibleFrames();
        int offset = 0;
        for (JInternalFrame frame : frames) {
            try { frame.setMaximum(false); } catch (Exception e) { }
            frame.setBounds(offset, offset, 900, 600);
            offset += 30;
            try { frame.setSelected(true); } catch (Exception e) { }
        }
    }

    private JInternalFrame[] getVisibleFrames() {
        return internalFrames.stream()
                .filter(f -> f.isVisible() && !f.isClosed())
                .toArray(JInternalFrame[]::new);
    }

    private ImageIcon loadIcon(String path) {
        java.net.URL url = getClass().getResource(path);
        return url != null ? new ImageIcon(url) : null;
    }

    // ============================================= Main =============================================

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {
        }
        SwingUtilities.invokeLater(() -> new ActivitProcessTesterMainFrame().setVisible(true));
    }
}
