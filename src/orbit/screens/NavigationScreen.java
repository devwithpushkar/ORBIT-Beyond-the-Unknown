package orbit.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import orbit.controllers.NavigationResult;
import orbit.controllers.OrbitController;
import orbit.models.Planet;
import orbit.models.Spaceship;
import orbit.render.AssetManager;
import orbit.render.NavigationSceneManager;
import orbit.render.OrbitGame;
import orbit.render.RenderManager;
import orbit.render.audio.AudioManager;
import orbit.render.components.HoloButton;
import orbit.render.components.HoloDialog;
import orbit.render.components.ScreenTransitionOverlay;
import orbit.render.theme.CinematicTheme;

import java.util.List;

/**
 * NavigationScreen provides the dedicated interactive 3D Holographic Galaxy experience.
 *
 * Implements:
 * - 3D holographic system projection emerging from physical navigation table
 * - 5 distinct planetary destinations with procedural textures
 * - Mouse drag orbit camera and scroll zoom
 * - Direct 3D ray-picking of planets
 * - Real-time astrogation telemetry via OrbitController
 * - Fuel validation, corridor clearance, and transit burn execution
 * - Cinematic warp transit sequence returning to Command Center
 */
public class NavigationScreen implements Screen {

    private final OrbitGame game;
    private final OrbitController controller;
    private final AssetManager assetManager;
    private final RenderManager renderManager;
    private final AudioManager audioManager;

    private NavigationSceneManager sceneManager;
    private Stage stage;
    private Skin skin;
    private ScreenTransitionOverlay transitionOverlay;

    // Selected Target State
    private Planet selectedPlanet;

    // UI Telemetry Labels
    private Label vesselNameLabel;
    private Label currentLocationLabel;
    private Label fuelValueLabel;
    private ProgressBar fuelBar;
    private Label targetNameLabel;
    private Label targetClassLabel;
    private Label targetDescLabel;
    private Label targetEnvLabel;
    private Label corridorStatusLabel;
    private Label fuelRequirementLabel;
    private Label transitFeasibilityLabel;
    private TextButton engageTransitBtn;

    // Left Rail Planetary Buttons
    private Table planetListTable;

    // In-Flight Warp Overlay
    private Table warpOverlayTable;
    private Label warpStatusLabel;
    private Label warpSubLabel;

    // Mouse Drag Tracking for 3D Camera Orbit
    private int lastScreenX = 0;
    private int lastScreenY = 0;
    private boolean isDragging3D = false;

    // Performance & Telemetry Diagnostics
    private int frameCount = 0;
    private float logTimer = 0f;

    public NavigationScreen(OrbitGame game, OrbitController controller, AssetManager assetManager,
                            RenderManager renderManager, AudioManager audioManager) {
        this.game = game;
        this.controller = controller;
        this.assetManager = assetManager;
        this.renderManager = renderManager;
        this.audioManager = audioManager;
        this.skin = assetManager.getUiSkin();

        this.sceneManager = new NavigationSceneManager(assetManager);
        this.stage = new Stage(renderManager.getUiViewport());

        buildUI();
        setupInput();
        refreshFromController();
    }

    private void buildUI() {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);
        root.pad(18f);

        // --- 1. Top Avionics Telemetry Bar ---
        Table topBar = new Table();
        topBar.setBackground(skin.newDrawable("white", new Color(0.04f, 0.08f, 0.16f, 0.88f)));
        topBar.pad(10f, 18f, 10f, 18f);

        Table vesselInfo = new Table();
        vesselInfo.align(Align.left);
        vesselNameLabel = new Label("VESSEL: ORBIT-I // DEEP SPACE RECON", skin, "subtitle");
        vesselNameLabel.setColor(CinematicTheme.CYAN_AVIONICS);
        vesselInfo.add(vesselNameLabel).left().row();

        currentLocationLabel = new Label("CURRENT PARKING ORBIT: EARTH // DOCKED", skin, "mono");
        currentLocationLabel.setColor(CinematicTheme.TEXT_MUTED);
        vesselInfo.add(currentLocationLabel).left();

        topBar.add(vesselInfo).left().expandX();

        // Warp Core & Fuel Reserves
        Table fuelTelemetry = new Table();
        fuelTelemetry.align(Align.right);

