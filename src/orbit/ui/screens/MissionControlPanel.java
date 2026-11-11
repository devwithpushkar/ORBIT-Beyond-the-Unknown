package orbit.ui.screens;

import orbit.controllers.EndingResult;
import orbit.controllers.OrbitController;
import orbit.models.Inventory;
import orbit.models.MissionObjective;
import orbit.models.Spaceship;
import orbit.ui.OrbitFrame;
import orbit.ui.OrbitTheme;
import orbit.ui.components.SciFiButton;
import orbit.ui.components.SciFiDialog;
import orbit.ui.components.SciFiPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

/**
 * MissionControlPanel displays the full campaign strategic directives matrix,
 * including active campaign stage, objective checklists, progress tracking,
 * High Command tactical advisories, and access to the final mission climax.
 */
public class MissionControlPanel extends JPanel {

    private final OrbitController controller;
    private final OrbitFrame frame;

    // Header readouts
    private JLabel stageBadge;
    private JLabel progressPctLabel;
    private JLabel completedCountLabel;
    private JProgressBar overallProgressBar;
    private JLabel advisoryTextLabel;

    // Stage filtering
    private String selectedStageFilter = "ALL";
    private JPanel objectivesContainer;

    // Actions
    private SciFiButton finalDirectiveButton;
    private SciFiButton returnButton;
    private SciFiButton summaryButton;

    public MissionControlPanel(OrbitController controller, OrbitFrame frame) {
        this.controller = controller;
        this.frame = frame;

        setBackground(OrbitTheme.BG_VOID);
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        buildUI();
    }

