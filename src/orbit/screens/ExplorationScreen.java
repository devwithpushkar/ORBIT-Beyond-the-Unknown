package orbit.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import orbit.controllers.EndingResult;
import orbit.controllers.ExpeditionResult;
import orbit.controllers.OrbitController;
import orbit.models.ExplorationEvent;
import orbit.models.Inventory;
import orbit.models.Planet;
import orbit.models.Spaceship;
import orbit.render.AssetManager;
import orbit.render.ExplorationSceneManager;
import orbit.render.OrbitGame;
import orbit.render.RenderManager;
import orbit.render.audio.AudioManager;
import orbit.render.components.HoloDialog;
import orbit.render.components.HoloPanel;
import orbit.render.components.ScreenTransitionOverlay;
import orbit.render.theme.CinematicTheme;

import java.util.List;

/**
 * ExplorationScreen provides the dedicated 3D planetary reconnaissance and surface excursion experience.
 *
 * Implements:
 * - 3D close-orbital celestial view of the currently orbited planet with holographic scan radar.
 * - Planetary terrain, atmospheric reading, and landing zone telemetry.
 * - Explicit launch cost validation and player acknowledgement (10 Energy, 5 Oxygen, 0 Fuel).
 * - Full resolution of all 13 exploration events: Hazard choices & countermeasures,
 *   Beacon triangulation protocols, Xenological monolith harmonization, and Kepler climax directives.
 * - Instant state synchronization across controller, mission log, and ship vitals.
 * - Bidirectional return-to-bridge navigation.
 */
public class ExplorationScreen implements Screen {

    private final OrbitGame game;
    private final OrbitController controller;
    private final AssetManager assetManager;
    private final RenderManager renderManager;
    private final AudioManager audioManager;

    private ExplorationSceneManager sceneManager;
    private Stage stage;
    private Skin skin;
    private ScreenTransitionOverlay transitionOverlay;

    // Telemetry Labels
    private Label planetTitleLabel;
    private Label planetDescLabel;
    private Label planetEnvLabel;
    private Label explorationStatusLabel;
    private Label costNoticeLabel;
    private Label vitalsTelemetryLabel;
    private Label statusFeedbackLabel;

    // Action Controls
    private TextButton launchBtn;
    private TextButton returnBtn;

    private int frameCount = 0;
    private float logTimer = 0f;

