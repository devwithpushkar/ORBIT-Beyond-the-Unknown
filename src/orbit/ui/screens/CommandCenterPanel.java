package orbit.ui.screens;

import orbit.controllers.OrbitController;
import orbit.models.Inventory;
import orbit.models.MissionObjective;
import orbit.models.Planet;
import orbit.models.Player;
import orbit.models.Spaceship;
import orbit.ui.OrbitFrame;
import orbit.ui.OrbitTheme;
import orbit.ui.components.ResourceGauge;
import orbit.ui.components.SciFiButton;
import orbit.ui.components.SciFiDialog;
import orbit.ui.components.SciFiPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Command Center screen for ORBIT: Beyond the Unknown.
 * Provides the central tactical bridge displaying commander credentials,
 * planetary location, live spacecraft vitals gauges, cargo stores,
 * campaign objectives, and access points for navigation, inventory, and flight logs.
 */
public class CommandCenterPanel extends JPanel {

    private final OrbitFrame frame;
    private final OrbitController controller;

    // Header labels
    private JLabel commanderBadge;
    private JLabel vesselBadge;
    private JLabel locationBadge;
    private JLabel systemStatusBadge;

    // Telemetry Gauges
    private ResourceGauge fuelGauge;
    private ResourceGauge oxygenGauge;
    private ResourceGauge energyGauge;
    private ResourceGauge hullGauge;

    // Cargo Manifest labels
    private JLabel metalLabel;
    private JLabel energyCellsLabel;
    private JLabel oxygenCanistersLabel;
    private JLabel fuelCellsLabel;
    private JLabel artifactsLabel;

    // Mission Directives
    private JLabel stageLabel;
    private JLabel progressLabel;
    private JProgressBar progressBar;
    private JPanel objectivesListPanel;
    private SciFiButton exploreBtn;

    // Footer
    private JLabel statusFeedbackLabel;

