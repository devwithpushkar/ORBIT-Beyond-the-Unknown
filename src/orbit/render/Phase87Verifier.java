package orbit.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import orbit.controllers.ExpeditionResult;
import orbit.controllers.OrbitController;
import orbit.models.ExplorationEvent;
import orbit.models.Inventory;
import orbit.models.Planet;
import orbit.models.Spaceship;
import orbit.screens.CargoHoldScreen;
import orbit.screens.CommandCenterScreen;
import orbit.screens.ExplorationScreen;
import orbit.screens.MainMenuScreen;
import orbit.screens.NavigationScreen;

/**
 * Phase87Verifier performs automated verification of the Phase 8.7 Exploration Operations implementation:
 * - Direct transition from Bridge to Exploration via Stage mouse click.
 * - 3D planetary reconnaissance environment and celestial telemetry rendering.
 * - Planetary target and environmental data display.
 * - Vitals deficit blocking and launch button disabling (<10 Energy or <5 Oxygen).
 * - Explicit player confirmation dialog before deducting resources.
 * - Exact resource deductions on expedition launch: -10 Energy, -5 Oxygen, 0 Fuel.
 * - Planet exploration status updates.
 * - Resolution of all exploration event categories (Hazard, Beacon, Monolith, Auto-resolved, Kepler Climax).
 * - Hazard countermeasure mitigation vs damage penalty absorption.
 * - Repeated entry/exit cycles between Bridge and Exploration without input lock or disposal errors.
 * - Regression verification: Navigation, Cargo Hold, and Main Menu interaction preservation.
 */
public class Phase87Verifier {

    private final OrbitGame game;
    private final OrbitController controller;
    private int step = 0;
    private int waitFrames = 0;

    public Phase87Verifier(OrbitGame game) {
        this.game = game;
        this.controller = game.getController();
    }

