package orbit.ui.screens;

import orbit.controllers.ItemUseResult;
import orbit.controllers.OrbitController;
import orbit.models.Inventory;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Full graphical Cargo Hold and Supply Dispenser interface for ORBIT: Beyond the Unknown.
 * Provides real-time manifest inspection, item lore, consumable vs non-consumable indicators,
 * live spaceship telemetry gauges, and authoritative item consumption controls.
 */
public class InventoryPanel extends JPanel {

    private final OrbitFrame frame;
    private final OrbitController controller;

    private String selectedItem = Inventory.METAL;

    // Header Badges
    private JLabel totalCargoBadge;
    private JLabel holdStatusBadge;

    // Left Column: Cargo Manifest Card List
    private JPanel manifestListContainer;
    private final List<ItemCardComponent> itemCards = new ArrayList<>();

    // Right Column: Telemetry & Dispenser Console
    private ResourceGauge fuelGauge;
    private ResourceGauge oxygenGauge;
    private ResourceGauge energyGauge;
    private ResourceGauge hullGauge;

    private JLabel selectedItemHeader;
    private JLabel consumableTypeBadge;
    private JTextArea itemDescriptionArea;
    private JLabel targetResourceLabel;
    private JLabel restorationAmountLabel;
    private JLabel currentToProjectedLabel;
    private JLabel dispenserStatusLabel;
    private SciFiButton dispenseButton;