        Table fuelHeader = new Table();
        Label fuelTag = new Label("PROPULSION FUEL RESERVES: ", skin, "mono");
        fuelTag.setColor(CinematicTheme.TEXT_MUTED);
        fuelHeader.add(fuelTag).left();

        fuelValueLabel = new Label("100%", skin, "mono");
        fuelValueLabel.setColor(CinematicTheme.STATUS_NOMINAL);
        fuelHeader.add(fuelValueLabel).left();
        fuelTelemetry.add(fuelHeader).right().row();

        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = skin.newDrawable("white", new Color(0.12f, 0.16f, 0.22f, 0.9f));
        barStyle.knobBefore = skin.newDrawable("white", CinematicTheme.CYAN_AVIONICS);
        fuelBar = new ProgressBar(0f, 100f, 1f, false, barStyle);
        fuelBar.setValue(100f);
        fuelBar.setAnimateDuration(0.35f);
        fuelTelemetry.add(fuelBar).width(240f).height(8f).padTop(4f);

        topBar.add(fuelTelemetry).right();
        root.add(topBar).fillX().expandX().top().padBottom(12f).row();

        // --- 2. Main Work Area (Left Rail: Planetary Selector | Center: 3D Hologram | Right: Telemetry Panel) ---
        Table workspace = new Table();

        // Left Rail: Planetary Destination Selector
        Table leftRail = new Table();
        leftRail.setBackground(skin.newDrawable("white", new Color(0.02f, 0.05f, 0.10f, 0.60f)));
        leftRail.pad(10f);
        leftRail.align(Align.topLeft);

        Label destinationsTitle = new Label("CELESTIAL DESTINATIONS", skin, "mono");
        destinationsTitle.setColor(CinematicTheme.CYAN_AVIONICS);
        destinationsTitle.setFontScale(0.85f);
        leftRail.add(destinationsTitle).left().padBottom(8f).row();

        planetListTable = new Table();
        planetListTable.align(Align.topLeft);
        leftRail.add(planetListTable).expand().fill().row();

