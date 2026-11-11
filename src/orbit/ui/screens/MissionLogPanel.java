package orbit.ui.screens;

import orbit.controllers.OrbitController;
import orbit.models.MissionLog;
import orbit.ui.OrbitFrame;
import orbit.ui.OrbitTheme;
import orbit.ui.components.SciFiButton;
import orbit.ui.components.SciFiPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Full graphical Mission Log interface for ORBIT: Beyond the Unknown.
 * Presents the complete chronological audit trail of significant flight operations,
 * interplanetary voyages, surface expeditions, anomaly signals, hazards,
 * and mission milestones.
 */
public class MissionLogPanel extends JPanel {

    private final OrbitFrame frame;
    private final OrbitController controller;

    private JLabel totalEntriesBadge;
    private JLabel currentStageBadge;
    private JPanel logEntriesContainer;

    public MissionLogPanel(OrbitFrame frame, OrbitController controller) {
        this.frame = frame;
        this.controller = controller;

        setLayout(new BorderLayout(0, 10));
        setBackground(OrbitTheme.BG_VOID);
        setBorder(new EmptyBorder(12, 16, 12, 16));

        initHeader();
        initContent();
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout(16, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OrbitTheme.BORDER_SUBTLE),
                new EmptyBorder(4, 8, 10, 8)
        ));

        // Left Header Titles
        JPanel leftHeader = new JPanel();
        leftHeader.setOpaque(false);
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("MISSION LOG // CHRONOLOGICAL FLIGHT RECORDER");
        title.setFont(OrbitTheme.FONT_HEADER_MONO);
        title.setForeground(OrbitTheme.CYAN_ELECTRIC);

        JLabel subTitle = new JLabel("QUANTUM MEMORY CORE // AUDIT TRAIL OF EXPEDITION TELEMETRY");
        subTitle.setFont(OrbitTheme.FONT_TINY_MONO);
        subTitle.setForeground(OrbitTheme.TEXT_MUTED);

        leftHeader.add(title);
        leftHeader.add(Box.createVerticalStrut(2));
        leftHeader.add(subTitle);

        // Right Header Badges
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightHeader.setOpaque(false);

        currentStageBadge = createBadge("STAGE 1 — DEPARTURE", OrbitTheme.TEXT_CYAN);
        totalEntriesBadge = createBadge("ENTRIES: 0", OrbitTheme.STATUS_NOMINAL);

        rightHeader.add(currentStageBadge);
        rightHeader.add(totalEntriesBadge);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private JLabel createBadge(String text, Color fgColor) {
        JLabel badge = new JLabel(text);
        badge.setFont(OrbitTheme.FONT_MONO_BOLD);
        badge.setForeground(fgColor);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrbitTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
        badge.setOpaque(true);
        badge.setBackground(OrbitTheme.BG_CARD);
        return badge;
    }

    private void initContent() {
        SciFiPanel logPanel = new SciFiPanel("FLIGHT RECORDER AUDIT CHRONICLE", "OFFICIAL EXPEDITION ARCHIVE");

        logEntriesContainer = new JPanel();
        logEntriesContainer.setOpaque(false);
        logEntriesContainer.setLayout(new BoxLayout(logEntriesContainer, BoxLayout.Y_AXIS));
        logEntriesContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(logEntriesContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(OrbitTheme.BORDER_SUBTLE, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        logPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Controls
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        footer.setOpaque(false);

        SciFiButton returnBtn = new SciFiButton("RETURN TO COMMAND CENTER", SciFiButton.Variant.PRIMARY);
        returnBtn.addActionListener(e -> frame.showScreen(OrbitFrame.SCREEN_COMMAND_CENTER));

        footer.add(returnBtn);
        logPanel.add(footer, BorderLayout.SOUTH);

        add(logPanel, BorderLayout.CENTER);
    }

    /**
     * Refreshes the log entries directly from the active GameState's MissionLog.
     */
    public void refreshFromController() {
        if (!controller.isGameActive()) {
            return;
        }

        MissionLog log = controller.getMissionLog();
        List<MissionLog.LogEntry> entries = (log != null) ? log.getEntries() : null;

        int count = (entries != null) ? entries.size() : 0;
        totalEntriesBadge.setText("LOGGED EVENTS: " + count);

        if (controller.getGameState() != null) {
            currentStageBadge.setText(controller.getGameState().getCurrentStage().toUpperCase());
        }

        logEntriesContainer.removeAll();

        if (entries == null || entries.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);
            JLabel emptyLabel = new JLabel("NO FLIGHT RECORDER ENTRIES CURRENTLY ARCHIVED.");
            emptyLabel.setFont(OrbitTheme.FONT_HEADER_MONO);
            emptyLabel.setForeground(OrbitTheme.TEXT_MUTED);
            emptyPanel.add(emptyLabel);
            logEntriesContainer.add(emptyPanel);
        } else {
            for (MissionLog.LogEntry entry : entries) {
                JPanel row = createLogEntryCard(entry);
                logEntriesContainer.add(row);
                logEntriesContainer.add(Box.createVerticalStrut(6));
            }
        }

        logEntriesContainer.revalidate();
        logEntriesContainer.repaint();
    }

    private JPanel createLogEntryCard(MissionLog.LogEntry entry) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setOpaque(true);
        card.setBackground(OrbitTheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrbitTheme.BORDER_MUTED, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        // Left: Timestamp + Category Badge
        JPanel leftBadgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBadgePanel.setOpaque(false);

        JLabel tsLabel = new JLabel("[" + entry.getTimestamp() + "]");
        tsLabel.setFont(OrbitTheme.FONT_TINY_MONO);
        tsLabel.setForeground(OrbitTheme.TEXT_MUTED);

        Color catColor = resolveCategoryColor(entry.getCategory());
        JLabel catLabel = new JLabel("[" + entry.getCategory().toUpperCase() + "]");
        catLabel.setFont(OrbitTheme.FONT_TINY_MONO);
        catLabel.setForeground(catColor);

        leftBadgePanel.add(tsLabel);
        leftBadgePanel.add(catLabel);

        // Center: Description Narrative
        JLabel descLabel = new JLabel(entry.getDescription());
        descLabel.setFont(OrbitTheme.FONT_MONO);
        descLabel.setForeground(OrbitTheme.TEXT_PRIMARY);

        card.add(leftBadgePanel, BorderLayout.WEST);
        card.add(descLabel, BorderLayout.CENTER);

        return card;
    }

    private Color resolveCategoryColor(String category) {
        if (category == null) return OrbitTheme.TEXT_SECONDARY;
        return switch (category.trim().toUpperCase()) {
            case "MISSION" -> OrbitTheme.BLUE_ACCENT;
            case "NAVIGATION" -> OrbitTheme.CYAN_ELECTRIC;
            case "DISCOVERY" -> OrbitTheme.STATUS_NOMINAL;
            case "RESOURCE" -> OrbitTheme.STATUS_WARNING;
            case "HAZARD" -> OrbitTheme.STATUS_ALERT;
            case "SIGNAL" -> new Color(0xA8, 0x55, 0xF7); // Magenta / Violet
            case "ENCOUNTER" -> new Color(0x38, 0xBD, 0xF8); // Electric Sky
            case "EXPEDITION" -> new Color(0x94, 0xA3, 0xB8); // Slate
            default -> OrbitTheme.TEXT_SECONDARY;
        };
    }
}

