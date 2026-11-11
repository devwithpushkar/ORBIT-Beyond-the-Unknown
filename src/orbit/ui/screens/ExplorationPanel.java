package orbit.ui.screens;

import orbit.controllers.ExpeditionResult;
import orbit.controllers.OrbitController;
import orbit.models.ExplorationEvent;
import orbit.models.Planet;
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
 * Full graphical Planetary Exploration interface for ORBIT: Beyond the Unknown.
 * Provides planetary surface reconnaissance excursions, environmental threat assessment,
 * interactive hazard mitigation choices, xenological anomaly investigations,
 * and authoritative mission objective updates.
 */
public class ExplorationPanel extends JPanel {

    private final OrbitFrame frame;
    private final OrbitController controller;

    // Header Badges
    private JLabel currentPlanetBadge;
    private JLabel explorationStatusBadge;

    // Telemetry Gauges
    private ResourceGauge fuelGauge;
    private ResourceGauge oxygenGauge;
    private ResourceGauge energyGauge;
    private ResourceGauge hullGauge;

    // Left Column: Environment & Launch Controls
    private JTextArea envProfileArea;
    private JLabel readinessStatusLabel;
    private SciFiButton launchExpeditionButton;
    private SciFiButton finalDirectiveButton;

    // Right Column: Field Reconnaissance & Event Console
    private JLabel eventClassificationBadge;
    private JLabel eventTitleLabel;
    private JTextArea eventNarrativeArea;
    private JPanel dynamicActionContainer;
    private JPanel effectsContainer;
    private JScrollPane effectsScrollPane;

    // Pending event state awaiting player decision
    private ExplorationEvent pendingEvent = null;

    public ExplorationPanel(OrbitFrame frame, OrbitController controller) {
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

        JLabel title = new JLabel("PLANETARY RECONNAISSANCE // SURFACE EXPEDITION");
        title.setFont(OrbitTheme.FONT_HEADER_MONO);
        title.setForeground(OrbitTheme.CYAN_ELECTRIC);

        JLabel subTitle = new JLabel("SURFACE RECONNAISSANCE ROVER & FIELD TELEMETRY CONSOLE");
        subTitle.setFont(OrbitTheme.FONT_TINY_MONO);
        subTitle.setForeground(OrbitTheme.TEXT_MUTED);

        leftHeader.add(title);
        leftHeader.add(Box.createVerticalStrut(2));
        leftHeader.add(subTitle);

        // Right Header Badges
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightHeader.setOpaque(false);

        currentPlanetBadge = createBadge("TARGET: EARTH SURFACE", OrbitTheme.STATUS_NOMINAL);
        explorationStatusBadge = createBadge("[SURFACE SURVEY PENDING]", OrbitTheme.STATUS_WARNING);

        rightHeader.add(currentPlanetBadge);
        rightHeader.add(explorationStatusBadge);

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
        JPanel splitContainer = new JPanel(new BorderLayout(14, 0));
        splitContainer.setOpaque(false);

        // --- Left: Planetary Environment & Expedition Telemetry (~40% width) ---
        JPanel leftColumn = createLeftColumn();
        leftColumn.setPreferredSize(new Dimension(440, 600));
        splitContainer.add(leftColumn, BorderLayout.WEST);

        // --- Right: Field Reconnaissance Event Log & Narrative Console (~60% width) ---
        JPanel rightColumn = createRightColumn();
        splitContainer.add(rightColumn, BorderLayout.CENTER);

        add(splitContainer, BorderLayout.CENTER);
    }