        // Return to Bridge Button
        TextButton returnBtn = new TextButton("<< RETURN TO BRIDGE [ESC]", skin);
        returnBtn.getLabel().setFontScale(0.80f);
        returnBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleReturnToBridge();
            }
        });
        leftRail.add(returnBtn).width(210f).height(38f).padTop(8f);

        workspace.add(leftRail).width(230f).fillY().expandY().left();

        // Center Area: Transparent spacer for 3D Holographic System Viewport
        Table centerSpacer = new Table();
        workspace.add(centerSpacer).expand().fill();

        // Right Rail: Astrogation Telemetry & Target Inspection
        Table rightRail = new Table();
        rightRail.setBackground(skin.newDrawable("white", new Color(0.02f, 0.04f, 0.08f, 0.58f)));
        rightRail.pad(12f, 14f, 12f, 14f);
        rightRail.align(Align.topLeft);

        Label targetTitle = new Label("TARGET ASTROGATION TELEMETRY", skin, "mono");
        targetTitle.setColor(CinematicTheme.AMBER_WARM);
        targetTitle.setFontScale(0.82f);
        rightRail.add(targetTitle).left().padBottom(8f).row();

        targetNameLabel = new Label("MARS", skin, "title");
        targetNameLabel.setColor(CinematicTheme.TEXT_PRIMARY);
        targetNameLabel.setFontScale(0.95f);
        rightRail.add(targetNameLabel).left().padBottom(2f).row();

        targetClassLabel = new Label("CLASSIFICATION: ARID DESERT WORLD", skin, "mono");
        targetClassLabel.setColor(CinematicTheme.CYAN_AVIONICS);
        targetClassLabel.setFontScale(0.80f);
        rightRail.add(targetClassLabel).left().padBottom(10f).row();

        targetDescLabel = new Label("", skin, "mono");
        targetDescLabel.setColor(CinematicTheme.TEXT_SECONDARY);
        targetDescLabel.setWrap(true);
        targetDescLabel.setFontScale(0.80f);
        rightRail.add(targetDescLabel).width(260f).left().padBottom(8f).row();

        Label envHeader = new Label("ATMOSPHERE & SURFACE TELEMETRY:", skin, "mono");
        envHeader.setColor(CinematicTheme.TEXT_MUTED);
        envHeader.setFontScale(0.76f);
        rightRail.add(envHeader).left().padBottom(2f).row();

        targetEnvLabel = new Label("", skin, "mono");
        targetEnvLabel.setColor(CinematicTheme.TEXT_PRIMARY);
        targetEnvLabel.setWrap(true);
        targetEnvLabel.setFontScale(0.78f);
        rightRail.add(targetEnvLabel).width(260f).left().padBottom(12f).row();

        // Route & Fuel Cost Evaluation
        Table routeBox = new Table();
        routeBox.setBackground(skin.newDrawable("white", new Color(0.04f, 0.08f, 0.15f, 0.65f)));
        routeBox.pad(8f);
        routeBox.align(Align.left);

        corridorStatusLabel = new Label("CORRIDOR: AUTHORIZED", skin, "mono");
        corridorStatusLabel.setColor(CinematicTheme.STATUS_NOMINAL);
        corridorStatusLabel.setFontScale(0.80f);
        routeBox.add(corridorStatusLabel).left().row();

        fuelRequirementLabel = new Label("TRANSIT BURN REQUIREMENT: -20% FUEL", skin, "mono");
        fuelRequirementLabel.setColor(CinematicTheme.AMBER_WARM);
        fuelRequirementLabel.setFontScale(0.80f);
        routeBox.add(fuelRequirementLabel).left().padTop(2f).row();

        transitFeasibilityLabel = new Label("STATUS: READY FOR TRANSIT BURN", skin, "mono");
        transitFeasibilityLabel.setColor(CinematicTheme.STATUS_NOMINAL);
        transitFeasibilityLabel.setFontScale(0.80f);
        routeBox.add(transitFeasibilityLabel).left().padTop(2f);

        rightRail.add(routeBox).width(260f).padBottom(12f).row();

        // Engage Transit Burn Button
        engageTransitBtn = new TextButton("ENGAGE TRANSIT BURN", skin);
        engageTransitBtn.getLabel().setFontScale(0.85f);
        engageTransitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedPlanet != null && controller.canTravelTo(selectedPlanet)) {
                    promptTransitConfirmation();
                } else if (audioManager != null) {
                    audioManager.playAlert();
                }
            }
        });
        rightRail.add(engageTransitBtn).width(260f).height(42f).padBottom(8f).row();

        Label hintLabel = new Label("ORBIT CONTROLS: [DRAG] ROTATE // [SCROLL] ZOOM\n[CLICK] 3D CELESTIAL NODE TO SELECT", skin, "mono");
        hintLabel.setColor(CinematicTheme.TEXT_MUTED);
        hintLabel.setFontScale(0.70f);
        hintLabel.setAlignment(Align.center);
        rightRail.add(hintLabel).width(260f).center();

        workspace.add(rightRail).width(285f).fillY().expandY().right();
        root.add(workspace).expand().fill().row();

        stage.addActor(root);

        // --- 3. In-Flight Cinematic Warp Telemetry Overlay ---
        warpOverlayTable = new Table();
        warpOverlayTable.setFillParent(true);
        warpOverlayTable.setBackground(skin.newDrawable("white", new Color(0.01f, 0.02f, 0.05f, 0.70f)));
        warpOverlayTable.setVisible(false);

        warpStatusLabel = new Label("RELATIVISTIC PROPULSION BURN ENGAGED", skin, "title");
        warpStatusLabel.setColor(CinematicTheme.CYAN_AVIONICS);
        warpStatusLabel.setAlignment(Align.center);
        warpOverlayTable.add(warpStatusLabel).center().padBottom(12f).row();

        warpSubLabel = new Label("COMPUTING GRAVITATIONAL VECTOR TO TARGET...", skin, "subtitle");
        warpSubLabel.setColor(CinematicTheme.AMBER_WARM);
        warpSubLabel.setAlignment(Align.center);
        warpOverlayTable.add(warpSubLabel).center();

        stage.addActor(warpOverlayTable);

        // --- 4. Screen Transition Fades ---
        transitionOverlay = new ScreenTransitionOverlay(skin.getDrawable("white"));
        stage.addActor(transitionOverlay);
    }

    private void setupInput() {
        InputAdapter sceneInputProcessor = new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (sceneManager.isWarping()) return false;
                lastScreenX = screenX;
                lastScreenY = screenY;
                isDragging3D = true;
                return false; // Allow click detection on touchUp
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (!isDragging3D || sceneManager.isWarping()) return false;
                float deltaX = (screenX - lastScreenX) * 0.35f;
                float deltaY = (screenY - lastScreenY) * 0.28f;
                lastScreenX = screenX;
                lastScreenY = screenY;

                sceneManager.rotateCamera(deltaX, deltaY);
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                isDragging3D = false;
                if (sceneManager.isWarping()) return false;

                // Test 3D Ray-Picking on mouse release
                Planet hitPlanet = sceneManager.getPlanetAtScreen(screenX, screenY, controller.getPlanets());
                if (hitPlanet != null) {
                    selectPlanet(hitPlanet);
                    return true;
                }
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (sceneManager.isWarping()) return false;
                sceneManager.zoomCamera(amountY * 1.4f);
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    handleReturnToBridge();
                    return true;
                }
                return false;
            }
        };

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(sceneInputProcessor);
        Gdx.input.setInputProcessor(multiplexer);
    }

    /**
     * Refreshes all navigation telemetry from OrbitController.
     */
    public void refreshFromController() {
        if (!controller.isGameActive()) return;

        Spaceship ship = controller.getGameState().getSpaceship();
        Planet current = controller.getGameState().getCurrentLocation();

        vesselNameLabel.setText("VESSEL: " + controller.getGameState().getPlayer().getSpaceshipName().toUpperCase() + " // " + controller.getCurrentStage().toUpperCase());
        currentLocationLabel.setText("CURRENT PARKING ORBIT: " + (current != null ? current.getName().toUpperCase() : "SOL SYSTEM"));

        int fuel = ship != null ? ship.getFuel() : 100;
        fuelValueLabel.setText(fuel + "%");
        fuelBar.setValue(fuel);
        if (fuel <= 20) {
            fuelValueLabel.setColor(CinematicTheme.STATUS_CRITICAL);
        } else if (fuel <= 40) {
            fuelValueLabel.setColor(CinematicTheme.STATUS_CAUTION);
        } else {
            fuelValueLabel.setColor(CinematicTheme.STATUS_NOMINAL);
        }

        sceneManager.setCurrentLocation(current != null ? current.getName() : "Earth");

        // Ensure selectedPlanet references a valid planet instance from active GameState
        if (selectedPlanet != null && controller.getGameState() != null) {
            selectedPlanet = controller.getGameState().getPlanetByName(selectedPlanet.getName());
        }

        // Default selection to next eligible or first available
        if (selectedPlanet == null || (current != null && selectedPlanet.getName().equalsIgnoreCase(current.getName()))) {
            for (Planet p : controller.getPlanets()) {
                if (current != null && !p.getName().equalsIgnoreCase(current.getName()) && p.isDiscovered()) {
                    selectedPlanet = p;
                    break;
                }
            }
            if (selectedPlanet == null && !controller.getPlanets().isEmpty()) {
                selectedPlanet = controller.getPlanets().get(0);
            }
        }

        if (selectedPlanet != null) {
            sceneManager.setSelectedPlanet(selectedPlanet.getName());
        }

        // Rebuild planetary selector buttons with accurate isSelected highlight
        rebuildPlanetList();

        updateTargetTelemetry();
    }

    private void rebuildPlanetList() {
        planetListTable.clear();

        List<Planet> planets = controller.getPlanets();
        Planet current = controller.getGameState().getCurrentLocation();

        for (Planet p : planets) {
            boolean isCurrent = current != null && p.getName().equalsIgnoreCase(current.getName());
            boolean isDiscovered = p.isDiscovered();
            boolean isSelected = selectedPlanet != null && p.getName().equalsIgnoreCase(selectedPlanet.getName());

            Table item = new Table();
            Color bgCol = isSelected ? new Color(0.08f, 0.18f, 0.32f, 0.95f) : new Color(0.05f, 0.09f, 0.16f, 0.85f);
            item.setBackground(skin.newDrawable("white", bgCol));
            item.pad(8f, 10f, 8f, 10f);
            item.align(Align.left);

            String prefix = switch (p.getName().toLowerCase()) {
                case "earth" -> "[SOL-01] ";
                case "mars" -> "[SOL-02] ";
                case "europa" -> "[JUV-01] ";
                case "titan" -> "[SAT-01] ";
                case "kepler-186f" -> "[EXO-01] ";
                default -> "[NAV-00] ";
            };

            Label nameLbl = new Label(prefix + p.getName().toUpperCase(), skin, "mono");
            if (isCurrent) {
                nameLbl.setColor(CinematicTheme.CYAN_AVIONICS);
            } else if (isDiscovered) {
                nameLbl.setColor(CinematicTheme.TEXT_PRIMARY);
            } else {
                nameLbl.setColor(CinematicTheme.TEXT_MUTED);
            }
            item.add(nameLbl).left().expandX();

            String statusTag;
            Color statusColor;
            if (isCurrent) {
                statusTag = "[HERE]";
                statusColor = CinematicTheme.CYAN_AVIONICS;
            } else if (!isDiscovered) {
                statusTag = "[LOCKED]";
                statusColor = CinematicTheme.STATUS_CRITICAL;
            } else {
                statusTag = "-" + controller.getFuelCost(p) + "% F";
                statusColor = CinematicTheme.STATUS_NOMINAL;
            }

            Label tagLbl = new Label(statusTag, skin, "mono");
            tagLbl.setColor(statusColor);
            tagLbl.setFontScale(0.80f);
            item.add(tagLbl).right();

            item.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectPlanet(p);
                }
            });

            planetListTable.add(item).width(210f).height(36f).padBottom(4f).row();
        }
    }

    private void selectPlanet(Planet p) {
        if (p == null) return;
        this.selectedPlanet = p;
        if (audioManager != null) audioManager.playSelect();
        sceneManager.setSelectedPlanet(p.getName());
        rebuildPlanetList();
        updateTargetTelemetry();
    }

    private void updateTargetTelemetry() {
        if (selectedPlanet == null) return;

        targetNameLabel.setText(selectedPlanet.getName().toUpperCase());

        String classification = switch (selectedPlanet.getName().toLowerCase()) {
            case "earth" -> "CLASSIFICATION: HABITABLE TERRESTRIAL BIOSPHERE";
            case "mars" -> "CLASSIFICATION: ARID SILICATE DESERT WORLD";
            case "europa" -> "CLASSIFICATION: CRYOGENIC ICE CRUST SUB-OCEAN MOON";
            case "titan" -> "CLASSIFICATION: DENSE NITROGEN-METHANE ATMOSPHERIC MOON";
            case "kepler-186f" -> "CLASSIFICATION: EXTRASOLAR HABITABLE-ZONE SUPER-EARTH";
            default -> "CLASSIFICATION: UNIDENTIFIED CELESTIAL BODY";
        };
        targetClassLabel.setText(classification);

        targetDescLabel.setText(selectedPlanet.getDescription());
        targetEnvLabel.setText(selectedPlanet.getEnvironmentDescription());

        Planet current = controller.getGameState().getCurrentLocation();
        boolean isCurrent = current != null && selectedPlanet.getName().equalsIgnoreCase(current.getName());
        boolean isDiscovered = selectedPlanet.isDiscovered();
        int fuelCost = controller.getFuelCost(selectedPlanet);
        int availableFuel = controller.getGameState().getSpaceship().getFuel();
        boolean hasFuel = availableFuel >= fuelCost;

        if (controller.isCampaignCompleted()) {
            corridorStatusLabel.setText("CORRIDOR: SUSPENDED // CAMPAIGN CONCLUDED");
            corridorStatusLabel.setColor(CinematicTheme.STATUS_CRITICAL);
            fuelRequirementLabel.setText("TRANSIT BURN REQUIREMENT: [FLIGHT OPERATIONS SUSPENDED]");
            String ending = controller.getCompletedEnding();
            transitFeasibilityLabel.setText("STATUS: ALL FLIGHT DIRECTIVES RESOLVED (" + (ending != null ? ending.toUpperCase() : "CONCLUDED") + ")");
            transitFeasibilityLabel.setColor(CinematicTheme.STATUS_CRITICAL);
            engageTransitBtn.setDisabled(true);
            engageTransitBtn.setText("[FLIGHT SUSPENDED // CONCLUDED]");
        } else if (isCurrent) {
            corridorStatusLabel.setText("CORRIDOR: CURRENT LOCATION // ORBIT ACTIVE");
            corridorStatusLabel.setColor(CinematicTheme.CYAN_AVIONICS);
            fuelRequirementLabel.setText("TRANSIT BURN REQUIREMENT: 0% FUEL (IN SITU)");
            transitFeasibilityLabel.setText("STATUS: VESSEL CURRENTLY IN PARKING ORBIT");
            transitFeasibilityLabel.setColor(CinematicTheme.CYAN_AVIONICS);
            engageTransitBtn.setDisabled(true);
            engageTransitBtn.setText("CURRENT PARKING ORBIT");
        } else if (!isDiscovered) {
            corridorStatusLabel.setText("CORRIDOR: RESTRICTED // CLEARANCE REQUIRED");
            corridorStatusLabel.setColor(CinematicTheme.STATUS_CRITICAL);
            fuelRequirementLabel.setText("TRANSIT BURN REQUIREMENT: [REDACTED]");
            transitFeasibilityLabel.setText("STATUS: TRAJECTORY ENCRYPTION LOCKED BY FLEET COMMAND");
            transitFeasibilityLabel.setColor(CinematicTheme.STATUS_CRITICAL);
            engageTransitBtn.setDisabled(true);
            engageTransitBtn.setText("[LOCKED // RESTRICTED]");
        } else if (!hasFuel) {
            corridorStatusLabel.setText("CORRIDOR: AUTHORIZED FOR TRANSIT");
            corridorStatusLabel.setColor(CinematicTheme.STATUS_NOMINAL);
            fuelRequirementLabel.setText("TRANSIT BURN REQUIREMENT: -" + fuelCost + "% FUEL (" + availableFuel + "% AVAIL)");
            transitFeasibilityLabel.setText("STATUS: INSUFFICIENT PROPELLANT FOR ORBITAL TRANSFER");
            transitFeasibilityLabel.setColor(CinematicTheme.STATUS_CRITICAL);
            engageTransitBtn.setDisabled(true);
            engageTransitBtn.setText("INSUFFICIENT PROPELLANT");
        } else {
            corridorStatusLabel.setText("CORRIDOR: AUTHORIZED FOR TRANSIT");
            corridorStatusLabel.setColor(CinematicTheme.STATUS_NOMINAL);
            fuelRequirementLabel.setText("TRANSIT BURN REQUIREMENT: -" + fuelCost + "% FUEL (RESERVES: " + availableFuel + "% -> " + (availableFuel - fuelCost) + "%)");
            transitFeasibilityLabel.setText("STATUS: FLIGHT VECTOR COMPUTED // CLEAR FOR INSERTION");
            transitFeasibilityLabel.setColor(CinematicTheme.STATUS_NOMINAL);
            engageTransitBtn.setDisabled(false);
            engageTransitBtn.setText("ENGAGE TRANSIT BURN (-" + fuelCost + "% FUEL)");
        }
    }

    /**
     * Tactical Confirmation Dialog for Interplanetary Transit.
     */
    private void promptTransitConfirmation() {
        if (selectedPlanet == null || audioManager == null) return;
        audioManager.playConfirm();

        Planet current = controller.getGameState().getCurrentLocation();
        int cost = controller.getFuelCost(selectedPlanet);
        int fuel = controller.getGameState().getSpaceship().getFuel();

        HoloDialog dialog = new HoloDialog("FLIGHT COMPUTER // CONFIRM TRANSIT BURN", skin, audioManager);
        Table content = dialog.getContentTable();
        content.align(Align.topLeft);

        Label header = new Label("INTERPLANETARY TRAJECTORY CLEARANCE", skin, "subtitle");
        header.setColor(CinematicTheme.AMBER_WARM);
        content.add(header).left().padBottom(10f).row();

        String routeText = String.format("TRAJECTORY: %s ---> %s\nPROPELLANT CONSUMPTION: -%d%% FUEL\nPROJECTED TANK LEVEL: %d%% ---> %d%%\n\nProceeding will execute main thruster burns and transition parking orbit.",
                current != null ? current.getName().toUpperCase() : "SOL",
                selectedPlanet.getName().toUpperCase(),
                cost, fuel, fuel - cost);

        Label details = new Label(routeText, skin, "mono");
        details.setColor(CinematicTheme.TEXT_PRIMARY);
        content.add(details).left().padBottom(16f).row();

        TextButton abortBtn = new TextButton("ABORT TRANSIT", skin);
        abortBtn.getLabel().setFontScale(0.85f);
        abortBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (audioManager != null) audioManager.playCancel();
                dialog.hide();
            }
        });

        TextButton confirmBtn = new TextButton("CONFIRM & IGNITE THRUSTERS", skin);
        confirmBtn.getLabel().setFontScale(0.85f);
        confirmBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
                executeTransitBurn(selectedPlanet);
            }
        });

        dialog.getButtonTable().add(abortBtn).width(160f).height(40f).padRight(12f);
        dialog.getButtonTable().add(confirmBtn).width(240f).height(40f);
        dialog.show(stage);
    }

    /**
     * Executes the actual interplanetary transit operation via OrbitController
     * and triggers the cinematic warp animation.
     */
    private void executeTransitBurn(Planet target) {
        NavigationResult res = controller.travelTo(target);

        if (!res.isSuccess()) {
            if (audioManager != null) audioManager.playAlert();
            showStatusMessage("[-] TRANSIT REJECTED: " + res.getMessage());
            updateTargetTelemetry();
            return;
        }

        if (audioManager != null) audioManager.playConfirm();

        // 1. Activate In-Flight Warp Overlay
        warpStatusLabel.setText("TRANSIT BURN IN PROGRESS: " + target.getName().toUpperCase());
        warpSubLabel.setText(String.format("-%d%% FUEL CONSUMED // RELATIVISTIC ACCELERATION ENGAGED...", res.getFuelConsumed()));
        warpOverlayTable.setVisible(true);
        warpOverlayTable.getColor().a = 0f;
        warpOverlayTable.addAction(Actions.fadeIn(0.4f));

        // 2. Trigger 3D SceneManager Warp Streaks & Camera Push
        sceneManager.startTransitBurn(target, () -> {
            warpSubLabel.setText("ORBITAL INSERTION COMPLETED: " + target.getName().toUpperCase());
            // Fade out and return cleanly to Command Center
            transitionOverlay.fadeOut(0.6f, () -> {
                game.showCommandCenter();
            });
        });
    }

    private void handleReturnToBridge() {
        if (sceneManager.isWarping()) return;
        if (audioManager != null) audioManager.playCancel();
        transitionOverlay.fadeOut(0.4f, () -> {
            game.showCommandCenter();
        });
    }

    private void showStatusMessage(String msg) {
        Label toast = new Label(msg, skin, "mono");
        toast.setColor(CinematicTheme.STATUS_CRITICAL);
        toast.setPosition(Gdx.graphics.getWidth() * 0.5f, 60f, Align.center);
        toast.addAction(Actions.sequence(
                Actions.fadeIn(0.2f),
                Actions.delay(3.0f),
                Actions.fadeOut(0.5f),
                Actions.removeActor()
        ));
        stage.addActor(toast);
    }

    @Override
    public void show() {
        System.out.println("[ORBIT-SCREEN] NavigationScreen active. 3D holographic projection online.");
        setupInput();
        if (stage != null) {
            stage.unfocusAll();
            for (Actor actor : new com.badlogic.gdx.utils.Array.ArrayIterator<>(stage.getActors())) {
                if (actor instanceof com.badlogic.gdx.scenes.scene2d.ui.Dialog) {
                    actor.remove();
                }
            }
        }
        checkAndTriggerScriptedBriefings();
        refreshFromController();
        transitionOverlay.fadeIn(0.5f, null);
    }

    /**
     * Checks and triggers any scripted mission navigation briefings from High Command
     * (e.g. Mars authorization on first visit, Outer Moons in Stage 2, Kepler-186f in Stage 3).
     */
    public void checkAndTriggerScriptedBriefings() {
        if (!controller.isGameActive()) return;

        // 1. Initial Mars Authorization Directive (First visit to Navigation)
        if (!controller.isMarsUnlocked()) {
            String briefingText = controller.getInitialNavigationBriefingText();
            boolean newlyUnlocked = controller.triggerInitialNavigationBriefing();
            if (newlyUnlocked) {
                Planet mars = controller.getGameState().getPlanetByName("Mars");
                if (mars != null) {
                    selectedPlanet = mars;
                    sceneManager.setSelectedPlanet(mars.getName());
                }
                showScriptedBriefingDialog(
                        "MISSION CONTROL // FLIGHT AUTHORIZATION DIRECTIVE",
                        briefingText
                );
            }
            return;
        }

        // 2. Stage 2 Outer Moons (Europa & Titan) Authorization Directive
        if (controller.needsOuterMoonsNavigationBriefing()) {
            String briefingText = controller.getOuterMoonsNavigationBriefingText();
            boolean newlyLogged = controller.triggerOuterMoonsNavigationBriefing();
            if (newlyLogged) {
                showScriptedBriefingDialog(
                        "MISSION CONTROL // FLIGHT AUTHORIZATION DIRECTIVE",
                        briefingText
                );
            }
            return;
        }

        // 3. Stage 3 Kepler-186f Extrasolar Trajectory Directive
        if (controller.needsKeplerNavigationBriefing()) {
            String briefingText = controller.getKeplerNavigationBriefingText();
            boolean newlyLogged = controller.triggerKeplerNavigationBriefing();
            if (newlyLogged) {
                showScriptedBriefingDialog(
                        "MISSION CONTROL // EXTRASOLAR TRAJECTORY DIRECTIVE",
                        briefingText
                );
            }
        }
    }

    public void showScriptedBriefingDialog(String title, String briefingText) {
        if (audioManager != null) audioManager.playConfirm();

        HoloDialog dialog = new HoloDialog(title, skin, audioManager);
        Table content = dialog.getContentTable();
        content.align(Align.topLeft);

        Label body = new Label(briefingText, skin, "mono");
        body.setColor(CinematicTheme.TEXT_PRIMARY);
        body.setFontScale(0.82f);
        content.add(body).left().padBottom(16f).row();

        TextButton ackBtn = new TextButton("ACKNOWLEDGE DIRECTIVE", skin);
        ackBtn.getLabel().setFontScale(0.85f);
        ackBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (audioManager != null) audioManager.playConfirm();
                dialog.hide();
            }
        });

        dialog.getButtonTable().add(ackBtn).width(240f).height(38f);
        dialog.key(Input.Keys.ENTER, null);
        dialog.key(Input.Keys.ESCAPE, null);
        dialog.show(stage);
    }

    @Override
    public void render(float delta) {
        // 1. Update & Render 3D Hologram, Console Table, Starfield
        sceneManager.update(delta);
        sceneManager.render();

        // 2. Render Scene2D Diegetic Telemetry Overlay
        stage.act(delta);
        stage.draw();

        frameCount++;
        logTimer += delta;
        if (logTimer >= 2.0f) {
            System.out.println(String.format("[ORBIT-RENDER] Navigation: %d frames | Target 60 FPS (Current: %d FPS)", frameCount, Gdx.graphics.getFramesPerSecond()));
            logTimer = 0f;
        }
    }

    @Override
    public void resize(int width, int height) {
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

    public Planet getSelectedPlanet() {
        return selectedPlanet;
    }

    public TextButton getEngageTransitButton() {
        return engageTransitBtn;
    }

    public Label getCorridorStatusLabel() {
        return corridorStatusLabel;
    }

    public Label getTransitFeasibilityLabel() {
        return transitFeasibilityLabel;
    }

    public Label getFuelRequirementLabel() {
        return fuelRequirementLabel;
    }

    public NavigationSceneManager getSceneManager() {
        return sceneManager;
    }

    @Override
    public void dispose() {
        if (sceneManager != null) sceneManager.dispose();
        if (stage != null) stage.dispose();
    }
}

