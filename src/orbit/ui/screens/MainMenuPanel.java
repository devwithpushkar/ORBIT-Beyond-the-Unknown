package orbit.ui.screens;

import orbit.controllers.OrbitController;
import orbit.ui.OrbitFrame;
import orbit.ui.OrbitTheme;
import orbit.ui.components.SciFiButton;
import orbit.ui.components.SciFiDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Main title and menu screen for ORBIT: Beyond the Unknown.
 * Features high-contrast sci-fi typography, telemetry indicators,
 * new game initialization with commander designation input,
 * and save file detection for continuing active campaigns.
 */
public class MainMenuPanel extends JPanel {

    private final OrbitFrame frame;
    private final OrbitController controller;

    private SciFiButton continueButton;
    private JPanel menuButtonStack;
    private JPanel newGamePromptPanel;
    private JTextField commanderInputField;
    private JLabel saveStatusLabel;

    public MainMenuPanel(OrbitFrame frame, OrbitController controller) {
        this.frame = frame;
        this.controller = controller;

        setLayout(new BorderLayout());
        setBackground(OrbitTheme.BG_VOID);

        initComponents();
        refreshState();
    }

    private void initComponents() {
        // --- Center Content Wrapper ---
        JPanel centerContainer = new JPanel();
        centerContainer.setOpaque(false);
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setBorder(new EmptyBorder(60, 40, 40, 40));

        // 1. Branding Header
        JLabel logoLabel = new JLabel("O R B I T");
        logoLabel.setFont(OrbitTheme.FONT_TITLE);
        logoLabel.setForeground(OrbitTheme.CYAN_ELECTRIC);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("B E Y O N D   T H E   U N K N O W N");
        subtitleLabel.setFont(OrbitTheme.FONT_SUBTITLE);
        subtitleLabel.setForeground(OrbitTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taglineLabel = new JLabel("DEEP SPACE EXPLORATION & SURVIVAL TELEMETRY SYSTEM");
        taglineLabel.setFont(OrbitTheme.FONT_TINY_MONO);
        taglineLabel.setForeground(OrbitTheme.TEXT_MUTED);
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerContainer.add(logoLabel);
        centerContainer.add(Box.createVerticalStrut(8));
        centerContainer.add(subtitleLabel);
        centerContainer.add(Box.createVerticalStrut(4));
        centerContainer.add(taglineLabel);
        centerContainer.add(Box.createVerticalStrut(40));

        // 2. Action Menu Stack
        menuButtonStack = new JPanel();
        menuButtonStack.setOpaque(false);
        menuButtonStack.setLayout(new BoxLayout(menuButtonStack, BoxLayout.Y_AXIS));
        menuButtonStack.setAlignmentX(Component.CENTER_ALIGNMENT);

        SciFiButton newGameButton = new SciFiButton("NEW GAME", SciFiButton.Variant.PRIMARY);
        newGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGameButton.setMaximumSize(new Dimension(320, 44));
        newGameButton.addActionListener(e -> showNewGamePrompt(true));

        continueButton = new SciFiButton("CONTINUE GAME", SciFiButton.Variant.SECONDARY);
        continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        continueButton.setMaximumSize(new Dimension(320, 44));
        continueButton.addActionListener(e -> handleContinueGame());

        SciFiButton settingsButton = new SciFiButton("SYSTEM SETTINGS", SciFiButton.Variant.SECONDARY);
        settingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingsButton.setMaximumSize(new Dimension(320, 44));
        settingsButton.addActionListener(e -> handleSettings());

        SciFiButton creditsButton = new SciFiButton("MISSION CREDITS", SciFiButton.Variant.SECONDARY);
        creditsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        creditsButton.setMaximumSize(new Dimension(320, 44));
        creditsButton.addActionListener(e -> handleCredits());

        SciFiButton exitButton = new SciFiButton("POWER DOWN (EXIT)", SciFiButton.Variant.DANGER);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setMaximumSize(new Dimension(320, 44));
        exitButton.addActionListener(e -> handleExit());

        saveStatusLabel = new JLabel(" ");
        saveStatusLabel.setFont(OrbitTheme.FONT_TINY_MONO);
        saveStatusLabel.setForeground(OrbitTheme.TEXT_MUTED);
        saveStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        menuButtonStack.add(newGameButton);
        menuButtonStack.add(Box.createVerticalStrut(12));
        menuButtonStack.add(continueButton);
        menuButtonStack.add(Box.createVerticalStrut(6));
        menuButtonStack.add(saveStatusLabel);
        menuButtonStack.add(Box.createVerticalStrut(10));
        menuButtonStack.add(settingsButton);
        menuButtonStack.add(Box.createVerticalStrut(12));
        menuButtonStack.add(creditsButton);
        menuButtonStack.add(Box.createVerticalStrut(12));
        menuButtonStack.add(exitButton);

        // 3. New Game Commander Input Prompt (inline card)
        newGamePromptPanel = createNewGamePromptPanel();
        newGamePromptPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGamePromptPanel.setVisible(false);

        centerContainer.add(menuButtonStack);
        centerContainer.add(newGamePromptPanel);

        add(centerContainer, BorderLayout.CENTER);

        // --- Bottom Telemetry Footer ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel sysStatus = new JLabel("AVIONICS: ONLINE // KERNEL v4.19 // BUS: STABLE");
        sysStatus.setFont(OrbitTheme.FONT_TINY_MONO);
        sysStatus.setForeground(OrbitTheme.TEXT_MUTED);

        JLabel buildInfo = new JLabel("ORBIT GUI RUNTIME // SOL-SECTOR-00");
        buildInfo.setFont(OrbitTheme.FONT_TINY_MONO);
        buildInfo.setForeground(OrbitTheme.TEXT_MUTED);

        footer.add(sysStatus, BorderLayout.WEST);
        footer.add(buildInfo, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createNewGamePromptPanel() {
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(420, 220));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrbitTheme.CYAN_ELECTRIC, 1),
                new EmptyBorder(20, 24, 20, 24)
        ));

