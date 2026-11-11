package orbit.ui.screens;

import orbit.controllers.EndingResult;
import orbit.controllers.OrbitController;
import orbit.models.Inventory;
import orbit.ui.OrbitFrame;
import orbit.ui.OrbitTheme;
import orbit.ui.components.SciFiButton;
import orbit.ui.components.SciFiPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * EndingPanel renders the narrative climax, campaign evaluation debriefing,
 * and final mission telemetry for ORBIT's three distinct campaign endings:
 * - Ending A: Successful Return
 * - Ending B: Lost in Space
 * - Ending C: The Unknown
 */
public class EndingPanel extends JPanel {

    private final OrbitController controller;
    private final OrbitFrame frame;
    private EndingResult endingResult;

    // Header Components
    private JLabel bannerLabel;
    private JLabel titleLabel;
    private JLabel officerLabel;

    // Narrative Component
    private JTextArea narrativeArea;

    // Telemetry & Statistics
    private JLabel fuelLabel;
    private JLabel oxygenLabel;
    private JLabel energyLabel;
    private JLabel hullLabel;

    private JPanel cargoContainer;
    private JPanel planetsContainer;
    private JPanel objectivesContainer;

    public EndingPanel(OrbitController controller, OrbitFrame frame) {
        this.controller = controller;
        this.frame = frame;

        setBackground(OrbitTheme.BG_VOID);
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        buildUI();
    }

