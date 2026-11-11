package orbit.render;

import com.badlogic.gdx.Game;
import orbit.controllers.EndingResult;
import orbit.controllers.OrbitController;
import orbit.models.Inventory;
import orbit.models.Planet;
import orbit.render.audio.AudioManager;
import orbit.screens.CargoHoldScreen;
import orbit.screens.CommandCenterScreen;
import orbit.screens.EndingScreen;
import orbit.screens.ExplorationScreen;
import orbit.screens.MainMenuScreen;
import orbit.screens.MissionControlScreen;
import orbit.screens.MissionLogScreen;
import orbit.screens.NavigationScreen;

/**
 * OrbitGame is the primary libGDX Game lifecycle orchestrator.
 * Owns the authoritative OrbitController instance, manages GPU assets,
 * and controls active screen transitions.
 */
public class OrbitGame extends Game {

    private final OrbitController controller;

    private AssetManager assetManager;
    private RenderManager renderManager;
    private AudioManager audioManager;
    private MainMenuScreen mainMenuScreen;
    private CommandCenterScreen commandCenterScreen;
    private NavigationScreen navigationScreen;
    private CargoHoldScreen cargoHoldScreen;
    private ExplorationScreen explorationScreen;
    private MissionControlScreen missionControlScreen;
    private MissionLogScreen missionLogScreen;
    private EndingScreen endingScreen;
    private Phase89Verifier verifier;

    public OrbitGame() {
        this(new OrbitController());
    }

    public OrbitGame(OrbitController controller) {
        this.controller = (controller != null) ? controller : new OrbitController();
    }

    @Override
    public void create() {
        System.out.println("[ORBIT-CORE] Initializing AssetManager (GPU textures, skins)...");
        this.assetManager = new AssetManager();
        System.out.println("[ORBIT-CORE] Initializing RenderManager (viewports, projection)...");
        this.renderManager = new RenderManager();
        System.out.println("[ORBIT-CORE] Initializing AudioManager (avionics sound bank)...");
        this.audioManager = new AudioManager();
        System.out.println("[ORBIT-CORE] Instantiating MainMenuScreen (3D scene, Scene2D deck)...");
        this.mainMenuScreen = new MainMenuScreen(this, controller, assetManager, renderManager, audioManager);

        if ("true".equalsIgnoreCase(System.getProperty("orbit.verify"))) {
            setScreen(mainMenuScreen);
            this.verifier = new Phase89Verifier(this);
            System.out.println("[ORBIT-CORE] Verification harness active: Phase89Verifier engaged.");
        } else if ("true".equalsIgnoreCase(System.getProperty("orbit.launchEndingA"))) {
            System.out.println("[ORBIT-CORE] Launching Ending A (Successful Return) cinematic preview...");
            controller.startNewGame("Vance");
            prepareEndingState(true, true);
            EndingResult res = controller.resolveReturnEnding();
            showEndingScreen(res);
        } else if ("true".equalsIgnoreCase(System.getProperty("orbit.launchEndingB"))) {
            System.out.println("[ORBIT-CORE] Launching Ending B (Lost in Space) cinematic preview...");
            controller.startNewGame("Vance");
            prepareEndingState(true, false);
            EndingResult res = controller.resolveReturnEnding();
            showEndingScreen(res);
        } else if ("true".equalsIgnoreCase(System.getProperty("orbit.launchEndingC"))) {
            System.out.println("[ORBIT-CORE] Launching Ending C (The Unknown) cinematic preview...");
            controller.startNewGame("Vance");
            prepareEndingState(true, true);
            EndingResult res = controller.resolveUnknownEnding();
            showEndingScreen(res);
        } else if ("true".equalsIgnoreCase(System.getProperty("orbit.launchMissionControl"))) {
            System.out.println("[ORBIT-CORE] Auto-commissioning test expedition for mission control verification...");
            controller.startNewGame("Vance");
            showMissionControlScreen();
        } else if ("true".equalsIgnoreCase(System.getProperty("orbit.launchMissionLog"))) {
            System.out.println("[ORBIT-CORE] Auto-commissioning test expedition for mission log verification...");
            controller.startNewGame("Vance");
            showMissionLogScreen();
        } else if ("true".equalsIgnoreCase(System.getProperty("orbit.launchExploration"))) {
            System.out.println("[ORBIT-CORE] Auto-commissioning test expedition for exploration verification...");
            controller.startNewGame("Vance");
            showExplorationScreen();
        } else if ("true".equalsIgnoreCase(System.getProperty("orbit.launchNav"))) {
            System.out.println("[ORBIT-CORE] Auto-commissioning test expedition for navigation verification...");
            controller.startNewGame("Vance");
            showNavigationScreen();
        } else if ("true".equalsIgnoreCase(System.getProperty("orbit.launchCargo"))) {
            System.out.println("[ORBIT-CORE] Auto-commissioning test expedition for cargo hold verification...");
            controller.startNewGame("Vance");
            showCargoScreen();
        } else if ("true".equalsIgnoreCase(System.getProperty("orbit.launchBridge"))) {
            System.out.println("[ORBIT-CORE] Auto-commissioning test expedition for bridge verification...");
            controller.startNewGame("Vance");
            showCommandCenter();
        } else {
            setScreen(mainMenuScreen);
            System.out.println("[ORBIT-CORE] MainMenuScreen set active. Window & 3D scene live.");
        }
    }