        JLabel title = new JLabel("COMMANDER REGISTRATION");
        title.setFont(OrbitTheme.FONT_HEADER_MONO);
        title.setForeground(OrbitTheme.CYAN_ELECTRIC);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel prompt = new JLabel("Enter commanding officer designation:");
        prompt.setFont(OrbitTheme.FONT_BODY);
        prompt.setForeground(OrbitTheme.TEXT_SECONDARY);
        prompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        commanderInputField = new JTextField("Vance", 18);
        commanderInputField.setFont(OrbitTheme.FONT_MONO_BOLD);
        commanderInputField.setForeground(OrbitTheme.TEXT_PRIMARY);
        commanderInputField.setBackground(OrbitTheme.BG_INPUT);
        commanderInputField.setCaretColor(OrbitTheme.CYAN_ELECTRIC);
        commanderInputField.setHorizontalAlignment(JTextField.CENTER);
        commanderInputField.setMaximumSize(new Dimension(340, 36));
        commanderInputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrbitTheme.BORDER_BRIGHT, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        commanderInputField.addActionListener(e -> handleLaunchNewGame());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonRow.setOpaque(false);

        SciFiButton launchBtn = new SciFiButton("COMMISSION VESSEL", SciFiButton.Variant.PRIMARY);
        launchBtn.addActionListener(e -> handleLaunchNewGame());

        SciFiButton cancelBtn = new SciFiButton("CANCEL", SciFiButton.Variant.SECONDARY);
        cancelBtn.addActionListener(e -> showNewGamePrompt(false));

        buttonRow.add(launchBtn);
        buttonRow.add(cancelBtn);

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(prompt);
        card.add(Box.createVerticalStrut(16));
        card.add(commanderInputField);
        card.add(Box.createVerticalStrut(18));
        card.add(buttonRow);

