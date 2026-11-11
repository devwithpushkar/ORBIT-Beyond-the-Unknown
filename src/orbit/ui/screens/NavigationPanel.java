package orbit.ui.screens;

import orbit.controllers.NavigationResult;
import orbit.controllers.OrbitController;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Full graphical Navigation and Galaxy Map subsystem for ORBIT: Beyond the Unknown.
 * Provides interactive interplanetary map plotting, trajectory calculation,
 * fuel reserve verification, destination unlock visualization, and burn execution.
 */
public class NavigationPanel extends JPanel {

    private final OrbitFrame frame;
    private final OrbitController controller;

    private Planet selectedPlanet;

    // Header labels
    private JLabel currentOrbitBadge;
    private ResourceGauge fuelGauge;

    // Flight Info Panel components
    private JLabel targetNameLabel;
    private JLabel targetStatusBadge;
    private JTextArea targetDescArea;
    private JTextArea targetEnvArea;
    private JLabel departureLabel;
    private JLabel destinationLabel;
    private JLabel burnCostLabel;
    private JLabel fuelRemainingLabel;
    private JLabel vectorStatusLabel;
    private SciFiButton engageBurnButton;

    // Galaxy Map Canvas
    private GalaxyMapCanvas mapCanvas;

    public NavigationPanel(OrbitFrame frame, OrbitController controller) {
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

        JLabel title = new JLabel("NAVIGATION // GALAXY MAP & FLIGHT VECTOR PLOTTER");
        title.setFont(OrbitTheme.FONT_HEADER_MONO);
        title.setForeground(OrbitTheme.CYAN_ELECTRIC);

        JLabel subTitle = new JLabel("ASTROGATION MATRIX // ORBITAL TRAJECTORY COMPUTATION");
        subTitle.setFont(OrbitTheme.FONT_TINY_MONO);
        subTitle.setForeground(OrbitTheme.TEXT_MUTED);

        leftHeader.add(title);
        leftHeader.add(Box.createVerticalStrut(2));
        leftHeader.add(subTitle);

        // Right Header Badges
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightHeader.setOpaque(false);

        currentOrbitBadge = createBadge("CURRENT ORBIT: EARTH", OrbitTheme.STATUS_NOMINAL);
        fuelGauge = new ResourceGauge("PROPULSION FUEL", 100);
        fuelGauge.setPreferredSize(new Dimension(180, 46));

        rightHeader.add(currentOrbitBadge);
        rightHeader.add(fuelGauge);

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

        // --- Left: Galaxy Map Canvas (Interactive graphical map) ---
        mapCanvas = new GalaxyMapCanvas();
        SciFiPanel mapWrapper = new SciFiPanel("INTERPLANETARY SECTOR CHART", "CLICK CELESTIAL BODY TO PLOT VECTOR");
        mapWrapper.add(mapCanvas, BorderLayout.CENTER);
        splitContainer.add(mapWrapper, BorderLayout.CENTER);

        // --- Right: Flight Vector Details & Telemetry ---
        JPanel flightPanel = createFlightInfoPanel();
        flightPanel.setPreferredSize(new Dimension(420, 600));
        splitContainer.add(flightPanel, BorderLayout.EAST);

        add(splitContainer, BorderLayout.CENTER);
    }