    public void showCommandCenter() {
        System.out.println("[ORBIT-CORE] Transitioning to CommandCenterScreen (tactical bridge)...");
        if (controller.isCampaignCompleted()) {
            System.out.println("[ORBIT-CORE] Campaign has concluded (" + controller.getCompletedEnding() + "). Redirecting to EndingScreen debriefing.");
            showEndingScreen(controller.getCompletedEndingResult());
            return;
        }
        if (commandCenterScreen == null) {
            commandCenterScreen = new CommandCenterScreen(this, controller, assetManager, renderManager, audioManager);
        } else {
            commandCenterScreen.refreshFromController();
        }
        setScreen(commandCenterScreen);
    }

    public void showNavigationScreen() {
        System.out.println("[ORBIT-CORE] Transitioning to NavigationScreen (holographic galaxy)...");
        if (controller.isCampaignCompleted()) {
            System.out.println("[ORBIT-CORE] Campaign has concluded (" + controller.getCompletedEnding() + "). Redirecting to EndingScreen debriefing.");
            showEndingScreen(controller.getCompletedEndingResult());
            return;
        }
        if (navigationScreen == null) {
            navigationScreen = new NavigationScreen(this, controller, assetManager, renderManager, audioManager);
        } else {
            navigationScreen.refreshFromController();
        }
        setScreen(navigationScreen);
    }

    public void showCargoScreen() {
        System.out.println("[ORBIT-CORE] Transitioning to CargoHoldScreen (cargo hold bay)...");
        if (controller.isCampaignCompleted()) {
            System.out.println("[ORBIT-CORE] Campaign has concluded (" + controller.getCompletedEnding() + "). Redirecting to EndingScreen debriefing.");
            showEndingScreen(controller.getCompletedEndingResult());
            return;
        }
        if (cargoHoldScreen == null) {
            cargoHoldScreen = new CargoHoldScreen(this, controller, assetManager, renderManager, audioManager);
        } else {
            cargoHoldScreen.refreshFromController();
        }
        setScreen(cargoHoldScreen);
    }

    public void showExplorationScreen() {
        System.out.println("[ORBIT-CORE] Transitioning to ExplorationScreen (surface reconnaissance)...");
        if (controller.isCampaignCompleted()) {
            System.out.println("[ORBIT-CORE] Campaign has concluded (" + controller.getCompletedEnding() + "). Redirecting to EndingScreen debriefing.");
            showEndingScreen(controller.getCompletedEndingResult());
            return;
        }
        if (explorationScreen == null) {
            explorationScreen = new ExplorationScreen(this, controller, assetManager, renderManager, audioManager);
        } else {
            explorationScreen.refreshFromController();
        }
        setScreen(explorationScreen);
    }

    public void commissionExpedition(String commanderName) {
        System.out.println("[ORBIT-CORE] Commissioning fresh expedition under Commander " + commanderName.toUpperCase() + "...");
        controller.startNewGame(commanderName);
        resetGameplayScreens();
        showCommandCenter();
    }