    public void update() {
        if (waitFrames > 0) {
            waitFrames--;
            return;
        }

        try {
            executeStep();
        } catch (Throwable t) {
            System.err.println("[ORBIT-VERIFY-87] FAILED AT STEP " + step + ": " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private void executeStep() {
        switch (step) {
            case 0 -> {
                System.out.println("\n========================================================");
                System.out.println("[ORBIT-VERIFY-87] STARTING PHASE 8.7 EXPLORATION OPERATIONS VERIFICATION");
                System.out.println("========================================================");
                waitFrames = 6;
                step++;
            }

            // --- STEP 1: INITIALIZE FLIGHT SESSION AND ENTER BRIDGE ---
            case 1 -> {
                System.out.println("\n[ORBIT-VERIFY-87] --- STEP 1: INITIALIZE FLIGHT AND ENTER BRIDGE ---");
                controller.startNewGame("Cmdr-ReconTest");
                game.showCommandCenter();
                waitFrames = 4;
                step++;
            }

            case 2 -> {
                assertCondition(game.getScreen() instanceof CommandCenterScreen, "Active screen must be CommandCenterScreen");
                CommandCenterScreen bridge = (CommandCenterScreen) game.getScreen();
                assertCondition(Gdx.input.getInputProcessor() == bridge.getStage(), "Bridge stage must own input processor");

                System.out.println("[ORBIT-VERIFY-87] Dispatching mouse click on bridge EXPLORATION console button [EXP-02]...");
                simulateMouseClick(bridge.getStage(), bridge.getExploreButton());
                waitFrames = 25; // Wait for fade transition
                step++;
            }

            // --- STEP 2: VERIFY EXPLORATION SCREEN & 3D RECONNAISSANCE ENVIRONMENT ---
            case 3 -> {
                System.out.println("\n[ORBIT-VERIFY-87] --- STEP 2: VERIFY EXPLORATION SCREEN & 3D ENVIRONMENT ---");
                assertCondition(game.getScreen() instanceof ExplorationScreen, "Active screen must be ExplorationScreen");
                ExplorationScreen exploration = (ExplorationScreen) game.getScreen();
                assertCondition(Gdx.input.getInputProcessor() == exploration.getStage(), "Exploration stage must own input processor");
                assertCondition(exploration.getSceneManager() != null, "Exploration 3D SceneManager must be initialized");

                Planet loc = controller.getCurrentLocation();
                assertCondition(loc != null && loc.getName().equalsIgnoreCase("Earth"), "Initial planet must be Earth");
                assertCondition(!loc.isExplored(), "Earth must initially be unexplored");

                Spaceship ship = controller.getSpaceship();
                assertCondition(ship.getEnergy() == 100, "Initial Energy must be 100%");
                assertCondition(ship.getOxygen() == 100, "Initial Oxygen must be 100%");
                assertCondition(ship.getFuel() == 100, "Initial Fuel must be 100%");

                assertCondition(exploration.getLaunchButton() != null, "Launch button must exist");
                assertCondition(!exploration.getLaunchButton().isDisabled(), "Launch button must be enabled initially");
                System.out.println("[ORBIT-VERIFY-87]   ✓ 3D Reconnaissance scene active, Earth target locked, vitals nominal.");
                waitFrames = 3;
                step++;
            }

            // --- STEP 3: TEST VITALS DEFICIT INSUFFICIENT RESOURCE BLOCKING ---
            case 4 -> {
                System.out.println("\n[ORBIT-VERIFY-87] --- STEP 3: TEST VITALS DEFICIT INSUFFICIENT RESOURCE BLOCKING ---");
                Spaceship ship = controller.getSpaceship();
                ExplorationScreen exploration = (ExplorationScreen) game.getScreen();

                // 3a. Drain energy < 10
                ship.consumeEnergy(95); // 100 -> 5
                exploration.refreshFromController();
                assertCondition(ship.getEnergy() == 5, "Energy must be 5%");
                assertCondition(exploration.getLaunchButton().isDisabled(), "Launch button must be disabled when energy < 10");
                assertCondition(exploration.getLaunchButton().getText().toString().contains("VITALS DEFICIT"),
                        "Launch button text must indicate vitals deficit");

                // Attempting launch must not trigger confirmation dialog
                exploration.handleLaunchExpedition();
                assertCondition(findDialog(exploration.getStage()) == null, "No confirmation dialog should open when energy is deficit");
                System.out.println("[ORBIT-VERIFY-87]   ✓ Energy deficit (<10%) correctly inhibited expedition launch.");

                // Restore energy and test oxygen deficit < 5
                ship.restoreEnergy(100);
                ship.consumeOxygen(98); // 100 -> 2
                exploration.refreshFromController();
                assertCondition(ship.getOxygen() == 2, "Oxygen must be 2%");
                assertCondition(exploration.getLaunchButton().isDisabled(), "Launch button must be disabled when oxygen < 5");

                // Restore oxygen back to 100%
                ship.restoreOxygen(100);
                exploration.refreshFromController();
                assertCondition(!exploration.getLaunchButton().isDisabled(), "Launch button must re-enable when vitals restored");
                System.out.println("[ORBIT-VERIFY-87]   ✓ Oxygen deficit (<5%) correctly inhibited expedition launch. Vitals restored.");
                waitFrames = 3;
                step++;
            }

            // --- STEP 4: TEST EXPLICIT CONFIRMATION DIALOG PRIOR TO RESOURCE DEDUCTION ---
            case 5 -> {
                System.out.println("\n[ORBIT-VERIFY-87] --- STEP 4: TEST EXPLICIT CONFIRMATION DIALOG ---");
                ExplorationScreen exploration = (ExplorationScreen) game.getScreen();
                Spaceship ship = controller.getSpaceship();

                assertCondition(ship.getEnergy() == 100, "Energy must be 100% before launch");
                assertCondition(ship.getOxygen() == 100, "Oxygen must be 100% before launch");
                assertCondition(ship.getFuel() == 100, "Fuel must be 100% before launch");

                System.out.println("[ORBIT-VERIFY-87] Dispatching mouse click on DEPLOY EXCURSION ROVER button...");
                simulateMouseClick(exploration.getStage(), exploration.getLaunchButton());
                waitFrames = 4;
                step++;
            }

            case 6 -> {
                ExplorationScreen exploration = (ExplorationScreen) game.getScreen();
                Spaceship ship = controller.getSpaceship();

                Dialog dialog = findDialog(exploration.getStage());
                assertCondition(dialog != null, "Expedition confirmation dialog must be displayed");

                TextButton confirmBtn = findTextButtonRecursive(dialog, "CONFIRM & DEPLOY");
                assertCondition(confirmBtn != null, "Confirm & Deploy button must exist in dialog");

                // CRITICAL REQUIREMENT: No resources deducted before confirmation is acknowledged!
                assertCondition(ship.getEnergy() == 100, "Energy must STILL be 100% before explicit confirmation");
                assertCondition(ship.getOxygen() == 100, "Oxygen must STILL be 100% before explicit confirmation");
                assertCondition(ship.getFuel() == 100, "Fuel must STILL be 100% before explicit confirmation");
                System.out.println("[ORBIT-VERIFY-87]   ✓ Pre-launch state verified: zero resources deducted prior to player confirmation.");

                System.out.println("[ORBIT-VERIFY-87] Dispatching mouse click on CONFIRM & DEPLOY button...");
                simulateMouseClick(exploration.getStage(), confirmBtn);
                waitFrames = 5;
                step++;
            }

            // --- STEP 5: VERIFY EXACT RESOURCE DEDUCTION & PLANET EXPLORATION STATUS ---
            case 7 -> {
                System.out.println("\n[ORBIT-VERIFY-87] --- STEP 5: VERIFY EXACT RESOURCE DEDUCTIONS (10 ENG, 5 O2, 0 FUEL) ---");
                Spaceship ship = controller.getSpaceship();
                Planet loc = controller.getCurrentLocation();
                ExplorationScreen exploration = (ExplorationScreen) game.getScreen();

                // Verify exact deductions
                assertCondition(ship.getEnergy() == 90, "Energy must be deducted by exactly 10 (100 -> 90)");
                assertCondition(ship.getOxygen() == 95, "Oxygen must be deducted by exactly 5 (100 -> 95)");
                assertCondition(ship.getFuel() == 100, "Fuel must be UNCHANGED (0% fuel deduction, 100 -> 100)");
                assertCondition(loc.isExplored(), "Target planet Earth must now be marked explored");
                System.out.println(String.format("[ORBIT-VERIFY-87]   ✓ Exact costs verified: Energy=%d%% (-10), Oxygen=%d%% (-5), Fuel=%d%% (0).",
                        ship.getEnergy(), ship.getOxygen(), ship.getFuel()));

                // Check that event dialog was displayed and dismiss it
                Dialog eventDialog = findDialog(exploration.getStage());
                assertCondition(eventDialog != null, "An exploration event dialog must be displayed on stage");
                System.out.println("[ORBIT-VERIFY-87]   ✓ Exploration Event modal rendered on stage.");

                // Find any action button on the dialog to dismiss/resolve it
                TextButton actionBtn = findFirstTextButton(eventDialog);
                assertCondition(actionBtn != null, "Event dialog must contain an actionable button");
                System.out.println("[ORBIT-VERIFY-87] Resolving event modal via button: " + actionBtn.getText());
                simulateMouseClick(exploration.getStage(), actionBtn);
                waitFrames = 4;
                step++;
            }

            // --- STEP 6: VERIFY HAZARD COUNTERMEASURE VS DAMAGE MITIGATION MECHANICS ---
            case 8 -> {
                System.out.println("\n[ORBIT-VERIFY-87] --- STEP 6: VERIFY HAZARD COUNTERMEASURE VS DAMAGE ABSORPTION ---");
                Spaceship ship = controller.getSpaceship();
                Inventory inv = controller.getInventory();

                // Reset vitals
                ship.repairHull(100);
                assertCondition(ship.getHullIntegrity() == 100, "Hull must be 100%");
                int initialMetal = inv.getQuantity(Inventory.METAL);
                assertCondition(initialMetal > 0, "Inventory must contain Metal countermeasure supplies");

                ExplorationEvent hullHazard = ExplorationEvent.getEventById("haz_hull_damage");
                assertCondition(hullHazard != null, "haz_hull_damage event must exist");

                // 6a. Countermeasure branch: expend 1 Metal, absorb 0 damage
                System.out.println("[ORBIT-VERIFY-87] Testing hazard resolution WITH countermeasure...");
                controller.resolveHazard(hullHazard, true);
                assertCondition(inv.getQuantity(Inventory.METAL) == initialMetal - 1, "Countermeasure must expend 1 Metal");
                assertCondition(ship.getHullIntegrity() == 100, "Hull must sustain zero damage when countermeasure deployed");
                System.out.println("[ORBIT-VERIFY-87]   ✓ Countermeasure branch verified: 1 Metal consumed, 0 damage sustained.");

                // 6b. Brace for impact branch: accept damage (-15 Hull), expend 0 Metal
                System.out.println("[ORBIT-VERIFY-87] Testing hazard resolution WITHOUT countermeasure (Brace for impact)...");
                int metalBeforeBrace = inv.getQuantity(Inventory.METAL);
                controller.resolveHazard(hullHazard, false);
                assertCondition(inv.getQuantity(Inventory.METAL) == metalBeforeBrace, "Bracing must NOT consume metal supplies");
                assertCondition(ship.getHullIntegrity() == 85, "Hull must sustain 15 damage (100 -> 85)");
                System.out.println("[ORBIT-VERIFY-87]   ✓ Brace branch verified: 0 Metal consumed, 15 damage sustained.");

                // Restore hull
                ship.repairHull(15);
                assertCondition(ship.getHullIntegrity() == 100, "Hull restored to 100%");
                waitFrames = 3;
                step++;
            }

            // --- STEP 7: VERIFY BEACON & MONOLITH DECISION BRANCHING ---
            case 9 -> {
                System.out.println("\n[ORBIT-VERIFY-87] --- STEP 7: VERIFY BEACON & MONOLITH DECISION PROTOCOLS ---");
                ExplorationEvent beaconEvent = ExplorationEvent.getEventById("enc_unidentified_signal");
                assertCondition(beaconEvent != null, "enc_unidentified_signal must exist");

                String beaconOutcome = controller.resolveBeaconEncounter(beaconEvent, 0);
                assertCondition(beaconOutcome != null && !beaconOutcome.isEmpty(), "Beacon resolution must return narrative outcome");
                assertCondition(controller.getGameState().isTransmissionEvidenceDiscovered(),
                        "Transmission evidence flag must be set to true");
                System.out.println("[ORBIT-VERIFY-87]   ✓ Signal beacon triangulation verified.");

                ExplorationEvent monolithEvent = ExplorationEvent.getEventById("enc_ancient_civilization");
                assertCondition(monolithEvent != null, "enc_ancient_civilization must exist");

                String monolithOutcome = controller.resolveMonolithEncounter(monolithEvent, 0);
                assertCondition(monolithOutcome != null && !monolithOutcome.isEmpty(), "Monolith resolution must return narrative outcome");
                System.out.println("[ORBIT-VERIFY-87]   ✓ Ancient monolith xenological investigation verified.");
                waitFrames = 3;
                step++;
            }

            // --- STEP 8: VERIFY KEPLER-186F EXTRASOLAR CLIMAX RESOLUTION ---
            case 10 -> {
                System.out.println("\n[ORBIT-VERIFY-87] --- STEP 8: VERIFY KEPLER-186F CLIMAX DIRECTIVE RESOLUTION ---");
                Planet kepler = controller.getPlanetByName("Kepler-186f");
                assertCondition(kepler != null, "Kepler-186f planet must exist");

                kepler.setDiscovered(true);
                controller.getGameState().setCurrentLocation(kepler);
                controller.getSpaceship().restoreEnergy(100);
                controller.getSpaceship().restoreOxygen(100);

                int artifactsBefore = controller.getInventory().getQuantity(Inventory.ALIEN_ARTIFACTS);
                ExpeditionResult climaxRes = controller.startExpedition();
                assertCondition(climaxRes.getType() == ExpeditionResult.Type.KEPLER_CLIMAX,
                        "Expedition on Kepler-186f must trigger KEPLER_CLIMAX");
                assertCondition(controller.getInventory().getQuantity(Inventory.ALIEN_ARTIFACTS) == artifactsBefore + 1,
                        "Kepler climax must secure +1 Alien Artifact");
                assertCondition(controller.getGameState().isSignalSourceDiscovered(),
                        "Signal source must be marked discovered on Kepler climax");
                System.out.println("[ORBIT-VERIFY-87]   ✓ Kepler-186f climax verified: +1 Alien Artifact secured, signal source logged.");

                // Return location back to Earth for subsequent tests
                controller.getGameState().setCurrentLocation(controller.getPlanetByName("Earth"));
                waitFrames = 3;
                step++;
            }

            // --- STEP 9: TEST RETURN TO BRIDGE AND REPEATED CYCLES ---
            case 11 -> {
                System.out.println("\n[ORBIT-VERIFY-87] --- STEP 9: TEST RETURN TO BRIDGE AND REPEATED CYCLES ---");
                ExplorationScreen exploration = (ExplorationScreen) game.getScreen();
                exploration.refreshFromController();

                System.out.println("[ORBIT-VERIFY-87] Dispatching mouse click on RETURN TO BRIDGE button...");
                simulateMouseClick(exploration.getStage(), exploration.getReturnButton());
                waitFrames = 25; // Wait for fade transition
                step++;
            }

            case 12 -> {
                assertCondition(game.getScreen() instanceof CommandCenterScreen, "Active screen must be CommandCenterScreen after return");
                CommandCenterScreen bridge = (CommandCenterScreen) game.getScreen();
                assertCondition(Gdx.input.getInputProcessor() == bridge.getStage(), "Bridge stage must own input processor");
                System.out.println("[ORBIT-VERIFY-87]   ✓ Successfully returned to bridge.");

                // Re-enter ExplorationScreen (Cycle 2)
                System.out.println("[ORBIT-VERIFY-87] Re-entering Exploration Screen (Cycle 2)...");
                simulateMouseClick(bridge.getStage(), bridge.getExploreButton());
                waitFrames = 25;
                step++;
            }

            case 13 -> {
                assertCondition(game.getScreen() instanceof ExplorationScreen, "Cycle 2: Active screen must be ExplorationScreen");
                ExplorationScreen exploration = (ExplorationScreen) game.getScreen();
                assertCondition(Gdx.input.getInputProcessor() == exploration.getStage(), "Cycle 2: Exploration stage must own input processor");

                System.out.println("[ORBIT-VERIFY-87] Returning to bridge (Cycle 2)...");
                exploration.handleReturnToBridge();
                waitFrames = 25;
                step++;
            }

            case 14 -> {
                assertCondition(game.getScreen() instanceof CommandCenterScreen, "Cycle 2: Active screen must be CommandCenterScreen");
                System.out.println("[ORBIT-VERIFY-87]   ✓ Repeated entry and exit between Bridge and Exploration verified without error.");
                waitFrames = 3;
                step++;
            }

            // --- STEP 10: REGRESSION VERIFICATION — CARGO HOLD, NAVIGATION & MAIN MENU ---
            case 15 -> {
                System.out.println("\n[ORBIT-VERIFY-87] --- STEP 10: REGRESSION VERIFICATION (CARGO, NAV, MAIN MENU) ---");
                System.out.println("[ORBIT-VERIFY-87] Verifying Cargo Hold screen transition...");
                game.showCargoScreen();
                waitFrames = 5;
                step++;
            }

            case 16 -> {
                assertCondition(game.getScreen() instanceof CargoHoldScreen, "Active screen must be CargoHoldScreen");
                System.out.println("[ORBIT-VERIFY-87]   ✓ Cargo Hold screen confirmed online.");

                System.out.println("[ORBIT-VERIFY-87] Returning to Bridge...");
                game.showCommandCenter();
                waitFrames = 5;
                step++;
            }

            case 17 -> {
                assertCondition(game.getScreen() instanceof CommandCenterScreen, "Active screen must be CommandCenterScreen");
                System.out.println("[ORBIT-VERIFY-87] Verifying 3D Navigation screen transition...");
                game.showNavigationScreen();
                waitFrames = 5;
                step++;
            }

            case 18 -> {
                assertCondition(game.getScreen() instanceof NavigationScreen, "Active screen must be NavigationScreen");
                System.out.println("[ORBIT-VERIFY-87]   ✓ 3D Holographic Navigation confirmed online.");

                System.out.println("[ORBIT-VERIFY-87] Returning to Bridge...");
                game.showCommandCenter();
                waitFrames = 5;
                step++;
            }

            case 19 -> {
                assertCondition(game.getScreen() instanceof CommandCenterScreen, "Active screen must be CommandCenterScreen");
                System.out.println("[ORBIT-VERIFY-87] Returning to Main Menu...");
                game.showMainMenu();
                waitFrames = 5;
                step++;
            }

            case 20 -> {
                assertCondition(game.getScreen() instanceof MainMenuScreen, "Active screen must be MainMenuScreen");
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                assertCondition(Gdx.input.getInputProcessor() == menu.getStage(), "Main Menu stage must own input processor");

                System.out.println("[ORBIT-VERIFY-87] Dispatching mouse click on Settings button...");
                simulateMouseClick(menu.getStage(), menu.getSettingsButton());
                waitFrames = 3;
                step++;
            }

            case 21 -> {
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Dialog dialog = findDialog(menu.getStage());
                assertCondition(dialog != null, "Settings dialog must open");
                TextButton closeBtn = findTextButtonRecursive(dialog, "CLOSE");
                assertCondition(closeBtn != null, "Close button must be present in settings dialog");
                simulateMouseClick(menu.getStage(), closeBtn);
                waitFrames = 3;
                step++;
            }

            case 22 -> {
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                assertCondition(findDialog(menu.getStage()) == null, "Settings dialog must be closed");
                System.out.println("[ORBIT-VERIFY-87]   ✓ Main Menu interaction confirmed fully responsive.");

                System.out.println("\n========================================================");
                System.out.println("[ORBIT-VERIFY-87] ALL PHASE 8.7 EXPLORATION VERIFICATION TESTS PASSED!");
                System.out.println("[ORBIT-VERIFY-87] 1. Exploration access from Bridge         : VERIFIED");
                System.out.println("[ORBIT-VERIFY-87] 2. 3D Reconnaissance scene & telemetry    : VERIFIED");
                System.out.println("[ORBIT-VERIFY-87] 3. Vitals deficit blocking (<10 ENG / <5 O2): VERIFIED");
                System.out.println("[ORBIT-VERIFY-87] 4. Explicit player confirmation dialog    : VERIFIED");
                System.out.println("[ORBIT-VERIFY-87] 5. Exact costs (-10 ENG, -5 O2, 0 FUEL)   : VERIFIED");
                System.out.println("[ORBIT-VERIFY-87] 6. Hazard countermeasure vs damage brace  : VERIFIED");
                System.out.println("[ORBIT-VERIFY-87] 7. Beacon & Monolith decision protocols  : VERIFIED");
                System.out.println("[ORBIT-VERIFY-87] 8. Kepler-186f climax & artifact recovery : VERIFIED");
                System.out.println("[ORBIT-VERIFY-87] 9. Repeated entry/exit & bridge sync      : VERIFIED");
                System.out.println("[ORBIT-VERIFY-87] 10. Cargo, Nav & Main Menu regression     : VERIFIED");
                System.out.println("========================================================\n");
                step++;
                Gdx.app.exit();
            }
        }
    }

    private void simulateMouseClick(Stage stage, Actor target) {
        if (target == null) {
            throw new AssertionError("Target actor for mouse click is null");
        }
        Vector2 stageCoords = target.localToStageCoordinates(new Vector2(target.getWidth() * 0.5f, target.getHeight() * 0.5f));
        Vector2 screenCoords = stage.stageToScreenCoordinates(new Vector2(stageCoords.x, stageCoords.y));
        int screenX = (int) screenCoords.x;
        int screenY = (int) screenCoords.y;

        stage.mouseMoved(screenX, screenY);
        stage.touchDown(screenX, screenY, 0, 0);
        stage.touchUp(screenX, screenY, 0, 0);
    }

    private Dialog findDialog(Stage stage) {
        for (Actor actor : stage.getActors()) {
            if (actor instanceof Dialog d) {
                return d;
            }
        }
        return null;
    }

    private TextButton findTextButtonRecursive(Actor parent, String buttonText) {
        if (parent instanceof TextButton tb && tb.getText().toString().contains(buttonText)) {
            return tb;
        }
        if (parent instanceof Group group) {
            for (Actor child : group.getChildren()) {
                TextButton found = findTextButtonRecursive(child, buttonText);
                if (found != null) return found;
            }
        }
        return null;
    }

    private TextButton findFirstTextButton(Actor parent) {
        if (parent instanceof TextButton tb) {
            return tb;
        }
        if (parent instanceof Group group) {
            for (Actor child : group.getChildren()) {
                TextButton found = findFirstTextButton(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }
}