    public CommandCenterPanel(OrbitFrame frame, OrbitController controller) {
        this.frame = frame;
        this.controller = controller;

        setLayout(new BorderLayout(0, 10));
        setBackground(OrbitTheme.BG_VOID);
        setBorder(new EmptyBorder(12, 16, 12, 16));

        initHeader();
        initDashboard();
        initFooter();
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout(16, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, OrbitTheme.BORDER_SUBTLE),
                new EmptyBorder(4, 8, 12, 8)
        ));

        // Left: Ship & Base designation
        JPanel leftHeader = new JPanel();
        leftHeader.setOpaque(false);
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("ORBIT COMMAND CENTER");
        title.setFont(OrbitTheme.FONT_HEADER_MONO);
        title.setForeground(OrbitTheme.CYAN_ELECTRIC);

        vesselBadge = new JLabel("VESSEL: ODYSSEY-07 // TACTICAL BRIDGE");
        vesselBadge.setFont(OrbitTheme.FONT_TINY_MONO);
        vesselBadge.setForeground(OrbitTheme.TEXT_MUTED);

        leftHeader.add(title);
        leftHeader.add(Box.createVerticalStrut(2));
        leftHeader.add(vesselBadge);

        // Center / Right: Commander & Location Badges
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightHeader.setOpaque(false);

        commanderBadge = createBadge("CMDR: UNASSIGNED", OrbitTheme.TEXT_CYAN);
        locationBadge = createBadge("LOC: EARTH", OrbitTheme.STATUS_NOMINAL);
        systemStatusBadge = createBadge("● SYSTEMS NOMINAL", OrbitTheme.STATUS_NOMINAL);

        rightHeader.add(commanderBadge);
        rightHeader.add(locationBadge);
        rightHeader.add(systemStatusBadge);

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
                new EmptyBorder(4, 10, 4, 10)
        ));
        badge.setOpaque(true);
        badge.setBackground(OrbitTheme.BG_CARD);
        return badge;
    }

    private void initDashboard() {
        JPanel mainGrid = new JPanel(new GridLayout(1, 2, 14, 0));
        mainGrid.setOpaque(false);

        // --- Left Column: Spacecraft Vitals & Cargo Manifest ---
        JPanel leftColumn = new JPanel();
        leftColumn.setOpaque(false);
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));

        // 1. Ship Vitals Panel
        SciFiPanel vitalsPanel = new SciFiPanel("SPACECRAFT TELEMETRY", "REAL-TIME AVIONICS");
        JPanel vitalsContent = new JPanel();
        vitalsContent.setOpaque(false);
        vitalsContent.setLayout(new BoxLayout(vitalsContent, BoxLayout.Y_AXIS));

        fuelGauge = new ResourceGauge("PROPULSION FUEL", 100);
        oxygenGauge = new ResourceGauge("LIFE SUPPORT OXYGEN", 100);
        energyGauge = new ResourceGauge("REACTOR ENERGY", 100);
        hullGauge = new ResourceGauge("HULL INTEGRITY", 100);

        vitalsContent.add(fuelGauge);
        vitalsContent.add(Box.createVerticalStrut(10));
        vitalsContent.add(oxygenGauge);
        vitalsContent.add(Box.createVerticalStrut(10));
        vitalsContent.add(energyGauge);
        vitalsContent.add(Box.createVerticalStrut(10));
        vitalsContent.add(hullGauge);

        vitalsPanel.add(vitalsContent, BorderLayout.CENTER);
        leftColumn.add(vitalsPanel);
        leftColumn.add(Box.createVerticalStrut(12));

        // 2. Cargo Manifest Summary Panel
        SciFiPanel cargoPanel = new SciFiPanel("CARGO MANIFEST", "SUPPLY STORES");
        JPanel cargoContent = new JPanel(new GridLayout(5, 1, 0, 4));
        cargoContent.setOpaque(false);

        metalLabel = createCargoRow("Metal (Hull Repairs)");
        energyCellsLabel = createCargoRow("Energy Cells (Reactor)");
        oxygenCanistersLabel = createCargoRow("Oxygen Canisters (Life Support)");
        fuelCellsLabel = createCargoRow("Fuel Cells (Propellant)");
        artifactsLabel = createCargoRow("Alien Artifacts (Discoveries)");

        cargoContent.add(metalLabel);
        cargoContent.add(energyCellsLabel);
        cargoContent.add(oxygenCanistersLabel);
        cargoContent.add(fuelCellsLabel);
        cargoContent.add(artifactsLabel);

        cargoPanel.add(cargoContent, BorderLayout.CENTER);
        leftColumn.add(cargoPanel);

        // --- Right Column: Mission Directives & Operations Grid ---
        JPanel rightColumn = new JPanel();
        rightColumn.setOpaque(false);
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));

        // 3. Mission Directive Panel
        SciFiPanel missionPanel = new SciFiPanel("CAMPAIGN DIRECTIVES", "FLIGHT ORDERS");
        JPanel missionContent = new JPanel();
        missionContent.setOpaque(false);
        missionContent.setLayout(new BoxLayout(missionContent, BoxLayout.Y_AXIS));

        JPanel stageRow = new JPanel(new BorderLayout());
        stageRow.setOpaque(false);
        stageLabel = new JLabel("STAGE: STAGE 1 — DEPARTURE");
        stageLabel.setFont(OrbitTheme.FONT_BODY_BOLD);
        stageLabel.setForeground(OrbitTheme.CYAN_ELECTRIC);

        progressLabel = new JLabel("0% COMPLETE");
        progressLabel.setFont(OrbitTheme.FONT_TINY_MONO);
        progressLabel.setForeground(OrbitTheme.TEXT_SECONDARY);

        stageRow.add(stageLabel, BorderLayout.WEST);
        stageRow.add(progressLabel, BorderLayout.EAST);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(100, 8));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        progressBar.setForeground(OrbitTheme.CYAN_ELECTRIC);
        progressBar.setBackground(OrbitTheme.BG_VOID);
        progressBar.setBorderPainted(false);

        objectivesListPanel = new JPanel();
        objectivesListPanel.setOpaque(false);
        objectivesListPanel.setLayout(new BoxLayout(objectivesListPanel, BoxLayout.Y_AXIS));

        missionContent.add(stageRow);
        missionContent.add(Box.createVerticalStrut(6));
        missionContent.add(progressBar);
        missionContent.add(Box.createVerticalStrut(10));
        missionContent.add(objectivesListPanel);
        missionContent.add(Box.createVerticalStrut(8));

        SciFiButton missionControlBtn = new SciFiButton("OPEN MISSION CONTROL DIRECTIVES", SciFiButton.Variant.SECONDARY);
        missionControlBtn.addActionListener(e -> frame.showScreen(OrbitFrame.SCREEN_MISSION_CONTROL));
        missionContent.add(missionControlBtn);

        missionPanel.add(missionContent, BorderLayout.CENTER);
        rightColumn.add(missionPanel);
        rightColumn.add(Box.createVerticalStrut(12));

        // 4. Command Operations Grid (Access Points)
        SciFiPanel opsPanel = new SciFiPanel("FLIGHT OPERATIONS", "SUBSYSTEM ACCESS");
        JPanel opsGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        opsGrid.setOpaque(false);

        SciFiButton navBtn = new SciFiButton("NAVIGATION", SciFiButton.Variant.PRIMARY);
        navBtn.addActionListener(e -> handleNavigationAccess());

        SciFiButton invBtn = new SciFiButton("INVENTORY", SciFiButton.Variant.SECONDARY);
        invBtn.addActionListener(e -> handleInventoryAccess());

        SciFiButton logBtn = new SciFiButton("MISSION LOG", SciFiButton.Variant.SECONDARY);
        logBtn.addActionListener(e -> handleMissionLogAccess());

        exploreBtn = new SciFiButton("EXPLORE SURFACE", SciFiButton.Variant.SECONDARY);
        exploreBtn.addActionListener(e -> handleExploreAccess());

        opsGrid.add(navBtn);
        opsGrid.add(invBtn);
        opsGrid.add(logBtn);
        opsGrid.add(exploreBtn);

        opsPanel.add(opsGrid, BorderLayout.CENTER);
        rightColumn.add(opsPanel);

        mainGrid.add(leftColumn);
        mainGrid.add(rightColumn);

        add(mainGrid, BorderLayout.CENTER);
    }

    private JLabel createCargoRow(String itemName) {
        JLabel row = new JLabel("  * " + itemName + " : 0");
        row.setFont(OrbitTheme.FONT_MONO);
        row.setForeground(OrbitTheme.TEXT_SECONDARY);
        return row;
    }

    private void initFooter() {
        JPanel footer = new JPanel(new BorderLayout(16, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, OrbitTheme.BORDER_SUBTLE),
                new EmptyBorder(10, 8, 4, 8)
        ));

        statusFeedbackLabel = new JLabel("STANDBY // AWAITING COMMAND INPUT");
        statusFeedbackLabel.setFont(OrbitTheme.FONT_TINY_MONO);
        statusFeedbackLabel.setForeground(OrbitTheme.TEXT_MUTED);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        SciFiButton saveBtn = new SciFiButton("SAVE TELEMETRY", SciFiButton.Variant.PRIMARY);
        saveBtn.addActionListener(e -> handleSaveGame());

        SciFiButton menuBtn = new SciFiButton("RETURN TO TITLE DECK", SciFiButton.Variant.SECONDARY);
        menuBtn.addActionListener(e -> frame.showScreen(OrbitFrame.SCREEN_MAIN_MENU));

        controls.add(saveBtn);
        controls.add(menuBtn);

        footer.add(statusFeedbackLabel, BorderLayout.WEST);
        footer.add(controls, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);
    }

    // --- Action Handlers ---

    private void handleNavigationAccess() {
        frame.showScreen(OrbitFrame.SCREEN_NAVIGATION);
    }

    private void handleInventoryAccess() {
        frame.showScreen(OrbitFrame.SCREEN_INVENTORY);
    }

    private void handleMissionLogAccess() {
        frame.showScreen(OrbitFrame.SCREEN_MISSION_LOG);
    }

    private void handleExploreAccess() {
        if (controller.canInitiateFinalDirective()) {
            frame.getMissionControlPanel().promptFinalDirectiveDialog();
        } else {
            frame.showScreen(OrbitFrame.SCREEN_EXPLORATION);
        }
    }

    private void handleSaveGame() {
        boolean saved = controller.saveGame();
        if (saved) {
            statusFeedbackLabel.setText("TELEMETRY SUCCESSFULLY PERSISTED TO saves/orbit_save.dat");
            statusFeedbackLabel.setForeground(OrbitTheme.STATUS_NOMINAL);
            SciFiDialog.showMessage(
                    frame,
                    "TELEMETRY ARCHIVE",
                    "SAVE OPERATION CONFIRMED",
                    "Flight session successfully archived to saves/orbit_save.dat.\n"
                    + "All resources, position coordinates, and campaign progress preserved."
            );
        } else {
            statusFeedbackLabel.setText("SAVE FAILED: IO DISK ERROR ENCOUNTERED");
            statusFeedbackLabel.setForeground(OrbitTheme.STATUS_ALERT);
            SciFiDialog.showMessage(
                    frame,
                    "TELEMETRY ARCHIVE",
                    "SAVE FAILED",
                    "Failed to write telemetry data to disk. Please check file write permissions."
            );
        }
    }

    /**
     * Refreshes all UI readouts from the live active GameState via OrbitController.
     */
    public void refreshFromController() {
        if (!controller.isGameActive()) {
            return;
        }

        Player player = controller.getPlayer();
        Spaceship ship = controller.getSpaceship();
        Planet location = controller.getCurrentLocation();
        Inventory inventory = controller.getInventory();

        // 1. Header Badges
        if (player != null) {
            commanderBadge.setText("CMDR: " + player.getCommanderName().toUpperCase());
            vesselBadge.setText("VESSEL: " + player.getSpaceshipName().toUpperCase() + " // TACTICAL BRIDGE");
        }
        if (location != null) {
            locationBadge.setText("LOC: " + location.getName().toUpperCase());
        }

        // 2. Vitals Gauges
        if (ship != null) {
            fuelGauge.setValue(ship.getFuel());
            oxygenGauge.setValue(ship.getOxygen());
            energyGauge.setValue(ship.getEnergy());
            hullGauge.setValue(ship.getHullIntegrity());

            boolean isCritical = (ship.getFuel() <= Spaceship.CRITICAL_THRESHOLD
                    || ship.getOxygen() <= Spaceship.CRITICAL_THRESHOLD
                    || ship.getEnergy() <= Spaceship.CRITICAL_THRESHOLD
                    || ship.getHullIntegrity() <= Spaceship.CRITICAL_THRESHOLD);
            boolean isCaution = (ship.getFuel() <= Spaceship.CAUTION_THRESHOLD
                    || ship.getOxygen() <= Spaceship.CAUTION_THRESHOLD
                    || ship.getEnergy() <= Spaceship.CAUTION_THRESHOLD
                    || ship.getHullIntegrity() <= Spaceship.CAUTION_THRESHOLD);

            if (isCritical) {
                systemStatusBadge.setText("▲ SYSTEM CRITICAL");
                systemStatusBadge.setForeground(OrbitTheme.STATUS_ALERT);
            } else if (isCaution) {
                systemStatusBadge.setText("▲ SYSTEM CAUTION");
                systemStatusBadge.setForeground(OrbitTheme.STATUS_WARNING);
            } else {
                systemStatusBadge.setText("● SYSTEMS NOMINAL");
                systemStatusBadge.setForeground(OrbitTheme.STATUS_NOMINAL);
            }
        }

        // 3. Cargo Manifest
        if (inventory != null) {
            metalLabel.setText(String.format("  * %-20s : %2d QTY", "Metal", inventory.getQuantity(Inventory.METAL)));
            energyCellsLabel.setText(String.format("  * %-20s : %2d QTY", "Energy Cells", inventory.getQuantity(Inventory.ENERGY_CELLS)));
            oxygenCanistersLabel.setText(String.format("  * %-20s : %2d QTY", "Oxygen Canisters", inventory.getQuantity(Inventory.OXYGEN_CANISTERS)));
            fuelCellsLabel.setText(String.format("  * %-20s : %2d QTY", "Fuel Cells", inventory.getQuantity(Inventory.FUEL_CELLS)));
            artifactsLabel.setText(String.format("  * %-20s : %2d QTY", "Alien Artifacts", inventory.getQuantity(Inventory.ALIEN_ARTIFACTS)));
        }

        // 4. Mission Directives
        if (controller.getGameState() != null) {
            String stage = controller.getGameState().getCurrentStage();
            int progress = controller.getGameState().getMissionProgress();

            stageLabel.setText("STAGE: " + stage.toUpperCase());
            progressLabel.setText(progress + "% COMPLETE");
            progressBar.setValue(progress);

            // Rebuild active objectives list
            objectivesListPanel.removeAll();
            List<MissionObjective> objectives = controller.getObjectives();
            if (objectives != null) {
                for (MissionObjective obj : objectives) {
                    if (obj.getStage().equalsIgnoreCase(stage)) {
                        JPanel row = new JPanel(new BorderLayout(6, 0));
                        row.setOpaque(false);

                        JLabel titleLbl = new JLabel("• " + obj.getTitle());
                        titleLbl.setFont(OrbitTheme.FONT_MONO);
                        titleLbl.setForeground(obj.isCompleted() ? OrbitTheme.TEXT_MUTED : OrbitTheme.TEXT_PRIMARY);

                        JLabel statusLbl = new JLabel(obj.isCompleted() ? "[DONE]" : "[IN PROGRESS]");
                        statusLbl.setFont(OrbitTheme.FONT_TINY_MONO);
                        statusLbl.setForeground(obj.isCompleted() ? OrbitTheme.STATUS_NOMINAL : OrbitTheme.CYAN_ELECTRIC);

                        row.add(titleLbl, BorderLayout.WEST);
                        row.add(statusLbl, BorderLayout.EAST);
                        objectivesListPanel.add(row);
                        objectivesListPanel.add(Box.createVerticalStrut(4));
                    }
                }
            }
            objectivesListPanel.revalidate();
            objectivesListPanel.repaint();
        }

        if (exploreBtn != null) {
            if (controller.canInitiateFinalDirective()) {
                exploreBtn.setText("FINAL DIRECTIVE");
            } else {
                exploreBtn.setText("EXPLORE SURFACE");
            }
        }

        if (controller.isCampaignCompleted()) {
            statusFeedbackLabel.setText("CAMPAIGN CONCLUDED // ALL DIRECTIVES RESOLVED (" + controller.getCompletedEnding() + ")");
            statusFeedbackLabel.setForeground(OrbitTheme.STATUS_NOMINAL);
        } else {
            statusFeedbackLabel.setText("TELEMETRY ACTIVE // SYSTEM CHANNELS NOMINAL");
            statusFeedbackLabel.setForeground(OrbitTheme.TEXT_MUTED);
        }

        revalidate();
        repaint();
    }
}
