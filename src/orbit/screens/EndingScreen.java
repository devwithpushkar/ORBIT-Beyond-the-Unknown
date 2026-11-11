package orbit.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import orbit.controllers.EndingResult;
import orbit.controllers.OrbitController;
import orbit.render.AssetManager;
import orbit.render.EndingSceneManager;
import orbit.render.OrbitGame;
import orbit.render.RenderManager;
import orbit.render.audio.AudioManager;
import orbit.render.components.HoloButton;
import orbit.render.components.HoloPanel;
import orbit.render.components.ScreenTransitionOverlay;
import orbit.render.theme.CinematicTheme;

import java.util.List;

/**
 * EndingScreen presents the cinematic conclusion, debriefing dossier,
 * and authoritative mission statistics for ORBIT's three campaign endings:
 *
 * - Ending A: Successful Return (Triumphant Earth orbit, artifact delivered)
 * - Ending B: Lost in Space (Adrift in interstellar void, distress beacon call)
 * - Ending C: The Unknown (Transcendence through Kepler-186f alien gateway)
 */
public class EndingScreen implements Screen {

    private final OrbitGame game;
    private final OrbitController controller;
    private final AssetManager assetManager;
    private final RenderManager renderManager;
    private final AudioManager audioManager;
    private final Skin skin;

    private final Stage stage;
    private final EndingSceneManager sceneManager;
    private ScreenTransitionOverlay transitionOverlay;

    private EndingResult currentResult;

    // Header UI
    private Label officerBadgeLabel;
    private Label outcomeBadgeLabel;

    // Left Column: Narrative Dossier
    private HoloPanel narrativePanel;
    private Label outcomeBannerLabel;
    private Label transmissionSubtitleLabel;
    private Table failureReasonBox;
    private Label failureReasonLabel;
    private Label narrativeLabel;
    private ScrollPane narrativeScrollPane;

    // Right Column: Telemetry & Expedition Manifest
    private HoloPanel telemetryPanel;
    private Label fuelValueLabel;
    private Label oxygenValueLabel;
    private Label energyValueLabel;
    private Label hullValueLabel;

    private Table cargoContainer;
    private Table planetsContainer;
    private Table objectivesContainer;

    // Bottom Action Rail
    private TextButton menuBtn;
    private TextButton replayBtn;
    private TextButton logBtn;
    private Label statusFeedbackLabel;

    public EndingScreen(OrbitGame game, OrbitController controller, AssetManager assetManager, RenderManager renderManager, AudioManager audioManager) {
        this.game = game;
        this.controller = controller;
        this.assetManager = assetManager;
        this.renderManager = renderManager;
        this.audioManager = (audioManager != null) ? audioManager : new AudioManager();

        this.skin = assetManager.getUiSkin();
        this.stage = new Stage(renderManager.getUiViewport(), renderManager.getSpriteBatch());
        this.sceneManager = new EndingSceneManager(assetManager);

        buildInterface();

        // Default or completed result from controller if available
        EndingResult initial = controller.getCompletedEndingResult();
        if (initial == null) {
            initial = EndingResult.endingA(controller.getGameState());
        }
        setEndingResult(initial);
    }

