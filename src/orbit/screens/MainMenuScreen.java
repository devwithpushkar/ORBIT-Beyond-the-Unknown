package orbit.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import orbit.controllers.OrbitController;
import orbit.models.GameState;
import orbit.models.Spaceship;
import orbit.render.AssetManager;
import orbit.render.OrbitGame;
import orbit.render.RenderManager;
import orbit.render.SceneManager;
import orbit.render.audio.AudioManager;
import orbit.render.components.HoloButton;
import orbit.render.components.HoloDialog;
import orbit.render.components.HoloPanel;
import orbit.render.components.ScreenTransitionOverlay;
import orbit.render.components.ShipResourceGauge;
import orbit.render.components.TelemetryReadout;
import orbit.render.theme.CinematicTheme;

/**
 * MainMenuScreen provides the initial cinematic gateway for ORBIT.
 * Combines an animated 3D space scene (planet, clouds, spacecraft, multi-depth starfield)
 * with a grounded, diegetic Scene2D holographic flight deck.
 *
 * Implements the Phase 8.3 Cinematic Visual Design System and connects directly
 * to the authoritative OrbitController.
 */
public class MainMenuScreen implements Screen {

    private final OrbitGame game;
    private final OrbitController controller;
    private final AssetManager assetManager;
    private final RenderManager renderManager;
    private final SceneManager sceneManager;
    private final AudioManager audioManager;
    private final Stage stage;
    private final Skin skin;

    // UI Widgets
    private HoloButton newGameBtn;
    private HoloButton continueButton;
    private HoloButton settingsBtn;
    private HoloButton creditsBtn;
    private HoloButton exitBtn;
    private Label archiveStatusLabel;
    private Label feedbackToastLabel;

    // Authentic 4-Resource Telemetry Instruments
    private ShipResourceGauge fuelGauge;
    private ShipResourceGauge o2Gauge;
    private ShipResourceGauge energyGauge;
    private ShipResourceGauge hullGauge;
    private TelemetryReadout vectorReadout;
    private TelemetryReadout commanderReadout;

    private ScreenTransitionOverlay transitionOverlay;

    private long frameCount = 0;
    private float logTimer = 0f;

    public MainMenuScreen(OrbitController controller, AssetManager assetManager, RenderManager renderManager) {
        this(null, controller, assetManager, renderManager, new AudioManager());
    }

    public MainMenuScreen(OrbitController controller, AssetManager assetManager, RenderManager renderManager, AudioManager audioManager) {
        this(null, controller, assetManager, renderManager, audioManager);
    }

    public MainMenuScreen(OrbitGame game, OrbitController controller, AssetManager assetManager, RenderManager renderManager, AudioManager audioManager) {
        this.game = game;
        this.controller = controller;
        this.assetManager = assetManager;
        this.renderManager = renderManager;
        this.audioManager = (audioManager != null) ? audioManager : new AudioManager();
        this.sceneManager = new SceneManager(assetManager);
        this.skin = assetManager.getUiSkin();
        this.stage = new Stage(renderManager.getUiViewport(), renderManager.getSpriteBatch());

        buildInterface();
        refreshSaveState();
    }

