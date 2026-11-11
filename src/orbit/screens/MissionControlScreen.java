package orbit.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import orbit.controllers.EndingResult;
import orbit.controllers.OrbitController;
import orbit.models.Inventory;
import orbit.models.MissionObjective;
import orbit.models.Planet;
import orbit.models.Spaceship;
import orbit.render.AssetManager;
import orbit.render.OrbitGame;
import orbit.render.RenderManager;
import orbit.render.audio.AudioManager;
import orbit.render.components.HoloButton;
import orbit.render.components.HoloDialog;
import orbit.render.components.HoloPanel;
import orbit.render.components.ScreenTransitionOverlay;
import orbit.render.theme.CinematicTheme;

import java.util.List;

/**
 * MissionControlScreen provides the dedicated cinematic command operations station for ORBIT.
 *
 * Implements:
 * - Full three-stage campaign progression deck (Stage 1 Departure, Stage 2 Deep Voyager, Stage 3 Beyond the Unknown).
 * - Live mission objectives tracker with active/completed/locked status and progress metrics.
 * - Overall expedition progress gauge and High Command tactical directive briefings.
 * - Strategic campaign milestone checklist (planetary corridors, materials, transmissions, anomalies).
 * - Kepler climax and final directive authorization protocol telemetry.
 * - Bidirectional return-to-bridge navigation.
 */
public class MissionControlScreen implements Screen {

    private final OrbitGame game;
    private final OrbitController controller;
    private final AssetManager assetManager;
    private final RenderManager renderManager;
    private final AudioManager audioManager;

    private Stage stage;
    private Skin skin;
    private ScreenTransitionOverlay transitionOverlay;

    // Progression Telemetry Labels
    private Label stageHeaderLabel;
    private Label missionTitleLabel;
    private Label progressLabel;
    private Table progressBarFilled;
    private Table progressBarTrack;
    private Label directiveAdviceLabel;

    // Containers
    private Table objectivesListTable;
    private Table milestonesTable;
    private Label climaxSignalStatusLabel;
    private Label climaxDirectiveStatusLabel;
    private Label climaxEndingLabel;
    private Label statusFeedbackLabel;

    // Controls
    private TextButton returnBtn;
    private TextButton climaxActionBtn;
    private String activeFilter = "ALL";
    private TextButton filterAllBtn;
    private TextButton filterStage1Btn;
    private TextButton filterStage2Btn;
    private TextButton filterStage3Btn;

    private int frameCount = 0;
    private float logTimer = 0f;

    public MissionControlScreen(OrbitGame game, OrbitController controller, AssetManager assetManager, RenderManager renderManager, AudioManager audioManager) {
        this.game = game;
        this.controller = controller;
        this.assetManager = assetManager;
        this.renderManager = renderManager;
        this.audioManager = (audioManager != null) ? audioManager : new AudioManager();

        this.skin = assetManager.getUiSkin();
        this.stage = new Stage(renderManager.getUiViewport(), renderManager.getSpriteBatch());

        buildInterface();
        refreshFromController();
    }