    private void buildInterface() {
        stage.clear();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.pad(14f, 22f, 14f, 22f);

        // --- 1. Top Avionics Debriefing Header ---
        Table topBar = new Table();
        topBar.align(Align.left);

        officerBadgeLabel = new Label("EXPEDITION DEBRIEFING // FINAL CAMPAIGN OUTCOME", skin, "mono");
        officerBadgeLabel.setColor(CinematicTheme.SOLAR_GOLD);
        officerBadgeLabel.setFontScale(0.92f);

        outcomeBadgeLabel = new Label("[ OUTCOME: MISSION ACCOMPLISHED // TRIUMPHANT RETURN ]", skin, "mono");
        outcomeBadgeLabel.setColor(CinematicTheme.STATUS_NOMINAL);
        outcomeBadgeLabel.setFontScale(0.80f);

        topBar.add(officerBadgeLabel).left().expandX();
        topBar.add(outcomeBadgeLabel).right();
        rootTable.add(topBar).fillX().top().padBottom(10f).row();

        // --- 2. Center Workspace: Left Narrative Dossier / Right Final Telemetry ---
        Table centerWorkspace = new Table();
        centerWorkspace.align(Align.topLeft);

        // ==========================================
        // LEFT COLUMN: Narrative Transmission Log (Width: 630px)
        // ==========================================
        Table leftColumn = new Table();
        leftColumn.align(Align.topLeft);

        narrativePanel = new HoloPanel("MISSION DEBRIEFING & OFFICIAL TRANSMISSION", skin);
        narrativePanel.setBracketColor(CinematicTheme.STATUS_NOMINAL);
        Table nContent = narrativePanel.getContentTable();
        nContent.align(Align.topLeft);

        outcomeBannerLabel = new Label("VICTORY: HUMANITY'S FIRST INTERSTELLAR TRIUMPH", skin, "subtitle");
        outcomeBannerLabel.setColor(CinematicTheme.STATUS_NOMINAL);
        outcomeBannerLabel.setFontScale(0.94f);
        nContent.add(outcomeBannerLabel).left().padBottom(3f).row();

        transmissionSubtitleLabel = new Label("VESSEL ODYSSEY-07 // COMMANDER VANCE // FLIGHT LOG ARCHIVE", skin, "mono");
        transmissionSubtitleLabel.setColor(CinematicTheme.TEXT_SECONDARY);
        transmissionSubtitleLabel.setFontScale(0.78f);
        nContent.add(transmissionSubtitleLabel).left().padBottom(10f).row();

        // Failure Reason Box (Only visible for Ending B)
        failureReasonBox = new Table();
        failureReasonBox.setBackground(skin.newDrawable("white", new Color(0.20f, 0.04f, 0.06f, 0.85f)));
        failureReasonBox.pad(6f, 10f, 6f, 10f);
        failureReasonLabel = new Label("[!] CASUALTY REPORT: Propulsion fuel depleted in interstellar void.", skin, "mono");
        failureReasonLabel.setColor(CinematicTheme.STATUS_CRITICAL);
        failureReasonLabel.setFontScale(0.78f);
        failureReasonLabel.setWrap(true);
        failureReasonBox.add(failureReasonLabel).width(580f).left();
        nContent.add(failureReasonBox).width(600f).padBottom(10f).left().row();

        // Narrative Body Scrollable Area
        narrativeLabel = new Label("", skin, "mono");
        narrativeLabel.setColor(CinematicTheme.TEXT_PRIMARY);
        narrativeLabel.setFontScale(0.82f);
        narrativeLabel.setWrap(true);

        Table narrativeContainer = new Table();
        narrativeContainer.align(Align.topLeft);
        narrativeContainer.add(narrativeLabel).width(580f).left().padBottom(12f).row();

        narrativeScrollPane = new ScrollPane(narrativeContainer, skin);
        narrativeScrollPane.setFadeScrollBars(false);
        narrativeScrollPane.setScrollingDisabled(true, false);
        nContent.add(narrativeScrollPane).width(600f).height(380f).left().top().row();

        leftColumn.add(narrativePanel).width(624f).height(550f).top().left();
        centerWorkspace.add(leftColumn).width(624f).top().left().padRight(16f);

        // ==========================================
        // RIGHT COLUMN: Final Mission Telemetry & Exploration Manifest (Width: 590px)
        // ==========================================
        Table rightColumn = new Table();
        rightColumn.align(Align.topLeft);

        telemetryPanel = new HoloPanel("FINAL MISSION TELEMETRY & EXPEDITION MANIFEST", skin);
        telemetryPanel.setBracketColor(CinematicTheme.CYAN_AVIONICS);
        Table tContent = telemetryPanel.getContentTable();
        tContent.align(Align.topLeft);

        // 1. Spacecraft Final Vitals
        tContent.add(createSectionHeader("FINAL SPACECRAFT VITALS")).left().padBottom(4f).row();

        Table vitalsTable = new Table();
        vitalsTable.setBackground(skin.newDrawable("white", new Color(0.04f, 0.08f, 0.15f, 0.75f)));
        vitalsTable.pad(6f, 10f, 6f, 10f);

        fuelValueLabel = createVitalsRow(vitalsTable, "PROPULSION FUEL RESERVES", "100%", CinematicTheme.CYAN_AVIONICS);
        oxygenValueLabel = createVitalsRow(vitalsTable, "LIFE SUPPORT OXYGEN", "100%", CinematicTheme.CYAN_AVIONICS);
        energyValueLabel = createVitalsRow(vitalsTable, "REACTOR CORE ENERGY", "100%", CinematicTheme.CYAN_AVIONICS);
        hullValueLabel = createVitalsRow(vitalsTable, "HULL INTEGRITY", "100%", CinematicTheme.CYAN_AVIONICS);

        tContent.add(vitalsTable).width(560f).left().padBottom(8f).row();

        // 2. Cargo Manifest Snapshot
        tContent.add(createSectionHeader("RECOVERED CARGO MANIFEST")).left().padBottom(4f).row();
        cargoContainer = new Table();
        cargoContainer.align(Align.topLeft);
        cargoContainer.setBackground(skin.newDrawable("white", new Color(0.04f, 0.08f, 0.15f, 0.75f)));
        cargoContainer.pad(4f, 8f, 4f, 8f);
        tContent.add(cargoContainer).width(560f).left().padBottom(8f).row();

        // 3. Celestial Exploration Catalog
        tContent.add(createSectionHeader("CELESTIAL RECONNAISSANCE CATALOG")).left().padBottom(4f).row();
        planetsContainer = new Table();
        planetsContainer.align(Align.topLeft);
        planetsContainer.setBackground(skin.newDrawable("white", new Color(0.04f, 0.08f, 0.15f, 0.75f)));
        planetsContainer.pad(4f, 8f, 4f, 8f);
        tContent.add(planetsContainer).width(560f).left().padBottom(8f).row();

        // 4. Strategic Objectives & Milestones
        tContent.add(createSectionHeader("CAMPAIGN STRATEGIC OBJECTIVES")).left().padBottom(4f).row();
        objectivesContainer = new Table();
        objectivesContainer.align(Align.topLeft);
        objectivesContainer.setBackground(skin.newDrawable("white", new Color(0.04f, 0.08f, 0.15f, 0.75f)));
        objectivesContainer.pad(4f, 8f, 4f, 8f);
        tContent.add(objectivesContainer).width(560f).left().row();

        rightColumn.add(telemetryPanel).width(584f).height(550f).top().left();
        centerWorkspace.add(rightColumn).width(584f).top().left();

        rootTable.add(centerWorkspace).fill().expand().top().padBottom(10f).row();

        // --- 3. Bottom Action Bar ---
        Table bottomRail = new Table();
        bottomRail.align(Align.left);

        menuBtn = new TextButton("[ESC] RETURN TO MAIN MENU", skin);
        menuBtn.getLabel().setFontScale(0.82f);

        replayBtn = new TextButton("[CIN] REPLAY CINEMATIC", skin);
        replayBtn.getLabel().setFontScale(0.82f);

        logBtn = new TextButton("[LOG] VIEW MISSION LOG", skin);
        logBtn.getLabel().setFontScale(0.82f);

        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleReturnToMainMenu();
            }
        });

        replayBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleReplayCinematic();
            }
        });

        logBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleViewMissionLog();
            }
        });

        statusFeedbackLabel = new Label("CAMPAIGN CONCLUDED // FLIGHT DATA ARCHIVED SECURELY", skin, "mono");
        statusFeedbackLabel.setColor(CinematicTheme.TEXT_MUTED);
        statusFeedbackLabel.setFontScale(0.78f);

        bottomRail.add(menuBtn).width(230f).height(38f).padRight(12f);
        bottomRail.add(replayBtn).width(200f).height(38f).padRight(12f);
        bottomRail.add(logBtn).width(200f).height(38f).padRight(18f);
        bottomRail.add(statusFeedbackLabel).left().expandX();

        rootTable.add(bottomRail).fillX().bottom();

        stage.addActor(rootTable);

        // Screen Transition Overlay
        transitionOverlay = new ScreenTransitionOverlay(skin.getDrawable("white"));
        transitionOverlay.setFillParent(true);
        stage.addActor(transitionOverlay);
        transitionOverlay.fadeIn(0.4f, null);
    }

    private Label createSectionHeader(String text) {
        Label lbl = new Label("• " + text, skin, "mono");
        lbl.setColor(CinematicTheme.SOLAR_GOLD);
        lbl.setFontScale(0.78f);
        return lbl;
    }

    private Label createVitalsRow(Table table, String name, String value, Color color) {
        Table row = new Table();
        Label nameLbl = new Label(name, skin, "mono");
        nameLbl.setColor(CinematicTheme.TEXT_SECONDARY);
        nameLbl.setFontScale(0.75f);

        Label valLbl = new Label(value, skin, "mono");
        valLbl.setColor(color);
        valLbl.setFontScale(0.78f);

        row.add(nameLbl).left().expandX();
        row.add(valLbl).right();
        table.add(row).fillX().padBottom(2f).row();
        return valLbl;
    }

    /**
     * Reconfigures the 3D cinematic scene and 2D diegetic debriefing interface
     * to reflect the specified ending result outcome.
     */
    public void setEndingResult(EndingResult result) {
        if (result == null) return;
        this.currentResult = result;

        // 1. Notify 3D Scene Manager
        sceneManager.setupEnding(result.getType());

        // 2. Theming & Outcome Customization
        Color themeColor;
        String badgeText;
        switch (result.getType()) {
            case ENDING_A_SUCCESSFUL_RETURN -> {
                themeColor = CinematicTheme.STATUS_NOMINAL;
                badgeText = "[ OUTCOME: MISSION ACCOMPLISHED // TRIUMPHANT RETURN ]";
                failureReasonBox.setVisible(false);
                narrativePanel.setBracketColor(CinematicTheme.STATUS_NOMINAL);
                telemetryPanel.setBracketColor(CinematicTheme.SOLAR_GOLD);
                if (audioManager != null) audioManager.playConfirm();
            }
            case ENDING_B_LOST_IN_SPACE -> {
                themeColor = CinematicTheme.STATUS_CRITICAL;
                badgeText = "[ OUTCOME: VESSEL CONTACT LOST // DRIFTING IN VOID ]";
                failureReasonBox.setVisible(true);
                String reason = (result.getFailureReason() != null) ? result.getFailureReason() : "Vessel lost telemetry contact during deep-space transit.";
                failureReasonLabel.setText("[!] CASUALTY REPORT: " + reason);
                narrativePanel.setBracketColor(CinematicTheme.STATUS_CRITICAL);
                telemetryPanel.setBracketColor(CinematicTheme.STATUS_CAUTION);
                if (audioManager != null) audioManager.playAlert();
            }
            case ENDING_C_THE_UNKNOWN -> {
                themeColor = new Color(0.78f, 0.55f, 1.0f, 1.0f);
                badgeText = "[ OUTCOME: TRANSCENDENCE // BEYOND EVENT HORIZON ]";
                failureReasonBox.setVisible(false);
                narrativePanel.setBracketColor(themeColor);
                telemetryPanel.setBracketColor(CinematicTheme.CYAN_AVIONICS);
                if (audioManager != null) audioManager.playConfirm();
            }
            default -> {
                themeColor = CinematicTheme.STATUS_NOMINAL;
                badgeText = "[ OUTCOME: CAMPAIGN CONCLUDED ]";
                failureReasonBox.setVisible(false);
            }
        }

        // 3. Populate Header & Banner
        outcomeBadgeLabel.setText(badgeText);
        outcomeBadgeLabel.setColor(themeColor);

        outcomeBannerLabel.setText(result.getOutcomeBanner());
        outcomeBannerLabel.setColor(themeColor);

        String cmdr = (result.getCommanderName() != null) ? result.getCommanderName().toUpperCase() : "COMMANDER";
        String vessel = (result.getSpaceshipName() != null) ? result.getSpaceshipName().toUpperCase() : "ODYSSEY-07";
        transmissionSubtitleLabel.setText("VESSEL " + vessel + " // COMMANDER " + cmdr + " // FLIGHT ARCHIVE");

        // 4. Populate Narrative
        narrativeLabel.setText(result.getNarrative());
        narrativeScrollPane.setScrollY(0f);

        // 5. Populate Vitals
        fuelValueLabel.setText(result.getFinalFuel() + "%");
        fuelValueLabel.setColor((result.getFinalFuel() >= 20) ? CinematicTheme.STATUS_NOMINAL : CinematicTheme.STATUS_CRITICAL);

        oxygenValueLabel.setText(result.getFinalOxygen() + "%");
        oxygenValueLabel.setColor((result.getFinalOxygen() > 0) ? CinematicTheme.STATUS_NOMINAL : CinematicTheme.STATUS_CRITICAL);

        energyValueLabel.setText(result.getFinalEnergy() + "%");
        energyValueLabel.setColor((result.getFinalEnergy() > 0) ? CinematicTheme.CYAN_AVIONICS : CinematicTheme.STATUS_CRITICAL);

        hullValueLabel.setText(result.getFinalHull() + "%");
        hullValueLabel.setColor((result.getFinalHull() > 0) ? CinematicTheme.STATUS_NOMINAL : CinematicTheme.STATUS_CRITICAL);

        // 6. Populate Cargo Manifest
        cargoContainer.clear();
        List<EndingResult.CargoSnapshot> cargoList = result.getCargoSnapshot();
        if (cargoList != null && !cargoList.isEmpty()) {
            for (EndingResult.CargoSnapshot item : cargoList) {
                Table row = new Table();
                Label nameLbl = new Label("  " + item.getName(), skin, "mono");
                nameLbl.setColor(CinematicTheme.TEXT_PRIMARY);
                nameLbl.setFontScale(0.75f);

                Label qtyLbl = new Label(item.getQuantity() + " units", skin, "mono");
                qtyLbl.setColor(item.getQuantity() > 0 ? CinematicTheme.SOLAR_GOLD : CinematicTheme.TEXT_MUTED);
                qtyLbl.setFontScale(0.75f);

                row.add(nameLbl).left().expandX();
                row.add(qtyLbl).right();
                cargoContainer.add(row).fillX().padBottom(1f).row();
            }
        }

        // 7. Populate Celestial Reconnaissance Catalog
        planetsContainer.clear();
        List<EndingResult.PlanetSnapshot> planets = result.getPlanetSnapshot();
        if (planets != null && !planets.isEmpty()) {
            for (EndingResult.PlanetSnapshot p : planets) {
                Table row = new Table();
                Label nameLbl = new Label("  " + p.getName(), skin, "mono");
                nameLbl.setColor(CinematicTheme.TEXT_PRIMARY);
                nameLbl.setFontScale(0.75f);

                String status = p.isExplored() ? "[SURVEYED]" : (p.isDiscovered() ? "[DISCOVERED]" : "[UNMAPPED]");
                Color statusCol = p.isExplored() ? CinematicTheme.STATUS_NOMINAL : (p.isDiscovered() ? CinematicTheme.AMBER_WARM : CinematicTheme.TEXT_MUTED);

                Label statusLbl = new Label(status, skin, "mono");
                statusLbl.setColor(statusCol);
                statusLbl.setFontScale(0.72f);

                row.add(nameLbl).left().expandX();
                row.add(statusLbl).right();
                planetsContainer.add(row).fillX().padBottom(1f).row();
            }
        }

        // 8. Populate Strategic Objectives
        objectivesContainer.clear();
        List<EndingResult.ObjectiveSnapshot> objectives = result.getObjectiveSnapshot();
        if (objectives != null && !objectives.isEmpty()) {
            for (EndingResult.ObjectiveSnapshot obj : objectives) {
                Table row = new Table();
                String mark = obj.isCompleted() ? "[X] " : "[ ] ";
                Label titleLbl = new Label("  " + mark + obj.getTitle(), skin, "mono");
                titleLbl.setColor(obj.isCompleted() ? CinematicTheme.STATUS_NOMINAL : CinematicTheme.TEXT_MUTED);
                titleLbl.setFontScale(0.73f);

                Label stageLbl = new Label("(" + obj.getStage() + ")", skin, "mono");
                stageLbl.setColor(CinematicTheme.TEXT_SECONDARY);
                stageLbl.setFontScale(0.70f);

                row.add(titleLbl).left().expandX();
                row.add(stageLbl).right();
                objectivesContainer.add(row).fillX().padBottom(1f).row();
            }
        }

        // Narrative Reveal Animation
        narrativeLabel.clearActions();
        narrativeLabel.setColor(CinematicTheme.TEXT_PRIMARY.r, CinematicTheme.TEXT_PRIMARY.g, CinematicTheme.TEXT_PRIMARY.b, 0.0f);
        narrativeLabel.addAction(Actions.sequence(
                Actions.delay(0.2f),
                Actions.fadeIn(0.9f)
        ));
    }

    public void handleReturnToMainMenu() {
        if (audioManager != null) audioManager.playCancel();
        transitionOverlay.fadeOut(0.4f, () -> {
            game.showMainMenu();
        });
    }

    public void handleReplayCinematic() {
        if (audioManager != null) audioManager.playConfirm();
        sceneManager.resetCinematic();
        if (currentResult != null) {
            setEndingResult(currentResult);
        }
        showStatusFeedback("[!] CINEMATIC CHOREOGRAPHY RESET // PLAYBACK INITIALIZED", CinematicTheme.CYAN_AVIONICS);
    }

    public void handleViewMissionLog() {
        if (audioManager != null) audioManager.playSelect();
        transitionOverlay.fadeOut(0.4f, () -> {
            game.showMissionLogScreen();
        });
    }

    private void showStatusFeedback(String message, Color color) {
        statusFeedbackLabel.setText(message);
        statusFeedbackLabel.setColor(color);
        statusFeedbackLabel.clearActions();
        statusFeedbackLabel.addAction(Actions.sequence(
                Actions.color(color, 0.1f),
                Actions.delay(3.0f),
                Actions.color(CinematicTheme.TEXT_MUTED, 0.8f)
        ));
    }

    @Override
    public void show() {
        System.out.println("[ORBIT-SCREEN] EndingScreen active. Campaign debriefing online.");
        Gdx.input.setInputProcessor(stage);
        if (stage != null) {
            stage.unfocusAll();
        }
    }

    @Override
    public void render(float delta) {
        // 1. Update & Render 3D Cinematic Scene Layer
        sceneManager.update(delta);
        sceneManager.render();

        // 2. Update & Render 2D Diegetic Telemetry UI
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        System.out.println(String.format("[ORBIT-SCREEN] Ending Screen window resized to %dx%d.", width, height));
        renderManager.update(width, height);
        if (sceneManager != null) {
            sceneManager.resize(width, height);
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (sceneManager != null) sceneManager.dispose();
    }

    public EndingResult getCurrentResult() {
        return currentResult;
    }

    public EndingSceneManager getSceneManager() {
        return sceneManager;
    }

    public TextButton getMenuButton() {
        return menuBtn;
    }

    public TextButton getReplayButton() {
        return replayBtn;
    }

    public TextButton getLogButton() {
        return logBtn;
    }

    public Label getNarrativeLabel() {
        return narrativeLabel;
    }

    public Label getOutcomeBannerLabel() {
        return outcomeBannerLabel;
    }

    public Label getFailureReasonLabel() {
        return failureReasonLabel;
    }

    public Stage getStage() {
        return stage;
    }
}