    private void buildInterface() {
        stage.clear();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.pad(20f, 32f, 20f, 32f);

        // --- 1. Top Avionics Telemetry Bar ---
        Table topBar = new Table();
        Label kernelBadge = new Label("SPACECRAFT ODYSSEY-07 // AVIONICS BUS: ONLINE", skin, "mono");
        kernelBadge.setColor(CinematicTheme.AMBER_WARM);

        Label subStatus = new Label("● BEACON: 1420.405 MHz  |  AUDIO: ACTIVE  |  V-SYNC: 60 FPS", skin, "mono");
        subStatus.setColor(CinematicTheme.STATUS_NOMINAL);

        topBar.add(kernelBadge).left().expandX();
        topBar.add(subStatus).right();
        rootTable.add(topBar).fillX().top().padBottom(24f).row();

        // --- 2. Center Workspace (Split Left: Menu / Right: Open Space for 3D Planet) ---
        Table centerSplit = new Table();

        // Left Column: Branding and Menu Controls (~45% width)
        Table leftColumn = new Table();
        leftColumn.align(Align.topLeft);

        // Title Branding
        Label titleLabel = new Label("O R B I T", skin, "title");
        titleLabel.setFontScale(2.4f);
        titleLabel.setColor(CinematicTheme.AMBER_WARM);

        Label subtitleLabel = new Label("B E Y O N D   T H E   U N K N O W N", skin, "subtitle");
        subtitleLabel.setColor(CinematicTheme.TEXT_PRIMARY);

        Label taglineLabel = new Label("DEEP SPACE RECONNAISSANCE & SURVIVAL TELEMETRY SYSTEM", skin, "mono");
        taglineLabel.setColor(CinematicTheme.TEXT_MUTED);

        leftColumn.add(titleLabel).left().padBottom(2f).row();
        leftColumn.add(subtitleLabel).left().padBottom(4f).row();
        leftColumn.add(taglineLabel).left().padBottom(20f).row();

        // Menu Flight Switches (Diegetic tactile controls)
        newGameBtn = new HoloButton("[EXP-01]", "COMMISSION EXPEDITION", "INITIALIZE DEEP SPACE MISSION SESSION", skin, audioManager, this::showNewGameDialog);
        newGameBtn.setAccentColor(CinematicTheme.AMBER_WARM);

        continueButton = new HoloButton("[TEL-02]", "RESUME TELEMETRY", "RESTORE ACTIVE FLIGHT ARCHIVE FROM DISK", skin, audioManager, this::handleContinueGame);
        continueButton.setAccentColor(CinematicTheme.STATUS_NOMINAL);

        archiveStatusLabel = new Label("NO ACTIVE TELEMETRY ARCHIVE FOUND", skin, "mono");
        archiveStatusLabel.setColor(CinematicTheme.TEXT_MUTED);
        archiveStatusLabel.setFontScale(0.78f);

        settingsBtn = new HoloButton("[CFG-03]", "AVIONICS CONFIGURATION", "HARDWARE, GRAPHICS & AUDIO ARCHITECTURE", skin, audioManager, this::showSettingsDialog);
        settingsBtn.setAccentColor(CinematicTheme.CYAN_AVIONICS);

        creditsBtn = new HoloButton("[LOG-04]", "EXPEDITION DOSSIER", "FLIGHT CREW & PROGRAM CREDITS", skin, audioManager, this::showCreditsDialog);
        creditsBtn.setAccentColor(CinematicTheme.STEEL_BLUE);

        exitBtn = new HoloButton("[PWR-05]", "POWER DOWN CORE", "SHUT DOWN AVIONICS & TERMINATE APPLICATION", skin, audioManager, this::handleExitGame);
        exitBtn.setAccentColor(CinematicTheme.STATUS_CRITICAL);

        float btnW = 390f;
        float btnH = 50f;
        leftColumn.add(newGameBtn).left().width(btnW).height(btnH).padBottom(6f).row();
        leftColumn.add(continueButton).left().width(btnW).height(btnH).padBottom(2f).row();
        leftColumn.add(archiveStatusLabel).left().padLeft(12f).padBottom(8f).row();
        leftColumn.add(settingsBtn).left().width(btnW).height(btnH).padBottom(6f).row();
        leftColumn.add(creditsBtn).left().width(btnW).height(btnH).padBottom(6f).row();
        leftColumn.add(exitBtn).left().width(btnW).height(btnH).padBottom(14f).row();

        // Lower Flight Telemetry Bay (Diegetic HoloPanel)
        HoloPanel telemetryBay = new HoloPanel("SPACECRAFT TELEMETRY // PRIMARY VITALS", skin);
        telemetryBay.setBracketColor(CinematicTheme.AMBER_DIM);
        Table bayContent = telemetryBay.getContentTable();

        fuelGauge = new ShipResourceGauge(ShipResourceGauge.ResourceType.FUEL, skin);
        o2Gauge = new ShipResourceGauge(ShipResourceGauge.ResourceType.OXYGEN, skin);
        energyGauge = new ShipResourceGauge(ShipResourceGauge.ResourceType.ENERGY, skin);
        hullGauge = new ShipResourceGauge(ShipResourceGauge.ResourceType.HULL, skin);

        bayContent.add(fuelGauge).fillX().padBottom(4f).row();
        bayContent.add(o2Gauge).fillX().padBottom(4f).row();
        bayContent.add(energyGauge).fillX().padBottom(4f).row();
        bayContent.add(hullGauge).fillX().padBottom(6f).row();

        vectorReadout = new TelemetryReadout("SECTOR VECTOR", "EARTH ORBIT // HIGH ALTITUDE", "STANDBY", skin);
        commanderReadout = new TelemetryReadout("COMMAND DESIGNATION", "AWAITING COMMISSION", skin);

        Table readoutsTable = new Table();
        readoutsTable.add(vectorReadout).left().expandX();
        readoutsTable.add(commanderReadout).right();
        bayContent.add(readoutsTable).fillX();

        leftColumn.add(telemetryBay).left().width(btnW).row();

        centerSplit.add(leftColumn).width(440f).top().left();
        // Right area remains clear for unobstructed viewing of the 3D rotating planet and spacecraft
        centerSplit.add().expand().fill();

        rootTable.add(centerSplit).expand().fill().row();

        // --- 3. Bottom Status Toast Bar ---
        Table bottomBar = new Table();
        feedbackToastLabel = new Label("ALL SUBSYSTEMS NOMINAL // STANDBY FOR FLIGHT DIRECTIVE", skin, "mono");
        feedbackToastLabel.setColor(CinematicTheme.TEXT_MUTED);

        Label buildLabel = new Label("ORBIT v2.0 // LIBGDX 1.14.2 // CINEMATIC ENGINE", skin, "mono");
        buildLabel.setColor(CinematicTheme.TEXT_DIM);

        bottomBar.add(feedbackToastLabel).left().expandX();
        bottomBar.add(buildLabel).right();
        rootTable.add(bottomBar).fillX().bottom();

        stage.addActor(rootTable);

        // Entrance Animation: smooth fade-in
        rootTable.getColor().a = 0.0f;
        rootTable.addAction(Actions.fadeIn(CinematicTheme.DURATION_SCREEN_FADE, CinematicTheme.EASING_SMOOTH));

        // Screen Transition Overlay
        transitionOverlay = new ScreenTransitionOverlay(skin.getDrawable("white"));
        transitionOverlay.setFillParent(true);
        stage.addActor(transitionOverlay);
        transitionOverlay.fadeIn(0.8f, null);
    }