    private JPanel createFlightInfoPanel() {
        SciFiPanel panel = new SciFiPanel("FLIGHT VECTOR TELEMETRY", "TRAJECTORY CALCULATION");
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // 1. Target Name & Status Banner
        JPanel targetBanner = new JPanel(new BorderLayout());
        targetBanner.setOpaque(false);
        targetBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrbitTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        targetBanner.setBackground(OrbitTheme.BG_CARD);

        targetNameLabel = new JLabel("TARGET: MARS");
        targetNameLabel.setFont(OrbitTheme.FONT_HEADER_MONO);
        targetNameLabel.setForeground(OrbitTheme.CYAN_ELECTRIC);

        targetStatusBadge = new JLabel("[CORRIDOR AUTHORIZED]");
        targetStatusBadge.setFont(OrbitTheme.FONT_TINY_MONO);
        targetStatusBadge.setForeground(OrbitTheme.STATUS_NOMINAL);

        targetBanner.add(targetNameLabel, BorderLayout.WEST);
        targetBanner.add(targetStatusBadge, BorderLayout.EAST);
        content.add(targetBanner);
        content.add(Box.createVerticalStrut(10));

        // 2. Planet Description & Environment
        JLabel descHeader = new JLabel("PLANETARY SURVEY:");
        descHeader.setFont(OrbitTheme.FONT_TINY_MONO);
        descHeader.setForeground(OrbitTheme.TEXT_MUTED);
        content.add(descHeader);
        content.add(Box.createVerticalStrut(2));

        targetDescArea = createReadonlyTextArea(3);
        content.add(targetDescArea);
        content.add(Box.createVerticalStrut(6));

        JLabel envHeader = new JLabel("ATMOSPHERIC CONDITIONS & HAZARDS:");
        envHeader.setFont(OrbitTheme.FONT_TINY_MONO);
        envHeader.setForeground(OrbitTheme.TEXT_MUTED);
        content.add(envHeader);
        content.add(Box.createVerticalStrut(2));

        targetEnvArea = createReadonlyTextArea(3);
        content.add(targetEnvArea);
        content.add(Box.createVerticalStrut(12));

        // 3. Burn Telemetry Card
        JPanel burnCard = new JPanel(new GridLayout(4, 1, 0, 4));
        burnCard.setOpaque(true);
        burnCard.setBackground(OrbitTheme.BG_CARD);
        burnCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrbitTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        departureLabel = createTelemetryRow("DEPARTURE POINT", "EARTH");
        destinationLabel = createTelemetryRow("DESTINATION", "MARS");
        burnCostLabel = createTelemetryRow("PROPULSION BURN", "20 FUEL");
        fuelRemainingLabel = createTelemetryRow("REMAINING AFTER BURN", "80 FUEL");

        burnCard.add(departureLabel);
        burnCard.add(destinationLabel);
        burnCard.add(burnCostLabel);
        burnCard.add(fuelRemainingLabel);

        content.add(burnCard);
        content.add(Box.createVerticalStrut(12));

        // 4. Pre-Flight Status Message
        vectorStatusLabel = new JLabel("TRAJECTORY CLEARED // ALL CHANNELS NOMINAL");
        vectorStatusLabel.setFont(OrbitTheme.FONT_TINY_MONO);
        vectorStatusLabel.setForeground(OrbitTheme.STATUS_NOMINAL);
        vectorStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(vectorStatusLabel);
        content.add(Box.createVerticalGlue());

        // 5. Action Buttons
        JPanel actionRow = new JPanel(new GridLayout(2, 1, 0, 8));
        actionRow.setOpaque(false);

        engageBurnButton = new SciFiButton("ENGAGE PROPULSION BURN", SciFiButton.Variant.PRIMARY);
        engageBurnButton.addActionListener(e -> handleEngageBurn());

        SciFiButton returnButton = new SciFiButton("RETURN TO COMMAND CENTER", SciFiButton.Variant.SECONDARY);
        returnButton.addActionListener(e -> frame.showScreen(OrbitFrame.SCREEN_COMMAND_CENTER));

        actionRow.add(engageBurnButton);
        actionRow.add(returnButton);

        content.add(actionRow);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JTextArea createReadonlyTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setFont(OrbitTheme.FONT_BODY);
        area.setForeground(OrbitTheme.TEXT_SECONDARY);
        area.setBackground(OrbitTheme.BG_INPUT);
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrbitTheme.BORDER_MUTED, 1),
                new EmptyBorder(6, 8, 6, 8)
        ));
        return area;
    }

    private JLabel createTelemetryRow(String label, String value) {
        JLabel l = new JLabel(String.format("%-22s: %s", label, value));
        l.setFont(OrbitTheme.FONT_MONO);
        l.setForeground(OrbitTheme.TEXT_PRIMARY);
        return l;
    }

    // --- Screen Lifecycle & Briefing ---

    /**
     * Invoked when this screen becomes active in OrbitFrame's CardLayout.
     * Triggers the initial Mars navigation briefing if it has not yet been presented,
     * and refreshes the galaxy chart.
     */
    public void onEnterScreen() {
        refreshFromController();

        // Check if Mars needs the initial mission navigation briefing
        if (!controller.isMarsUnlocked()) {
            String briefingText = controller.getInitialNavigationBriefingText();
            boolean newlyUnlocked = controller.triggerInitialNavigationBriefing();
            if (newlyUnlocked) {
                // Refresh map so Mars immediately reflects its newly authorized status
                refreshFromController();

                // Display High Command authorization directive dialog
                SciFiDialog.showMessage(
                        frame,
                        "MISSION CONTROL // FLIGHT AUTHORIZATION DIRECTIVE",
                        "PRIORITY TRANSMISSION // SOL-EARTH",
                        briefingText
                );
            }
        }

        // Check if Outer Moons (Europa & Titan) need Stage 2 authorization briefing
        if (controller.needsOuterMoonsNavigationBriefing()) {
            String briefingText = controller.getOuterMoonsNavigationBriefingText();
            boolean newlyLogged = controller.triggerOuterMoonsNavigationBriefing();
            if (newlyLogged) {
                refreshFromController();
                SciFiDialog.showMessage(
                        frame,
                        "MISSION CONTROL // FLIGHT AUTHORIZATION DIRECTIVE",
                        "PRIORITY TRANSMISSION // STAGE 2 DEEP VOYAGER",
                        briefingText
                );
            }
        }

        // Check if Kepler-186f needs Stage 3 authorization briefing
        if (controller.needsKeplerNavigationBriefing()) {
            String briefingText = controller.getKeplerNavigationBriefingText();
            boolean newlyLogged = controller.triggerKeplerNavigationBriefing();
            if (newlyLogged) {
                refreshFromController();
                SciFiDialog.showMessage(
                        frame,
                        "MISSION CONTROL // EXTRASOLAR TRAJECTORY DIRECTIVE",
                        "PRIORITY TRANSMISSION // STAGE 3 BEYOND THE UNKNOWN",
                        briefingText
                );
            }
        }

        // Ensure a sensible default selection
        if (selectedPlanet == null || selectedPlanet.getName().equalsIgnoreCase(controller.getCurrentLocation().getName())) {
            Planet mars = (controller.getGameState() != null) ? controller.getGameState().getPlanetByName("Mars") : null;
            if (mars != null && mars.isDiscovered()) {
                selectPlanet(mars);
            } else {
                selectPlanet(controller.getCurrentLocation());
            }
        } else {
            selectPlanet(selectedPlanet);
        }
    }

    /**
     * Refreshes all UI telemetry and updates the galaxy map canvas.
     */
    public void refreshFromController() {
        if (!controller.isGameActive()) {
            return;
        }

        Planet current = controller.getCurrentLocation();
        Spaceship ship = controller.getSpaceship();

        if (current != null) {
            currentOrbitBadge.setText("CURRENT ORBIT: " + current.getName().toUpperCase());
        }
        if (ship != null) {
            fuelGauge.setValue(ship.getFuel());
        }

        if (selectedPlanet != null) {
            // Re-resolve selected planet reference from current GameState
            Planet updated = controller.getGameState().getPlanetByName(selectedPlanet.getName());
            if (updated != null) {
                selectedPlanet = updated;
            }
            updateFlightTelemetry();
        }

        mapCanvas.repaint();
    }

    private void selectPlanet(Planet planet) {
        if (planet == null) return;
        this.selectedPlanet = planet;
        updateFlightTelemetry();
        mapCanvas.repaint();
    }

    private void updateFlightTelemetry() {
        if (selectedPlanet == null || !controller.isGameActive()) {
            return;
        }

        Planet current = controller.getCurrentLocation();
        Spaceship ship = controller.getSpaceship();

        targetNameLabel.setText("TARGET: " + selectedPlanet.getName().toUpperCase());
        targetDescArea.setText(selectedPlanet.getDescription());
        targetEnvArea.setText(selectedPlanet.getEnvironmentDescription());

        departureLabel.setText(String.format("%-22s: %s", "DEPARTURE POINT", current.getName().toUpperCase()));
        destinationLabel.setText(String.format("%-22s: %s", "DESTINATION", selectedPlanet.getName().toUpperCase()));

        boolean isCurrent = selectedPlanet.getName().equalsIgnoreCase(current.getName());
        boolean isUnlocked = selectedPlanet.isDiscovered();
        int fuelCost = controller.getFuelCost(selectedPlanet);
        int currentFuel = ship.getFuel();
        int remainingFuel = currentFuel - fuelCost;

        burnCostLabel.setText(String.format("%-22s: %d FUEL", "PROPULSION BURN", isCurrent ? 0 : fuelCost));
        fuelRemainingLabel.setText(String.format("%-22s: %d FUEL", "REMAINING AFTER BURN", isCurrent ? currentFuel : remainingFuel));

        if (isCurrent) {
            targetStatusBadge.setText("[PARKING ORBIT]");
            targetStatusBadge.setForeground(OrbitTheme.STATUS_NOMINAL);
            vectorStatusLabel.setText("VESSEL CURRENTLY IN ORBITAL MOORING AT THIS DESTINATION");
            vectorStatusLabel.setForeground(OrbitTheme.TEXT_MUTED);
            engageBurnButton.setEnabled(false);
        } else if (!isUnlocked) {
            targetStatusBadge.setText("[NAVIGATION LOCKED]");
            targetStatusBadge.setForeground(OrbitTheme.STATUS_ALERT);
            vectorStatusLabel.setText("COORDINATES RESTRICTED // AWAIT HIGH COMMAND AUTHORIZATION");
            vectorStatusLabel.setForeground(OrbitTheme.STATUS_ALERT);
            engageBurnButton.setEnabled(false);
        } else if (currentFuel < fuelCost) {
            targetStatusBadge.setText("[CORRIDOR AUTHORIZED]");
            targetStatusBadge.setForeground(OrbitTheme.STATUS_NOMINAL);
            vectorStatusLabel.setText(String.format("INSUFFICIENT PROPELLANT (REQUIRES %d FUEL, CURRENT: %d%%)", fuelCost, currentFuel));
            vectorStatusLabel.setForeground(OrbitTheme.STATUS_WARNING);
            engageBurnButton.setEnabled(false);
        } else {
            targetStatusBadge.setText("[CORRIDOR AUTHORIZED]");
            targetStatusBadge.setForeground(OrbitTheme.STATUS_NOMINAL);
            vectorStatusLabel.setText("TRAJECTORY CLEARED // ALL CHANNELS NOMINAL // READY TO BURN");
            vectorStatusLabel.setForeground(OrbitTheme.STATUS_NOMINAL);
            engageBurnButton.setEnabled(true);
        }
    }

    private void handleEngageBurn() {
        if (selectedPlanet == null || !controller.isGameActive()) {
            return;
        }

        Planet current = controller.getCurrentLocation();
        Planet target = selectedPlanet;
        int fuelCost = controller.getFuelCost(target);
        int currentFuel = controller.getSpaceship().getFuel();

        // Fail-safe checks using controller
        if (target.getName().equalsIgnoreCase(current.getName())) {
            SciFiDialog.showMessage(
                    frame,
                    "NAVIGATION REJECTED",
                    "ALREADY IN ORBIT",
                    "Vessel ODYSSEY-07 is already maintaining a stable orbit around " + target.getName() + "."
            );
            return;
        }

        if (!controller.isDestinationUnlocked(target)) {
            SciFiDialog.showMessage(
                    frame,
                    "NAVIGATION LOCKED",
                    "DESTINATION RESTRICTED",
                    "Flight corridor to " + target.getName() + " is restricted in this operational phase."
            );
            return;
        }

        if (currentFuel < fuelCost) {
            SciFiDialog.showMessage(
                    frame,
                    "PROPULSION ABORTED",
                    "INSUFFICIENT FUEL",
                    "Interplanetary burn to " + target.getName() + " requires " + fuelCost + " Fuel, but reserves are at " + currentFuel + "%."
            );
            return;
        }

        // Flight confirmation dialog
        String confirmMsg = "Confirm flight burn sequence?\n\n"
                + "• Departure Point : " + current.getName().toUpperCase() + "\n"
                + "• Destination     : " + target.getName().toUpperCase() + "\n"
                + "• Propulsion Burn : -" + fuelCost + " FUEL\n"
                + "• Projected Fuel  : " + (currentFuel - fuelCost) + " FUEL\n\n"
                + "Execute propulsion burn vector?";

        boolean confirmed = SciFiDialog.showConfirm(
                frame,
                "FLIGHT VECTOR CONFIRMATION",
                "INTERPLANETARY BURN SEQUENCE",
                confirmMsg,
                "EXECUTE BURN",
                "ABORT VECTOR"
        );

        if (!confirmed) {
            return;
        }

        // Execute travel through authoritative controller
        NavigationResult result = controller.travelTo(target);

        if (result.isSuccess()) {
            // Update UI state
            refreshFromController();
            frame.getCommandCenterPanel().refreshFromController();

            // Display arrival confirmation
            SciFiDialog.showMessage(
                    frame,
                    "ORBITAL INSERTION CONFIRMED",
                    "ARRIVAL AT " + target.getName().toUpperCase(),
                    "Propulsion burn executed successfully (-" + result.getFuelConsumed() + " Fuel).\n\n"
                    + "Vessel ODYSSEY-07 has completed orbital insertion and established parking orbit at "
                    + target.getName() + ".\n\n"
                    + "Atmospheric Telemetry: " + target.getEnvironmentDescription()
            );
        } else {
            SciFiDialog.showMessage(
                    frame,
                    "NAVIGATION FAILED",
                    "VECTOR ABORTED",
                    result.getMessage()
            );
        }
    }

    // ==========================================
    // Custom Galaxy Map Canvas Component
    // ==========================================

    private class GalaxyMapCanvas extends JComponent {

        private static class PlanetNode {
            final String name;
            final double relX;
            final double relY;
            final int radius;
            final Color primaryColor;
            final Color secondaryColor;
            final String sectorTag;

            PlanetNode(String name, double relX, double relY, int radius, Color primary, Color secondary, String sectorTag) {
                this.name = name;
                this.relX = relX;
                this.relY = relY;
                this.radius = radius;
                this.primaryColor = primary;
                this.secondaryColor = secondary;
                this.sectorTag = sectorTag;
            }
        }

        private final List<PlanetNode> planetNodes = new ArrayList<>();
        private PlanetNode hoveredNode = null;

        public GalaxyMapCanvas() {
            setOpaque(false);

            // 5 Celestial Destinations with relative layout coordinates
            planetNodes.add(new PlanetNode("Earth", 0.16, 0.50, 24, new Color(0x25, 0x63, 0xEB), new Color(0x38, 0xBD, 0xF8), "SOL // 00-INNER"));
            planetNodes.add(new PlanetNode("Mars", 0.34, 0.28, 20, new Color(0xDC, 0x26, 0x26), new Color(0xF9, 0x73, 0x16), "SOL // 01-MID"));
            planetNodes.add(new PlanetNode("Europa", 0.50, 0.68, 18, new Color(0x06, 0xB6, 0xD4), new Color(0xE0, 0xF2, 0xFE), "JOVIAN SYSTEM"));
            planetNodes.add(new PlanetNode("Titan", 0.68, 0.32, 22, new Color(0xD9, 0x77, 0x06), new Color(0xFB, 0xBF, 0x24), "SATURNIAN SYSTEM"));
            planetNodes.add(new PlanetNode("Kepler-186f", 0.86, 0.54, 26, new Color(0x7C, 0x3A, 0xED), new Color(0x00, 0xE5, 0xFF), "EXTRASOLAR SECTOR"));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    PlanetNode clicked = findNodeAt(e.getX(), e.getY());
                    if (clicked != null && controller.isGameActive()) {
                        Planet target = controller.getGameState().getPlanetByName(clicked.name);
                        if (target != null) {
                            selectPlanet(target);
                        }
                    }
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    PlanetNode node = findNodeAt(e.getX(), e.getY());
                    if (node != hoveredNode) {
                        hoveredNode = node;
                        setCursor(node != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                        repaint();
                    }
                }
            });
        }

        private PlanetNode findNodeAt(int mouseX, int mouseY) {
            int w = getWidth();
            int h = getHeight();
            for (PlanetNode node : planetNodes) {
                int px = (int) (node.relX * w);
                int py = (int) (node.relY * h);
                int hitRadius = node.radius + 14;
                int dx = mouseX - px;
                int dy = mouseY - py;
                if ((dx * dx + dy * dy) <= (hitRadius * hitRadius)) {
                    return node;
                }
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            OrbitTheme.enableAntiAliasing(g2);

            int width = getWidth();
            int height = getHeight();

            // 1. Dark Void & Grid
            g2.setColor(OrbitTheme.BG_VOID);
            g2.fillRect(0, 0, width, height);

            // Background Sector Grid Lines
            g2.setColor(new Color(0x0E, 0x18, 0x2C));
            int gridSpacing = 48;
            for (int x = 0; x < width; x += gridSpacing) {
                g2.drawLine(x, 0, x, height);
            }
            for (int y = 0; y < height; y += gridSpacing) {
                g2.drawLine(0, y, width, y);
            }

            // Sol Core Glow at far left
            g2.setColor(new Color(0xF5, 0x9E, 0x0B, 40));
            g2.fillOval(-60, height / 2 - 120, 140, 240);
            g2.setColor(new Color(0xF5, 0x9E, 0x0B, 180));
            g2.drawOval(-60, height / 2 - 120, 140, 240);

            // 2. Orbital Elliptical Tracks
            g2.setColor(new Color(0x1E, 0x30, 0x50, 120));
            g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f, 6.0f}, 0.0f));
            for (PlanetNode node : planetNodes) {
                int px = (int) (node.relX * width);
                int py = (int) (node.relY * height);
                g2.draw(new Ellipse2D.Double(0, py - 40, px * 2, 80));
            }

            // 3. Trajectory Vector Line (Current Location -> Selected Target)
            if (controller.isGameActive() && selectedPlanet != null) {
                Planet currentPlanet = controller.getCurrentLocation();
                if (currentPlanet != null && !selectedPlanet.getName().equalsIgnoreCase(currentPlanet.getName())) {
                    PlanetNode fromNode = getNode(currentPlanet.getName());
                    PlanetNode toNode = getNode(selectedPlanet.getName());
                    if (fromNode != null && toNode != null) {
                        drawTrajectoryVector(g2, width, height, fromNode, toNode);
                    }
                }
            }

            // 4. Render All Planet Nodes
            for (PlanetNode node : planetNodes) {
                paintPlanetNode(g2, width, height, node);
            }

            g2.dispose();
        }

        private PlanetNode getNode(String name) {
            for (PlanetNode n : planetNodes) {
                if (n.name.equalsIgnoreCase(name)) {
                    return n;
                }
            }
            return null;
        }

        private void drawTrajectoryVector(Graphics2D g2, int w, int h, PlanetNode from, PlanetNode to) {
            int x1 = (int) (from.relX * w);
            int y1 = (int) (from.relY * h);
            int x2 = (int) (to.relX * w);
            int y2 = (int) (to.relY * h);

            // Glowing cyan dashed vector line
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, new float[]{8.0f, 6.0f}, 0.0f));
            g2.setColor(new Color(0x00, 0xE5, 0xFF, 180));
            g2.drawLine(x1, y1, x2, y2);

            // Vector trajectory label along midpoint
            int midX = (x1 + x2) / 2;
            int midY = (y1 + y2) / 2 - 8;

            Planet targetPlanet = controller.getGameState().getPlanetByName(to.name);
            int cost = (targetPlanet != null) ? controller.getFuelCost(targetPlanet) : 20;

            g2.setFont(OrbitTheme.FONT_TINY_MONO);
            g2.setColor(OrbitTheme.CYAN_ELECTRIC);
            String costBadge = "VECTOR: -" + cost + " FUEL";
            FontMetrics fm = g2.getFontMetrics();
            int badgeW = fm.stringWidth(costBadge) + 12;

            g2.setColor(OrbitTheme.BG_VOID);
            g2.fillRect(midX - badgeW / 2, midY - 14, badgeW, 18);
            g2.setColor(OrbitTheme.BORDER_BRIGHT);
            g2.drawRect(midX - badgeW / 2, midY - 14, badgeW, 18);

            g2.setColor(OrbitTheme.CYAN_ELECTRIC);
            g2.drawString(costBadge, midX - fm.stringWidth(costBadge) / 2, midY);
        }

        private void paintPlanetNode(Graphics2D g2, int w, int h, PlanetNode node) {
            int cx = (int) (node.relX * w);
            int cy = (int) (node.relY * h);
            int r = node.radius;

            Planet planetModel = (controller.isGameActive()) ? controller.getGameState().getPlanetByName(node.name) : null;
            boolean isDiscovered = (planetModel != null && planetModel.isDiscovered());
            boolean isCurrent = (controller.isGameActive() && controller.getCurrentLocation().getName().equalsIgnoreCase(node.name));
            boolean isSelected = (selectedPlanet != null && selectedPlanet.getName().equalsIgnoreCase(node.name));
            boolean isHovered = (node == hoveredNode);

            // 1. Current Location Beacon Pulse Ring
            if (isCurrent) {
                g2.setColor(new Color(0x10, 0xB9, 0x81, 70));
                g2.fillOval(cx - r - 14, cy - r - 14, (r + 14) * 2, (r + 14) * 2);
                g2.setColor(OrbitTheme.STATUS_NOMINAL);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(cx - r - 12, cy - r - 12, (r + 12) * 2, (r + 12) * 2);

                // Small ship chevron indicator
                g2.setFont(OrbitTheme.FONT_TINY_MONO);
                g2.drawString("▲ VESSEL", cx - 22, cy - r - 18);
            }

            // 2. Selected Reticle
            if (isSelected) {
                g2.setColor(OrbitTheme.CYAN_ELECTRIC);
                g2.setStroke(new BasicStroke(1.5f));
                int boxSize = r + 10;
                int tick = 6;
                // Top-left
                g2.drawLine(cx - boxSize, cy - boxSize, cx - boxSize + tick, cy - boxSize);
                g2.drawLine(cx - boxSize, cy - boxSize, cx - boxSize, cy - boxSize + tick);
                // Top-right
                g2.drawLine(cx + boxSize, cy - boxSize, cx + boxSize - tick, cy - boxSize);
                g2.drawLine(cx + boxSize, cy - boxSize, cx + boxSize, cy - boxSize + tick);
                // Bottom-left
                g2.drawLine(cx - boxSize, cy + boxSize, cx - boxSize + tick, cy + boxSize);
                g2.drawLine(cx - boxSize, cy + boxSize, cx - boxSize, cy + boxSize - tick);
                // Bottom-right
                g2.drawLine(cx + boxSize, cy + boxSize, cx + boxSize - tick, cy + boxSize);
                g2.drawLine(cx + boxSize, cy + boxSize, cx + boxSize, cy + boxSize - tick);
            } else if (isHovered) {
                g2.setColor(new Color(0x3B, 0x82, 0xF6, 120));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawOval(cx - r - 6, cy - r - 6, (r + 6) * 2, (r + 6) * 2);
            }

            // 3. Planet Sphere Body
            if (isDiscovered) {
                // Gradient/shading on planet surface
                Paint oldPaint = g2.getPaint();
                RadialGradientPaint p = new RadialGradientPaint(
                        cx - r / 3.0f, cy - r / 3.0f, r * 1.2f,
                        new float[]{0.0f, 0.7f, 1.0f},
                        new Color[]{node.secondaryColor, node.primaryColor, OrbitTheme.BG_VOID}
                );
                g2.setPaint(p);
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                g2.setPaint(oldPaint);

                // Planet boundary line
                g2.setColor(node.secondaryColor);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);

                // Kepler beacon rings
                if (node.name.equalsIgnoreCase("Kepler-186f")) {
                    g2.setColor(new Color(0x00, 0xE5, 0xFF, 120));
                    g2.drawOval(cx - r - 4, cy - r - 4, (r + 4) * 2, (r + 4) * 2);
                }
            } else {
                // Locked Planet: Darkened, desaturated with warning outline
                g2.setColor(new Color(0x15, 0x1D, 0x2D));
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);

                g2.setColor(new Color(0xEF, 0x44, 0x44, 180));
                g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{3.0f, 3.0f}, 0.0f));
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);

                // Small padlock / restriction mark in center
                g2.setFont(OrbitTheme.FONT_TINY_MONO);
                g2.setColor(OrbitTheme.STATUS_ALERT);
                g2.drawString("✕", cx - 4, cy + 4);
            }

            // 4. Planet Label & Subtitle Text
            g2.setFont(OrbitTheme.FONT_MONO_BOLD);
            FontMetrics fm = g2.getFontMetrics();

            String label = node.name.toUpperCase();
            int labelW = fm.stringWidth(label);
            int labelX = cx - labelW / 2;
            int labelY = cy + r + 18;

            if (isDiscovered) {
                g2.setColor(isCurrent ? OrbitTheme.STATUS_NOMINAL : (isSelected ? OrbitTheme.CYAN_ELECTRIC : OrbitTheme.TEXT_PRIMARY));
                g2.drawString(label, labelX, labelY);
            } else {
                g2.setColor(OrbitTheme.TEXT_MUTED);
                g2.drawString(label, labelX, labelY);

                g2.setFont(OrbitTheme.FONT_TINY_MONO);
                g2.setColor(OrbitTheme.STATUS_ALERT);
                String lockedTag = "[RESTRICTED]";
                int tagW = g2.getFontMetrics().stringWidth(lockedTag);
                g2.drawString(lockedTag, cx - tagW / 2, labelY + 14);
            }

            // Sector Tag
            if (isDiscovered) {
                g2.setFont(OrbitTheme.FONT_TINY_MONO);
                g2.setColor(OrbitTheme.TEXT_MUTED);
                FontMetrics tfm = g2.getFontMetrics();
                int tagW = tfm.stringWidth(node.sectorTag);
                g2.drawString(node.sectorTag, cx - tagW / 2, labelY + 14);
            }
        }
    }
}

