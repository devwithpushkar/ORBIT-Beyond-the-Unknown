package orbit.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import orbit.render.components.HoloButton;
import orbit.screens.CommandCenterScreen;
import orbit.screens.MainMenuScreen;
import orbit.screens.NavigationScreen;

/**
 * Phase851Verifier conducts automated runtime verification of:
 * - Sequence A: Main Menu initialization, input processor ownership, modal dialog dismissal.
 * - Sequence B: Complete round-trip Main Menu -> Command Center -> Navigation -> Bridge -> Main Menu.
 * - Sequence C: Multiple repeated cycles confirming zero input lock or stale dialog retention.
 */
public class Phase851Verifier {

    private final OrbitGame game;
    private int step = 0;
    private int waitFrames = 0;
    private int cycleCount = 0;

    public Phase851Verifier(OrbitGame game) {
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
            System.err.println("[ORBIT-VERIFY] FAILED AT STEP " + step + ": " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private void executeStep() {
        switch (step) {
            case 0 -> {
                System.out.println("\n========================================================");
                System.out.println("[ORBIT-VERIFY] STARTING PHASE 8.5.1 AUTOMATED VERIFICATION");
                System.out.println("========================================================");
                waitFrames = 5; // Allow initial render frames to settle
                step++;
            }

            // --- SEQUENCE A: MAIN MENU VERIFICATION ---
            case 1 -> {
                System.out.println("[ORBIT-VERIFY] --- SEQUENCE A: VERIFYING MAIN MENU INPUT & DIALOGS ---");
                assertCondition(game.getScreen() instanceof MainMenuScreen, "Active screen must be MainMenuScreen");
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();
                assertCondition(Gdx.input.getInputProcessor() == stage, "Gdx.input must be bound to MainMenuScreen stage");

                // Verify no modal dialogs
                assertNoModalDialogs(stage, "Initial MainMenuScreen");

                // Verify button hit-testing
                testButtonHit(stage, menu.getNewGameButton(), "NEW EXPEDITION BUTTON");
                testButtonHit(stage, menu.getSettingsButton(), "SETTINGS BUTTON");
                testButtonHit(stage, menu.getCreditsButton(), "CREDITS BUTTON");

                // Test dialog open & close
                System.out.println("[ORBIT-VERIFY] Testing Settings dialog open/close...");
                menu.showSettingsDialog();
                Dialog dialog = findDialog(stage);
                assertCondition(dialog != null, "Settings dialog must appear on stage");
                dialog.hide();
                assertCondition(findDialog(stage) == null, "Settings dialog must be immediately removed");
                assertNoModalDialogs(stage, "Post-Settings dialog");

                System.out.println("[ORBIT-VERIFY] Testing Credits dialog open/close...");
                menu.showCreditsDialog();
                dialog = findDialog(stage);
                assertCondition(dialog != null, "Credits dialog must appear on stage");
                dialog.hide();
                assertCondition(findDialog(stage) == null, "Credits dialog must be immediately removed");
                assertNoModalDialogs(stage, "Post-Credits dialog");

                System.out.println("[ORBIT-VERIFY] SEQUENCE A PASSED: Main Menu input processor, dialogs & buttons verified.");
                waitFrames = 3;
                step++;
            }

            // --- SEQUENCE B: ROUND-TRIP TRANSITIONS ---
            case 2 -> {
                System.out.println("\n[ORBIT-VERIFY] --- SEQUENCE B: ROUND-TRIP NAVIGATION & RETURN ---");
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                System.out.println("[ORBIT-VERIFY] Starting game and transitioning to CommandCenterScreen...");
                game.getController().startNewGame("Cmdr-Verifier");
                game.showCommandCenter();
                waitFrames = 4;
                step++;
            }

            case 3 -> {
                assertCondition(game.getScreen() instanceof CommandCenterScreen, "Active screen must be CommandCenterScreen");
                CommandCenterScreen bridge = (CommandCenterScreen) game.getScreen();
                assertCondition(Gdx.input.getInputProcessor() == bridge.getStage(), "Input processor must belong to bridge stage");
                assertNoModalDialogs(bridge.getStage(), "CommandCenterScreen");
                System.out.println("[ORBIT-VERIFY] Command Center online. Transitioning to 3D Navigation...");
                game.showNavigationScreen();
                waitFrames = 4;
                step++;
            }

            case 4 -> {
                assertCondition(game.getScreen() instanceof NavigationScreen, "Active screen must be NavigationScreen");
                NavigationScreen nav = (NavigationScreen) game.getScreen();
                assertCondition(Gdx.input.getInputProcessor() != null, "Navigation input processor must be active");
                assertNoModalDialogs(nav.getStage(), "NavigationScreen");
                System.out.println("[ORBIT-VERIFY] NavigationScreen online. Holographic galaxy active.");

                System.out.println("[ORBIT-VERIFY] Returning from Navigation to Bridge...");
                game.showCommandCenter();
                waitFrames = 4;
                step++;
            }

            case 5 -> {
                assertCondition(game.getScreen() instanceof CommandCenterScreen, "Active screen must be CommandCenterScreen");
                System.out.println("[ORBIT-VERIFY] Returned to Bridge. Now returning to Main Menu...");
                game.showMainMenu();
                waitFrames = 4;
                step++;
            }

            case 6 -> {
                System.out.println("[ORBIT-VERIFY] Verifying Main Menu after return from Bridge...");
                assertCondition(game.getScreen() instanceof MainMenuScreen, "Active screen must be MainMenuScreen");
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();

                assertCondition(Gdx.input.getInputProcessor() == stage, "Input processor must be restored to MainMenuScreen stage");
                assertNoModalDialogs(stage, "MainMenuScreen after return");

                // Test that every button is unblocked and hit-testable
                testButtonHit(stage, menu.getNewGameButton(), "NEW EXPEDITION BUTTON (POST-RETURN)");
                testButtonHit(stage, menu.getContinueButton(), "CONTINUE EXPEDITION BUTTON (POST-RETURN)");
                testButtonHit(stage, menu.getSettingsButton(), "SETTINGS BUTTON (POST-RETURN)");
                testButtonHit(stage, menu.getCreditsButton(), "CREDITS BUTTON (POST-RETURN)");

                // Test clicking New Game dialog after return
                menu.showNewGameDialog();
                Dialog dialog = findDialog(stage);
                assertCondition(dialog != null, "New Game dialog must appear after return");
                dialog.hide();
                assertCondition(findDialog(stage) == null, "New Game dialog must be removed cleanly");
                assertNoModalDialogs(stage, "Post-NewGame dismissal");

                System.out.println("[ORBIT-VERIFY] SEQUENCE B PASSED: Round-trip transition preserved input responsiveness.");
                cycleCount = 0;
                waitFrames = 3;
                step++;
            }

            // --- SEQUENCE C: REPEATED CYCLES ---
            case 7 -> {
                cycleCount++;
                System.out.println(String.format("\n[ORBIT-VERIFY] --- SEQUENCE C: REPEAT CYCLE %d OF 2 ---", cycleCount));
                game.showCommandCenter();
                waitFrames = 3;
                step++;
            }

            case 8 -> {
                game.showNavigationScreen();
                waitFrames = 3;
                step++;
            }

            case 9 -> {
                game.showCommandCenter();
                waitFrames = 3;
                step++;
            }

            case 10 -> {
                game.showMainMenu();
                waitFrames = 3;
                step++;
            }

            case 11 -> {
                MainMenuScreen menu = (MainMenuScreen) game.getScreen();
                Stage stage = menu.getStage();
                assertCondition(Gdx.input.getInputProcessor() == stage, "Cycle " + cycleCount + ": stage must own input processor");
                assertNoModalDialogs(stage, "Cycle " + cycleCount + " MainMenuScreen");

                testButtonHit(stage, menu.getNewGameButton(), "Cycle " + cycleCount + " New Game");
                testButtonHit(stage, menu.getContinueButton(), "Cycle " + cycleCount + " Continue");
                testButtonHit(stage, menu.getSettingsButton(), "Cycle " + cycleCount + " Settings");
                testButtonHit(stage, menu.getCreditsButton(), "Cycle " + cycleCount + " Credits");

                System.out.println(String.format("[ORBIT-VERIFY] Cycle %d verified successfully.", cycleCount));
                if (cycleCount < 2) {
                    step = 7; // Repeat cycle
                } else {
                    step = 12; // Finished all cycles
                }
                waitFrames = 2;
            }

            case 12 -> {
                System.out.println("\n========================================================");
                System.out.println("[ORBIT-VERIFY] ALL PHASE 8.5.1 REGRESSION SEQUENCES (A, B, C) PASSED!");
                System.out.println("[ORBIT-VERIFY] - Sequence A: Main menu input & modal dialog dismissal: VERIFIED");
                System.out.println("[ORBIT-VERIFY] - Sequence B: Round-trip Navigation -> Bridge -> Menu: VERIFIED");
                System.out.println("[ORBIT-VERIFY] - Sequence C: Multiple repeated transition cycles: VERIFIED (2 cycles)");
                System.out.println("========================================================\n");
                step++;
                Gdx.app.exit();
            }
        }
    }

    private void testButtonHit(Stage stage, HoloButton btn, String label) {
        if (btn == null) {
            throw new AssertionError("Button " + label + " is null");
        }
        Vector2 stageCoords = btn.localToStageCoordinates(new Vector2(btn.getWidth() * 0.5f, btn.getHeight() * 0.5f));
        Actor hit = stage.hit(stageCoords.x, stageCoords.y, true);
        if (hit == null) {
            throw new AssertionError("Hit testing returned null for " + label + " at " + stageCoords);
        }
        if (!hit.isDescendantOf(btn) && hit != btn) {
            throw new AssertionError("Hit testing for " + label + " hit unexpected actor: " + hit + " (class " + hit.getClass().getName() + ") instead of button");
        }
        System.out.println(String.format("[ORBIT-VERIFY]   ✓ Hit test passed for %s (hit actor: %s)", label, hit.getClass().getSimpleName()));
    }

    private void assertNoModalDialogs(Stage stage, String context) {
        for (Actor actor : stage.getActors()) {
            if (actor instanceof Dialog) {
                throw new AssertionError("Stale modal Dialog found on stage during " + context + ": " + actor);
            }
        }
    }

    private Dialog findDialog(Stage stage) {
        for (Actor actor : stage.getActors()) {
            if (actor instanceof Dialog) {
                return (Dialog) actor;
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