    // --- Interactive Operations Connected to OrbitController ---

    public void showNewGameDialog() {
        HoloDialog dialog = new HoloDialog("COMMISSION EXPEDITION // COMMANDER ASSIGNMENT", skin, audioManager);

        Label prompt = new Label("Enter commanding officer designation:", skin, "default");
        TextField nameField = new TextField("Vance", skin);
        nameField.setAlignment(Align.center);

        Table content = dialog.getContentTable();
        content.add(prompt).padBottom(14f).row();
        content.add(nameField).width(320f).height(40f).padBottom(18f).row();

        TextButton launchBtn = new TextButton("INITIALIZE FLIGHT", skin);
        launchBtn.getLabel().setFontScale(0.85f);
        TextButton cancelBtn = new TextButton("ABORT", skin);
        cancelBtn.getLabel().setFontScale(0.85f);

        launchBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String cmdr = nameField.getText().trim();
                if (!cmdr.isEmpty()) {
                    dialog.remove();
                    if (audioManager != null) audioManager.playConfirm();
                    if (game != null) {
                        transitionOverlay.fadeOut(0.4f, () -> {
                            game.commissionExpedition(cmdr);
                            refreshSaveState();
                        });
                    } else {
                        controller.startNewGame(cmdr);
                        refreshSaveState();
                        showFeedbackToast("[+] EXPEDITION COMMISSIONED: COMMANDER " + cmdr.toUpperCase() + " ASSIGNED", CinematicTheme.STATUS_NOMINAL);
                    }
                }
            }
        });

        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.remove();
            }
        });

        Table buttonTable = dialog.getButtonTable();
        buttonTable.add(launchBtn).width(160f).height(40f).padRight(12f);
        buttonTable.add(cancelBtn).width(120f).height(40f);

        dialog.show(stage);
        stage.setKeyboardFocus(nameField);
    }

    public void handleContinueGame() {
        if (!controller.hasSave()) {
            if (audioManager != null) audioManager.playAlert();
            showFeedbackToast("[-] NO SAVE ARCHIVE DETECTED ON DISK", CinematicTheme.STATUS_CRITICAL);
            return;
        }

        boolean success = controller.continueGame();
        if (success) {
            refreshSaveState();
            if (audioManager != null) audioManager.playConfirm();
            if (game != null) {
                if (controller.isCampaignCompleted()) {
                    transitionOverlay.fadeOut(0.4f, () -> game.showEndingScreen(controller.getCompletedEndingResult()));
                } else {
                    transitionOverlay.fadeOut(0.4f, () -> game.showCommandCenter());
                }
            } else {
                if (controller.isCampaignCompleted()) {
                    showFeedbackToast("[!] ARCHIVE RESTORED: CAMPAIGN CONCLUDED (" + controller.getCompletedEnding().toUpperCase() + ")", CinematicTheme.STATUS_NOMINAL);
                } else {
                    showFeedbackToast("[+] FLIGHT TELEMETRY RESTORED FROM saves/orbit_save.dat", CinematicTheme.STATUS_NOMINAL);
                }
            }
        } else {
            if (audioManager != null) audioManager.playAlert();
            showFeedbackToast("[-] FAILED TO RESTORE SAVE: CORRUPTED DATA", CinematicTheme.STATUS_CRITICAL);
        }
    }

    private Runnable exitHandler = () -> Gdx.app.exit();

    public void setExitHandler(Runnable handler) {
        this.exitHandler = handler;
    }

    public void handleExitGame() {
        if (audioManager != null) audioManager.playCancel();
        if (exitHandler != null) {
            exitHandler.run();
        } else {
            Gdx.app.exit();
        }
    }

    public void showSettingsDialog() {
        HoloDialog dialog = new HoloDialog("AVIONICS CONFIGURATION & RUNTIME ARCHITECTURE", skin, audioManager);

        String info = "• Graphic Framework : libGDX 1.14.2 (LWJGL 3 Backend)\n"
                + "• Runtime Core      : OpenJDK 25 LTS (Windows x86_64)\n"
                + "• 3D Pipeline       : Hardware-Accelerated OpenGL 3.3 Core Profile\n"
                + "• Refresh Rate      : 60 FPS (V-Sync Active)\n"
                + "• Resolution        : 1280 x 800 (Native Windowed Mode)\n"
                + "• Audio Architecture: Procedural PCM Waveform Synthesis (16-bit 44.1kHz)\n"
                + "• Persistence Target: saves/orbit_save.dat";

        Label body = new Label(info, skin, "mono");
        body.setColor(CinematicTheme.TEXT_SECONDARY);

        dialog.getContentTable().add(body).left().padBottom(18f).row();

        TextButton closeBtn = new TextButton("CLOSE", skin);
        closeBtn.getLabel().setFontScale(0.85f);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.remove();
            }
        });
        dialog.getButtonTable().add(closeBtn).width(140f).height(38f);

        dialog.show(stage);
    }

    public void showCreditsDialog() {
        HoloDialog dialog = new HoloDialog("ORBIT // EXPEDITION DOSSIER", skin, audioManager);

        String text = "ORBIT: Beyond the Unknown\n"
                + "Cinematic Deep Space Exploration & Survival Adventure\n\n"
                + "• Creative Direction : Grounded Cinematic Sci-Fi (Alien: Isolation, The Expanse)\n"
                + "• Visual Architecture: Diegetic Avionics Design System & Multi-Depth 3D\n"
                + "• Audio Architecture : Autonomous Procedural Waveform Engine\n"
                + "• Gameplay Systems   : Authoritative OrbitController & GameState Engine\n"
                + "• Sol Command Fleet  : Directorate of Advanced Interplanetary Navigation";

        Label body = new Label(text, skin, "default");
        body.setColor(CinematicTheme.TEXT_SECONDARY);

        dialog.getContentTable().add(body).left().padBottom(18f).row();

        TextButton closeBtn = new TextButton("ACKNOWLEDGE", skin);
        closeBtn.getLabel().setFontScale(0.85f);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.remove();
            }
        });
        dialog.getButtonTable().add(closeBtn).width(160f).height(38f);

        dialog.show(stage);
    }

    public void refreshSaveState() {
        boolean hasSave = controller.hasSave();
        continueButton.setDisabled(false); // Fully interactive with clear telemetry toast if archive is missing

        if (hasSave) {
            continueButton.setAccentColor(CinematicTheme.STATUS_NOMINAL);
            String summary = controller.getSaveSummary();
            archiveStatusLabel.setText("ARCHIVE: " + summary.toUpperCase());
            archiveStatusLabel.setColor(CinematicTheme.STATUS_NOMINAL);
        } else {
            continueButton.setAccentColor(CinematicTheme.TEXT_MUTED);
            archiveStatusLabel.setText("NO ACTIVE TELEMETRY ARCHIVE FOUND");
            archiveStatusLabel.setColor(CinematicTheme.TEXT_MUTED);
        }

        // Live Controller State Snapshot across authentic 4 resources
        if (controller.isGameActive()) {
            GameState state = controller.getGameState();
            Spaceship ship = state.getSpaceship();

            fuelGauge.setValue(ship.getFuel());
            o2Gauge.setValue(ship.getOxygen());
            energyGauge.setValue(ship.getEnergy());
            hullGauge.setValue(ship.getHullIntegrity());

            vectorReadout.setValue(state.getCurrentLocation().getName().toUpperCase() + " ORBIT");
            vectorReadout.setStatus("IN FLIGHT", CinematicTheme.STATUS_NOMINAL);

            commanderReadout.setValue("CMDR " + state.getPlayer().getCommanderName().toUpperCase());
            commanderReadout.setStatus(state.getPlayer().getSpaceshipName().toUpperCase(), CinematicTheme.AMBER_WARM);
        } else if (hasSave) {
            fuelGauge.setValue(100);
            o2Gauge.setValue(100);
            energyGauge.setValue(100);
            hullGauge.setValue(100);

            vectorReadout.setValue("ARCHIVED SESSION DETECTED");
            vectorReadout.setStatus("READY", CinematicTheme.STATUS_NOMINAL);

            commanderReadout.setValue("SELECT [TEL-02] TO RESTORE");
            commanderReadout.setStatus("OFFLINE", CinematicTheme.TEXT_MUTED);
        } else {
            fuelGauge.setValue(100);
            o2Gauge.setValue(100);
            energyGauge.setValue(100);
            hullGauge.setValue(100);

            vectorReadout.setValue("STANDBY // AWAITING ORDERS");
            vectorReadout.setStatus("STANDBY", CinematicTheme.AMBER_WARM);

            commanderReadout.setValue("SELECT [EXP-01] TO COMMISSION");
            commanderReadout.setStatus("STANDBY", CinematicTheme.TEXT_MUTED);
        }
    }

    private void showFeedbackToast(String message, Color color) {
        feedbackToastLabel.setText(message);
        feedbackToastLabel.setColor(color);
        feedbackToastLabel.clearActions();
        feedbackToastLabel.addAction(Actions.sequence(
                Actions.color(color, 0.1f),
                Actions.delay(3.5f),
                Actions.color(CinematicTheme.TEXT_MUTED, 0.8f)
        ));
    }

    @Override
    public void show() {
        System.out.println("[ORBIT-SCREEN] MainMenuScreen displayed with Phase 8.3 Cinematic Design System.");
        Gdx.input.setInputProcessor(stage);
        stage.unfocusAll();

        // Defensively remove any lingering modal dialogs to guarantee clean button input
        for (Actor actor : new com.badlogic.gdx.utils.Array.ArrayIterator<>(stage.getActors())) {
            if (actor instanceof com.badlogic.gdx.scenes.scene2d.ui.Dialog) {
                actor.remove();
            }
        }

        refreshSaveState();
        if (transitionOverlay != null) {
            transitionOverlay.fadeIn(0.5f, null);
        }
    }

    @Override
    public void render(float delta) {
        // 1. Update & Render 3D Animated Scene (Starfield, 3D Rotating Planet, Clouds, Spacecraft, Exhaust)
        sceneManager.update(delta);
        sceneManager.render();

        // 2. Render 2D Diegetic Scene2D UI Overlay
        stage.act(delta);
        stage.draw();

        frameCount++;
        logTimer += delta;
        if (logTimer >= 2.0f) {
            System.out.println(String.format("[ORBIT-RENDER] Active: %d frames rendered | Target 60 FPS (Current: %d FPS)", frameCount, Gdx.graphics.getFramesPerSecond()));
            logTimer = 0f;
        }
    }

    @Override
    public void resize(int width, int height) {
        System.out.println(String.format("[ORBIT-SCREEN] Window resized to %dx%d. Updating viewports.", width, height));
        renderManager.update(width, height);
        sceneManager.resize(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        stage.unfocusAll();
    }

    public Stage getStage() {
        return stage;
    }

    public HoloButton getNewGameButton() {
        return newGameBtn;
    }

    public HoloButton getContinueButton() {
        return continueButton;
    }

    public HoloButton getSettingsButton() {
        return settingsBtn;
    }

    public HoloButton getCreditsButton() {
        return creditsBtn;
    }

    public HoloButton getExitButton() {
        return exitBtn;
    }

    @Override
    public void dispose() {
        sceneManager.dispose();
        stage.dispose();
    }
}
