package orbit.ui;

import orbit.controllers.OrbitController;
import orbit.ui.screens.CommandCenterPanel;
import orbit.ui.screens.EndingPanel;
import orbit.ui.screens.ExplorationPanel;
import orbit.ui.screens.InventoryPanel;
import orbit.ui.screens.MainMenuPanel;
import orbit.ui.screens.MissionControlPanel;
import orbit.ui.screens.MissionLogPanel;
import orbit.ui.screens.NavigationPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Main application window for ORBIT: Beyond the Unknown.
 * Hosts the desktop application shell, manages CardLayout screen switching,
 * owns the shared OrbitController instance, and maintains UI execution on the
 * Swing Event Dispatch Thread.
 */
public class OrbitFrame extends JFrame {

    public static final String SCREEN_MAIN_MENU = "MAIN_MENU";
    public static final String SCREEN_COMMAND_CENTER = "COMMAND_CENTER";
    public static final String SCREEN_NAVIGATION = "NAVIGATION";
    public static final String SCREEN_INVENTORY = "INVENTORY";
    public static final String SCREEN_EXPLORATION = "EXPLORATION";
    public static final String SCREEN_MISSION_LOG = "MISSION_LOG";
    public static final String SCREEN_MISSION_CONTROL = "MISSION_CONTROL";
    public static final String SCREEN_ENDING = "ENDING";

    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 800;
    public static final int MIN_WIDTH = 1024;
    public static final int MIN_HEIGHT = 680;

    private final OrbitController controller;

    private CardLayout cardLayout;
    private JPanel cardsPanel;

    private MainMenuPanel mainMenuPanel;
    private CommandCenterPanel commandCenterPanel;
    private NavigationPanel navigationPanel;
    private InventoryPanel inventoryPanel;
    private ExplorationPanel explorationPanel;
    private MissionLogPanel missionLogPanel;
    private MissionControlPanel missionControlPanel;
    private EndingPanel endingPanel;

    public OrbitFrame() {
        this(new OrbitController());
    }

    public OrbitFrame(OrbitController controller) {
        super("ORBIT: Beyond the Unknown // Spacecraft Telemetry System");
        this.controller = (controller != null) ? controller : new OrbitController();

        initWindow();
        initScreens();

        showScreen(SCREEN_MAIN_MENU);
    }

    private void initWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setLocationRelativeTo(null);

        // Dark background on root container
        getContentPane().setBackground(OrbitTheme.BG_VOID);