    private void buildUI() {
        // 1. Header & Strategic Status Deck
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // Subsystem Title Bar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);

        JLabel titleLabel = new JLabel("MISSION CONTROL // CAMPAIGN DIRECTIVES");
        titleLabel.setFont(OrbitTheme.FONT_TITLE);
        titleLabel.setForeground(OrbitTheme.CYAN_ELECTRIC);

        stageBadge = new JLabel("[STAGE 1 — DEPARTURE]");
        stageBadge.setFont(OrbitTheme.FONT_SUBTITLE);
        stageBadge.setForeground(OrbitTheme.STATUS_WARNING);

        titleBar.add(titleLabel, BorderLayout.WEST);
        titleBar.add(stageBadge, BorderLayout.EAST);
        topPanel.add(titleBar);
        topPanel.add(Box.createVerticalStrut(12));

        // Strategic Status Deck (3-column layout)
        JPanel statusDeck = new JPanel(new GridLayout(1, 2, 16, 0));
        statusDeck.setOpaque(false);

        // Card 1: Overall Progress & Telemetry
        SciFiPanel progressCard = new SciFiPanel("CAMPAIGN PROGRESS", "DIRECTIVE COMPLETION");
        JPanel progContent = new JPanel();
        progContent.setOpaque(false);
        progContent.setLayout(new BoxLayout(progContent, BoxLayout.Y_AXIS));

        JPanel progHeader = new JPanel(new BorderLayout());
        progHeader.setOpaque(false);
        progressPctLabel = new JLabel("0% COMPLETE");
        progressPctLabel.setFont(OrbitTheme.FONT_TITLE);
        progressPctLabel.setForeground(OrbitTheme.CYAN_ELECTRIC);

        completedCountLabel = new JLabel("0 / 12 Objectives Completed");
        completedCountLabel.setFont(OrbitTheme.FONT_MONO);
        completedCountLabel.setForeground(OrbitTheme.TEXT_SECONDARY);

        progHeader.add(progressPctLabel, BorderLayout.WEST);
        progHeader.add(completedCountLabel, BorderLayout.EAST);

        overallProgressBar = new JProgressBar(0, 100);
        overallProgressBar.setValue(0);
        overallProgressBar.setPreferredSize(new Dimension(200, 12));
        overallProgressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        overallProgressBar.setForeground(OrbitTheme.CYAN_ELECTRIC);
        overallProgressBar.setBackground(OrbitTheme.BG_VOID);
        overallProgressBar.setBorderPainted(false);

        progContent.add(progHeader);
        progContent.add(Box.createVerticalStrut(8));
        progContent.add(overallProgressBar);
        progressCard.add(progContent, BorderLayout.CENTER);

        // Card 2: High Command Strategic Advisory
        SciFiPanel advisoryCard = new SciFiPanel("OPERATIONAL DIRECTIVE", "HIGH COMMAND ADVISORY");
        JPanel advContent = new JPanel(new BorderLayout(8, 8));
        advContent.setOpaque(false);

        JLabel promptIcon = new JLabel(">> ");
        promptIcon.setFont(OrbitTheme.FONT_TITLE);
        promptIcon.setForeground(OrbitTheme.STATUS_WARNING);
        promptIcon.setVerticalAlignment(SwingConstants.TOP);

        advisoryTextLabel = new JLabel("<html>Disengage mooring clamps and burn propulsion vector to Mars via Navigation Console.</html>");
        advisoryTextLabel.setFont(OrbitTheme.FONT_BODY);
        advisoryTextLabel.setForeground(OrbitTheme.TEXT_PRIMARY);

        advContent.add(promptIcon, BorderLayout.WEST);
        advContent.add(advisoryTextLabel, BorderLayout.CENTER);
        advisoryCard.add(advContent, BorderLayout.CENTER);

        statusDeck.add(progressCard);
        statusDeck.add(advisoryCard);

        topPanel.add(statusDeck);
        topPanel.add(Box.createVerticalStrut(12));

        // Filter Bar (Tabs)
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterBar.setOpaque(false);

        JLabel filterLabel = new JLabel("FILTER BY STAGE: ");
        filterLabel.setFont(OrbitTheme.FONT_TINY_MONO);
        filterLabel.setForeground(OrbitTheme.TEXT_MUTED);
        filterBar.add(filterLabel);

        ButtonGroup filterGroup = new ButtonGroup();
        String[] filters = {"ALL", MissionObjective.STAGE_1, MissionObjective.STAGE_2, MissionObjective.STAGE_3};
        for (String filter : filters) {
            String label = filter.equals("ALL") ? "ALL DIRECTIVES (12)" : filter;
            SciFiButton btn = new SciFiButton(label, filter.equals("ALL") ? SciFiButton.Variant.PRIMARY : SciFiButton.Variant.SECONDARY);
            btn.setPreferredSize(new Dimension(filter.equals("ALL") ? 160 : 220, 28));
            btn.addActionListener(e -> {
                this.selectedStageFilter = filter;
                refreshDirectivesList();
            });
            filterBar.add(btn);
        }

        topPanel.add(filterBar);
        add(topPanel, BorderLayout.NORTH);

        // 2. Center: Scrollable Directives Matrix
        objectivesContainer = new JPanel();
        objectivesContainer.setOpaque(false);
        objectivesContainer.setLayout(new BoxLayout(objectivesContainer, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(objectivesContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(OrbitTheme.BORDER_SUBTLE, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);

        returnButton = new SciFiButton("RETURN TO COMMAND CENTER", SciFiButton.Variant.SECONDARY);
        returnButton.addActionListener(e -> {
            if (controller.isCampaignCompleted()) {
                frame.showScreen(OrbitFrame.SCREEN_ENDING);
            } else {
                frame.showScreen(OrbitFrame.SCREEN_COMMAND_CENTER);
            }
        });
        leftActions.add(returnButton);

        summaryButton = new SciFiButton("VIEW CAMPAIGN REPORT", SciFiButton.Variant.SECONDARY);
        summaryButton.setVisible(false);
        summaryButton.addActionListener(e -> frame.showScreen(OrbitFrame.SCREEN_ENDING));
        leftActions.add(summaryButton);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        finalDirectiveButton = new SciFiButton("RESOLVE FINAL MISSION DIRECTIVE", SciFiButton.Variant.PRIMARY);
        finalDirectiveButton.setVisible(false);
        finalDirectiveButton.addActionListener(e -> promptFinalDirectiveDialog());
        rightActions.add(finalDirectiveButton);

        bottomBar.add(leftActions, BorderLayout.WEST);
        bottomBar.add(rightActions, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);
    }

    public void refreshFromController() {
        if (!controller.isGameActive()) {
            return;
        }

        String stage = controller.getCurrentStage();
        stageBadge.setText("[" + stage.toUpperCase() + "]");

        int progress = controller.getMissionProgress();
        progressPctLabel.setText(progress + "% COMPLETE");
        overallProgressBar.setValue(progress);

        List<MissionObjective> allObjs = controller.getObjectives();
        int completedCount = 0;
        for (MissionObjective obj : allObjs) {
            if (obj.isCompleted()) completedCount++;
        }
        completedCountLabel.setText(completedCount + " / " + allObjs.size() + " Objectives Completed");

        String advice = controller.getNextDirectiveAdvice();
        advisoryTextLabel.setText("<html>" + advice + "</html>");

        boolean canFinal = controller.canInitiateFinalDirective();
        finalDirectiveButton.setVisible(canFinal);

        boolean isCompleted = controller.isCampaignCompleted();
        summaryButton.setVisible(isCompleted);

        refreshDirectivesList();
        revalidate();
        repaint();
    }

    private void refreshDirectivesList() {
        objectivesContainer.removeAll();

        List<MissionObjective> allObjs = controller.getObjectives();
        int index = 1;
        for (MissionObjective obj : allObjs) {
            if (selectedStageFilter.equals("ALL") || obj.getStage().equalsIgnoreCase(selectedStageFilter)) {
                objectivesContainer.add(createObjectiveCard(obj, index));
                objectivesContainer.add(Box.createVerticalStrut(8));
            }
            index++;
        }

        objectivesContainer.revalidate();
        objectivesContainer.repaint();
    }

    private JPanel createObjectiveCard(MissionObjective obj, int index) {
        JPanel card = new JPanel(new BorderLayout(12, 6));
        card.setOpaque(true);
        card.setBackground(new Color(0x0E, 0x1A, 0x2E, 230));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(obj.isCompleted() ? OrbitTheme.STATUS_NOMINAL :
                        (obj.isInProgress() ? OrbitTheme.CYAN_ELECTRIC : OrbitTheme.BORDER_SUBTLE), 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        // Header: Title and Status Badge
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);

        JLabel titleLabel = new JLabel(String.format("[%02d] %s", index, obj.getTitle()));
        titleLabel.setFont(OrbitTheme.FONT_SUBTITLE);
        titleLabel.setForeground(obj.isCompleted() ? OrbitTheme.TEXT_MUTED : OrbitTheme.TEXT_PRIMARY);

        JLabel statusBadge = new JLabel();
        statusBadge.setFont(OrbitTheme.FONT_MONO);
        if (obj.isCompleted()) {
            statusBadge.setText("[V] COMPLETED");
            statusBadge.setForeground(OrbitTheme.STATUS_NOMINAL);
        } else if (obj.isInProgress()) {
            statusBadge.setText("[>] IN PROGRESS");
            statusBadge.setForeground(OrbitTheme.CYAN_ELECTRIC);
        } else {
            statusBadge.setText("[ ] PENDING");
            statusBadge.setForeground(OrbitTheme.TEXT_MUTED);
        }

        header.add(titleLabel, BorderLayout.WEST);
        header.add(statusBadge, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        // Body: Description and Progress Info
        JPanel body = new JPanel(new BorderLayout(8, 4));
        body.setOpaque(false);

        JLabel descLabel = new JLabel("<html>" + obj.getDescription() + "</html>");
        descLabel.setFont(OrbitTheme.FONT_BODY);
        descLabel.setForeground(OrbitTheme.TEXT_SECONDARY);

        JPanel metaRow = new JPanel(new BorderLayout());
        metaRow.setOpaque(false);

        JLabel stageLabel = new JLabel(obj.getStage().toUpperCase());
        stageLabel.setFont(OrbitTheme.FONT_TINY_MONO);
        stageLabel.setForeground(OrbitTheme.STATUS_WARNING);

        String progStr = obj.getProgressInfo().isEmpty() ? "" : "PROGRESS: " + obj.getProgressInfo();
        JLabel progLabel = new JLabel(progStr);
        progLabel.setFont(OrbitTheme.FONT_TINY_MONO);
        progLabel.setForeground(obj.isCompleted() ? OrbitTheme.STATUS_NOMINAL : OrbitTheme.CYAN_ELECTRIC);

        metaRow.add(stageLabel, BorderLayout.WEST);
        metaRow.add(progLabel, BorderLayout.EAST);

        body.add(descLabel, BorderLayout.CENTER);
        body.add(metaRow, BorderLayout.SOUTH);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    /**
     * Prompts the player with the authentic climax decision dialog when on Kepler-186f
     * with the signal source discovered.
     */
    public void promptFinalDirectiveDialog() {
        if (!controller.canInitiateFinalDirective()) {
            return;
        }

        Spaceship ship = controller.getSpaceship();
        Inventory inv = controller.getInventory();
        int fuel = (ship != null) ? ship.getFuel() : 0;
        int fuelCells = (inv != null) ? inv.getQuantity(Inventory.FUEL_CELLS) : 0;

        StringBuilder msg = new StringBuilder();
        msg.append("You have reached the origin point of the deep-space transmission.\n");
        msg.append("Beneath the basalt plateaus of Kepler-186f, the alien megastructure\n");
        msg.append("stands active, casting harmonic geometries across the cavern walls.\n\n");
        msg.append("High Command directives mandate securing the Alien Artifact and returning to Sol.\n");
        msg.append("Yet an open trans-dimensional gateway hums on the dais, offering a journey beyond.\n\n");
        msg.append("RETURN TRAJECTORY VIABILITY:\n");
        msg.append("• Vessel Operational : ").append(ship != null && ship.isOperational() ? "OPERATIONAL" : "CRITICAL CASUALTY").append("\n");
        msg.append("• Propulsion Fuel    : ").append(fuel).append("% (Return burn requires 20 Fuel)\n");
        msg.append("• Cargo Fuel Cells   : ").append(fuelCells).append(" available\n\n");

        if (fuel < 20 && fuelCells > 0) {
            msg.append("NOTICE: Emergency refueling with Fuel Cells will be prompted if initiated.\n\n");
        }

        msg.append("Select your final mission directive:");

        String[] options = {
                "INITIATE RETURN VOYAGE TO SOL-EARTH",
                "VENTURE INTO THE UNKNOWN",
                "SUSPEND DECISION / CANCEL"
        };

        int choice = SciFiDialog.showOptionDialog(
                frame,
                "FINAL MISSION DIRECTIVE",
                "KEPLER-186F // ANOMALY CONVERGENCE POINT",
                msg.toString(),
                options
        );

        if (choice == 0) {
            // Choice 1: Return Voyage
            if (controller.canPerformEmergencyRefuel()) {
                int cellsToUse = controller.getEmergencyRefuelCellsNeeded();
                Spaceship shipObj = controller.getSpaceship();
                Inventory invObj = controller.getInventory();
                int currentFuel = (shipObj != null) ? shipObj.getFuel() : 0;
                int availableCells = (invObj != null) ? invObj.getQuantity(Inventory.FUEL_CELLS) : 0;

                String refuelMsg = "Current fuel (" + currentFuel + "%) is insufficient for return burn (20 Fuel).\n"
                        + "Cargo manifest contains " + availableCells + " Fuel Cell(s).\n\n"
                        + "Transfer " + cellsToUse + " Fuel Cell(s) to propulsion drive?";

                boolean confirmed = SciFiDialog.showConfirm(
                        frame,
                        "EMERGENCY PROPULSION RESTORATION",
                        "TRANSFER PROPELLANT",
                        refuelMsg,
                        "YES",
                        "NO"
                );

                if (confirmed) {
                    int transferred = controller.performEmergencyRefuel();
                    if (transferred > 0) {
                        SciFiDialog.showMessage(
                                frame,
                                "EMERGENCY REFUELING",
                                "PROPULSION RESTORED",
                                transferred + " Fuel Cell(s) transferred into propulsion drive (+ " + (transferred * 20) + " Fuel)."
                        );
                    }
                }
            }

            EndingResult result = controller.resolveReturnEnding();
            if (result != null) {
                frame.getEndingPanel().displayEnding(result);
                frame.showScreen(OrbitFrame.SCREEN_ENDING);
            }
        } else if (choice == 1) {
            // Choice 2: Step into the Unknown
            boolean confirm = SciFiDialog.showConfirm(
                    frame,
                    "TRANSCENDENCE PROTOCOL",
                    "STEP INTO THE UNKNOWN",
                    "Commander, choosing to remain and enter the alien gateway is permanent.\n"
                    + "You will leave vessel ODYSSEY-07 and human civilization behind,\n"
                    + "stepping across the galactic threshold into uncharted existence.\n\n"
                    + "Confirm decision to step into the gateway?"
            );
            if (confirm) {
                EndingResult result = controller.resolveUnknownEnding();
                if (result != null) {
                    frame.getEndingPanel().displayEnding(result);
                    frame.showScreen(OrbitFrame.SCREEN_ENDING);
                }
            }
        }
    }
}