    private void buildUI() {
        // 1. Header: Outcome Banner & Title
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        bannerLabel = new JLabel("VICTORY: HUMANITY'S FIRST INTERSTELLAR TRIUMPH", SwingConstants.CENTER);
        bannerLabel.setFont(OrbitTheme.FONT_TITLE);
        bannerLabel.setForeground(OrbitTheme.STATUS_NOMINAL);
        bannerLabel.setAlignmentX(CENTER_ALIGNMENT);

        titleLabel = new JLabel("MISSION CONCLUDED // ENDING A: SUCCESSFUL RETURN", SwingConstants.CENTER);
        titleLabel.setFont(OrbitTheme.FONT_SUBTITLE);
        titleLabel.setForeground(OrbitTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        officerLabel = new JLabel("VESSEL ODYSSEY-07 // COMMANDER SHEPARD", SwingConstants.CENTER);
        officerLabel.setFont(OrbitTheme.FONT_MONO);
        officerLabel.setForeground(OrbitTheme.TEXT_MUTED);
        officerLabel.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(bannerLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(officerLabel);
        headerPanel.add(Box.createVerticalStrut(10));

        add(headerPanel, BorderLayout.NORTH);

        // 2. Center: Scrollable Narrative & Final Mission Statistics Report
        JPanel contentContainer = new JPanel();
        contentContainer.setOpaque(false);
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));

        // Narrative Card
        SciFiPanel narrativePanel = new SciFiPanel("CAMPAIGN NARRATIVE DEBRIEFING", "HISTORICAL TRANSMISSION ARCHIVE");
        narrativeArea = new JTextArea();
        narrativeArea.setOpaque(false);
        narrativeArea.setEditable(false);
        narrativeArea.setLineWrap(true);
        narrativeArea.setWrapStyleWord(true);
        narrativeArea.setFont(OrbitTheme.FONT_BODY);
        narrativeArea.setForeground(OrbitTheme.TEXT_PRIMARY);
        narrativeArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        narrativePanel.add(narrativeArea, BorderLayout.CENTER);
        contentContainer.add(narrativePanel);
        contentContainer.add(Box.createVerticalStrut(14));

        // Statistics Matrix: 2x2 Grid
        JPanel statsGrid = new JPanel(new GridLayout(2, 2, 14, 14));
        statsGrid.setOpaque(false);

        // Sub-panel 1: Final Spacecraft Vitals
        SciFiPanel vitalsPanel = new SciFiPanel("FINAL SPACECRAFT VITALS", "SUBSYSTEM HEALTH AT CONVOLUTION");
        JPanel vitalsList = new JPanel();
        vitalsList.setOpaque(false);
        vitalsList.setLayout(new BoxLayout(vitalsList, BoxLayout.Y_AXIS));

        fuelLabel = createStatRow("Propulsion Fuel Reserves", "100%");
        oxygenLabel = createStatRow("Life Support Oxygen", "100%");
        energyLabel = createStatRow("Reactor Core Energy", "100%");
        hullLabel = createStatRow("Hull Structural Integrity", "100%");

        vitalsList.add(fuelLabel);
        vitalsList.add(Box.createVerticalStrut(6));
        vitalsList.add(oxygenLabel);
        vitalsList.add(Box.createVerticalStrut(6));
        vitalsList.add(energyLabel);
        vitalsList.add(Box.createVerticalStrut(6));
        vitalsList.add(hullLabel);

        vitalsPanel.add(vitalsList, BorderLayout.CENTER);
        statsGrid.add(vitalsPanel);

        // Sub-panel 2: Recovered Cargo Manifest
        SciFiPanel cargoPanel = new SciFiPanel("CARGO MANIFEST & SALVAGE", "RECOVERED ARTIFACTS AND MATERIALS");
        cargoContainer = new JPanel();
        cargoContainer.setOpaque(false);
        cargoContainer.setLayout(new BoxLayout(cargoContainer, BoxLayout.Y_AXIS));
        cargoPanel.add(cargoContainer, BorderLayout.CENTER);
        statsGrid.add(cargoPanel);

        // Sub-panel 3: Charted Celestial Bodies
        SciFiPanel planetsPanel = new SciFiPanel("CHARTED CELESTIAL BODIES", "GALAXY CARTOGRAPHY");
        planetsContainer = new JPanel();
        planetsContainer.setOpaque(false);
        planetsContainer.setLayout(new BoxLayout(planetsContainer, BoxLayout.Y_AXIS));
        planetsPanel.add(planetsContainer, BorderLayout.CENTER);
        statsGrid.add(planetsPanel);

        // Sub-panel 4: Completed Directives
        SciFiPanel objectivesPanel = new SciFiPanel("DIRECTIVE FULFILLMENT", "12 CAMPAIGN MILESTONES");
        objectivesContainer = new JPanel();
        objectivesContainer.setOpaque(false);
        objectivesContainer.setLayout(new BoxLayout(objectivesContainer, BoxLayout.Y_AXIS));
        objectivesPanel.add(objectivesContainer, BorderLayout.CENTER);
        statsGrid.add(objectivesPanel);

        contentContainer.add(statsGrid);

        JScrollPane scrollPane = new JScrollPane(contentContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(OrbitTheme.BORDER_SUBTLE, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);

        SciFiButton missionLogBtn = new SciFiButton("REVIEW FLIGHT MISSION LOG", SciFiButton.Variant.SECONDARY);
        missionLogBtn.addActionListener(e -> frame.showScreen(OrbitFrame.SCREEN_MISSION_LOG));
        leftActions.add(missionLogBtn);

        SciFiButton directivesBtn = new SciFiButton("CAMPAIGN DIRECTIVES MATRIX", SciFiButton.Variant.SECONDARY);
        directivesBtn.addActionListener(e -> frame.showScreen(OrbitFrame.SCREEN_MISSION_CONTROL));
        leftActions.add(directivesBtn);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        SciFiButton mainMenuBtn = new SciFiButton("RETURN TO MAIN MENU", SciFiButton.Variant.PRIMARY);
        mainMenuBtn.addActionListener(e -> frame.showScreen(OrbitFrame.SCREEN_MAIN_MENU));
        rightActions.add(mainMenuBtn);

        bottomBar.add(leftActions, BorderLayout.WEST);
        bottomBar.add(rightActions, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);
    }

    private JLabel createStatRow(String label, String value) {
        JLabel row = new JLabel("  * " + String.format("%-26s", label) + " : " + value);
        row.setFont(OrbitTheme.FONT_MONO);
        row.setForeground(OrbitTheme.TEXT_PRIMARY);
        return row;
    }

    public void displayEnding(EndingResult result) {
        this.endingResult = result;
        refreshFromEndingResult();
    }

    public void refreshFromEndingResult() {
        if (endingResult == null) {
            endingResult = controller.getCompletedEndingResult();
        }
        if (endingResult == null) {
            return;
        }

        // Color Palette based on ending
        Color themeColor;
        switch (endingResult.getType()) {
            case ENDING_A_SUCCESSFUL_RETURN -> themeColor = OrbitTheme.STATUS_NOMINAL;
            case ENDING_B_LOST_IN_SPACE -> themeColor = OrbitTheme.STATUS_ALERT;
            case ENDING_C_THE_UNKNOWN -> themeColor = new Color(0xC0, 0x84, 0xFC);
            default -> themeColor = OrbitTheme.CYAN_ELECTRIC;
        }

        bannerLabel.setText(endingResult.getOutcomeBanner());
        bannerLabel.setForeground(themeColor);

        titleLabel.setText("MISSION CONCLUDED // " + endingResult.getEndingTitle().toUpperCase());
        officerLabel.setText("VESSEL " + endingResult.getSpaceshipName().toUpperCase()
                + " // COMMANDER " + endingResult.getCommanderName().toUpperCase()
                + " // TOTAL COMPLETION: 100%");

        narrativeArea.setText(endingResult.getNarrative());

        // Vitals
        fuelLabel.setText("  * " + String.format("%-26s", "Propulsion Fuel Reserves") + " : " + endingResult.getFinalFuel() + "%");
        oxygenLabel.setText("  * " + String.format("%-26s", "Life Support Oxygen") + " : " + endingResult.getFinalOxygen() + "%");
        energyLabel.setText("  * " + String.format("%-26s", "Reactor Core Energy") + " : " + endingResult.getFinalEnergy() + "%");
        hullLabel.setText("  * " + String.format("%-26s", "Hull Structural Integrity") + " : " + endingResult.getFinalHull() + "%");

        // Cargo
        cargoContainer.removeAll();
        for (EndingResult.CargoSnapshot cargo : endingResult.getCargoSnapshot()) {
            JLabel row = new JLabel("  * " + String.format("%-20s", cargo.getName()) + " : " + cargo.getQuantity() + " units");
            row.setFont(OrbitTheme.FONT_MONO);
            row.setForeground(cargo.getQuantity() > 0 ? OrbitTheme.STATUS_NOMINAL : OrbitTheme.TEXT_MUTED);
            cargoContainer.add(row);
            cargoContainer.add(Box.createVerticalStrut(4));
        }

        // Planets
        planetsContainer.removeAll();
        for (EndingResult.PlanetSnapshot p : endingResult.getPlanetSnapshot()) {
            String badge = p.isExplored() ? "[SURFACE EXPLORED]" : (p.isDiscovered() ? "[ORBIT CHARTED]   " : "[UNEXPLORED]     ");
            JLabel row = new JLabel("  * " + String.format("%-12s", p.getName()) + " : " + badge);
            row.setFont(OrbitTheme.FONT_MONO);
            row.setForeground(p.isExplored() ? OrbitTheme.STATUS_NOMINAL : (p.isDiscovered() ? OrbitTheme.CYAN_ELECTRIC : OrbitTheme.TEXT_MUTED));
            planetsContainer.add(row);
            planetsContainer.add(Box.createVerticalStrut(4));
        }

        // Objectives
        objectivesContainer.removeAll();
        for (EndingResult.ObjectiveSnapshot obj : endingResult.getObjectiveSnapshot()) {
            String mark = obj.isCompleted() ? "[V] COMPLETED" : "[>] IN PROGRESS";
            JLabel row = new JLabel("  * " + mark + " : " + obj.getTitle());
            row.setFont(OrbitTheme.FONT_MONO);
            row.setForeground(obj.isCompleted() ? OrbitTheme.STATUS_NOMINAL : OrbitTheme.STATUS_WARNING);
            objectivesContainer.add(row);
            objectivesContainer.add(Box.createVerticalStrut(4));
        }

        revalidate();
        repaint();
    }
}