        // Generate custom procedural sci-fi window icon
        setIconImage(createAppIcon());
    }

    private void initScreens() {
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(OrbitTheme.BG_VOID);

        mainMenuPanel = new MainMenuPanel(this, controller);
        commandCenterPanel = new CommandCenterPanel(this, controller);
        navigationPanel = new NavigationPanel(this, controller);
        inventoryPanel = new InventoryPanel(this, controller);
        explorationPanel = new ExplorationPanel(this, controller);
        missionLogPanel = new MissionLogPanel(this, controller);
        missionControlPanel = new MissionControlPanel(controller, this);
        endingPanel = new EndingPanel(controller, this);

        cardsPanel.add(mainMenuPanel, SCREEN_MAIN_MENU);
        cardsPanel.add(commandCenterPanel, SCREEN_COMMAND_CENTER);
        cardsPanel.add(navigationPanel, SCREEN_NAVIGATION);
        cardsPanel.add(inventoryPanel, SCREEN_INVENTORY);
        cardsPanel.add(explorationPanel, SCREEN_EXPLORATION);
        cardsPanel.add(missionLogPanel, SCREEN_MISSION_LOG);
        cardsPanel.add(missionControlPanel, SCREEN_MISSION_CONTROL);
        cardsPanel.add(endingPanel, SCREEN_ENDING);

        getContentPane().add(cardsPanel, BorderLayout.CENTER);
    }

    /**
     * Switches the active viewport card to the target screen.
     * Triggers necessary state refreshes on the destination panel.
     *
     * @param screenName Screen identifier constant
     */
    public void showScreen(String screenName) {
        if (SCREEN_COMMAND_CENTER.equals(screenName)) {
            if (controller.isCampaignCompleted()) {
                showScreen(SCREEN_ENDING);
                return;
            }
            commandCenterPanel.refreshFromController();
        } else if (SCREEN_MAIN_MENU.equals(screenName)) {
            mainMenuPanel.refreshState();
        } else if (SCREEN_NAVIGATION.equals(screenName)) {
            if (controller.isCampaignCompleted()) {
                showScreen(SCREEN_ENDING);
                return;
            }
            navigationPanel.onEnterScreen();
        } else if (SCREEN_INVENTORY.equals(screenName)) {
            if (controller.isCampaignCompleted()) {
                showScreen(SCREEN_ENDING);
                return;
            }
            inventoryPanel.refreshFromController();
        } else if (SCREEN_EXPLORATION.equals(screenName)) {
            if (controller.isCampaignCompleted()) {
                showScreen(SCREEN_ENDING);
                return;
            }
            explorationPanel.refreshFromController();
        } else if (SCREEN_MISSION_LOG.equals(screenName)) {
            missionLogPanel.refreshFromController();
        } else if (SCREEN_MISSION_CONTROL.equals(screenName)) {
            missionControlPanel.refreshFromController();
        } else if (SCREEN_ENDING.equals(screenName)) {
            endingPanel.refreshFromEndingResult();
        }

        cardLayout.show(cardsPanel, screenName);
        revalidate();
        repaint();
    }

    public OrbitController getController() {
        return controller;
    }

    public MainMenuPanel getMainMenuPanel() {
        return mainMenuPanel;
    }

    public CommandCenterPanel getCommandCenterPanel() {
        return commandCenterPanel;
    }

    public NavigationPanel getNavigationPanel() {
        return navigationPanel;
    }

    public InventoryPanel getInventoryPanel() {
        return inventoryPanel;
    }

    public ExplorationPanel getExplorationPanel() {
        return explorationPanel;
    }

    public MissionLogPanel getMissionLogPanel() {
        return missionLogPanel;
    }

    public MissionControlPanel getMissionControlPanel() {
        return missionControlPanel;
    }

    public EndingPanel getEndingPanel() {
        return endingPanel;
    }

    /**
     * Creates an in-memory 32x32 sci-fi emblem icon for the window title bar.
     */
    private Image createAppIcon() {
        int size = 32;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        OrbitTheme.enableAntiAliasing(g2);

        // Dark circular backing
        g2.setColor(OrbitTheme.BG_VOID);
        g2.fillOval(2, 2, size - 4, size - 4);

        // Outer cyan ring
        g2.setColor(OrbitTheme.CYAN_ELECTRIC);
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawOval(3, 3, size - 7, size - 7);

        // Core celestial dot
        g2.setColor(OrbitTheme.BLUE_ACCENT);
        g2.fillOval(size / 2 - 4, size / 2 - 4, 8, 8);

        // Crosshair ticks
        g2.setColor(OrbitTheme.CYAN_DIM);
        g2.drawLine(size / 2, 0, size / 2, 4);
        g2.drawLine(size / 2, size - 5, size / 2, size - 1);
        g2.drawLine(0, size / 2, 4, size / 2);
        g2.drawLine(size - 5, size / 2, size - 1, size / 2);

        g2.dispose();
        return img;
    }

    /**
     * Desktop graphical application launch entrypoint.
     * Ensures frame construction and presentation executes on the Swing Event Dispatch Thread.
     */
    public static void main(String[] args) {
        // Set system-appropriate look and feel properties if available
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            OrbitFrame frame = new OrbitFrame();
            frame.setVisible(true);
        });
    }
}

