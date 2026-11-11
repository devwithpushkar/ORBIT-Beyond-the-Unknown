package orbit.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import orbit.render.components.HoloButton;
import orbit.screens.CommandCenterScreen;
import orbit.screens.MainMenuScreen;
import orbit.screens.NavigationScreen;

/**
 * Phase852Verifier executes automated verification of Main Menu mouse interactions
 * by dispatching actual Scene2D mouse events (mouseMoved, touchDown, touchUp)
 * through the Stage and verifying that the resulting actions, callbacks,
 * dialogs, and screen transitions execute cleanly.
 */
public class Phase852Verifier {

    private final OrbitGame game;
    private int step = 0;
    private int waitFrames = 0;
    private final boolean[] exitInvoked = { false };

    public Phase852Verifier(OrbitGame game) {
        this.game = game;
    }

    public void update() {
        if (waitFrames > 0) {
            waitFrames--;
            return;
        }

        try {
            executeStep();
        } catch (Throwable t) {
            System.err.println("[ORBIT-VERIFY-852] FAILED AT STEP " + step + ": " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private void executeStep() {
        switch (step) {
            case 0 -> {
                System.out.println("\n========================================================");
                System.out.println("[ORBIT-VERIFY-852] STARTING MAIN MENU MOUSE INTERACTION VERIFICATION");
                System.out.println("========================================================");
                waitFrames = 6;
                step++;
            }

            // --- 1. VERIFY AVIONICS CONFIGURATION (SETTINGS) VIA STAGE MOUSE EVENTS ---
            case 1 -> {
                System.out.println("\n[ORBIT-VERIFY-852] --- TEST 1: AVIONICS CONFIGURATION (SETTINGS) ---");
                assertCondition(game.getScreen() instanceof MainMenuScreen, "Active screen must be MainMenuScreen");
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();
                assertCondition(Gdx.input.getInputProcessor() == stage, "Gdx.input must be bound to MainMenuScreen stage");

                System.out.println("[ORBIT-VERIFY-852] Dispatching mouse click through Stage on Settings button...");
                simulateMouseClick(stage, menu.getSettingsButton());
                waitFrames = 3;
                step++;
            }

            case 2 -> {
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();
                Dialog dialog = findDialog(stage);
                assertCondition(dialog != null, "Settings dialog must appear on stage after clicking Settings button");
                System.out.println("[ORBIT-VERIFY-852]   ✓ Settings dialog opened via Stage mouse click.");

                TextButton closeBtn = findTextButton(dialog, "CLOSE");
                assertCondition(closeBtn != null, "Close button must be present in Settings dialog");

                System.out.println("[ORBIT-VERIFY-852] Dispatching mouse click on dialog CLOSE button...");
                simulateMouseClick(stage, closeBtn);
                waitFrames = 3;
                step++;
            }

            case 3 -> {
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();
                Dialog dialog = findDialog(stage);
                assertCondition(dialog == null, "Settings dialog must be closed and removed after clicking CLOSE");
                System.out.println("[ORBIT-VERIFY-852]   ✓ Settings dialog closed cleanly via Stage mouse click.");
                System.out.println("[ORBIT-VERIFY-852] TEST 1 PASSED: Avionics Configuration fully verified.");
                waitFrames = 3;
                step++;
            }

            // --- 2. VERIFY EXPEDITION DOSSIER (CREDITS) VIA STAGE MOUSE EVENTS ---
            case 4 -> {
                System.out.println("\n[ORBIT-VERIFY-852] --- TEST 2: EXPEDITION DOSSIER (CREDITS) ---");
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();

                System.out.println("[ORBIT-VERIFY-852] Dispatching mouse click through Stage on Credits button...");
                simulateMouseClick(stage, menu.getCreditsButton());
                waitFrames = 3;
                step++;
            }

            case 5 -> {
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();
                Dialog dialog = findDialog(stage);
                assertCondition(dialog != null, "Credits dialog must appear on stage after clicking Credits button");
                System.out.println("[ORBIT-VERIFY-852]   ✓ Credits dialog opened via Stage mouse click.");

                TextButton ackBtn = findTextButton(dialog, "ACKNOWLEDGE");
                assertCondition(ackBtn != null, "Acknowledge button must be present in Credits dialog");

                System.out.println("[ORBIT-VERIFY-852] Dispatching mouse click on dialog ACKNOWLEDGE button...");
                simulateMouseClick(stage, ackBtn);
                waitFrames = 3;
                step++;
            }

            case 6 -> {
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();
                Dialog dialog = findDialog(stage);
                assertCondition(dialog == null, "Credits dialog must be closed and removed after clicking ACKNOWLEDGE");
                System.out.println("[ORBIT-VERIFY-852]   ✓ Credits dialog closed cleanly via Stage mouse click.");
                System.out.println("[ORBIT-VERIFY-852] TEST 2 PASSED: Expedition Dossier fully verified.");
                waitFrames = 3;
                step++;
            }

            // --- 3. VERIFY POWER DOWN CORE (EXIT ACTION) VIA STAGE MOUSE EVENTS ---
            case 7 -> {
                System.out.println("\n[ORBIT-VERIFY-852] --- TEST 3: POWER DOWN CORE (EXIT ACTION) ---");
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();

                exitInvoked[0] = false;
                menu.setExitHandler(() -> {
                    System.out.println("[ORBIT-VERIFY-852]   -> Exit action handler successfully triggered!");
                    exitInvoked[0] = true;
                });

                System.out.println("[ORBIT-VERIFY-852] Dispatching mouse click through Stage on Exit button...");
                simulateMouseClick(stage, menu.getExitButton());
                waitFrames = 2;
                step++;
            }

            case 8 -> {
                assertCondition(exitInvoked[0], "Power Down Core click must execute the configured exit action");
                System.out.println("[ORBIT-VERIFY-852]   ✓ Power Down Core verified: Exit action invoked.");
                System.out.println("[ORBIT-VERIFY-852] TEST 3 PASSED: Power Down Core fully verified.");

                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                menu.setExitHandler(() -> Gdx.app.exit()); // Restore normal application termination handler
                waitFrames = 3;
                step++;
            }

            // --- 4. VERIFY RESUME TELEMETRY (SAVE-LOADING FLOW) VIA STAGE MOUSE EVENTS ---
            case 9 -> {
                System.out.println("\n[ORBIT-VERIFY-852] --- TEST 4: RESUME TELEMETRY (SAVE-LOADING FLOW) ---");
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();

                // Test clicking Resume Telemetry when no save exists: should trigger informative feedback toast
                System.out.println("[ORBIT-VERIFY-852] Dispatching click on Resume Telemetry without active save...");
                simulateMouseClick(stage, menu.getContinueButton());
                System.out.println("[ORBIT-VERIFY-852]   ✓ Resume Telemetry safely handled click without save.");

                // Now commission a save file to verify active restoration flow
                System.out.println("[ORBIT-VERIFY-852] Creating test flight telemetry archive on disk...");
                game.getController().startNewGame("Cmdr-SaveTester");
                boolean saved = game.getController().saveGame();
                assertCondition(saved, "Test game state must save successfully");
                menu.refreshSaveState();
                assertCondition(game.getController().hasSave(), "Controller must detect active save archive");

                System.out.println("[ORBIT-VERIFY-852] Dispatching mouse click on Resume Telemetry with active save...");
                simulateMouseClick(stage, menu.getContinueButton());
                waitFrames = 30; // Wait for fade transition
                step++;
            }

            case 10 -> {
                assertCondition(game.getScreen() instanceof CommandCenterScreen, "Resume Telemetry click must transition into CommandCenterScreen");
                System.out.println("[ORBIT-VERIFY-852]   ✓ Restored flight session: Command Center bridge active.");
                System.out.println("[ORBIT-VERIFY-852] TEST 4 PASSED: Resume Telemetry save-loading flow fully verified.");

                // Return to Main Menu for Test 5
                System.out.println("[ORBIT-VERIFY-852] Returning to Main Menu...");
                game.showMainMenu();
                waitFrames = 5;
                step++;
            }

            // --- 5. VERIFY COMMISSION EXPEDITION (NEW GAME FLOW) VIA STAGE MOUSE EVENTS ---
            case 11 -> {
                System.out.println("\n[ORBIT-VERIFY-852] --- TEST 5: COMMISSION EXPEDITION (NEW GAME FLOW) ---");
                assertCondition(game.getScreen() instanceof MainMenuScreen, "Active screen must be MainMenuScreen");
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();

                System.out.println("[ORBIT-VERIFY-852] Dispatching mouse click through Stage on New Game button...");
                simulateMouseClick(stage, menu.getNewGameButton());
                waitFrames = 3;
                step++;
            }

            case 12 -> {
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();
                Dialog dialog = findDialog(stage);
                assertCondition(dialog != null, "Commission Expedition dialog must appear on stage");
                System.out.println("[ORBIT-VERIFY-852]   ✓ New Expedition dialog opened via Stage mouse click.");

                TextButton launchBtn = findTextButton(dialog, "INITIALIZE FLIGHT");
                assertCondition(launchBtn != null, "Initialize Flight button must be present in dialog");

                System.out.println("[ORBIT-VERIFY-852] Dispatching mouse click on INITIALIZE FLIGHT button...");
                simulateMouseClick(stage, launchBtn);
                waitFrames = 30; // Wait for fade transition to Command Center
                step++;
            }

            case 13 -> {
                assertCondition(game.getScreen() instanceof CommandCenterScreen, "Commission Expedition click must transition into CommandCenterScreen");
                assertCondition(game.getController().isGameActive(), "Controller game session must be active");
                System.out.println("[ORBIT-VERIFY-852]   ✓ New expedition commissioned: Command Center bridge active.");
                System.out.println("[ORBIT-VERIFY-852] TEST 5 PASSED: Commission Expedition fully verified.");
                waitFrames = 5;
                step++;
            }

            // --- 6. VERIFY POST-NAVIGATION RESILIENCE VIA ACTUAL STAGE MOUSE EVENTS ---
            case 14 -> {
                System.out.println("\n[ORBIT-VERIFY-852] --- TEST 6: POST-NAVIGATION RESILIENCE ---");
                System.out.println("[ORBIT-VERIFY-852] Transitioning Command Center -> 3D Navigation...");
                game.showNavigationScreen();
                waitFrames = 5;
                step++;
            }

            case 15 -> {
                assertCondition(game.getScreen() instanceof NavigationScreen, "Active screen must be NavigationScreen");
                System.out.println("[ORBIT-VERIFY-852] Navigation online. Transitioning Navigation -> Command Center...");
                game.showCommandCenter();
                waitFrames = 5;
                step++;
            }

            case 16 -> {
                assertCondition(game.getScreen() instanceof CommandCenterScreen, "Active screen must be CommandCenterScreen");
                System.out.println("[ORBIT-VERIFY-852] Bridge online. Transitioning Command Center -> Main Menu...");
                game.showMainMenu();
                waitFrames = 5;
                step++;
            }

            case 17 -> {
                assertCondition(game.getScreen() instanceof MainMenuScreen, "Active screen must be MainMenuScreen");
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();
                assertCondition(Gdx.input.getInputProcessor() == stage, "Stage must own input processor after returning from Navigation");
                assertCondition(findDialog(stage) == null, "No modal dialogs should linger on stage");

                System.out.println("[ORBIT-VERIFY-852] Testing mouse click on Settings button post-navigation...");
                simulateMouseClick(stage, menu.getSettingsButton());
                waitFrames = 3;
                step++;
            }

            case 18 -> {
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();
                Dialog dialog = findDialog(stage);
                assertCondition(dialog != null, "Settings dialog must open via mouse click post-navigation");
                TextButton closeBtn = findTextButton(dialog, "CLOSE");
                assertCondition(closeBtn != null, "Close button must be present in Settings dialog");
                simulateMouseClick(stage, closeBtn);
                waitFrames = 3;
                step++;
            }

            case 19 -> {
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();
                assertCondition(findDialog(stage) == null, "Settings dialog must close post-navigation");
                System.out.println("[ORBIT-VERIFY-852]   ✓ Settings opened and closed cleanly via mouse click post-navigation.");

                System.out.println("[ORBIT-VERIFY-852] Testing mouse click on Credits button post-navigation...");
                simulateMouseClick(stage, menu.getCreditsButton());
                waitFrames = 3;
                step++;
            }

            case 20 -> {
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();
                Dialog dialog = findDialog(stage);
                assertCondition(dialog != null, "Credits dialog must open via mouse click post-navigation");
                TextButton ackBtn = findTextButton(dialog, "ACKNOWLEDGE");
                assertCondition(ackBtn != null, "Acknowledge button must be present in Credits dialog");
                simulateMouseClick(stage, ackBtn);
                waitFrames = 3;
                step++;
            }

            case 21 -> {
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();
                assertCondition(findDialog(stage) == null, "Credits dialog must close post-navigation");
                System.out.println("[ORBIT-VERIFY-852]   ✓ Credits opened and closed cleanly via mouse click post-navigation.");
                System.out.println("[ORBIT-VERIFY-852] TEST 6 PASSED: Post-navigation mouse interaction resilience 100% verified.");
                waitFrames = 3;
                step++;
            }

            case 22 -> {
                System.out.println("\n========================================================");
                System.out.println("[ORBIT-VERIFY-852] ALL PHASE 8.5.2 MOUSE INTERACTION TESTS PASSED!");
                System.out.println("[ORBIT-VERIFY-852] 1. Commission Expedition : Stage mouse-click -> Dialog -> Bridge transition: VERIFIED");
                System.out.println("[ORBIT-VERIFY-852] 2. Resume Telemetry      : Stage mouse-click -> Save restoration -> Bridge transition: VERIFIED");
                System.out.println("[ORBIT-VERIFY-852] 3. Avionics Configuration: Stage mouse-click -> Dialog open & close: VERIFIED");
                System.out.println("[ORBIT-VERIFY-852] 4. Expedition Dossier    : Stage mouse-click -> Dialog open & close: VERIFIED");
                System.out.println("[ORBIT-VERIFY-852] 5. Power Down Core       : Stage mouse-click -> Exit action execution: VERIFIED");
                System.out.println("[ORBIT-VERIFY-852] 6. Post-Navigation Route : Navigation -> Bridge -> Menu -> clicks functional: VERIFIED");
                System.out.println("========================================================\n");
                step++;
                Gdx.app.exit();
            }
        }
    }

    /**
     * Dispatches genuine Scene2D mouse interaction events (mouseMoved, touchDown, touchUp)
     * through the Stage, simulating the exact event sequence generated by LWJGL3 for physical mouse clicks.
     */
    private void simulateMouseClick(Stage stage, Actor target) {
        if (target == null) {
            throw new AssertionError("Target actor for mouse click is null");
        }
        // 1. Calculate center of actor in stage coordinates
        Vector2 stageCoords = target.localToStageCoordinates(new Vector2(target.getWidth() * 0.5f, target.getHeight() * 0.5f));

        // 2. Convert to screen coordinates as received by LWJGL3 / Gdx.input
        Vector2 screenCoords = stage.stageToScreenCoordinates(new Vector2(stageCoords.x, stageCoords.y));
        int screenX = (int) screenCoords.x;
        int screenY = (int) screenCoords.y;

        // 3. Dispatch actual mouse-moved event through Stage (InputProcessor)
        stage.mouseMoved(screenX, screenY);

        // 4. Dispatch actual mouse-down event (pointer 0, button 0 = LEFT_BUTTON)
        stage.touchDown(screenX, screenY, 0, 0);

        // 5. Dispatch actual mouse-up event (pointer 0, button 0 = LEFT_BUTTON)
        stage.touchUp(screenX, screenY, 0, 0);
    }

    private TextButton findTextButton(Dialog dialog, String buttonText) {
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

    private Dialog findDialog(Stage stage) {
        for (Actor actor : stage.getActors()) {
            if (actor instanceof Dialog d) {
                return d;
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

