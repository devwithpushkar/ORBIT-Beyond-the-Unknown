package orbit.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import orbit.controllers.OrbitController;
import orbit.models.Inventory;
import orbit.models.Spaceship;
import orbit.screens.CargoHoldScreen;
import orbit.screens.CommandCenterScreen;
import orbit.screens.MainMenuScreen;
import orbit.screens.NavigationScreen;

/**
 * Phase86Verifier tests the dedicated Cinematic Cargo Hold implementation:
 * - Opening Cargo Hold from the Command Center via Stage mouse click.
 * - Accurate inventory quantity and category rendering.
 * - Exact consumable effects (+20 Hull, +25 Energy, +25 Oxygen, +25 Fuel).
 * - Non-consumable protection for Alien Artifacts.
 * - Immediate UI state synchronization.
 * - Repeated entry/exit between Cargo Hold and Command Center.
 * - Preservation of Main Menu and Navigation interaction flows.
 */
public class Phase86Verifier {

    private final OrbitGame game;
    private final OrbitController controller;
    private int step = 0;
    private int waitFrames = 0;

    public Phase86Verifier(OrbitGame game) {
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
            System.err.println("[ORBIT-VERIFY-86] FAILED AT STEP " + step + ": " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private void executeStep() {
        switch (step) {
            case 0 -> {
                System.out.println("\n========================================================");
                System.out.println("[ORBIT-VERIFY-86] STARTING PHASE 8.6 CARGO HOLD VERIFICATION");
                System.out.println("========================================================");
                waitFrames = 6;
                step++;
            }

            // --- 1. COMMISSION EXPEDITION AND TRANSITION TO BRIDGE ---
            case 1 -> {
                System.out.println("\n[ORBIT-VERIFY-86] --- STEP 1: INITIALIZE FLIGHT AND ENTER BRIDGE ---");
                controller.startNewGame("Cmdr-CargoTest");
                game.showCommandCenter();
                waitFrames = 4;
                step++;
            }

            case 2 -> {
                assertCondition(game.getScreen() instanceof CommandCenterScreen, "Active screen must be CommandCenterScreen");
                CommandCenterScreen bridge = (CommandCenterScreen) game.getScreen();
                assertCondition(Gdx.input.getInputProcessor() == bridge.getStage(), "Bridge stage must own input processor");

                System.out.println("[ORBIT-VERIFY-86] Dispatching mouse click on bridge CARGO console button...");
                simulateMouseClick(bridge.getStage(), bridge.getCargoButton());
                waitFrames = 25; // Wait for fade transition
                step++;
            }

            // --- 2. VERIFY CARGO HOLD ENVIRONMENT & INITIAL INVENTORY MANIFEST ---
            case 3 -> {
                System.out.println("\n[ORBIT-VERIFY-86] --- STEP 2: VERIFY CARGO HOLD SCREEN & INVENTORY DISPLAY ---");
                assertCondition(game.getScreen() instanceof CargoHoldScreen, "Active screen must be CargoHoldScreen");
                CargoHoldScreen cargo = (CargoHoldScreen) game.getScreen();
                assertCondition(Gdx.input.getInputProcessor() == cargo.getStage(), "Cargo stage must own input processor");
                assertCondition(cargo.getSceneManager() != null, "Cargo 3D SceneManager must be initialized");

                Inventory inv = controller.getInventory();
                assertCondition(inv != null, "Inventory must not be null");

                System.out.println(String.format("[ORBIT-VERIFY-86] Initial Hold Manifest: Metal=%d, Energy=%d, O2=%d, Fuel=%d, Artifacts=%d",
                        inv.getQuantity(Inventory.METAL),
                        inv.getQuantity(Inventory.ENERGY_CELLS),
                        inv.getQuantity(Inventory.OXYGEN_CANISTERS),
                        inv.getQuantity(Inventory.FUEL_CELLS),
                        inv.getQuantity(Inventory.ALIEN_ARTIFACTS)));

                assertCondition(inv.getQuantity(Inventory.METAL) == 2, "Initial Metal must be 2");
                assertCondition(inv.getQuantity(Inventory.ENERGY_CELLS) == 2, "Initial Energy Cells must be 2");
                assertCondition(inv.getQuantity(Inventory.OXYGEN_CANISTERS) == 2, "Initial Oxygen Canisters must be 2");
                assertCondition(inv.getQuantity(Inventory.FUEL_CELLS) == 2, "Initial Fuel Cells must be 2");
                assertCondition(inv.getQuantity(Inventory.ALIEN_ARTIFACTS) == 0, "Initial Alien Artifacts must be 0");

                System.out.println("[ORBIT-VERIFY-86]   ✓ All 5 cargo categories verified in manifest.");
                waitFrames = 3;
                step++;
            }

            // --- 3. TEST CONSUMABLE REPLENISHMENT: METAL (+20 HULL) ---
            case 4 -> {
                System.out.println("\n[ORBIT-VERIFY-86] --- STEP 3: TEST METAL ALLOY DISPENSE (+20% HULL) ---");
                Spaceship ship = controller.getSpaceship();
                ship.applyDamage(40); // Reduces hull from 100 to 60
                CargoHoldScreen cargo = (CargoHoldScreen) game.getScreen();
                cargo.refreshFromController();

                assertCondition(ship.getHullIntegrity() == 60, "Hull must be set to 60%");
                TextButton metalBtn = cargo.getDispenseButton(Inventory.METAL);
                assertCondition(metalBtn != null, "Metal dispense button must exist");
                assertCondition(!metalBtn.isDisabled(), "Metal button must be enabled when hull < 100 and stock > 0");

                System.out.println("[ORBIT-VERIFY-86] Dispatching mouse click on Metal dispense button...");
                simulateMouseClick(cargo.getStage(), metalBtn);
                waitFrames = 3;
                step++;
            }

            case 5 -> {
                Spaceship ship = controller.getSpaceship();
                Inventory inv = controller.getInventory();
                CargoHoldScreen cargo = (CargoHoldScreen) game.getScreen();

                assertCondition(ship.getHullIntegrity() == 80, "Hull must increase by +20 (from 60 to 80)");
                assertCondition(inv.getQuantity(Inventory.METAL) == 1, "Metal stock must decrement from 2 to 1");
                System.out.println(String.format("[ORBIT-VERIFY-86]   ✓ Metal consumed: Hull restored to %d%%, Remaining stock: %d",
                        ship.getHullIntegrity(), inv.getQuantity(Inventory.METAL)));
                waitFrames = 3;
                step++;
            }

            // --- 4. TEST CONSUMABLE REPLENISHMENT: ENERGY CELLS (+25 REACTOR ENERGY) ---
            case 6 -> {
                System.out.println("\n[ORBIT-VERIFY-86] --- STEP 4: TEST ENERGY CELLS DISPENSE (+25% ENERGY) ---");
                Spaceship ship = controller.getSpaceship();
                ship.consumeEnergy(50); // Reduces energy from 100 to 50
                CargoHoldScreen cargo = (CargoHoldScreen) game.getScreen();
                cargo.refreshFromController();

                assertCondition(ship.getEnergy() == 50, "Energy must be set to 50%");
                TextButton energyBtn = cargo.getDispenseButton(Inventory.ENERGY_CELLS);
                assertCondition(energyBtn != null && !energyBtn.isDisabled(), "Energy button must be enabled");

                System.out.println("[ORBIT-VERIFY-86] Dispatching mouse click on Energy Cells dispense button...");
                simulateMouseClick(cargo.getStage(), energyBtn);
                waitFrames = 3;
                step++;
            }

            case 7 -> {
                Spaceship ship = controller.getSpaceship();
                Inventory inv = controller.getInventory();

                assertCondition(ship.getEnergy() == 75, "Energy must increase by +25 (from 50 to 75)");
                assertCondition(inv.getQuantity(Inventory.ENERGY_CELLS) == 1, "Energy cells stock must decrement from 2 to 1");
                System.out.println(String.format("[ORBIT-VERIFY-86]   ✓ Energy Cells consumed: Energy restored to %d%%, Remaining stock: %d",
                        ship.getEnergy(), inv.getQuantity(Inventory.ENERGY_CELLS)));
                waitFrames = 3;
                step++;
            }

            // --- 5. TEST CONSUMABLE REPLENISHMENT: OXYGEN CANISTERS (+25 LIFE SUPPORT O2) ---
            case 8 -> {
                System.out.println("\n[ORBIT-VERIFY-86] --- STEP 5: TEST OXYGEN CANISTERS DISPENSE (+25% O2) ---");
                Spaceship ship = controller.getSpaceship();
                ship.consumeOxygen(50); // Reduces oxygen from 100 to 50
                CargoHoldScreen cargo = (CargoHoldScreen) game.getScreen();
                cargo.refreshFromController();

                assertCondition(ship.getOxygen() == 50, "Oxygen must be set to 50%");
                TextButton o2Btn = cargo.getDispenseButton(Inventory.OXYGEN_CANISTERS);
                assertCondition(o2Btn != null && !o2Btn.isDisabled(), "Oxygen button must be enabled");

                System.out.println("[ORBIT-VERIFY-86] Dispatching mouse click on Oxygen Canisters dispense button...");
                simulateMouseClick(cargo.getStage(), o2Btn);
                waitFrames = 3;
                step++;
            }

            case 9 -> {
                Spaceship ship = controller.getSpaceship();
                Inventory inv = controller.getInventory();

                assertCondition(ship.getOxygen() == 75, "Oxygen must increase by +25 (from 50 to 75)");
                assertCondition(inv.getQuantity(Inventory.OXYGEN_CANISTERS) == 1, "O2 canisters stock must decrement from 2 to 1");
                System.out.println(String.format("[ORBIT-VERIFY-86]   ✓ Oxygen consumed: O2 restored to %d%%, Remaining stock: %d",
                        ship.getOxygen(), inv.getQuantity(Inventory.OXYGEN_CANISTERS)));
                waitFrames = 3;
                step++;
            }

            // --- 6. TEST CONSUMABLE REPLENISHMENT: FUEL CELLS (+25 PROPULSION FUEL) ---
            case 10 -> {
                System.out.println("\n[ORBIT-VERIFY-86] --- STEP 6: TEST FUEL CELLS DISPENSE (+25% FUEL) ---");
                Spaceship ship = controller.getSpaceship();
                ship.consumeFuel(50); // Reduces fuel from 100 to 50
                CargoHoldScreen cargo = (CargoHoldScreen) game.getScreen();
                cargo.refreshFromController();

                assertCondition(ship.getFuel() == 50, "Fuel must be set to 50%");
                TextButton fuelBtn = cargo.getDispenseButton(Inventory.FUEL_CELLS);
                assertCondition(fuelBtn != null && !fuelBtn.isDisabled(), "Fuel button must be enabled");

                System.out.println("[ORBIT-VERIFY-86] Dispatching mouse click on Fuel Cells dispense button...");
                simulateMouseClick(cargo.getStage(), fuelBtn);
                waitFrames = 3;
                step++;
            }

            case 11 -> {
                Spaceship ship = controller.getSpaceship();
                Inventory inv = controller.getInventory();

                assertCondition(ship.getFuel() == 75, "Fuel must increase by +25 (from 50 to 75)");
                assertCondition(inv.getQuantity(Inventory.FUEL_CELLS) == 1, "Fuel cells stock must decrement from 2 to 1");
                System.out.println(String.format("[ORBIT-VERIFY-86]   ✓ Fuel consumed: Fuel restored to %d%%, Remaining stock: %d",
                        ship.getFuel(), inv.getQuantity(Inventory.FUEL_CELLS)));
                waitFrames = 3;
                step++;
            }

            // --- 7. TEST NON-CONSUMABLE PROTECTION FOR ALIEN ARTIFACTS ---
            case 12 -> {
                System.out.println("\n[ORBIT-VERIFY-86] --- STEP 7: TEST NON-CONSUMABLE ALIEN ARTIFACTS ---");
                Inventory inv = controller.getInventory();
                inv.add(Inventory.ALIEN_ARTIFACTS, 2); // Add xenological relics to test
                CargoHoldScreen cargo = (CargoHoldScreen) game.getScreen();
                cargo.refreshFromController();

                assertCondition(inv.getQuantity(Inventory.ALIEN_ARTIFACTS) == 2, "Artifact count must be 2");
                TextButton artifactBtn = cargo.getDispenseButton(Inventory.ALIEN_ARTIFACTS);
                assertCondition(artifactBtn != null, "Artifact button must exist");

                System.out.println("[ORBIT-VERIFY-86] Dispatching mouse click on Alien Artifacts vault button...");
                simulateMouseClick(cargo.getStage(), artifactBtn);
                waitFrames = 3;
                step++;
            }

            case 13 -> {
                Inventory inv = controller.getInventory();
                assertCondition(inv.getQuantity(Inventory.ALIEN_ARTIFACTS) == 2, "Alien Artifacts must NOT be consumed (count remains 2)");
                System.out.println("[ORBIT-VERIFY-86]   ✓ Alien Artifacts safely protected in stasis vault. Non-consumable status verified.");
                waitFrames = 3;
                step++;
            }

            // --- 8. TEST RETURN TO BRIDGE AND REPEATED CYCLES ---
            case 14 -> {
                System.out.println("\n[ORBIT-VERIFY-86] --- STEP 8: TEST RETURN TO BRIDGE AND REPEATED CYCLES ---");
                CargoHoldScreen cargo = (CargoHoldScreen) game.getScreen();
                System.out.println("[ORBIT-VERIFY-86] Dispatching mouse click on RETURN TO BRIDGE button...");
                simulateMouseClick(cargo.getStage(), cargo.getReturnButton());
                waitFrames = 25; // Wait for fade transition
                step++;
            }

            case 15 -> {
                assertCondition(game.getScreen() instanceof CommandCenterScreen, "Active screen must be CommandCenterScreen after return");
                CommandCenterScreen bridge = (CommandCenterScreen) game.getScreen();
                assertCondition(Gdx.input.getInputProcessor() == bridge.getStage(), "Bridge stage must own input processor");
                System.out.println("[ORBIT-VERIFY-86]   ✓ Successfully returned to bridge.");

                // Re-enter Cargo Hold (Cycle 2)
                System.out.println("[ORBIT-VERIFY-86] Re-entering Cargo Hold (Cycle 2)...");
                game.showCargoScreen();
                waitFrames = 10;
                step++;
            }

            case 16 -> {
                assertCondition(game.getScreen() instanceof CargoHoldScreen, "Cycle 2: Active screen must be CargoHoldScreen");
                CargoHoldScreen cargo = (CargoHoldScreen) game.getScreen();
                assertCondition(Gdx.input.getInputProcessor() == cargo.getStage(), "Cycle 2: Cargo stage must own input processor");

                System.out.println("[ORBIT-VERIFY-86] Returning to bridge (Cycle 2)...");
                cargo.handleReturnToBridge();
                waitFrames = 25;
                step++;
            }

            case 17 -> {
                assertCondition(game.getScreen() instanceof CommandCenterScreen, "Cycle 2: Active screen must be CommandCenterScreen");
                System.out.println("[ORBIT-VERIFY-86]   ✓ Repeated entry and exit between Bridge and Cargo Hold verified without input or disposal error.");
                waitFrames = 3;
                step++;
            }

            // --- 9. VERIFY EXISTING NAVIGATION AND MAIN MENU PRESERVATION ---
            case 18 -> {
                System.out.println("\n[ORBIT-VERIFY-86] --- STEP 9: VERIFY NAVIGATION & MAIN MENU PRESERVATION ---");
                System.out.println("[ORBIT-VERIFY-86] Transitioning from Bridge to 3D Navigation...");
                game.showNavigationScreen();
                waitFrames = 5;
                step++;
            }

            case 19 -> {
                assertCondition(game.getScreen() instanceof NavigationScreen, "Active screen must be NavigationScreen");
                System.out.println("[ORBIT-VERIFY-86]   ✓ 3D Holographic Navigation confirmed online.");

                System.out.println("[ORBIT-VERIFY-86] Returning from Navigation to Bridge...");
                game.showCommandCenter();
                waitFrames = 5;
                step++;
            }

            case 20 -> {
                assertCondition(game.getScreen() instanceof CommandCenterScreen, "Active screen must be CommandCenterScreen");
                System.out.println("[ORBIT-VERIFY-86] Returning from Bridge to Main Menu...");
                game.showMainMenu();
                waitFrames = 5;
                step++;
            }

            case 21 -> {
                assertCondition(game.getScreen() instanceof MainMenuScreen, "Active screen must be MainMenuScreen");
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                assertCondition(Gdx.input.getInputProcessor() == menu.getStage(), "Main Menu stage must own input processor");

                System.out.println("[ORBIT-VERIFY-86] Dispatching mouse click on Settings button...");
                simulateMouseClick(menu.getStage(), menu.getSettingsButton());
                waitFrames = 3;
                step++;
            }

            case 22 -> {
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                assertCondition(findDialog(menu.getStage()) != null, "Settings dialog must open");
                TextButton closeBtn = findTextButton(findDialog(menu.getStage()), "CLOSE");
                assertCondition(closeBtn != null, "Close button must be present");
                simulateMouseClick(menu.getStage(), closeBtn);
                waitFrames = 3;
                step++;
            }

            case 23 -> {
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                assertCondition(findDialog(menu.getStage()) == null, "Settings dialog must be closed");
                System.out.println("[ORBIT-VERIFY-86]   ✓ Main Menu interaction confirmed fully responsive.");

                System.out.println("\n========================================================");
                System.out.println("[ORBIT-VERIFY-86] ALL PHASE 8.6 CARGO HOLD VERIFICATION TESTS PASSED!");
                System.out.println("[ORBIT-VERIFY-86] 1. Cargo Hold access from Bridge           : VERIFIED");
                System.out.println("[ORBIT-VERIFY-86] 2. 5 Cargo categories & 3D architecture   : VERIFIED");
                System.out.println("[ORBIT-VERIFY-86] 3. Metal repair (+20% Hull)                : VERIFIED");
                System.out.println("[ORBIT-VERIFY-86] 4. Energy Cells recharge (+25% Energy)     : VERIFIED");
                System.out.println("[ORBIT-VERIFY-86] 5. Oxygen Canisters life support (+25% O2) : VERIFIED");
                System.out.println("[ORBIT-VERIFY-86] 6. Fuel Cells propellant burn (+25% Fuel)  : VERIFIED");
                System.out.println("[ORBIT-VERIFY-86] 7. Alien Artifacts stasis non-consumable   : VERIFIED");
                System.out.println("[ORBIT-VERIFY-86] 8. Repeated entry/exit & bridge sync       : VERIFIED");
                System.out.println("[ORBIT-VERIFY-86] 9. Main Menu & Navigation preservation     : VERIFIED");
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

    private com.badlogic.gdx.scenes.scene2d.ui.Dialog findDialog(Stage stage) {
        for (Actor actor : stage.getActors()) {
            if (actor instanceof com.badlogic.gdx.scenes.scene2d.ui.Dialog d) {
                return d;
            }
        }
        return null;
    }

    private TextButton findTextButton(com.badlogic.gdx.scenes.scene2d.ui.Dialog dialog, String buttonText) {
        for (Actor child : dialog.getButtonTable().getChildren()) {
            if (child instanceof TextButton tb) {
                if (tb.getText().toString().contains(buttonText)) {
                    return tb;
                }
            }
        }
        for (Actor child : dialog.getContentTable().getChildren()) {
            if (child instanceof TextButton tb) {
                if (tb.getText().toString().contains(buttonText)) {
                    return tb;
                }
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