    public InventoryPanel(OrbitFrame frame, OrbitController controller) {
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

        JLabel title = new JLabel("CARGO HOLD // LIFE SUPPORT & RESOURCE DISPENSER");
        title.setFont(OrbitTheme.FONT_HEADER_MONO);
        title.setForeground(OrbitTheme.CYAN_ELECTRIC);

        JLabel subTitle = new JLabel("ODYSSEY-07 LOGISTICS BAY // CONSUMABLES & SCIENTIFIC ARTIFACTS");
        subTitle.setFont(OrbitTheme.FONT_TINY_MONO);
        subTitle.setForeground(OrbitTheme.TEXT_MUTED);

        leftHeader.add(title);
        leftHeader.add(Box.createVerticalStrut(2));
        leftHeader.add(subTitle);

        // Right Header Badges
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightHeader.setOpaque(false);

        totalCargoBadge = createBadge("TOTAL CARGO: 8 UNITS", OrbitTheme.TEXT_CYAN);
        holdStatusBadge = createBadge("HOLD INTEGRITY: 100% NOMINAL", OrbitTheme.STATUS_NOMINAL);

        rightHeader.add(totalCargoBadge);
        rightHeader.add(holdStatusBadge);

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

        // --- Left: Cargo Manifest List (~60% width) ---
        SciFiPanel manifestPanel = new SciFiPanel("CARGO STORES MANIFEST", "CLICK SUPPLY ITEM TO INSPECT & DISPENSE");
        manifestListContainer = new JPanel();
        manifestListContainer.setOpaque(false);
        manifestListContainer.setLayout(new BoxLayout(manifestListContainer, BoxLayout.Y_AXIS));

        // Create 5 standard items
        addItemCard(Inventory.METAL, "Titanium-Carbide Plating", "Repairs spacecraft hull breach (+20 Hull)", "+20 HULL INTEGRITY", true);
        addItemCard(Inventory.ENERGY_CELLS, "Plasma Capacitor Battery", "Channels reserve plasma into reactor manifold (+25 Energy)", "+25 REACTOR ENERGY", true);
        addItemCard(Inventory.OXYGEN_CANISTERS, "Cryogenic Liquid O2 Flask", "Replenishes life-support atmospheric scrubbers (+25 Oxygen)", "+25 LIFE SUPPORT O2", true);
        addItemCard(Inventory.FUEL_CELLS, "Encapsulated Deuterium Rods", "Impulse & sub-light propellant for burn maneuvers (+25 Fuel)", "+25 PROPULSION FUEL", true);
        addItemCard(Inventory.ALIEN_ARTIFACTS, "Precursor Xenological Relics", "Exotic extraterrestrial artifact recovered from uncharted space", "CANNOT BE CONSUMED", false);

        JScrollPane scrollPane = new JScrollPane(manifestListContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        manifestPanel.add(scrollPane, BorderLayout.CENTER);
        splitContainer.add(manifestPanel, BorderLayout.CENTER);

        // --- Right: Spaceship Telemetry & Dispenser Console (~40% width) ---
        JPanel rightColumn = createRightColumn();
        rightColumn.setPreferredSize(new Dimension(420, 600));
        splitContainer.add(rightColumn, BorderLayout.EAST);

        add(splitContainer, BorderLayout.CENTER);
    }

    private void addItemCard(String itemName, String subtitle, String lore, String effectTag, boolean consumable) {
        ItemCardComponent card = new ItemCardComponent(itemName, subtitle, lore, effectTag, consumable);
        itemCards.add(card);
        manifestListContainer.add(card);
        manifestListContainer.add(Box.createVerticalStrut(8));
    }

    private JPanel createRightColumn() {
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        // 1. Live Spaceship Telemetry Gauges
        SciFiPanel vitalsPanel = new SciFiPanel("SPACECRAFT TELEMETRY", "REAL-TIME AVIONICS GAUGES");
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
        rightPanel.add(vitalsPanel);
        rightPanel.add(Box.createVerticalStrut(10));

        // 2. Supply Dispenser Console
        SciFiPanel dispenserPanel = new SciFiPanel("SUPPLY DISPENSER CONSOLE", "RESOURCE ALLOCATION");
        JPanel dispenserContent = new JPanel();
        dispenserContent.setOpaque(false);
        dispenserContent.setLayout(new BoxLayout(dispenserContent, BoxLayout.Y_AXIS));

        // Item Header Banner
        JPanel itemBanner = new JPanel(new BorderLayout());
        itemBanner.setOpaque(true);
        itemBanner.setBackground(OrbitTheme.BG_CARD);
        itemBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrbitTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        selectedItemHeader = new JLabel("ITEM: METAL");
        selectedItemHeader.setFont(OrbitTheme.FONT_HEADER_MONO);
        selectedItemHeader.setForeground(OrbitTheme.CYAN_ELECTRIC);

        consumableTypeBadge = new JLabel("[CONSUMABLE]");
        consumableTypeBadge.setFont(OrbitTheme.FONT_TINY_MONO);
        consumableTypeBadge.setForeground(OrbitTheme.STATUS_NOMINAL);

        itemBanner.add(selectedItemHeader, BorderLayout.WEST);
        itemBanner.add(consumableTypeBadge, BorderLayout.EAST);
        dispenserContent.add(itemBanner);
        dispenserContent.add(Box.createVerticalStrut(8));

        // Item Description Box
        itemDescriptionArea = new JTextArea(3, 20);
        itemDescriptionArea.setFont(OrbitTheme.FONT_BODY);
        itemDescriptionArea.setForeground(OrbitTheme.TEXT_SECONDARY);
        itemDescriptionArea.setBackground(OrbitTheme.BG_INPUT);
        itemDescriptionArea.setEditable(false);
        itemDescriptionArea.setFocusable(false);
        itemDescriptionArea.setLineWrap(true);
        itemDescriptionArea.setWrapStyleWord(true);
        itemDescriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrbitTheme.BORDER_MUTED, 1),
                new EmptyBorder(6, 8, 6, 8)
        ));
        dispenserContent.add(itemDescriptionArea);
        dispenserContent.add(Box.createVerticalStrut(8));

        // Restoration Projection Telemetry Box
        JPanel projectionBox = new JPanel(new GridLayout(3, 1, 0, 4));
        projectionBox.setOpaque(true);
        projectionBox.setBackground(OrbitTheme.BG_CARD);
        projectionBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrbitTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        targetResourceLabel = createTelemetryRow("TARGET SYSTEM", "HULL INTEGRITY");
        restorationAmountLabel = createTelemetryRow("RESTORATION AMOUNT", "+20%");
        currentToProjectedLabel = createTelemetryRow("PROJECTED RESTORATION", "100% -> 100%");

        projectionBox.add(targetResourceLabel);
        projectionBox.add(restorationAmountLabel);
        projectionBox.add(currentToProjectedLabel);

        dispenserContent.add(projectionBox);
        dispenserContent.add(Box.createVerticalStrut(10));

        // Pre-check Status Label
        dispenserStatusLabel = new JLabel("READY TO DISPENSE // SYSTEM READY");
        dispenserStatusLabel.setFont(OrbitTheme.FONT_TINY_MONO);
        dispenserStatusLabel.setForeground(OrbitTheme.STATUS_NOMINAL);
        dispenserContent.add(dispenserStatusLabel);
        dispenserContent.add(Box.createVerticalGlue());

        // Buttons
        JPanel actionRow = new JPanel(new GridLayout(2, 1, 0, 8));
        actionRow.setOpaque(false);

        dispenseButton = new SciFiButton("DISPENSE & CONSUME 1 UNIT", SciFiButton.Variant.PRIMARY);
        dispenseButton.addActionListener(e -> handleDispenseItem());

        SciFiButton returnBtn = new SciFiButton("RETURN TO COMMAND CENTER", SciFiButton.Variant.SECONDARY);
        returnBtn.addActionListener(e -> frame.showScreen(OrbitFrame.SCREEN_COMMAND_CENTER));

        actionRow.add(dispenseButton);
        actionRow.add(returnBtn);

        dispenserContent.add(actionRow);

        dispenserPanel.add(dispenserContent, BorderLayout.CENTER);
        rightPanel.add(dispenserPanel);

        return rightPanel;
    }

    private JLabel createTelemetryRow(String label, String value) {
        JLabel l = new JLabel(String.format("%-20s: %s", label, value));
        l.setFont(OrbitTheme.FONT_MONO);
        l.setForeground(OrbitTheme.TEXT_PRIMARY);
        return l;
    }

    // --- Lifecycle & State Updates ---

    /**
     * Refreshes all manifest cards, ship gauges, and dispenser readouts from OrbitController.
     */
    public void refreshFromController() {
        if (!controller.isGameActive()) {
            return;
        }

        Inventory inv = controller.getInventory();
        Spaceship ship = controller.getSpaceship();

        // 1. Header Badges
        if (inv != null) {
            totalCargoBadge.setText("TOTAL CARGO: " + inv.getTotalItemCount() + " UNITS");
        }

        // 2. Spaceship Vitals Gauges
        if (ship != null) {
            fuelGauge.setValue(ship.getFuel());
            oxygenGauge.setValue(ship.getOxygen());
            energyGauge.setValue(ship.getEnergy());
            hullGauge.setValue(ship.getHullIntegrity());
        }

        // 3. Item Cards
        for (ItemCardComponent card : itemCards) {
            int qty = (inv != null) ? inv.getQuantity(card.itemName) : 0;
            card.setQuantity(qty);
            card.setSelected(card.itemName.equalsIgnoreCase(selectedItem));
        }

        // 4. Dispenser Console Details
        updateDispenserConsole();

        revalidate();
        repaint();
    }

    private void selectItem(String itemName) {
        this.selectedItem = itemName;
        refreshFromController();
    }

    private void updateDispenserConsole() {
        if (!controller.isGameActive()) {
            return;
        }

        Inventory inv = controller.getInventory();
        Spaceship ship = controller.getSpaceship();

        selectedItemHeader.setText("ITEM: " + selectedItem.toUpperCase());
        itemDescriptionArea.setText(Inventory.getItemDescription(selectedItem));

        boolean isConsumable = Inventory.isConsumable(selectedItem);
        int qty = (inv != null) ? inv.getQuantity(selectedItem) : 0;
        int restoreAmount = Inventory.getRestorationAmount(selectedItem);

        if (!isConsumable) {
            consumableTypeBadge.setText("[NON-CONSUMABLE]");
            consumableTypeBadge.setForeground(new Color(0xA8, 0x55, 0xF7)); // Purple

            targetResourceLabel.setText(String.format("%-20s: %s", "TARGET SYSTEM", "NONE (SCIENTIFIC RESEARCH)"));
            restorationAmountLabel.setText(String.format("%-20s: %s", "RESTORATION AMOUNT", "0%"));
            currentToProjectedLabel.setText(String.format("%-20s: %s", "HOLD QUANTITY", qty + " UNITS"));

            dispenserStatusLabel.setText("NON-CONSUMABLE // ARTIFACT PRESERVED FOR SCIENTIFIC STUDY");
            dispenserStatusLabel.setForeground(new Color(0xA8, 0x55, 0xF7));
            dispenseButton.setEnabled(false);
            return;
        }

        consumableTypeBadge.setText("[CONSUMABLE]");
        consumableTypeBadge.setForeground(OrbitTheme.STATUS_NOMINAL);

        String targetResource = switch (selectedItem) {
            case Inventory.METAL -> "Hull Integrity";
            case Inventory.ENERGY_CELLS -> "Reactor Energy";
            case Inventory.OXYGEN_CANISTERS -> "Life Support Oxygen";
            case Inventory.FUEL_CELLS -> "Propulsion Fuel";
            default -> "System Resource";
        };

        int currentVal = switch (selectedItem) {
            case Inventory.METAL -> ship.getHullIntegrity();
            case Inventory.ENERGY_CELLS -> ship.getEnergy();
            case Inventory.OXYGEN_CANISTERS -> ship.getOxygen();
            case Inventory.FUEL_CELLS -> ship.getFuel();
            default -> 0;
        };

        int projected = Math.min(Spaceship.MAX_RESOURCE, currentVal + restoreAmount);

        targetResourceLabel.setText(String.format("%-20s: %s", "TARGET SYSTEM", targetResource.toUpperCase()));
        restorationAmountLabel.setText(String.format("%-20s: +%d%% RESTORATION", "RESTORATION AMOUNT", restoreAmount));
        currentToProjectedLabel.setText(String.format("%-20s: %d%% -> %d%% (+%d%%)", "PROJECTED LEVEL", currentVal, projected, (projected - currentVal)));

        if (qty <= 0) {
            dispenserStatusLabel.setText("SUPPLY DEPLETED // ZERO QUANTITY IN CARGO HOLD");
            dispenserStatusLabel.setForeground(OrbitTheme.TEXT_MUTED);
            dispenseButton.setEnabled(false);
        } else if (currentVal >= Spaceship.MAX_RESOURCE) {
            dispenserStatusLabel.setText("DISPENSATION REJECTED // TARGET RESOURCE ALREADY AT 100% MAXIMUM");
            dispenserStatusLabel.setForeground(OrbitTheme.STATUS_WARNING);
            dispenseButton.setEnabled(false);
        } else {
            dispenserStatusLabel.setText("READY TO DISPENSE // SYSTEM READY");
            dispenserStatusLabel.setForeground(OrbitTheme.STATUS_NOMINAL);
            dispenseButton.setEnabled(true);
        }
    }

    private void handleDispenseItem() {
        if (!controller.isGameActive() || selectedItem == null) {
            return;
        }

        if (!controller.canUseItem(selectedItem)) {
            SciFiDialog.showMessage(
                    frame,
                    "DISPENSATION ERROR",
                    "CANNOT DISPENSE ITEM",
                    "Cannot dispense " + selectedItem + ". Verify cargo quantity and that ship resource is below 100%."
            );
            return;
        }

        String confirmMsg = "Confirm resource dispensation?\n\n"
                + "• Item to Dispense: 1 unit " + selectedItem.toUpperCase() + "\n"
                + "• Target System   : " + Inventory.getItemDescription(selectedItem) + "\n\n"
                + "Channel cargo supplies into active spacecraft systems?";

        boolean confirmed = SciFiDialog.showConfirm(
                frame,
                "CONFIRM RESOURCE DISPENSATION",
                "DISPENSE 1 UNIT " + selectedItem.toUpperCase(),
                confirmMsg,
                "DISPENSE ITEM",
                "CANCEL"
        );

        if (!confirmed) {
            return;
        }

        // Execute item consumption via controller
        ItemUseResult result = controller.useItem(selectedItem);

        if (result.isSuccess()) {
            // Refresh InventoryPanel and CommandCenterPanel
            refreshFromController();
            frame.getCommandCenterPanel().refreshFromController();

            SciFiDialog.showMessage(
                    frame,
                    "SUPPLY APPLIED SUCCESSFULLY",
                    "RESOURCE DISPENSATION CONFIRMED",
                    result.getMessage() + "\n\n"
                    + "Spacecraft " + result.getTargetResource() + " is now at " + result.getNewValue() + "%.\n"
                    + "Remaining in hold: " + result.getRemainingQuantity() + " units."
            );
        } else {
            SciFiDialog.showMessage(
                    frame,
                    "DISPENSATION FAILED",
                    "OPERATION ABORTED",
                    result.getMessage()
            );
        }
    }

    // ==========================================
    // Custom Interactive Item Card Component
    // ==========================================

    private class ItemCardComponent extends JPanel {
        final String itemName;
        final String subtitle;
        final String lore;
        final String effectTag;
        final boolean consumable;

        private int quantity = 0;
        private boolean isSelected = false;
        private boolean isHovered = false;

        private final JLabel nameLabel;
        private final JLabel qtyBadge;
        private final JLabel loreLabel;
        private final JLabel effectLabel;
        private final JLabel categoryBadge;

        ItemCardComponent(String itemName, String subtitle, String lore, String effectTag, boolean consumable) {
            this.itemName = itemName;
            this.subtitle = subtitle;
            this.lore = lore;
            this.effectTag = effectTag;
            this.consumable = consumable;

            setLayout(new BorderLayout(8, 4));
            setOpaque(true);
            setBackground(OrbitTheme.BG_CARD);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(OrbitTheme.BORDER_SUBTLE, 1),
                    new EmptyBorder(10, 14, 10, 14)
            ));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Top row: Name & Quantity
            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);

            nameLabel = new JLabel(itemName.toUpperCase() + "  (" + subtitle + ")");
            nameLabel.setFont(OrbitTheme.FONT_BODY_BOLD);
            nameLabel.setForeground(OrbitTheme.TEXT_PRIMARY);

            qtyBadge = new JLabel("0 UNITS");
            qtyBadge.setFont(OrbitTheme.FONT_MONO_BOLD);
            qtyBadge.setForeground(OrbitTheme.CYAN_ELECTRIC);

            topRow.add(nameLabel, BorderLayout.WEST);
            topRow.add(qtyBadge, BorderLayout.EAST);

            // Middle row: Lore / Purpose description
            loreLabel = new JLabel(lore);
            loreLabel.setFont(OrbitTheme.FONT_BODY);
            loreLabel.setForeground(OrbitTheme.TEXT_SECONDARY);

            // Bottom row: Effect Tag & Consumability badge
            JPanel bottomRow = new JPanel(new BorderLayout());
            bottomRow.setOpaque(false);

            effectLabel = new JLabel("• " + effectTag);
            effectLabel.setFont(OrbitTheme.FONT_TINY_MONO);
            effectLabel.setForeground(consumable ? OrbitTheme.STATUS_NOMINAL : new Color(0xA8, 0x55, 0xF7));

            categoryBadge = new JLabel(consumable ? "[CONSUMABLE SUPPLY]" : "[NON-CONSUMABLE ARTIFACT]");
            categoryBadge.setFont(OrbitTheme.FONT_TINY_MONO);
            categoryBadge.setForeground(consumable ? OrbitTheme.CYAN_DIM : new Color(0xA8, 0x55, 0xF7));

            bottomRow.add(effectLabel, BorderLayout.WEST);
            bottomRow.add(categoryBadge, BorderLayout.EAST);

            add(topRow, BorderLayout.NORTH);
            add(loreLabel, BorderLayout.CENTER);
            add(bottomRow, BorderLayout.SOUTH);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectItem(itemName);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    updateVisualState();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    updateVisualState();
                }
            });
        }

        void setQuantity(int quantity) {
            this.quantity = quantity;
            qtyBadge.setText(quantity + " UNITS");
            if (quantity <= 0) {
                qtyBadge.setForeground(OrbitTheme.TEXT_MUTED);
            } else {
                qtyBadge.setForeground(consumable ? OrbitTheme.CYAN_ELECTRIC : new Color(0xA8, 0x55, 0xF7));
            }
        }

        void setSelected(boolean selected) {
            this.isSelected = selected;
            updateVisualState();
        }

        private void updateVisualState() {
            if (isSelected) {
                setBackground(OrbitTheme.BG_CARD_HOVER);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(OrbitTheme.CYAN_ELECTRIC, 1),
                        new EmptyBorder(10, 14, 10, 14)
                ));
                nameLabel.setForeground(OrbitTheme.CYAN_ELECTRIC);
            } else if (isHovered) {
                setBackground(OrbitTheme.BG_CARD_HOVER);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(OrbitTheme.BORDER_BRIGHT, 1),
                        new EmptyBorder(10, 14, 10, 14)
                ));
                nameLabel.setForeground(OrbitTheme.TEXT_PRIMARY);
            } else {
                setBackground(OrbitTheme.BG_CARD);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(OrbitTheme.BORDER_SUBTLE, 1),
                        new EmptyBorder(10, 14, 10, 14)
                ));
                nameLabel.setForeground(quantity > 0 ? OrbitTheme.TEXT_PRIMARY : OrbitTheme.TEXT_MUTED);
            }
            repaint();
        }
    }
}