    private void buildInterface() {
        stage.clear();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.pad(16f, 26f, 16f, 26f);

        // --- 1. Top Avionics & Fleet Header ---
        Table topBar = new Table();
        topBar.align(Align.left);

        Label deckBadge = new Label("SPACECRAFT ODYSSEY-07 // FLEET COMMAND OPERATIONS & STRATEGIC DIRECTIVES", skin, "mono");
        deckBadge.setColor(CinematicTheme.SOLAR_GOLD);
        deckBadge.setFontScale(0.92f);

        Label subStatus = new Label("SYS: FLEET SECURE  |  ENCRYPTION: MIL-SPEC  |  COMM: HIGH COMMAND", skin, "mono");
        subStatus.setColor(CinematicTheme.STATUS_NOMINAL);
        subStatus.setFontScale(0.80f);

        topBar.add(deckBadge).left().expandX();
        topBar.add(subStatus).right();
        rootTable.add(topBar).fillX().top().padBottom(14f).row();

        // --- 2. Center Workspace: Left Strategic Deck / Right Milestones & Climax ---
        Table centerWorkspace = new Table();
        centerWorkspace.align(Align.topLeft);

        // ==========================================
        // LEFT COLUMN: Campaign Stages & Objectives (Width: 690px)
        // ==========================================
        Table leftColumn = new Table();
        leftColumn.align(Align.topLeft);

        // A. Campaign Progression Deck HoloPanel
        HoloPanel progPanel = new HoloPanel("CAMPAIGN PROGRESSION & STRATEGIC ORDERS", skin);
        progPanel.setBracketColor(CinematicTheme.SOLAR_GOLD);
        Table pContent = progPanel.getContentTable();
        pContent.align(Align.topLeft);

        stageHeaderLabel = new Label("CURRENT CAMPAIGN STAGE: STAGE 1 — DEPARTURE", skin, "subtitle");
        stageHeaderLabel.setColor(CinematicTheme.TEXT_PRIMARY);
        stageHeaderLabel.setFontScale(0.95f);
        pContent.add(stageHeaderLabel).left().padBottom(3f).row();

        missionTitleLabel = new Label("OPERATION SOL DEPARTURE // FIRST INTERPLANETARY FLIGHT", skin, "mono");
        missionTitleLabel.setColor(CinematicTheme.AMBER_WARM);
        missionTitleLabel.setFontScale(0.82f);
        pContent.add(missionTitleLabel).left().padBottom(8f).row();

        // Progress Bar
        Table progressRow = new Table();
        progressRow.align(Align.left);

        progressLabel = new Label("EXPEDITION PROGRESS: 0% COMPLETE", skin, "mono");
        progressLabel.setColor(CinematicTheme.CYAN_AVIONICS);
        progressLabel.setFontScale(0.80f);
        progressRow.add(progressLabel).left().padBottom(4f).row();

        progressBarTrack = new Table();
        progressBarTrack.setBackground(skin.newDrawable("white", CinematicTheme.CHASSIS_PLATE));
        progressBarFilled = new Table();
        progressBarFilled.setBackground(skin.newDrawable("white", CinematicTheme.SOLAR_GOLD));
        progressBarTrack.add(progressBarFilled).left().height(10f);
        progressBarTrack.add().expandX().fillX();
        progressRow.add(progressBarTrack).width(660f).height(10f).left().padBottom(8f).row();

        pContent.add(progressRow).fillX().padBottom(4f).row();

        // Directive Briefing Box
        Table adviceBox = new Table();
        adviceBox.setBackground(skin.newDrawable("white", CinematicTheme.PANEL_GLASS));
        adviceBox.pad(6f, 10f, 6f, 10f);
        adviceBox.align(Align.topLeft);

        Label adviceHeader = new Label("• FLEET COMMAND TACTICAL DIRECTIVE BRIEFING:", skin, "mono");
        adviceHeader.setColor(CinematicTheme.CYAN_AVIONICS);
        adviceHeader.setFontScale(0.78f);
        adviceBox.add(adviceHeader).left().padBottom(3f).row();

        directiveAdviceLabel = new Label("Disengage mooring clamps and burn propulsion vector to Mars via Navigation Console.", skin, "mono");
        directiveAdviceLabel.setColor(CinematicTheme.TEXT_PRIMARY);
        directiveAdviceLabel.setFontScale(0.78f);
        directiveAdviceLabel.setWrap(true);
        adviceBox.add(directiveAdviceLabel).width(640f).left();

        pContent.add(adviceBox).fillX().row();
        leftColumn.add(progPanel).width(690f).padBottom(10f).row();

        // B. Objectives Console HoloPanel
        HoloPanel objPanel = new HoloPanel("CAMPAIGN OBJECTIVES & OPERATIONAL TARGETS", skin);
        objPanel.setBracketColor(CinematicTheme.CYAN_AVIONICS);
        Table oContent = objPanel.getContentTable();
        oContent.align(Align.topLeft);

        // Filter Bar
        Table filterBar = new Table();
        filterBar.align(Align.left);

        filterAllBtn = createFilterButton("ALL STAGES", "ALL");
        filterStage1Btn = createFilterButton("STAGE 1 (SOL)", MissionObjective.STAGE_1);
        filterStage2Btn = createFilterButton("STAGE 2 (OUTER)", MissionObjective.STAGE_2);
        filterStage3Btn = createFilterButton("STAGE 3 (EXTRASOLAR)", MissionObjective.STAGE_3);

        filterBar.add(filterAllBtn).height(26f).padRight(6f);
        filterBar.add(filterStage1Btn).height(26f).padRight(6f);
        filterBar.add(filterStage2Btn).height(26f).padRight(6f);
        filterBar.add(filterStage3Btn).height(26f);
        oContent.add(filterBar).left().padBottom(8f).row();

        // Scrollable Objectives List
        objectivesListTable = new Table();
        objectivesListTable.align(Align.topLeft);

        ScrollPane objScroll = new ScrollPane(objectivesListTable, skin);
        objScroll.setFadeScrollBars(false);
        objScroll.setScrollingDisabled(true, false);
        oContent.add(objScroll).width(660f).height(260f).fill().row();

        leftColumn.add(objPanel).width(690f).row();
        centerWorkspace.add(leftColumn).width(690f).top().left().padRight(16f);

        // ==========================================
        // RIGHT COLUMN: Milestones & Kepler Climax (Width: 520px)
        // ==========================================
        Table rightColumn = new Table();
        rightColumn.align(Align.topLeft);

        // C. Campaign Milestones HoloPanel
        HoloPanel milestonePanel = new HoloPanel("FLEET CAMPAIGN MILESTONES & DISCOVERIES", skin);
        milestonePanel.setBracketColor(CinematicTheme.AMBER_WARM);
        Table mContent = milestonePanel.getContentTable();
        mContent.align(Align.topLeft);

        milestonesTable = new Table();
        milestonesTable.align(Align.topLeft);
        mContent.add(milestonesTable).fillX().row();

        rightColumn.add(milestonePanel).width(520f).padBottom(10f).row();

        // D. Kepler Climax & Final Directives HoloPanel
        HoloPanel climaxPanel = new HoloPanel("EXTRASOLAR ANOMALY & CLIMAX PROTOCOLS", skin);
        climaxPanel.setBracketColor(CinematicTheme.SOLAR_GOLD);
        Table cContent = climaxPanel.getContentTable();
        cContent.align(Align.topLeft);

        Table climaxGrid = new Table();
        climaxGrid.align(Align.topLeft);

        climaxGrid.add(createSectionHeader("ANOMALOUS TRANSMISSION SOURCE (KEPLER-186F)")).left().padBottom(2f).row();
        climaxSignalStatusLabel = new Label("• Status: SEARCH IN PROGRESS (AWAITING CORRIDOR)", skin, "mono");
        climaxSignalStatusLabel.setColor(CinematicTheme.TEXT_MUTED);
        climaxSignalStatusLabel.setFontScale(0.78f);
        climaxGrid.add(climaxSignalStatusLabel).left().padBottom(8f).row();

        climaxGrid.add(createSectionHeader("FINAL MISSION DIRECTIVE CLEARANCE")).left().padBottom(2f).row();
        climaxDirectiveStatusLabel = new Label("• Clearance: LOCKED (COMPLETE PRECEDING STAGES)", skin, "mono");
        climaxDirectiveStatusLabel.setColor(CinematicTheme.TEXT_MUTED);
        climaxDirectiveStatusLabel.setFontScale(0.78f);
        climaxGrid.add(climaxDirectiveStatusLabel).left().padBottom(8f).row();

        climaxGrid.add(createSectionHeader("CAMPAIGN CONCLUSION STATUS")).left().padBottom(2f).row();
        climaxEndingLabel = new Label("• Conclusion: ACTIVE FLIGHT OPERATIONS IN PROGRESS", skin, "mono");
        climaxEndingLabel.setColor(CinematicTheme.STATUS_NOMINAL);
        climaxEndingLabel.setFontScale(0.78f);
        climaxGrid.add(climaxEndingLabel).left().padBottom(6f).row();

        climaxActionBtn = new TextButton("[DIR-CLIMAX] INITIATE FINAL DIRECTIVE", skin);
        climaxActionBtn.getLabel().setFontScale(0.82f);
        climaxActionBtn.setVisible(false);
        climaxActionBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (controller.canInitiateFinalDirective()) {
                    showFinalDirectiveChoiceDialog();
                } else if (controller.isCampaignCompleted()) {
                    transitionOverlay.fadeOut(0.4f, () -> game.showEndingScreen(controller.getCompletedEndingResult()));
                }
            }
        });
        climaxGrid.add(climaxActionBtn).width(490f).height(36f).padTop(4f).row();

        cContent.add(climaxGrid).fillX().row();
        rightColumn.add(climaxPanel).width(520f).row();

        centerWorkspace.add(rightColumn).width(520f).top().left();
        rootTable.add(centerWorkspace).expand().fill().padBottom(10f).row();

        // --- 3. Bottom Operations Rail ---
        Table bottomRail = new Table();
        statusFeedbackLabel = new Label("COMMAND DIRECTIVES ONLINE // ALL OBJECTIVE STREAMS SYNCHRONIZED", skin, "mono");
        statusFeedbackLabel.setColor(CinematicTheme.TEXT_MUTED);
        statusFeedbackLabel.setFontScale(0.82f);

        returnBtn = new TextButton("RETURN TO BRIDGE", skin);
        returnBtn.getLabel().setFontScale(0.82f);
        returnBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleReturnToBridge();
            }
        });

        bottomRail.add(statusFeedbackLabel).left().expandX();
        bottomRail.add(returnBtn).width(190f).height(38f).right();
        rootTable.add(bottomRail).fillX().bottom();

        stage.addActor(rootTable);

        // Screen Transition Overlay
        transitionOverlay = new ScreenTransitionOverlay(skin.getDrawable("white"));
        transitionOverlay.setFillParent(true);
        stage.addActor(transitionOverlay);
        transitionOverlay.fadeIn(0.4f, null);
    }

    private TextButton createFilterButton(String text, String filterKey) {
        TextButton btn = new TextButton(text, skin);
        btn.getLabel().setFontScale(0.75f);
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (audioManager != null) audioManager.playSelect();
                activeFilter = filterKey;
                refreshObjectivesTable();
            }
        });
        return btn;
    }

    private Label createSectionHeader(String title) {
        Label lbl = new Label(title, skin, "mono");
        lbl.setColor(CinematicTheme.CYAN_AVIONICS);
        lbl.setFontScale(0.76f);
        return lbl;
    }

    public void refreshFromController() {
        if (!controller.isGameActive()) return;

        String currentStage = controller.getCurrentStage();
        int progress = controller.getMissionProgress();

        // 1. Stage & Mission Title Readout
        stageHeaderLabel.setText("CURRENT CAMPAIGN STAGE: " + currentStage.toUpperCase());
        stageHeaderLabel.setColor(getStageColor(currentStage));
        missionTitleLabel.setText(controller.getStageMissionTitle(currentStage));

        // 2. Progress Gauge
        progressLabel.setText(String.format("OVERALL EXPEDITION PROGRESS: %d%% COMPLETE", progress));
        float filledW = Math.max(4f, 660f * (progress / 100f));
        progressBarFilled.clear();
        progressBarTrack.clearChildren();
        progressBarTrack.add(progressBarFilled).width(filledW).height(10f).left();
        progressBarTrack.add().expandX().fillX();

        // 3. Directive Advice
        directiveAdviceLabel.setText(controller.getNextDirectiveAdvice());

        // 4. Refresh Sub-sections
        refreshObjectivesTable();
        refreshMilestonesTable();
        refreshClimaxTelemetries();
    }

    private Color getStageColor(String stage) {
        if (stage == null) return CinematicTheme.TEXT_PRIMARY;
        if (stage.contains("1")) return CinematicTheme.CYAN_AVIONICS;
        if (stage.contains("2")) return CinematicTheme.AMBER_WARM;
        if (stage.contains("3")) return CinematicTheme.SOLAR_GOLD;
        return CinematicTheme.TEXT_PRIMARY;
    }

    private void refreshObjectivesTable() {
        objectivesListTable.clear();

        List<MissionObjective> objectives = controller.getObjectives();
        if (objectives == null || objectives.isEmpty()) {
            Label empty = new Label("No campaign objectives registered.", skin, "mono");
            empty.setColor(CinematicTheme.TEXT_MUTED);
            objectivesListTable.add(empty).left().pad(8f).row();
            return;
        }

        for (MissionObjective obj : objectives) {
            if (!activeFilter.equals("ALL") && !obj.getStage().equalsIgnoreCase(activeFilter)) {
                continue;
            }

            Table row = new Table();
            row.align(Align.left);
            row.pad(4f, 6f, 4f, 6f);
            row.setBackground(skin.newDrawable("white", obj.isCompleted() ? CinematicTheme.CHASSIS_PLATE : CinematicTheme.PANEL_GLASS));

            // Status Badge
            String tagText;
            Color tagColor;
            if (obj.isCompleted()) {
                tagText = "[DONE]     ";
                tagColor = CinematicTheme.STATUS_NOMINAL;
            } else if (obj.isInProgress()) {
                tagText = "[ACTIVE]   ";
                tagColor = CinematicTheme.CYAN_AVIONICS;
            } else {
                tagText = "[LOCKED]   ";
                tagColor = CinematicTheme.TEXT_MUTED;
            }

            Label statusTag = new Label(tagText, skin, "mono");
            statusTag.setColor(tagColor);
            statusTag.setFontScale(0.78f);
            row.add(statusTag).left().width(85f);

            // Title & Progress Info
            Table infoCol = new Table();
            infoCol.align(Align.left);

            Table titleRow = new Table();
            titleRow.align(Align.left);

            Label titleLbl = new Label(obj.getTitle(), skin, "mono");
            titleLbl.setColor(obj.isCompleted() ? CinematicTheme.TEXT_SECONDARY : CinematicTheme.TEXT_PRIMARY);
            titleLbl.setFontScale(0.80f);
            titleRow.add(titleLbl).left().expandX();

            Label progInfo = new Label("[" + obj.getProgressInfo() + "]", skin, "mono");
            progInfo.setColor(obj.isCompleted() ? CinematicTheme.STATUS_NOMINAL : CinematicTheme.TEXT_MUTED);
            progInfo.setFontScale(0.75f);
            titleRow.add(progInfo).right().padLeft(8f);
            infoCol.add(titleRow).fillX().row();

            Label descLbl = new Label(obj.getDescription(), skin, "mono");
            descLbl.setColor(CinematicTheme.TEXT_MUTED);
            descLbl.setFontScale(0.72f);
            descLbl.setWrap(true);
            infoCol.add(descLbl).width(550f).left();

            row.add(infoCol).expandX().fillX();
            objectivesListTable.add(row).fillX().padBottom(4f).row();
        }
    }

    private void refreshMilestonesTable() {
        milestonesTable.clear();

        Planet earth = controller.getPlanetByName("Earth");
        Planet mars = controller.getPlanetByName("Mars");
        Planet europa = controller.getPlanetByName("Europa");
        Planet titan = controller.getPlanetByName("Titan");
        Planet kepler = controller.getPlanetByName("Kepler-186f");

        boolean departed = controller.getGameState() != null && controller.getGameState().hasDepartedEarth();
        boolean marsOrbit = controller.getCurrentLocation() != null && controller.getCurrentLocation().getName().equalsIgnoreCase("Mars");
        boolean earthExplored = earth != null && earth.isExplored();
        boolean marsExplored = mars != null && mars.isExplored();
        boolean europaDiscovered = europa != null && europa.isDiscovered();
        boolean titanDiscovered = titan != null && titan.isDiscovered();
        boolean transmission = controller.getGameState() != null && controller.getGameState().isTransmissionEvidenceDiscovered();
        boolean keplerDiscovered = kepler != null && kepler.isDiscovered();
        boolean signalSource = controller.getGameState() != null && controller.getGameState().isSignalSourceDiscovered();

        addMilestoneRow("STAGE 1: Earth Docking Clamps Disengaged", departed);
        addMilestoneRow("STAGE 1: Mars Parking Orbit Insertion", marsOrbit || marsExplored);
        addMilestoneRow("STAGE 1: Planetary Surface Reconnaissance", earthExplored || marsExplored);
        addMilestoneRow("STAGE 2: Europa Outer Moon Trajectory Unlocked", europaDiscovered);
        addMilestoneRow("STAGE 2: Titan Cryogenic Atmosphere Unlocked", titanDiscovered);
        addMilestoneRow("STAGE 2: Sub-Space Signal Evidence Triangulated", transmission);
        addMilestoneRow("STAGE 3: Kepler-186f Interstellar Jump Unlocked", keplerDiscovered);
        addMilestoneRow("STAGE 3: Megastructure Core Anomaly Cataloged", signalSource);
    }

    private void addMilestoneRow(String title, boolean achieved) {
        Table row = new Table();
        row.align(Align.left);

        Label tag = new Label(achieved ? "  [ACHIEVED]  " : "  [PENDING]   ", skin, "mono");
        tag.setColor(achieved ? CinematicTheme.STATUS_NOMINAL : CinematicTheme.TEXT_MUTED);
        tag.setFontScale(0.74f);

        Label lbl = new Label(title, skin, "mono");
        lbl.setColor(achieved ? CinematicTheme.TEXT_PRIMARY : CinematicTheme.TEXT_MUTED);
        lbl.setFontScale(0.74f);

        row.add(tag).left();
        row.add(lbl).left().expandX();
        milestonesTable.add(row).fillX().padBottom(3f).row();
    }

    private void refreshClimaxTelemetries() {
        boolean signalDiscovered = controller.getGameState() != null && controller.getGameState().isSignalSourceDiscovered();
        boolean finalReady = controller.canInitiateFinalDirective();
        boolean completed = controller.isCampaignCompleted();
        String ending = controller.getCompletedEnding();

        if (signalDiscovered) {
            climaxSignalStatusLabel.setText("• Status: EXTRASOLAR ARTIFACT SECURED // SOURCE CATALOGED");
            climaxSignalStatusLabel.setColor(CinematicTheme.STATUS_NOMINAL);
        } else {
            climaxSignalStatusLabel.setText("• Status: SEARCH IN PROGRESS (AWAITING EXTRASOLAR SURVEY)");
            climaxSignalStatusLabel.setColor(CinematicTheme.AMBER_WARM);
        }

        if (finalReady) {
            climaxDirectiveStatusLabel.setText("• Clearance: FINAL MISSION DIRECTIVE AUTHORIZED");
            climaxDirectiveStatusLabel.setColor(CinematicTheme.SOLAR_GOLD);
        } else {
            climaxDirectiveStatusLabel.setText("• Clearance: LOCKED (COMPLETE EXTRASOLAR DISCOVERY)");
            climaxDirectiveStatusLabel.setColor(CinematicTheme.TEXT_MUTED);
        }

        if (completed) {
            climaxEndingLabel.setText("• Conclusion: CAMPAIGN CONCLUDED (" + ending + ")");
            climaxEndingLabel.setColor(CinematicTheme.STATUS_NOMINAL);
        } else {
            climaxEndingLabel.setText("• Conclusion: ACTIVE FLIGHT OPERATIONS IN PROGRESS");
            climaxEndingLabel.setColor(CinematicTheme.CYAN_AVIONICS);
        }

        if (climaxActionBtn != null) {
            if (finalReady) {
                climaxActionBtn.setText("[DIR-CLIMAX] INITIATE FINAL DIRECTIVE");
                climaxActionBtn.setVisible(true);
            } else if (completed) {
                climaxActionBtn.setText("[SUMMARY] VIEW CAMPAIGN CONCLAVE");
                climaxActionBtn.setVisible(true);
            } else {
                climaxActionBtn.setVisible(false);
            }
        }
    }

    public void showFinalDirectiveChoiceDialog() {
        if (audioManager != null) audioManager.playConfirm();

        HoloDialog dialog = new HoloDialog("FINAL MISSION DIRECTIVE // KEPLER-186F", skin, audioManager);
        Table content = dialog.getContentTable();
        content.align(Align.topLeft);

        Spaceship ship = controller.getSpaceship();
        Inventory inv = controller.getInventory();
        int fuel = (ship != null) ? ship.getFuel() : 0;
        int fuelCells = (inv != null) ? inv.getQuantity(Inventory.FUEL_CELLS) : 0;
        int artifactCount = (inv != null) ? inv.getQuantity(Inventory.ALIEN_ARTIFACTS) : 0;
        boolean operational = (ship != null && ship.isOperational());

        String lore = "Beneath the basalt plateaus of Kepler-186f, the alien megastructure stands active,\n"
                + "casting harmonic geometries across the cavern walls.\n\n"
                + "High Command directives mandate securing the Alien Artifact and returning to Sol.\n"
                + "Yet an open trans-dimensional gateway hums on the dais, offering a journey beyond.\n\n"
                + "RETURN TRAJECTORY VIABILITY ASSESSMENT:\n"
                + "• Vessel Operational : " + (operational ? "OPERATIONAL [NOMINAL]" : "CRITICAL STRUCTURAL CASUALTY") + "\n"
                + "• Propulsion Fuel    : " + fuel + "% (Return burn requires 20% Fuel)\n"
                + "• Cargo Fuel Cells   : " + fuelCells + " unit(s) available\n"
                + "• Alien Artifact     : " + (artifactCount >= 1 ? "SECURED IN STASIS BAY [1/1]" : "NOT DETECTED [0/1]");

        Label loreLbl = new Label(lore, skin, "mono");
        loreLbl.setColor(CinematicTheme.TEXT_PRIMARY);
        loreLbl.setFontScale(0.78f);
        content.add(loreLbl).left().padBottom(14f).row();

        // Choice 1: Initiate Return Voyage to Sol-Earth
        TextButton returnBtn = new TextButton("1. INITIATE RETURN VOYAGE TO SOL-EARTH", skin);
        returnBtn.getLabel().setFontScale(0.85f);
        returnBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
                executeReturnDirective();
            }
        });
        content.add(returnBtn).width(520f).height(38f).padBottom(6f).row();

        // Choice 2: Venture into the Unknown
        TextButton unknownBtn = new TextButton("2. VENTURE INTO THE UNKNOWN (TRANSCENDENCE)", skin);
        unknownBtn.getLabel().setFontScale(0.85f);
        unknownBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
                showUnknownConfirmationDialog();
            }
        });
        content.add(unknownBtn).width(520f).height(38f).padBottom(6f).row();

        // Cancel / Standby
        TextButton cancelBtn = new TextButton("3. SUSPEND DECISION / MAINTAIN HOLDING ORBIT", skin);
        cancelBtn.getLabel().setFontScale(0.85f);
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        content.add(cancelBtn).width(520f).height(38f).padBottom(4f).row();

        dialog.show(stage);
    }

    private void executeReturnDirective() {
        if (controller.canPerformEmergencyRefuel()) {
            int cellsToUse = controller.getEmergencyRefuelCellsNeeded();
            Spaceship ship = controller.getSpaceship();
            Inventory inv = controller.getInventory();
            int currentFuel = (ship != null) ? ship.getFuel() : 0;
            int availableCells = (inv != null) ? inv.getQuantity(Inventory.FUEL_CELLS) : 0;

            HoloDialog refuelDialog = new HoloDialog("EMERGENCY REFUELING PROTOCOL", skin, audioManager);
            Table content = refuelDialog.getContentTable();
            content.align(Align.topLeft);

            String prompt = "Current propulsion fuel (" + currentFuel + "%) is insufficient for return burn (20% required).\n"
                    + "Cargo bay holds " + availableCells + " Fuel Cell(s).\n\n"
                    + "Transfer " + cellsToUse + " Fuel Cell(s) to reaction drives (+ " + (cellsToUse * 20) + "% Fuel)?";
            Label promptLbl = new Label(prompt, skin, "mono");
            promptLbl.setColor(CinematicTheme.STATUS_CAUTION);
            promptLbl.setFontScale(0.80f);
            content.add(promptLbl).left().padBottom(14f).row();

            TextButton yesBtn = new TextButton("TRANSFER FUEL CELLS", skin);
            yesBtn.getLabel().setFontScale(0.85f);
            TextButton noBtn = new TextButton("PROCEED WITHOUT REFUELING", skin);
            noBtn.getLabel().setFontScale(0.85f);

            yesBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent ev, float x, float y) {
                    refuelDialog.hide();
                    controller.performEmergencyRefuel();
                    EndingResult result = controller.resolveReturnEnding();
                    if (result != null) {
                        transitionOverlay.fadeOut(0.4f, () -> game.showEndingScreen(result));
                    }
                }
            });

            noBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent ev, float x, float y) {
                    refuelDialog.hide();
                    EndingResult result = controller.resolveReturnEnding();
                    if (result != null) {
                        transitionOverlay.fadeOut(0.4f, () -> game.showEndingScreen(result));
                    }
                }
            });

            refuelDialog.getButtonTable().add(yesBtn).width(200f).height(36f).padRight(8f);
            refuelDialog.getButtonTable().add(noBtn).width(220f).height(36f);
            refuelDialog.show(stage);
        } else {
            EndingResult result = controller.resolveReturnEnding();
            if (result != null) {
                transitionOverlay.fadeOut(0.4f, () -> game.showEndingScreen(result));
            }
        }
    }

    private void showUnknownConfirmationDialog() {
        HoloDialog confirmDialog = new HoloDialog("TRANSCENDENCE PROTOCOL // STEP INTO THE UNKNOWN", skin, audioManager);
        Table content = confirmDialog.getContentTable();
        content.align(Align.topLeft);

        String prompt = "Commander, choosing to remain and cross the alien threshold on Kepler-186f is irreversible.\n\n"
                + "You will surrender vessel ODYSSEY-07 and human civilization to step across\n"
                + "the galactic threshold into uncharted existence.\n\n"
                + "Confirm decision to step beyond the unknown?";
        Label promptLbl = new Label(prompt, skin, "mono");
        promptLbl.setColor(new Color(0.78f, 0.55f, 1.0f, 1.0f));
        promptLbl.setFontScale(0.80f);
        content.add(promptLbl).left().padBottom(14f).row();

        TextButton yesBtn = new TextButton("CONFIRM TRANSCENDENCE", skin);
        yesBtn.getLabel().setFontScale(0.85f);
        TextButton noBtn = new TextButton("ABORT", skin);
        noBtn.getLabel().setFontScale(0.85f);

        yesBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent ev, float x, float y) {
                confirmDialog.hide();
                EndingResult result = controller.resolveUnknownEnding();
                if (result != null) {
                    transitionOverlay.fadeOut(0.4f, () -> game.showEndingScreen(result));
                }
            }
        });

        noBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent ev, float x, float y) {
                confirmDialog.hide();
            }
        });

        confirmDialog.getButtonTable().add(yesBtn).width(210f).height(36f).padRight(8f);
        confirmDialog.getButtonTable().add(noBtn).width(120f).height(36f);
        confirmDialog.show(stage);
    }

    public void handleReturnToBridge() {
        if (audioManager != null) audioManager.playCancel();
        transitionOverlay.fadeOut(0.4f, () -> {
            game.showCommandCenter();
        });
    }

    @Override
    public void show() {
        System.out.println("[ORBIT-SCREEN] MissionControlScreen active. Fleet command directives online.");
        Gdx.input.setInputProcessor(stage);
        if (stage != null) {
            stage.unfocusAll();
        }
        refreshFromController();
        if (transitionOverlay != null) {
            transitionOverlay.fadeIn(0.4f, null);
        }
    }

    @Override
    public void render(float delta) {
        // Deep void background
        Gdx.gl.glClearColor(0.024f, 0.035f, 0.055f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        frameCount++;
        logTimer += delta;
        if (logTimer >= 2.0f) {
            System.out.println(String.format("[ORBIT-RENDER] MissionControl: %d frames | Target 60 FPS (Current: %d FPS)", frameCount, Gdx.graphics.getFramesPerSecond()));
            logTimer = 0f;
        }
    }

    @Override
    public void resize(int width, int height) {
        System.out.println(String.format("[ORBIT-SCREEN] Mission Control window resized to %dx%d.", width, height));
        renderManager.update(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        if (stage != null) stage.unfocusAll();
    }

    public Stage getStage() {
        return stage;
    }

    public TextButton getReturnButton() {
        return returnBtn;
    }

    public Label getStageLabel() {
        return stageHeaderLabel;
    }

    public Label getProgressLabel() {
        return progressLabel;
    }

    public Label getDirectiveAdviceLabel() {
        return directiveAdviceLabel;
    }

    public TextButton getClimaxActionButton() {
        return climaxActionBtn;
    }

    public String getActiveFilter() {
        return activeFilter;
    }

    public void setActiveFilter(String filter) {
        this.activeFilter = (filter != null) ? filter : "ALL";
        refreshObjectivesTable();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }
}