    private JPanel createLeftColumn() {
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        // 1. Live Spaceship Telemetry
        SciFiPanel vitalsPanel = new SciFiPanel("SPACECRAFT TELEMETRY", "REAL-TIME AVIONICS");
        JPanel vitalsContent = new JPanel();
        vitalsContent.setOpaque(false);
        vitalsContent.setLayout(new BoxLayout(vitalsContent, BoxLayout.Y_AXIS));

        fuelGauge = new ResourceGauge("PROPULSION FUEL", 100);
        oxygenGauge = new ResourceGauge("LIFE SUPPORT OXYGEN", 100);
        energyGauge = new ResourceGauge("REACTOR ENERGY", 100);
        hullGauge = new ResourceGauge("HULL INTEGRITY", 100);

        vitalsContent.add(fuelGauge);
        vitalsContent.add(Box.createVerticalStrut(6));
        vitalsContent.add(oxygenGauge);
        vitalsContent.add(Box.createVerticalStrut(6));
        vitalsContent.add(energyGauge);
        vitalsContent.add(Box.createVerticalStrut(6));
        vitalsContent.add(hullGauge);

        vitalsPanel.add(vitalsContent, BorderLayout.CENTER);
        left.add(vitalsPanel);
        left.add(Box.createVerticalStrut(10));

        // 2. Planetary Environment & Operational Requirements
        SciFiPanel envPanel = new SciFiPanel("SURFACE ENVIRONMENT PROFILE", "ATMOSPHERIC TELEMETRY");
        JPanel envContent = new JPanel();
        envContent.setOpaque(false);
        envContent.setLayout(new BoxLayout(envContent, BoxLayout.Y_AXIS));

        envProfileArea = new JTextArea(4, 20);
        envProfileArea.setFont(OrbitTheme.FONT_BODY);
        envProfileArea.setForeground(OrbitTheme.TEXT_SECONDARY);
        envProfileArea.setBackground(OrbitTheme.BG_INPUT);
        envProfileArea.setEditable(false);
        envProfileArea.setFocusable(false);
        envProfileArea.setLineWrap(true);
        envProfileArea.setWrapStyleWord(true);
        envProfileArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrbitTheme.BORDER_MUTED, 1),
                new EmptyBorder(6, 8, 6, 8)
        ));
        envContent.add(envProfileArea);
        envContent.add(Box.createVerticalStrut(10));

        // Operational Requirements Card
        JPanel costCard = new JPanel(new GridLayout(3, 1, 0, 4));
        costCard.setOpaque(true);
        costCard.setBackground(OrbitTheme.BG_CARD);
        costCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrbitTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        costCard.add(createTelemetryRow("ENERGY CONSUMPTION", "10 ENERGY"));
        costCard.add(createTelemetryRow("OXYGEN CONSUMPTION", "5 OXYGEN"));

        readinessStatusLabel = new JLabel("[EXPEDITION READY // VITALS SUFFICIENT]");
        readinessStatusLabel.setFont(OrbitTheme.FONT_TINY_MONO);
        readinessStatusLabel.setForeground(OrbitTheme.STATUS_NOMINAL);
        costCard.add(readinessStatusLabel);

        envContent.add(costCard);
        envContent.add(Box.createVerticalStrut(12));

        // Launch & Return Buttons
        JPanel actionRow = new JPanel(new GridLayout(0, 1, 0, 8));
        actionRow.setOpaque(false);

        finalDirectiveButton = new SciFiButton("RESOLVE FINAL MISSION DIRECTIVE", SciFiButton.Variant.PRIMARY);
        finalDirectiveButton.setVisible(false);
        finalDirectiveButton.addActionListener(e -> frame.getMissionControlPanel().promptFinalDirectiveDialog());

        launchExpeditionButton = new SciFiButton("LAUNCH EXPEDITION (-10 ENERGY, -5 O2)", SciFiButton.Variant.PRIMARY);
        launchExpeditionButton.addActionListener(e -> handleLaunchExpedition());

        SciFiButton returnBtn = new SciFiButton("RETURN TO COMMAND CENTER", SciFiButton.Variant.SECONDARY);
        returnBtn.addActionListener(e -> frame.showScreen(OrbitFrame.SCREEN_COMMAND_CENTER));

        actionRow.add(finalDirectiveButton);
        actionRow.add(launchExpeditionButton);
        actionRow.add(returnBtn);

        envContent.add(actionRow);

        envPanel.add(envContent, BorderLayout.CENTER);
        left.add(envPanel);

        return left;
    }

    private JLabel createTelemetryRow(String label, String value) {
        JLabel l = new JLabel(String.format("%-20s: %s", label, value));
        l.setFont(OrbitTheme.FONT_MONO);
        l.setForeground(OrbitTheme.TEXT_PRIMARY);
        return l;
    }

    private JPanel createRightColumn() {
        SciFiPanel panel = new SciFiPanel("FIELD RECONNAISSANCE CONSOLE", "EXPEDITION NARRATIVE & THREAT LOG");
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // 1. Event Classification & Title Banner
        JPanel eventBanner = new JPanel(new BorderLayout());
        eventBanner.setOpaque(true);
        eventBanner.setBackground(OrbitTheme.BG_CARD);
        eventBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrbitTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        eventTitleLabel = new JLabel("STANDBY // AWAITING EXCURSION DEPLOYMENT");
        eventTitleLabel.setFont(OrbitTheme.FONT_HEADER_MONO);
        eventTitleLabel.setForeground(OrbitTheme.CYAN_ELECTRIC);

        eventClassificationBadge = new JLabel("[STANDBY]");
        eventClassificationBadge.setFont(OrbitTheme.FONT_TINY_MONO);
        eventClassificationBadge.setForeground(OrbitTheme.TEXT_MUTED);

        eventBanner.add(eventTitleLabel, BorderLayout.WEST);
        eventBanner.add(eventClassificationBadge, BorderLayout.EAST);
        content.add(eventBanner);
        content.add(Box.createVerticalStrut(10));

        // 2. Field Narrative Text Area
        eventNarrativeArea = new JTextArea(6, 20);
        eventNarrativeArea.setFont(OrbitTheme.FONT_BODY);
        eventNarrativeArea.setForeground(OrbitTheme.TEXT_PRIMARY);
        eventNarrativeArea.setBackground(OrbitTheme.BG_INPUT);
        eventNarrativeArea.setEditable(false);
        eventNarrativeArea.setFocusable(false);
        eventNarrativeArea.setLineWrap(true);
        eventNarrativeArea.setWrapStyleWord(true);
        eventNarrativeArea.setText("Surface reconnaissance rover is currently docked in the forward airlock bay.\n\n"
                + "Deploy an expedition to conduct subsurface scanning, recover salvageable cargo resources, "
                + "investigate cosmic anomaly beacons, and catalog planetary anomalies.");
        eventNarrativeArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrbitTheme.BORDER_MUTED, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        content.add(eventNarrativeArea);
        content.add(Box.createVerticalStrut(10));

        // 3. Dynamic Tactical Action Container (populated when decision needed)
        dynamicActionContainer = new JPanel();
        dynamicActionContainer.setOpaque(false);
        dynamicActionContainer.setLayout(new BoxLayout(dynamicActionContainer, BoxLayout.Y_AXIS));
        content.add(dynamicActionContainer);
        content.add(Box.createVerticalStrut(10));

        // 4. Effects & Rewards Telemetry Log Box
        effectsContainer = new JPanel();
        effectsContainer.setOpaque(true);
        effectsContainer.setBackground(OrbitTheme.BG_CARD);
        effectsContainer.setLayout(new BoxLayout(effectsContainer, BoxLayout.Y_AXIS));
        effectsContainer.setBorder(new EmptyBorder(8, 10, 8, 10));

        effectsScrollPane = new JScrollPane(effectsContainer);
        effectsScrollPane.setOpaque(false);
        effectsScrollPane.getViewport().setOpaque(false);
        effectsScrollPane.setBorder(BorderFactory.createLineBorder(OrbitTheme.BORDER_SUBTLE, 1));
        effectsScrollPane.setPreferredSize(new Dimension(400, 140));
        content.add(effectsScrollPane);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    // --- State Refresh & Lifecycle ---

    public void refreshFromController() {
        if (!controller.isGameActive()) {
            return;
        }

        Planet planet = controller.getCurrentLocation();
        Spaceship ship = controller.getSpaceship();

        if (planet != null) {
            currentPlanetBadge.setText("TARGET: " + planet.getName().toUpperCase() + " SURFACE");
            envProfileArea.setText(planet.getEnvironmentDescription());

            if (planet.isExplored()) {
                explorationStatusBadge.setText("[SURFACE SURVEYED / EXPLORED]");
                explorationStatusBadge.setForeground(OrbitTheme.STATUS_NOMINAL);
            } else {
                explorationStatusBadge.setText("[SURFACE SURVEY PENDING]");
                explorationStatusBadge.setForeground(OrbitTheme.STATUS_WARNING);
            }
        }

        if (ship != null) {
            fuelGauge.setValue(ship.getFuel());
            oxygenGauge.setValue(ship.getOxygen());
            energyGauge.setValue(ship.getEnergy());
            hullGauge.setValue(ship.getHullIntegrity());

            boolean canLaunch = controller.canLaunchExpedition(planet);
            launchExpeditionButton.setEnabled(canLaunch && pendingEvent == null);
            finalDirectiveButton.setVisible(controller.canInitiateFinalDirective());

            String readinessMsg = controller.getExpeditionReadinessMessage(planet);
            readinessStatusLabel.setText(readinessMsg);
            if (canLaunch) {
                readinessStatusLabel.setForeground(OrbitTheme.STATUS_NOMINAL);
            } else {
                readinessStatusLabel.setForeground(OrbitTheme.STATUS_ALERT);
            }
        }

        revalidate();
        repaint();
    }

    private void handleLaunchExpedition() {
        if (!controller.isGameActive()) {
            return;
        }

        Planet planet = controller.getCurrentLocation();
        if (!controller.isExplorationSupported(planet)) {
            SciFiDialog.showMessage(
                    frame,
                    "EXPEDITION INHIBITED",
                    "HAZARDOUS ATMOSPHERE",
                    "Surface landings on " + planet.getName() + " are prohibited in this operational phase."
            );
            return;
        }

        if (!controller.canLaunchExpedition(planet)) {
            SciFiDialog.showMessage(
                    frame,
                    "EXPEDITION INHIBITED",
                    "INSUFFICIENT RESERVES",
                    "Surface excursion requires 10 Energy and 5 Oxygen. Verify ship vitals."
            );
            return;
        }

        String confirmMsg = "Confirm expedition deployment?\n\n"
                + "• Destination: " + planet.getName().toUpperCase() + " SURFACE\n"
                + "• Operational Cost: -10 Energy, -5 Oxygen\n\n"
                + "Deploy surface reconnaissance rover?";

        boolean confirmed = SciFiDialog.showConfirm(
                frame,
                "CONFIRM EXPEDITION DEPLOYMENT",
                "SURFACE EXCURSION // " + planet.getName().toUpperCase(),
                confirmMsg,
                "DEPLOY EXCURSION",
                "ABORT"
        );

        if (!confirmed) {
            return;
        }

        // Start expedition via controller
        ExpeditionResult result = controller.startExpedition();
        displayExpeditionResult(result);

        // Refresh UI telemetry
        refreshFromController();
        frame.getCommandCenterPanel().refreshFromController();
    }

    private void displayExpeditionResult(ExpeditionResult result) {
        dynamicActionContainer.removeAll();
        effectsContainer.removeAll();

        if (!result.isSuccess()) {
            eventTitleLabel.setText("EXPEDITION INHIBITED");
            eventClassificationBadge.setText("[ERROR]");
            eventClassificationBadge.setForeground(OrbitTheme.STATUS_ALERT);
            eventNarrativeArea.setText(result.getMessage());
            dynamicActionContainer.revalidate();
            dynamicActionContainer.repaint();
            return;
        }

        ExplorationEvent event = result.getEvent();
        this.pendingEvent = event;

        switch (result.getType()) {
            case KEPLER_CLIMAX -> {
                this.pendingEvent = null;
                eventTitleLabel.setText("SIGNAL SOURCE DISCOVERED // EXTRASOLAR MEGASTRUCTURE");
                eventClassificationBadge.setText("[PRIORITY CLIMAX]");
                eventClassificationBadge.setForeground(new Color(0xA8, 0x55, 0xF7));
                eventNarrativeArea.setText(result.getMessage());
                populateEffects(result.getEffectLog());

                SciFiDialog.showMessage(
                        frame,
                        "SIGNAL SOURCE DISCOVERED",
                        "KEPLER-186F EXTRASOLAR ANOMALY",
                        result.getMessage() + "\n\n+1 ALIEN ARTIFACT secured into stasis cargo hold!"
                );
            }
            case AUTO_RESOLVED -> {
                this.pendingEvent = null;
                eventTitleLabel.setText(event.getTitle().toUpperCase());
                eventClassificationBadge.setText("[" + event.getCategory().toUpperCase() + "]");
                eventClassificationBadge.setForeground(event.getCategory().equalsIgnoreCase(ExplorationEvent.CATEGORY_RESOURCE)
                        ? OrbitTheme.STATUS_NOMINAL : OrbitTheme.CYAN_ELECTRIC);
                eventNarrativeArea.setText(event.getDescription());
                populateEffects(result.getEffectLog());
            }
            case HAZARD_CHOICE -> {
                eventTitleLabel.setText(event.getTitle().toUpperCase());
                eventClassificationBadge.setText("[HAZARD CODE RED]");
                eventClassificationBadge.setForeground(OrbitTheme.STATUS_ALERT);
                eventNarrativeArea.setText(event.getDescription() + "\n\nPredicted Threat Consequence: " + event.getPrimaryPenaltyDescription());

                String counterItem = event.getCountermeasureItem();
                int available = controller.getInventory().getQuantity(counterItem);

                if (available > 0) {
                    SciFiButton counterBtn = new SciFiButton("DEPLOY COUNTERMEASURE (-1 " + counterItem + ")", SciFiButton.Variant.PRIMARY);
                    counterBtn.addActionListener(e -> resolveHazardChoice(event, true));
                    dynamicActionContainer.add(counterBtn);
                    dynamicActionContainer.add(Box.createVerticalStrut(6));
                }

                SciFiButton braceBtn = new SciFiButton("BRACE FOR IMPACT & ACCEPT DAMAGE (" + event.getPrimaryPenaltyDescription() + ")", SciFiButton.Variant.DANGER);
                braceBtn.addActionListener(e -> resolveHazardChoice(event, false));
                dynamicActionContainer.add(braceBtn);
            }
            case BEACON_CHOICE -> {
                eventTitleLabel.setText(event.getTitle().toUpperCase());
                eventClassificationBadge.setText("[ANOMALOUS BEACON]");
                eventClassificationBadge.setForeground(new Color(0xA8, 0x55, 0xF7));
                eventNarrativeArea.setText(event.getDescription() + "\n\nSelect tactical astrogation response:");

                List<String> choices = result.getAvailableChoices();
                for (int i = 0; i < choices.size(); i++) {
                    final int choiceIdx = i;
                    SciFiButton btn = new SciFiButton(choices.get(i), SciFiButton.Variant.PRIMARY);
                    btn.addActionListener(e -> resolveBeaconChoice(event, choiceIdx));
                    dynamicActionContainer.add(btn);
                    dynamicActionContainer.add(Box.createVerticalStrut(6));
                }
            }
            case MONOLITH_CHOICE -> {
                eventTitleLabel.setText(event.getTitle().toUpperCase());
                eventClassificationBadge.setText("[XENOLOGICAL MONOLITH]");
                eventClassificationBadge.setForeground(OrbitTheme.STATUS_WARNING);
                eventNarrativeArea.setText(event.getDescription() + "\n\nSelect tactical exploration protocol:");

                List<String> choices = result.getAvailableChoices();
                for (int i = 0; i < choices.size(); i++) {
                    final int choiceIdx = i;
                    SciFiButton btn = new SciFiButton(choices.get(i), SciFiButton.Variant.PRIMARY);
                    btn.addActionListener(e -> resolveMonolithChoice(event, choiceIdx));
                    dynamicActionContainer.add(btn);
                    dynamicActionContainer.add(Box.createVerticalStrut(6));
                }
            }
            default -> {}
        }

        dynamicActionContainer.revalidate();
        dynamicActionContainer.repaint();
        refreshFromController();
    }

    private void resolveHazardChoice(ExplorationEvent event, boolean useCountermeasure) {
        List<String> log = controller.resolveHazard(event, useCountermeasure);
        this.pendingEvent = null;
        dynamicActionContainer.removeAll();
        populateEffects(log);

        dynamicActionContainer.revalidate();
        dynamicActionContainer.repaint();
        refreshFromController();
        frame.getCommandCenterPanel().refreshFromController();
    }

    private void resolveBeaconChoice(ExplorationEvent event, int choiceIdx) {
        String outcome = controller.resolveBeaconEncounter(event, choiceIdx);
        this.pendingEvent = null;
        dynamicActionContainer.removeAll();
        eventNarrativeArea.setText(outcome);
        populateEffects(List.of("[+] BEACON TELEMETRY ARCHIVED: Deep-space signal coordinates cataloged."));

        dynamicActionContainer.revalidate();
        dynamicActionContainer.repaint();
        refreshFromController();
        frame.getCommandCenterPanel().refreshFromController();
    }

    private void resolveMonolithChoice(ExplorationEvent event, int choiceIdx) {
        String outcome = controller.resolveMonolithEncounter(event, choiceIdx);
        this.pendingEvent = null;
        dynamicActionContainer.removeAll();
        eventNarrativeArea.setText(outcome);
        populateEffects(List.of("[i] XENOLOGICAL SURVEY: Monolith inscriptions and harmonic field archived."));

        dynamicActionContainer.revalidate();
        dynamicActionContainer.repaint();
        refreshFromController();
        frame.getCommandCenterPanel().refreshFromController();
    }

    private void populateEffects(List<String> log) {
        effectsContainer.removeAll();
        if (log != null) {
            for (String entry : log) {
                JLabel l = new JLabel(entry);
                l.setFont(OrbitTheme.FONT_MONO);
                if (entry.startsWith("[+]")) {
                    l.setForeground(OrbitTheme.STATUS_NOMINAL);
                } else if (entry.startsWith("[-]")) {
                    l.setForeground(OrbitTheme.STATUS_ALERT);
                } else {
                    l.setForeground(OrbitTheme.TEXT_CYAN);
                }
                effectsContainer.add(l);
                effectsContainer.add(Box.createVerticalStrut(3));
            }
        }
        effectsContainer.revalidate();
        effectsContainer.repaint();
    }
}