        return card;
    }

    private void showNewGamePrompt(boolean show) {
        menuButtonStack.setVisible(!show);
        newGamePromptPanel.setVisible(show);
        if (show) {
            commanderInputField.requestFocusInWindow();
            commanderInputField.selectAll();
        }
        revalidate();
        repaint();
    }

    private void handleLaunchNewGame() {
        String name = commanderInputField.getText();
        if (name == null || name.trim().isEmpty()) {
            SciFiDialog.showMessage(
                    frame,
                    "REGISTRATION ERROR",
                    "DESIGNATION REQUIRED",
                    "Commander designation cannot be empty. Please assign a valid officer name."
            );
            return;
        }

        controller.startNewGame(name.trim());
        showNewGamePrompt(false);
        frame.showScreen(OrbitFrame.SCREEN_COMMAND_CENTER);
    }

    private void handleContinueGame() {
        if (!controller.hasSave()) {
            return;
        }

        boolean success = controller.continueGame();
        if (success) {
            frame.showScreen(OrbitFrame.SCREEN_COMMAND_CENTER);
        } else {
            SciFiDialog.showMessage(
                    frame,
                    "RESTORE ERROR",
                    "ARCHIVE CORRUPTED",
                    "Unable to restore flight telemetry archive from saves/orbit_save.dat.\n"
                    + "The save archive may be corrupted or in an incompatible format."
            );
            refreshState();
        }
    }

    private void handleSettings() {
        SciFiDialog.showMessage(
                frame,
                "SYSTEM SETTINGS",
                "AVIONICS CONFIGURATION",
                "System Settings [Phase 2 Telemetry Shell]:\n\n"
                + "* Video Output: 1280 x 800 (Native Windowed Mode)\n"
                + "* Audio Engine: Subspace Synthesizer (Active)\n"
                + "* Flight Subsystem: Core Java Swing Avionics\n"
                + "* Save Target: saves/orbit_save.dat"
        );
    }

    private void handleCredits() {
        SciFiDialog.showMessage(
                frame,
                "MISSION CREDITS",
                "DEEP SPACE RECONNAISSANCE FLEET",
                "ORBIT: Beyond the Unknown\n"
                + "Flight Avionics Directorate & Systems Engineering\n\n"
                + "Designed for deep space survival, planetary survey expeditions,\n"
                + "and anomalous extrasolar signal investigation."
        );
    }

    private void handleExit() {
        frame.dispose();
        System.exit(0);
    }

    /**
     * Refreshes the state of the Main Menu, including detecting save files
     * and enabling or disabling the Continue button.
     */
    public void refreshState() {
        boolean hasSave = controller.hasSave();
        continueButton.setEnabled(hasSave);

        if (hasSave) {
            String summary = controller.getSaveSummary();
            if (summary != null && !summary.isEmpty()) {
                saveStatusLabel.setText("ARCHIVE DETECTED: " + summary.toUpperCase());
                saveStatusLabel.setForeground(OrbitTheme.STATUS_NOMINAL);
            } else {
                saveStatusLabel.setText("ARCHIVE DETECTED // READY TO RESUME");
                saveStatusLabel.setForeground(OrbitTheme.STATUS_NOMINAL);
            }
        } else {
            saveStatusLabel.setText("NO ACTIVE TELEMETRY ARCHIVE FOUND");
            saveStatusLabel.setForeground(OrbitTheme.TEXT_MUTED);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        OrbitTheme.enableAntiAliasing(g2);

        int width = getWidth();
        int height = getHeight();

        // Subtle background grid
        g2.setColor(new Color(0x0E, 0x16, 0x28));
        int gridSize = 40;
        for (int x = 0; x < width; x += gridSize) {
            g2.drawLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += gridSize) {
            g2.drawLine(0, y, width, y);
        }

        // Viewport corner brackets
        g2.setColor(OrbitTheme.BORDER_SUBTLE);
        int bracket = 24;
        // Top-left
        g2.drawLine(10, 10, 10 + bracket, 10);
        g2.drawLine(10, 10, 10, 10 + bracket);
        // Top-right
        g2.drawLine(width - 11, 10, width - 11 - bracket, 10);
        g2.drawLine(width - 11, 10, width - 11, 10 + bracket);
        // Bottom-left
        g2.drawLine(10, height - 11, 10 + bracket, height - 11);
        g2.drawLine(10, height - 11, 10, height - 11 - bracket);
        // Bottom-right
        g2.drawLine(width - 11, height - 11, width - 11 - bracket, height - 11);
        g2.drawLine(width - 11, height - 11, width - 11, height - 11 - bracket);

        g2.dispose();
    }
}