    public ExplorationScreen(OrbitGame game, OrbitController controller, AssetManager assetManager, RenderManager renderManager, AudioManager audioManager) {
        this.game = game;
        this.controller = controller;
        this.assetManager = assetManager;
        this.renderManager = renderManager;
        this.audioManager = (audioManager != null) ? audioManager : new AudioManager();

        this.sceneManager = new ExplorationSceneManager(assetManager);
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

        // --- 1. Top Avionics & Sensor Header ---
        Table topBar = new Table();
        topBar.align(Align.left);

        Label deckBadge = new Label("SPACECRAFT ODYSSEY-07 // SURFACE RECONNAISSANCE & EXPEDITIONS", skin, "mono");
        deckBadge.setColor(CinematicTheme.AMBER_WARM);
        deckBadge.setFontScale(0.92f);

        Label subStatus = new Label("RADAR SCANNER: ACTIVE  |  ORBIT: SYNCHRONOUS  |  EXCURSION ROVER: READY", skin, "mono");
        subStatus.setColor(CinematicTheme.STATUS_NOMINAL);
        subStatus.setFontScale(0.80f);

        topBar.add(deckBadge).left().expandX();
        topBar.add(subStatus).right();
        rootTable.add(topBar).fillX().top().padBottom(16f).row();

        // --- 2. Center Workspace: Left Telemetry Bay / Right Open 3D Planetary Viewing ---
        Table centerWorkspace = new Table();
        centerWorkspace.align(Align.topLeft);

        // Left Reconnaissance Console (Width: 440px)
        Table leftColumn = new Table();
        leftColumn.align(Align.topLeft);

        // A. Planetary Telemetry HoloPanel
        HoloPanel planetPanel = new HoloPanel("PLANETARY TARGET & ENVIRONMENT TELEMETRY", skin);
        planetPanel.setBracketColor(CinematicTheme.CYAN_AVIONICS);
        Table pContent = planetPanel.getContentTable();
        pContent.align(Align.topLeft);

        planetTitleLabel = new Label("TARGET: EARTH ORBIT", skin, "subtitle");
        planetTitleLabel.setColor(CinematicTheme.TEXT_PRIMARY);
        planetTitleLabel.setFontScale(0.95f);
        pContent.add(planetTitleLabel).left().padBottom(6f).row();

        planetDescLabel = new Label("• Terrain Overview: Terrestrial biosphere.", skin, "mono");
        planetDescLabel.setColor(CinematicTheme.TEXT_SECONDARY);
        planetDescLabel.setFontScale(0.78f);
        planetDescLabel.setWrap(true);
        pContent.add(planetDescLabel).width(410f).left().padBottom(4f).row();

        planetEnvLabel = new Label("• Atmospheric Data: 1.0 ATM O2/N2 atmosphere.", skin, "mono");
        planetEnvLabel.setColor(CinematicTheme.TEXT_MUTED);
        planetEnvLabel.setFontScale(0.78f);
        planetEnvLabel.setWrap(true);
        pContent.add(planetEnvLabel).width(410f).left().padBottom(8f).row();

        explorationStatusLabel = new Label("• Survey Status: UNEXPLORED SITE", skin, "mono");
        explorationStatusLabel.setColor(CinematicTheme.CYAN_AVIONICS);
        explorationStatusLabel.setFontScale(0.80f);
        pContent.add(explorationStatusLabel).left();

        leftColumn.add(planetPanel).width(440f).padBottom(12f).row();

        // B. Excursion Rover & Resource Telemetry HoloPanel
        HoloPanel roverPanel = new HoloPanel("EXCURSION ROVER TELEMETRY & LAUNCH BUDGET", skin);
        roverPanel.setBracketColor(CinematicTheme.AMBER_WARM);
        Table rContent = roverPanel.getContentTable();
        rContent.align(Align.topLeft);

        costNoticeLabel = new Label("• Survey Launch Cost: 10% Energy & 5% Oxygen (0% Fuel)", skin, "mono");
        costNoticeLabel.setColor(CinematicTheme.AMBER_WARM);
        costNoticeLabel.setFontScale(0.82f);
        rContent.add(costNoticeLabel).left().padBottom(6f).row();

        vitalsTelemetryLabel = new Label("• Available Reserves: 100% Energy / 100% O2 / 100% Fuel", skin, "mono");
        vitalsTelemetryLabel.setColor(CinematicTheme.STATUS_NOMINAL);
        vitalsTelemetryLabel.setFontScale(0.80f);
        rContent.add(vitalsTelemetryLabel).left().padBottom(14f).row();

        launchBtn = new TextButton("DEPLOY EXCURSION ROVER (-10 ENG, -5 O2)", skin);
        launchBtn.getLabel().setFontScale(0.85f);
        launchBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleLaunchExpedition();
            }
        });
        rContent.add(launchBtn).fillX().height(42f);

        leftColumn.add(roverPanel).width(440f).row();

        centerWorkspace.add(leftColumn).width(440f).top().left();
        // Right side remains clear for unobstructed viewing of the 3D rotating planet and scan grid
        centerWorkspace.add().expand().fill();

        rootTable.add(centerWorkspace).expand().fill().padBottom(10f).row();

        // --- 3. Bottom Operations Rail ---
        Table bottomRail = new Table();
        statusFeedbackLabel = new Label("SURFACE SENSORS ONLINE // RECONNAISSANCE SCAN READY", skin, "mono");
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

    public void handleLaunchExpedition() {
        if (!controller.isGameActive()) return;

        // Check if Kepler Climax protocol is active
        if (controller.canInitiateFinalDirective()) {
            showClimaxDirectiveDialog();
            return;
        }

        Planet p = controller.getCurrentLocation();
        String planetName = (p != null) ? p.getName().toUpperCase() : "EARTH";

        if (!controller.isExplorationSupported()) {
            if (audioManager != null) audioManager.playAlert();
            showStatusFeedback("[-] DESCENT RESTRICTED: Atmospheric turbulence prohibits surface excursions on " + planetName, CinematicTheme.STATUS_CAUTION);
            return;
        }

        Spaceship ship = controller.getSpaceship();
        int energy = (ship != null) ? ship.getEnergy() : 100;
        int oxygen = (ship != null) ? ship.getOxygen() : 100;

        if (energy < 10 || oxygen < 5) {
            if (audioManager != null) audioManager.playAlert();
            showStatusFeedback("[-] INSUFFICIENT VITALS: Minimum 10% Energy and 5% Oxygen required for excursion rover launch.", CinematicTheme.STATUS_CRITICAL);
            return;
        }

        // Require explicit player acknowledgement before deducting resources
        showExpeditionConfirmationDialog(planetName);
    }

    private void showExpeditionConfirmationDialog(String planetName) {
        if (audioManager != null) audioManager.playConfirm();

        HoloDialog dialog = new HoloDialog("SURFACE EXCURSION // ROVER LAUNCH CONFIRMATION", skin, audioManager);
        Table content = dialog.getContentTable();
        content.align(Align.topLeft);

        String prompt = "Target Destination: " + planetName + " SURFACE\n\n"
                + "Deploy the Odyssey-EX1 survey rover for planetary reconnaissance?\n\n"
                + "Expedition Resource Requirements:\n"
                + "• Energy Consumed: -10% Reactor Energy\n"
                + "• Oxygen Consumed: -5% Life Support O2\n"
                + "• Fuel Consumed  : 0% Reaction Mass (Preserved)";

        Label body = new Label(prompt, skin, "mono");
        body.setColor(CinematicTheme.TEXT_PRIMARY);
        body.setFontScale(0.82f);
        content.add(body).left().padBottom(16f).row();

        TextButton confirmBtn = new TextButton("CONFIRM & DEPLOY (-10 ENG, -5 O2)", skin);
        confirmBtn.getLabel().setFontScale(0.85f);
        confirmBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
                executeExpeditionLaunch();
            }
        });

        TextButton abortBtn = new TextButton("ABORT", skin);
        abortBtn.getLabel().setFontScale(0.85f);
        abortBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });

        dialog.getButtonTable().add(confirmBtn).width(300f).height(38f).padRight(12f);
        dialog.getButtonTable().add(abortBtn).width(120f).height(38f);
        dialog.show(stage);
    }

    public void executeExpeditionLaunch() {
        // Execute the authoritative expedition operation via OrbitController
        ExpeditionResult res = controller.startExpedition();
        refreshFromController();
        handleExpeditionResult(res);
    }

    private void handleExpeditionResult(ExpeditionResult res) {
        if (res == null) return;

        if (res.getType() == ExpeditionResult.Type.LAUNCH_INHIBITED || res.getType() == ExpeditionResult.Type.UNSUPPORTED_PLANET || !res.isSuccess()) {
            if (audioManager != null) audioManager.playAlert();
            showStatusFeedback("[-] EXCURSION INHIBITED: " + res.getMessage(), CinematicTheme.STATUS_CRITICAL);
            return;
        }

        if (audioManager != null) audioManager.playConfirm();

        switch (res.getType()) {
            case HAZARD_CHOICE -> {
                ExplorationEvent event = res.getEvent();
                HoloDialog dialog = new HoloDialog("HAZARD ENCOUNTER // " + (event != null ? event.getTitle() : "SURFACE ANOMALY"), skin, audioManager);
                Table content = dialog.getContentTable();
                content.align(Align.topLeft);

                Label desc = new Label(res.getMessage(), skin, "mono");
                desc.setColor(CinematicTheme.STATUS_CRITICAL);
                desc.setFontScale(0.82f);
                content.add(desc).left().padBottom(14f).row();

                String counterItem = (event != null) ? event.getCountermeasureItem() : null;
                int counterQty = (counterItem != null && controller.getInventory() != null) ? controller.getInventory().getQuantity(counterItem) : 0;

                if (counterItem != null && counterQty > 0) {
                    TextButton counterBtn = new TextButton("DEPLOY COUNTERMEASURE (-1 " + counterItem.toUpperCase() + ")", skin);
                    counterBtn.getLabel().setFontScale(0.82f);
                    counterBtn.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent ev, float x, float y) {
                            dialog.hide();
                            controller.resolveHazard(event, true);
                            refreshFromController();
                            showStatusFeedback("[+] HAZARD NEUTRALIZED VIA COUNTERMEASURE", CinematicTheme.STATUS_NOMINAL);
                        }
                    });
                    dialog.getButtonTable().add(counterBtn).width(310f).height(38f).padRight(10f);
                }

                TextButton braceBtn = new TextButton("BRACE FOR IMPACT (SUSTAIN PENALTY)", skin);
                braceBtn.getLabel().setFontScale(0.82f);
                braceBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent ev, float x, float y) {
                        dialog.hide();
                        controller.resolveHazard(event, false);
                        refreshFromController();
                        showStatusFeedback("[-] IMPACT SUSTAINED: DAMAGE RECORDED", CinematicTheme.STATUS_CRITICAL);
                    }
                });
                dialog.getButtonTable().add(braceBtn).width(280f).height(38f);
                dialog.show(stage);
            }
            case BEACON_CHOICE -> {
                ExplorationEvent event = res.getEvent();
                HoloDialog dialog = new HoloDialog("SIGNAL ENCOUNTER // UNIDENTIFIED BEACON", skin, audioManager);
                Table content = dialog.getContentTable();
                content.align(Align.topLeft);

                Label desc = new Label(res.getMessage(), skin, "mono");
                desc.setColor(CinematicTheme.AMBER_WARM);
                desc.setFontScale(0.82f);
                content.add(desc).left().padBottom(14f).row();

                List<String> choices = res.getAvailableChoices();
                if (choices != null) {
                    for (int i = 0; i < choices.size(); i++) {
                        final int choiceIdx = i;
                        TextButton choiceBtn = new TextButton(choices.get(i), skin);
                        choiceBtn.getLabel().setFontScale(0.82f);
                        choiceBtn.addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent ev, float x, float y) {
                                dialog.hide();
                                String outcome = controller.resolveBeaconEncounter(event, choiceIdx);
                                refreshFromController();
                                showStatusFeedback("[+] " + outcome, CinematicTheme.STATUS_NOMINAL);
                            }
                        });
                        content.add(choiceBtn).width(400f).height(36f).padBottom(4f).row();
                    }
                }
                dialog.show(stage);
            }
            case MONOLITH_CHOICE -> {
                ExplorationEvent event = res.getEvent();
                HoloDialog dialog = new HoloDialog("XENOLOGICAL ENCOUNTER // ANCIENT MONOLITH", skin, audioManager);
                Table content = dialog.getContentTable();
                content.align(Align.topLeft);

                Label desc = new Label(res.getMessage(), skin, "mono");
                desc.setColor(CinematicTheme.SOLAR_GOLD);
                desc.setFontScale(0.82f);
                content.add(desc).left().padBottom(14f).row();

                List<String> choices = res.getAvailableChoices();
                if (choices != null) {
                    for (int i = 0; i < choices.size(); i++) {
                        final int choiceIdx = i;
                        TextButton choiceBtn = new TextButton(choices.get(i), skin);
                        choiceBtn.getLabel().setFontScale(0.82f);
                        choiceBtn.addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent ev, float x, float y) {
                                dialog.hide();
                                String outcome = controller.resolveMonolithEncounter(event, choiceIdx);
                                refreshFromController();
                                showStatusFeedback("[+] " + outcome, CinematicTheme.STATUS_NOMINAL);
                            }
                        });
                        content.add(choiceBtn).width(400f).height(36f).padBottom(4f).row();
                    }
                }
                dialog.show(stage);
            }
            default -> {
                // Auto resolved or discovery
                HoloDialog dialog = new HoloDialog("SURFACE EXCURSION COMPLETED", skin, audioManager);
                Table content = dialog.getContentTable();
                content.align(Align.topLeft);

                Label msg = new Label(res.getMessage(), skin, "mono");
                msg.setColor(CinematicTheme.STATUS_NOMINAL);
                msg.setFontScale(0.82f);
                content.add(msg).left().padBottom(8f).row();

                List<String> effects = res.getEffectLog();
                if (effects != null) {
                    for (String eff : effects) {
                        Label effLbl = new Label("  " + eff, skin, "mono");
                        effLbl.setColor(CinematicTheme.TEXT_SECONDARY);
                        effLbl.setFontScale(0.78f);
                        content.add(effLbl).left().padBottom(2f).row();
                    }
                }

                TextButton closeBtn = new TextButton("ACKNOWLEDGE", skin);
                closeBtn.getLabel().setFontScale(0.85f);
                closeBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent ev, float x, float y) {
                        dialog.hide();
                    }
                });
                dialog.getButtonTable().add(closeBtn).width(160f).height(36f);
                dialog.show(stage);
            }
        }
    }

    private void showClimaxDirectiveDialog() {
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

    public void refreshFromController() {
        if (!controller.isGameActive()) return;

        Planet p = controller.getCurrentLocation();
        Spaceship ship = controller.getSpaceship();

        // 1. Update 3D Scene Target Planet
        if (p != null) {
            sceneManager.updatePlanet(p);
            planetTitleLabel.setText("TARGET: " + p.getName().toUpperCase() + " ORBIT");
            planetDescLabel.setText("• Terrain Overview: " + p.getDescription());
            planetEnvLabel.setText("• Atmospheric Data: " + p.getEnvironmentDescription());
            explorationStatusLabel.setText("• Survey Status: " + (p.isExplored() ? "SURVEY COMPLETED" : "UNEXPLORED SITE"));
            explorationStatusLabel.setColor(p.isExplored() ? CinematicTheme.STATUS_NOMINAL : CinematicTheme.CYAN_AVIONICS);
        }

        // 2. Update Vitals and Launch Button State
        if (ship != null) {
            int eng = ship.getEnergy();
            int o2 = ship.getOxygen();
            int fuel = ship.getFuel();

            vitalsTelemetryLabel.setText(String.format("• Available Reserves: %d%% ENG / %d%% O2 / %d%% FUEL", eng, o2, fuel));
            vitalsTelemetryLabel.setColor((eng >= 10 && o2 >= 5) ? CinematicTheme.STATUS_NOMINAL : CinematicTheme.STATUS_CRITICAL);

            boolean supported = controller.isExplorationSupported();
            boolean hasVitals = (eng >= 10 && o2 >= 5);

            if (!supported) {
                launchBtn.setText("SURFACE DESCENT RESTRICTED");
                launchBtn.setDisabled(true);
            } else if (!hasVitals) {
                launchBtn.setText("VITALS DEFICIT (<10 ENG / <5 O2)");
                launchBtn.setDisabled(true);
            } else {
                launchBtn.setText("DEPLOY EXCURSION ROVER (-10 ENG, -5 O2)");
                launchBtn.setDisabled(false);
            }
        }
    }

    public void showStatusFeedback(String message, Color color) {
        statusFeedbackLabel.setText(message);
        statusFeedbackLabel.setColor(color);
        statusFeedbackLabel.clearActions();
        statusFeedbackLabel.addAction(Actions.sequence(
                Actions.color(color, 0.1f),
                Actions.delay(3.5f),
                Actions.color(CinematicTheme.TEXT_MUTED, 0.8f)
        ));
    }

    @Override
    public void show() {
        System.out.println("[ORBIT-SCREEN] ExplorationScreen active. Reconnaissance sensors online.");
        Gdx.input.setInputProcessor(stage);
        if (stage != null) {
            stage.unfocusAll();
            for (Actor actor : new com.badlogic.gdx.utils.Array.ArrayIterator<>(stage.getActors())) {
                if (actor instanceof com.badlogic.gdx.scenes.scene2d.ui.Dialog) {
                    actor.remove();
                }
            }
        }
        refreshFromController();
        if (transitionOverlay != null) {
            transitionOverlay.fadeIn(0.4f, null);
        }
    }

    @Override
    public void render(float delta) {
        // 1. Render 3D Planetary Reconnaissance Scene
        sceneManager.update(delta);
        sceneManager.render();

        // 2. Render 2D Diegetic HUD Overlay
        stage.act(delta);
        stage.draw();

        frameCount++;
        logTimer += delta;
        if (logTimer >= 2.0f) {
            System.out.println(String.format("[ORBIT-RENDER] Exploration: %d frames | Target 60 FPS (Current: %d FPS)", frameCount, Gdx.graphics.getFramesPerSecond()));
            logTimer = 0f;
        }
    }

    @Override
    public void resize(int width, int height) {
        System.out.println(String.format("[ORBIT-SCREEN] Exploration window resized to %dx%d.", width, height));
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
        if (stage != null) stage.unfocusAll();
    }

    public Stage getStage() {
        return stage;
    }

    public TextButton getLaunchButton() {
        return launchBtn;
    }

    public TextButton getReturnButton() {
        return returnBtn;
    }

    public ExplorationSceneManager getSceneManager() {
        return sceneManager;
    }

    @Override
    public void dispose() {
        if (sceneManager != null) sceneManager.dispose();
        if (stage != null) stage.dispose();
    }
}