    public void resetGameplayScreens() {
        if (commandCenterScreen != null) {
            commandCenterScreen.dispose();
            commandCenterScreen = null;
        }
        if (navigationScreen != null) {
            navigationScreen.dispose();
            navigationScreen = null;
        }
        if (cargoHoldScreen != null) {
            cargoHoldScreen.dispose();
            cargoHoldScreen = null;
        }
        if (explorationScreen != null) {
            explorationScreen.dispose();
            explorationScreen = null;
        }
        if (missionControlScreen != null) {
            missionControlScreen.dispose();
            missionControlScreen = null;
        }
        if (missionLogScreen != null) {
            missionLogScreen.dispose();
            missionLogScreen = null;
        }
        if (endingScreen != null) {
            endingScreen.dispose();
            endingScreen = null;
        }
    }

    public void showMissionControlScreen() {
        System.out.println("[ORBIT-CORE] Transitioning to MissionControlScreen (command operations)...");
        if (missionControlScreen == null) {
            missionControlScreen = new MissionControlScreen(this, controller, assetManager, renderManager, audioManager);
        } else {
            missionControlScreen.refreshFromController();
        }
        setScreen(missionControlScreen);
    }

    public void showMissionLogScreen() {
        System.out.println("[ORBIT-CORE] Transitioning to MissionLogScreen (chronological archive)...");
        if (missionLogScreen == null) {
            missionLogScreen = new MissionLogScreen(this, controller, assetManager, renderManager, audioManager);
        } else {
            missionLogScreen.refreshFromController();
        }
        setScreen(missionLogScreen);
    }

    public void showMainMenu() {
        System.out.println("[ORBIT-CORE] Transitioning to MainMenuScreen (gateway deck)...");
        if (mainMenuScreen == null) {
            mainMenuScreen = new MainMenuScreen(this, controller, assetManager, renderManager, audioManager);
        }
        setScreen(mainMenuScreen);
    }

    public void showEndingScreen(EndingResult result) {
        System.out.println("[ORBIT-CORE] Transitioning to EndingScreen (cinematic campaign debriefing)...");
        if (endingScreen == null) {
            endingScreen = new EndingScreen(this, controller, assetManager, renderManager, audioManager);
        }
        if (result != null) {
            endingScreen.setEndingResult(result);
        }
        setScreen(endingScreen);
    }

    public void showEndingScreen() {
        showEndingScreen(controller.getCompletedEndingResult());
    }

    public EndingScreen getEndingScreen() {
        return endingScreen;
    }

    private void prepareEndingState(boolean operational, boolean sufficientFuel) {
        if (!controller.isGameActive()) {
            controller.startNewGame("Vance");
        }
        Planet kepler = controller.getGameState().getPlanetByName("Kepler-186f");
        if (kepler != null) {
            kepler.setDiscovered(true);
            controller.getGameState().setCurrentLocation(kepler);
        }
        controller.getGameState().setSignalSourceDiscovered(true);
        controller.getInventory().setQuantity(Inventory.ALIEN_ARTIFACTS, 1);
        if (operational) {
            controller.getSpaceship().repairHull(100);
            controller.getSpaceship().restoreOxygen(100);
        } else {
            controller.getSpaceship().applyDamage(100);
        }
        if (sufficientFuel) {
            controller.getSpaceship().restoreFuel(100);
        } else {
            int currentFuel = controller.getSpaceship().getFuel();
            controller.getSpaceship().consumeFuel(currentFuel);
            controller.getSpaceship().restoreFuel(5);
            controller.getInventory().setQuantity(Inventory.FUEL_CELLS, 0);
        }
    }

    public OrbitController getController() {
        return controller;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public RenderManager getRenderManager() {
        return renderManager;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    @Override
    public void render() {
        super.render();
        if (verifier != null) {
            verifier.update();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (navigationScreen != null) navigationScreen.dispose();
        if (cargoHoldScreen != null) cargoHoldScreen.dispose();
        if (explorationScreen != null) explorationScreen.dispose();
        if (missionControlScreen != null) missionControlScreen.dispose();
        if (missionLogScreen != null) missionLogScreen.dispose();
        if (endingScreen != null) endingScreen.dispose();
        if (commandCenterScreen != null) commandCenterScreen.dispose();
        if (mainMenuScreen != null) mainMenuScreen.dispose();
        if (renderManager != null) renderManager.dispose();
        if (assetManager != null) assetManager.dispose();
        if (audioManager != null) audioManager.dispose();
    }
}

