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
import orbit.controllers.ItemUseResult;
import orbit.controllers.NavigationResult;
import orbit.controllers.OrbitController;
import orbit.models.ExplorationEvent;
import orbit.models.Inventory;
import orbit.models.MissionLog;
import orbit.models.MissionObjective;
import orbit.models.Planet;
import orbit.models.Player;
import orbit.models.Spaceship;
import orbit.render.AssetManager;
import orbit.render.BridgeSceneManager;
import orbit.render.OrbitGame;
import orbit.render.RenderManager;
import orbit.render.audio.AudioManager;
import orbit.render.components.HoloButton;
import orbit.render.components.HoloDialog;
import orbit.render.components.HoloPanel;
import orbit.render.components.ScreenTransitionOverlay;
import orbit.render.components.ShipResourceGauge;
import orbit.render.components.TelemetryReadout;
import orbit.render.theme.CinematicTheme;

import java.util.List;

/**
 * CommandCenterScreen provides the operational bridge interface for ORBIT.
 * Simulates standing inside the spacecraft bridge looking out through a forward
 * observation viewport at surrounding space scenery, surrounded by physical-looking
 * instrument consoles and live telemetry readouts.
 *
 * Directly connects all operations to the authoritative OrbitController.
 */
public class CommandCenterScreen implements Screen {

    private final OrbitGame game;
    private final OrbitController controller;
    private final AssetManager assetManager;
    private final RenderManager renderManager;
    private final AudioManager audioManager;
    private final BridgeSceneManager bridgeScene;
    private final Stage stage;
    private final Skin skin;

    // Header Badges
    private Label commanderBadge;
    private Label vesselBadge;
    private Label locationBadge;
    private Label systemStatusBadge;

    // Telemetry Gauges (Authentic 4 Resources)
    private ShipResourceGauge fuelGauge;
    private ShipResourceGauge o2Gauge;
    private ShipResourceGauge energyGauge;
    private ShipResourceGauge hullGauge;

    // Cargo Manifest Labels
    private Label metalLabel;
    private Label energyCellsLabel;
    private Label oxygenCanistersLabel;
    private Label fuelCellsLabel;
    private Label artifactsLabel;

    // Campaign Directives
    private Label stageLabel;
    private Label progressLabel;
    private Table objectivesTable;
    private HoloButton exploreBtn;
    private HoloButton cargoBtn;
    private HoloButton logBtn;
    private HoloButton directivesBtn;

    // Footer
    private Label statusFeedbackLabel;
    private ScreenTransitionOverlay transitionOverlay;

    private long frameCount = 0;
    private float logTimer = 0f;

    public CommandCenterScreen(OrbitGame game, OrbitController controller, AssetManager assetManager, RenderManager renderManager, AudioManager audioManager) {
        this.game = game;
        this.controller = controller;
        this.assetManager = assetManager;
        this.renderManager = renderManager;
        this.audioManager = audioManager;
        this.bridgeScene = new BridgeSceneManager(assetManager);
        this.skin = assetManager.getUiSkin();
        this.stage = new Stage(renderManager.getUiViewport(), renderManager.getSpriteBatch());

        buildInterface();
        refreshFromController();
    }

    private void buildInterface() {
        stage.clear();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.pad(16f, 24f, 16f, 24f);

        // --- 1. Top Cockpit Status Rail (Overhead Bulkhead Display) ---
        Table topRail = new Table();
        vesselBadge = new Label("VESSEL: ODYSSEY-07 // TACTICAL BRIDGE", skin, "mono");
        vesselBadge.setColor(CinematicTheme.AMBER_WARM);
        vesselBadge.setFontScale(0.85f);

        commanderBadge = new Label("CMDR: UNASSIGNED", skin, "mono");
        commanderBadge.setColor(CinematicTheme.TEXT_PRIMARY);
        commanderBadge.setFontScale(0.85f);

        locationBadge = new Label("LOC: EARTH ORBIT", skin, "mono");
        locationBadge.setColor(CinematicTheme.CYAN_AVIONICS);
        locationBadge.setFontScale(0.85f);

        systemStatusBadge = new Label("● SYSTEMS NOMINAL", skin, "mono");
        systemStatusBadge.setColor(CinematicTheme.STATUS_NOMINAL);
        systemStatusBadge.setFontScale(0.85f);

        topRail.add(vesselBadge).left().padRight(18f);
        topRail.add(commanderBadge).left().padRight(18f);
        topRail.add(locationBadge).left().expandX();
        topRail.add(systemStatusBadge).right();
        rootTable.add(topRail).fillX().top().padBottom(14f).row();

        // --- 2. Center Tactical Workspace (Left Console / Center Viewport / Right Console) ---
        Table centerWorkspace = new Table();

        // --- LEFT CONSOLE: Spacecraft Vitals & Cargo Manifest ---
        HoloPanel leftConsole = new HoloPanel("TACTICAL AVIONICS // VITALS & CARGO", skin);
        leftConsole.setBracketColor(CinematicTheme.AMBER_DIM);
        Table leftContent = leftConsole.getContentTable();

        fuelGauge = new ShipResourceGauge(ShipResourceGauge.ResourceType.FUEL, skin);
        o2Gauge = new ShipResourceGauge(ShipResourceGauge.ResourceType.OXYGEN, skin);
        energyGauge = new ShipResourceGauge(ShipResourceGauge.ResourceType.ENERGY, skin);
        hullGauge = new ShipResourceGauge(ShipResourceGauge.ResourceType.HULL, skin);

        leftContent.add(fuelGauge).fillX().padBottom(4f).row();
        leftContent.add(o2Gauge).fillX().padBottom(4f).row();
        leftContent.add(energyGauge).fillX().padBottom(4f).row();
        leftContent.add(hullGauge).fillX().padBottom(8f).row();

        // Cargo Stores Divider
        Label cargoHeader = new Label("CARGO MANIFEST // HARDWARE STORES", skin, "mono");
        cargoHeader.setColor(CinematicTheme.CYAN_AVIONICS);
        cargoHeader.setFontScale(0.78f);
        leftContent.add(cargoHeader).left().padBottom(4f).row();

        metalLabel = createCargoRow("Metal (Structural Repair)");
        energyCellsLabel = createCargoRow("Energy Cells (Reactor)");
        oxygenCanistersLabel = createCargoRow("Oxygen Canisters (O2)");
        fuelCellsLabel = createCargoRow("Fuel Cells (Reaction Mass)");
        artifactsLabel = createCargoRow("Alien Artifacts (Relics)");

        leftContent.add(metalLabel).left().padBottom(2f).row();
        leftContent.add(energyCellsLabel).left().padBottom(2f).row();
        leftContent.add(oxygenCanistersLabel).left().padBottom(2f).row();
        leftContent.add(fuelCellsLabel).left().padBottom(2f).row();
        leftContent.add(artifactsLabel).left().row();

        centerWorkspace.add(leftConsole).width(350f).top().left();

        // --- CENTER VIEWPORT: Clear Forward Observation Area ---
        Table centerViewport = new Table();
        centerViewport.add().expand().fill();
        centerWorkspace.add(centerViewport).expand().fill();

        // --- RIGHT CONSOLE: Campaign Directives & Subsystem Controls ---
        Table rightColumn = new Table();
        rightColumn.align(Align.topRight);

        // Mission Directives Panel
        HoloPanel missionPanel = new HoloPanel("CAMPAIGN DIRECTIVES // FLEET ORDERS", skin);
        missionPanel.setBracketColor(CinematicTheme.CYAN_DIM);
        Table missionContent = missionPanel.getContentTable();

        stageLabel = new Label("STAGE 1 — DEPARTURE", skin, "subtitle");
        stageLabel.setColor(CinematicTheme.TEXT_PRIMARY);

        progressLabel = new Label("0% COMPLETE", skin, "mono");
        progressLabel.setColor(CinematicTheme.STATUS_NOMINAL);
        progressLabel.setFontScale(0.80f);

        Table stageHeader = new Table();
        stageHeader.add(stageLabel).left().expandX();
        stageHeader.add(progressLabel).right();
        missionContent.add(stageHeader).fillX().padBottom(6f).row();

        objectivesTable = new Table();
        objectivesTable.align(Align.topLeft);
        missionContent.add(objectivesTable).fillX().padBottom(8f).row();

        rightColumn.add(missionPanel).width(370f).padBottom(10f).row();

        // Tactical Subsystem Flight Switches
        HoloButton navBtn = new HoloButton("[NAV-01]", "INTERPLANETARY NAVIGATION", "CALIBRATE FLIGHT VECTOR & TRANSIT", skin, audioManager, this::handleNavigationAccess);
        navBtn.setAccentColor(CinematicTheme.CYAN_AVIONICS);

        exploreBtn = new HoloButton("[EXP-02]", "PLANETARY EXPLORATION", "SURFACE RECONNAISSANCE & SURVEY", skin, audioManager, this::handleExploreAccess);
        exploreBtn.setAccentColor(CinematicTheme.AMBER_WARM);

        cargoBtn = new HoloButton("[CRG-03]", "CARGO HOLD & SUPPLIES", "INSPECT & DISPENSE INVENTORY", skin, audioManager, this::handleInventoryAccess);
        cargoBtn.setAccentColor(CinematicTheme.STEEL_BLUE);

        logBtn = new HoloButton("[LOG-04]", "MISSION FLIGHT ARCHIVE", "CHRONOLOGICAL TELEMETRY LOGS", skin, audioManager, this::handleMissionLogAccess);
        logBtn.setAccentColor(CinematicTheme.TEXT_SECONDARY);

        directivesBtn = new HoloButton("[DIR-05]", "MISSION CONTROL ORDERS", "EXPEDITION STAGES & OBJECTIVES", skin, audioManager, this::handleMissionControlAccess);
        directivesBtn.setAccentColor(CinematicTheme.SOLAR_GOLD);

        float switchW = 370f;
        float switchH = 46f;
        rightColumn.add(navBtn).width(switchW).height(switchH).padBottom(4f).row();
        rightColumn.add(exploreBtn).width(switchW).height(switchH).padBottom(4f).row();
        rightColumn.add(cargoBtn).width(switchW).height(switchH).padBottom(4f).row();
        rightColumn.add(logBtn).width(switchW).height(switchH).padBottom(4f).row();
        rightColumn.add(directivesBtn).width(switchW).height(switchH).row();

        centerWorkspace.add(rightColumn).width(370f).top().right();
        rootTable.add(centerWorkspace).expand().fill().row();

        // --- 3. Bottom Bridge Operations Rail ---
        Table bottomRail = new Table();
        statusFeedbackLabel = new Label("COMMAND BRIDGE ONLINE // ALL STATIONS MANNED", skin, "mono");
        statusFeedbackLabel.setColor(CinematicTheme.TEXT_MUTED);
        statusFeedbackLabel.setFontScale(0.82f);

        Table bridgeControls = new Table();
        TextButton saveBtn = new TextButton("SAVE TELEMETRY", skin);
        saveBtn.getLabel().setFontScale(0.82f);
        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleSaveGame();
            }
        });

        TextButton returnBtn = new TextButton("RETURN TO MAIN MENU", skin);
        returnBtn.getLabel().setFontScale(0.82f);
        returnBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleReturnToMainMenu();
            }
        });

        bridgeControls.add(saveBtn).width(160f).height(38f).padRight(10f);
        bridgeControls.add(returnBtn).width(190f).height(38f);

        bottomRail.add(statusFeedbackLabel).left().expandX();
        bottomRail.add(bridgeControls).right();
        rootTable.add(bottomRail).fillX().bottom();

        stage.addActor(rootTable);

        // Transition Overlay for cinematic fade
        transitionOverlay = new ScreenTransitionOverlay(skin.getDrawable("white"));
        transitionOverlay.setFillParent(true);
        stage.addActor(transitionOverlay);
        transitionOverlay.fadeIn(0.6f, null);
    }

    private Label createCargoRow(String label) {
        Label row = new Label("• " + label + " : 0", skin, "mono");
        row.setColor(CinematicTheme.TEXT_SECONDARY);
        row.setFontScale(0.78f);
        return row;
    }

    // --- Action Handlers Connected to OrbitController ---

    /**
     * Connects bridge navigation action to the dedicated 3D Holographic Navigation experience.
     */
    private void handleNavigationAccess() {
        if (audioManager != null) audioManager.playConfirm();
        transitionOverlay.fadeOut(0.4f, () -> {
            game.showNavigationScreen();
        });
    }

    /**
     * Contextual Navigation Flight Computer Console (Preserved):
     * Displays all discovered and locked destinations, fuel requirements,
     * and allows executing orbital transit burns through controller.travelTo().
     */
    public void showContextualNavigationDialog() {
        if (audioManager != null) audioManager.playConfirm();
        if (!controller.isMarsUnlocked()) {
            controller.triggerInitialNavigationBriefing();
        }
        HoloDialog dialog = new HoloDialog("NAVIGATION // FLIGHT COMPUTER INTERFACE", skin, audioManager);

        Planet current = controller.getCurrentLocation();
        String currentLocName = (current != null) ? current.getName().toUpperCase() : "EARTH";
        int currentFuel = controller.getSpaceship() != null ? controller.getSpaceship().getFuel() : 100;

        Table content = dialog.getContentTable();
        content.align(Align.topLeft);

        Label headerLbl = new Label("CURRENT ORBIT: " + currentLocName + "  |  FUEL RESERVES: " + currentFuel + "%", skin, "subtitle");
        headerLbl.setColor(CinematicTheme.AMBER_WARM);
        content.add(headerLbl).left().padBottom(10f).row();

        List<Planet> planets = controller.getPlanets();
        if (planets != null) {
            for (Planet p : planets) {
                Table row = new Table();
                row.align(Align.left);
                row.pad(4f, 6f, 4f, 6f);

                boolean isCurrent = (current != null && p.getName().equalsIgnoreCase(current.getName()));
                boolean unlocked = p.isDiscovered();
                int cost = controller.getFuelCost(p);

                Label nameLbl = new Label(p.getName().toUpperCase(), skin, "subtitle");
                nameLbl.setColor(isCurrent ? CinematicTheme.AMBER_WARM : (unlocked ? CinematicTheme.TEXT_PRIMARY : CinematicTheme.TEXT_DIM));

                Label descLbl = new Label(" - " + p.getDescription() + " (Transit: " + cost + "% Fuel)", skin, "mono");
                descLbl.setColor(unlocked ? CinematicTheme.TEXT_MUTED : CinematicTheme.TEXT_DIM);
                descLbl.setFontScale(0.80f);

                row.add(nameLbl).left();
                row.add(descLbl).left().expandX();

                if (isCurrent) {
                    Label curTag = new Label("[CURRENT POSITION]", skin, "mono");
                    curTag.setColor(CinematicTheme.AMBER_WARM);
                    curTag.setFontScale(0.80f);
                    row.add(curTag).right();
                } else if (!unlocked) {
                    Label lockTag = new Label("[CORRIDOR RESTRICTED]", skin, "mono");
                    lockTag.setColor(CinematicTheme.TEXT_DIM);
                    lockTag.setFontScale(0.80f);
                    row.add(lockTag).right();
                } else if (controller.canTravelTo(p)) {
                    TextButton transitBtn = new TextButton("TRANSIT", skin);
                    transitBtn.getLabel().setFontScale(0.85f);
                    transitBtn.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            NavigationResult res = controller.travelTo(p);
                            dialog.hide();
                            if (res.isSuccess()) {
                                refreshFromController();
                                if (audioManager != null) audioManager.playConfirm();
                                showStatusToast("[+] ORBITAL INSERTION CONFIRMED: " + p.getName().toUpperCase() + " // -" + res.getFuelConsumed() + "% FUEL", CinematicTheme.STATUS_NOMINAL);
                            } else {
                                if (audioManager != null) audioManager.playAlert();
                                showStatusToast("[-] NAVIGATION FAILED: " + res.getMessage(), CinematicTheme.STATUS_CRITICAL);
                            }
                        }
                    });
                    row.add(transitBtn).width(120f).height(32f).right();
                } else {
                    Label fuelAlert = new Label("[INSUFFICIENT FUEL: NEED " + cost + "%]", skin, "mono");
                    fuelAlert.setColor(CinematicTheme.STATUS_CAUTION);
                    fuelAlert.setFontScale(0.78f);
                    row.add(fuelAlert).right();
                }

                content.add(row).fillX().padBottom(6f).row();
            }
        }

        TextButton closeBtn = new TextButton("CLOSE CONSOLE", skin);
        closeBtn.getLabel().setFontScale(0.85f);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        dialog.getButtonTable().add(closeBtn).width(150f).height(36f);
        dialog.show(stage);
    }

    /**
     * Connects bridge cargo action to the dedicated 3D Cargo Hold experience.
     */
    private void handleInventoryAccess() {
        if (audioManager != null) audioManager.playConfirm();
        transitionOverlay.fadeOut(0.4f, () -> {
            game.showCargoScreen();
        });
    }

    /**
     * Contextual Cargo Hold & Supply Dispenser Console (Preserved):
     * Displays live stock counts, explains restore values, and provides interactive
     * supply dispensing through controller.useItem(). Non-consumable artifacts are protected.
     */
    public void showContextualCargoDialog() {
        if (audioManager != null) audioManager.playConfirm();
        HoloDialog dialog = new HoloDialog("CARGO HOLD // SUPPLY DISPENSER CONSOLE", skin, audioManager);

        Table content = dialog.getContentTable();
        content.align(Align.topLeft);

        Inventory inv = controller.getInventory();
        Spaceship ship = controller.getSpaceship();

        Label intro = new Label("SPACECRAFT ODYSSEY-07 CARGO STORES // DISPENSE CONSUMABLES:", skin, "mono");
        intro.setColor(CinematicTheme.CYAN_AVIONICS);
        intro.setFontScale(0.85f);
        content.add(intro).left().padBottom(10f).row();

        // 1. Metal
        addSupplyRow(content, dialog, Inventory.METAL, "Metal (Hull Repair Matrix)", "+20% Hull Integrity",
                inv != null ? inv.getQuantity(Inventory.METAL) : 0,
                ship != null && ship.getHullIntegrity() < Spaceship.MAX_RESOURCE);

        // 2. Energy Cells
        addSupplyRow(content, dialog, Inventory.ENERGY_CELLS, "Energy Cells (Reactor Recharge)", "+25% Reactor Energy",
                inv != null ? inv.getQuantity(Inventory.ENERGY_CELLS) : 0,
                ship != null && ship.getEnergy() < Spaceship.MAX_RESOURCE);

        // 3. Oxygen Canisters
        addSupplyRow(content, dialog, Inventory.OXYGEN_CANISTERS, "Oxygen Canisters (Life Support)", "+25% Life Support O2",
                inv != null ? inv.getQuantity(Inventory.OXYGEN_CANISTERS) : 0,
                ship != null && ship.getOxygen() < Spaceship.MAX_RESOURCE);

        // 4. Fuel Cells
        addSupplyRow(content, dialog, Inventory.FUEL_CELLS, "Fuel Cells (Propellant Burn)", "+25% Propulsion Fuel",
                inv != null ? inv.getQuantity(Inventory.FUEL_CELLS) : 0,
                ship != null && ship.getFuel() < Spaceship.MAX_RESOURCE);

        // 5. Alien Artifacts (Non-consumable anomaly)
        Table artifactRow = new Table();
        artifactRow.align(Align.left);
        int artQty = inv != null ? inv.getQuantity(Inventory.ALIEN_ARTIFACTS) : 0;
        Label artLbl = new Label("• Alien Artifacts (Scientific Data Relics) : " + artQty + " units", skin, "mono");
        artLbl.setColor(CinematicTheme.SOLAR_GOLD);
        artLbl.setFontScale(0.82f);
        Label artTag = new Label("[NON-CONSUMABLE ANOMALY]", skin, "mono");
        artTag.setColor(CinematicTheme.TEXT_MUTED);
        artTag.setFontScale(0.75f);
        artifactRow.add(artLbl).left().expandX();
        artifactRow.add(artTag).right();
        content.add(artifactRow).fillX().padTop(6f).row();

        TextButton closeBtn = new TextButton("CLOSE CONSOLE", skin);
        closeBtn.getLabel().setFontScale(0.85f);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        dialog.getButtonTable().add(closeBtn).width(150f).height(36f);
        dialog.show(stage);
    }

    private void addSupplyRow(Table content, HoloDialog dialog, String itemName, String label, String effect, int qty, boolean canAbsorb) {
        Table row = new Table();
        row.align(Align.left);
        row.pad(3f, 0f, 3f, 0f);

        Label itemLbl = new Label("• " + label + " [" + qty + " units]", skin, "mono");
        itemLbl.setColor(qty > 0 ? CinematicTheme.TEXT_PRIMARY : CinematicTheme.TEXT_DIM);
        itemLbl.setFontScale(0.82f);

        Label effLbl = new Label(" (" + effect + ")", skin, "mono");
        effLbl.setColor(CinematicTheme.TEXT_MUTED);
        effLbl.setFontScale(0.75f);

        row.add(itemLbl).left();
        row.add(effLbl).left().expandX();

        boolean eligible = (qty > 0 && canAbsorb && controller.canUseItem(itemName));
        if (eligible) {
            TextButton useBtn = new TextButton("DISPENSE", skin);
            useBtn.getLabel().setFontScale(0.85f);
            useBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    ItemUseResult res = controller.useItem(itemName);
                    dialog.hide();
                    if (res.isSuccess()) {
                        refreshFromController();
                        if (audioManager != null) audioManager.playConfirm();
                        showStatusToast("[+] CONSUMED 1 " + itemName.toUpperCase() + ": " + res.getTargetResource().toUpperCase() + " RESTORED TO " + res.getNewValue() + "%", CinematicTheme.STATUS_NOMINAL);
                        handleInventoryAccess(); // Re-open with refreshed counts
                    } else {
                        if (audioManager != null) audioManager.playAlert();
                        showStatusToast("[-] DISPENSE FAILED: " + res.getMessage(), CinematicTheme.STATUS_CRITICAL);
                    }
                }
            });
            row.add(useBtn).width(110f).height(30f).right();
        } else if (qty <= 0) {
            Label noStock = new Label("[OUT OF STOCK]", skin, "mono");
            noStock.setColor(CinematicTheme.TEXT_DIM);
            noStock.setFontScale(0.75f);
            row.add(noStock).right();
        } else {
            Label full = new Label("[SYSTEM OPTIMAL: 100%]", skin, "mono");
            full.setColor(CinematicTheme.STATUS_NOMINAL);
            full.setFontScale(0.75f);
            row.add(full).right();
        }

        content.add(row).fillX().padBottom(4f).row();
    }

    /**
     * Connects bridge flight archive action to the dedicated Mission Log experience.
     */
    private void handleMissionLogAccess() {
        if (audioManager != null) audioManager.playConfirm();
        transitionOverlay.fadeOut(0.4f, () -> {
            game.showMissionLogScreen();
        });
    }

    /**
     * Contextual Mission Flight Archive Console (Preserved):
     * Displays chronological log entries from controller.getMissionLog().
     */
    public void showContextualMissionLogDialog() {
        if (audioManager != null) audioManager.playConfirm();
        HoloDialog dialog = new HoloDialog("MISSION LOG // CHRONOLOGICAL FLIGHT ARCHIVE", skin, audioManager);

        Table content = dialog.getContentTable();
        content.align(Align.topLeft);

        MissionLog log = controller.getMissionLog();
        if (log != null && log.getEntries() != null && !log.getEntries().isEmpty()) {
            List<MissionLog.LogEntry> entries = log.getEntries();
            int start = Math.max(0, entries.size() - 9);
            for (int i = start; i < entries.size(); i++) {
                MissionLog.LogEntry e = entries.get(i);
                Table row = new Table();
                row.align(Align.left);

                Label timeLbl = new Label("[" + e.getTimestamp() + "] ", skin, "mono");
                timeLbl.setColor(CinematicTheme.TEXT_MUTED);
                timeLbl.setFontScale(0.76f);

                Label catLbl = new Label("[" + e.getCategory() + "] ", skin, "mono");
                catLbl.setColor(getCategoryColor(e.getCategory()));
                catLbl.setFontScale(0.78f);

                Label msgLbl = new Label(e.getDescription(), skin, "mono");
                msgLbl.setColor(CinematicTheme.TEXT_PRIMARY);
                msgLbl.setFontScale(0.78f);

                row.add(timeLbl).left();
                row.add(catLbl).left();
                row.add(msgLbl).left().expandX();
                content.add(row).fillX().padBottom(3f).row();
            }
        } else {
            Label empty = new Label("No historical telemetry entries recorded in archive.", skin, "mono");
            empty.setColor(CinematicTheme.TEXT_MUTED);
            content.add(empty).left().padBottom(12f).row();
        }

        TextButton closeBtn = new TextButton("CLOSE ARCHIVE", skin);
        closeBtn.getLabel().setFontScale(0.85f);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        dialog.getButtonTable().add(closeBtn).width(150f).height(36f);
        dialog.show(stage);
    }

    private Color getCategoryColor(String cat) {
        if (cat == null) return CinematicTheme.TEXT_MUTED;
        return switch (cat.toUpperCase()) {
            case "DISCOVERY" -> CinematicTheme.AMBER_WARM;
            case "HAZARD" -> CinematicTheme.STATUS_CRITICAL;
            case "NAVIGATION" -> CinematicTheme.CYAN_AVIONICS;
            case "RESOURCE" -> CinematicTheme.SOLAR_GOLD;
            case "EXPEDITION" -> CinematicTheme.STATUS_NOMINAL;
            default -> CinematicTheme.TEXT_SECONDARY;
        };
    }

    /**
     * Connects bridge campaign directives action to the dedicated Mission Control experience.
     */
    private void handleMissionControlAccess() {
        if (audioManager != null) audioManager.playConfirm();
        transitionOverlay.fadeOut(0.4f, () -> {
            game.showMissionControlScreen();
        });
    }

    /**
     * Contextual Mission Control Directives Console (Preserved):
     * Displays overall campaign progress, current stage directives, and objectives.
     */
    public void showContextualMissionControlDialog() {
        if (audioManager != null) audioManager.playConfirm();
        HoloDialog dialog = new HoloDialog("MISSION CONTROL // CAMPAIGN DIRECTIVES CONSOLE", skin, audioManager);

        Table content = dialog.getContentTable();
        content.align(Align.topLeft);

        String currentStage = (controller.getGameState() != null) ? controller.getGameState().getCurrentStage() : "STAGE 1";
        int progress = (controller.getGameState() != null) ? controller.getGameState().getMissionProgress() : 0;

        Label header = new Label("FLEET HEADQUARTERS DIRECTIVES BRIEFING // " + currentStage.toUpperCase() + " (" + progress + "% COMPLETE)", skin, "subtitle");
        header.setColor(CinematicTheme.AMBER_WARM);
        content.add(header).left().padBottom(10f).row();

        List<MissionObjective> objectives = controller.getObjectives();
        if (objectives != null) {
            for (MissionObjective obj : objectives) {
                if (obj.getStage().equalsIgnoreCase(currentStage)) {
                    Table row = new Table();
                    row.align(Align.left);

                    Label statusTag = new Label(obj.isCompleted() ? "[DONE]   " : "[ACTIVE] ", skin, "mono");
                    statusTag.setColor(obj.isCompleted() ? CinematicTheme.STATUS_NOMINAL : CinematicTheme.CYAN_AVIONICS);
                    statusTag.setFontScale(0.80f);

                    Label objTitle = new Label(obj.getTitle(), skin, "mono");
                    objTitle.setColor(obj.isCompleted() ? CinematicTheme.TEXT_MUTED : CinematicTheme.TEXT_PRIMARY);
                    objTitle.setFontScale(0.80f);

                    row.add(statusTag).left();
                    row.add(objTitle).left().expandX();
                    content.add(row).fillX().padBottom(4f).row();
                }
            }
        }

        TextButton closeBtn = new TextButton("ACKNOWLEDGE", skin);
        closeBtn.getLabel().setFontScale(0.85f);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        dialog.getButtonTable().add(closeBtn).width(150f).height(36f);
        dialog.show(stage);
    }

    /**
     * Connects bridge exploration action to the dedicated 3D Exploration experience.
     */
    private void handleExploreAccess() {
        if (audioManager != null) audioManager.playConfirm();
        transitionOverlay.fadeOut(0.4f, () -> {
            game.showExplorationScreen();
        });
    }

    /**
     * Contextual Surface Reconnaissance / Exploration Console (Preserved):
     * Corrects exploration cost (10% Energy & 5% Oxygen, 0% Fuel).
     * Connects to controller.startExpedition() and dispatches hazard, beacon, and monolith choices.
     */
    public void showContextualExplorationDialog() {
        if (audioManager != null) audioManager.playConfirm();

        if (controller.canInitiateFinalDirective()) {
            showFinalDirectiveChoiceDialog();
            return;
        }

        HoloDialog dialog = new HoloDialog("SURFACE SURVEY // EXPLORATION TELEMETRY", skin, audioManager);
        Table content = dialog.getContentTable();
        content.align(Align.topLeft);

        Planet p = controller.getCurrentLocation();
        String planetName = (p != null) ? p.getName().toUpperCase() : "EARTH";
        boolean supported = controller.isExplorationSupported();

        Spaceship ship = controller.getSpaceship();
        int energy = (ship != null) ? ship.getEnergy() : 100;
        int oxygen = (ship != null) ? ship.getOxygen() : 100;
        boolean hasVitals = (energy >= 10 && oxygen >= 5);

        Label title = new Label("PLANETARY RECONNAISSANCE SENSORS // " + planetName, skin, "subtitle");
        title.setColor(CinematicTheme.AMBER_WARM);
        content.add(title).left().padBottom(8f).row();

        if (p != null) {
            Label overview = new Label("• Terrain Overview      : " + p.getDescription(), skin, "mono");
            overview.setColor(CinematicTheme.TEXT_SECONDARY);
            overview.setFontScale(0.80f);

            Label env = new Label("• Environment & Climate : " + p.getEnvironmentDescription(), skin, "mono");
            env.setColor(CinematicTheme.TEXT_SECONDARY);
            env.setFontScale(0.80f);

            Label status = new Label("• Exploration Status    : " + (p.isExplored() ? "SURVEY COMPLETE" : "UNEXPLORED SITE"), skin, "mono");
            status.setColor(p.isExplored() ? CinematicTheme.STATUS_NOMINAL : CinematicTheme.CYAN_AVIONICS);
            status.setFontScale(0.80f);

            // Accurate Exploration Cost per original ORBIT flight mechanics: 10 Energy, 5 Oxygen
            Label cost = new Label("• Survey Expedition Cost: 10% Reactor Energy & 5% Life Support O2 (0% Fuel)", skin, "mono");
            cost.setColor(CinematicTheme.AMBER_WARM);
            cost.setFontScale(0.80f);

            Label vitals = new Label("• Current Available     : " + energy + "% Energy / " + oxygen + "% Oxygen", skin, "mono");
            vitals.setColor(hasVitals ? CinematicTheme.STATUS_NOMINAL : CinematicTheme.STATUS_CRITICAL);
            vitals.setFontScale(0.80f);

            content.add(overview).left().padBottom(3f).row();
            content.add(env).left().padBottom(3f).row();
            content.add(status).left().padBottom(3f).row();
            content.add(cost).left().padBottom(3f).row();
            content.add(vitals).left().padBottom(12f).row();
        }

        if (!supported) {
            Label warn = new Label("[!] SURFACE DESCENT RESTRICTED: Celestial body atmospheric hazards prohibit excursions.", skin, "mono");
            warn.setColor(CinematicTheme.STATUS_CAUTION);
            warn.setFontScale(0.78f);
            content.add(warn).left().padBottom(12f).row();
        } else if (!hasVitals) {
            Label warn = new Label("[!] VITALS DEFICIT: Minimum 10% Energy and 5% Oxygen required for excursion rover launch.", skin, "mono");
            warn.setColor(CinematicTheme.STATUS_CRITICAL);
            warn.setFontScale(0.78f);
            content.add(warn).left().padBottom(12f).row();
        }

        if (supported && hasVitals) {
            TextButton deployBtn = new TextButton("DEPLOY EXCURSION ROVER (-10 ENG, -5 O2)", skin);
            deployBtn.getLabel().setFontScale(0.82f);
            deployBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    dialog.hide();
                    ExpeditionResult res = controller.startExpedition();
                    refreshFromController(); // Immediately synchronizes bridge gauges!
                    handleExpeditionResult(res);
                }
            });
            dialog.getButtonTable().add(deployBtn).width(340f).height(38f).padRight(10f);
        }

        TextButton closeBtn = new TextButton("ABORT", skin);
        closeBtn.getLabel().setFontScale(0.85f);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.hide();
            }
        });
        dialog.getButtonTable().add(closeBtn).width(120f).height(38f);
        dialog.show(stage);
    }

    private void handleExpeditionResult(ExpeditionResult res) {
        if (res == null) return;

        if (res.getType() == ExpeditionResult.Type.LAUNCH_INHIBITED || res.getType() == ExpeditionResult.Type.UNSUPPORTED_PLANET || !res.isSuccess()) {
            if (audioManager != null) audioManager.playAlert();
            showStatusToast("[-] EXCURSION INHIBITED: " + res.getMessage(), CinematicTheme.STATUS_CRITICAL);
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
                content.add(desc).left().padBottom(12f).row();

                String counterItem = (event != null) ? event.getCountermeasureItem() : null;
                int counterQty = (counterItem != null && controller.getInventory() != null) ? controller.getInventory().getQuantity(counterItem) : 0;

                if (counterItem != null && counterQty > 0) {
                    TextButton counterBtn = new TextButton("DEPLOY COUNTERMEASURE (-1 " + counterItem.toUpperCase() + ")", skin);
                    counterBtn.getLabel().setFontScale(0.82f);
                    counterBtn.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent ev, float x, float y) {
                            controller.resolveHazard(event, true);
                            dialog.hide();
                            refreshFromController();
                            showStatusToast("[+] HAZARD NEUTRALIZED VIA COUNTERMEASURE", CinematicTheme.STATUS_NOMINAL);
                        }
                    });
                    dialog.getButtonTable().add(counterBtn).width(300f).height(38f).padRight(10f);
                }

                TextButton braceBtn = new TextButton("BRACE FOR IMPACT (SUSTAIN PENALTY)", skin);
                braceBtn.getLabel().setFontScale(0.82f);
                braceBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent ev, float x, float y) {
                        controller.resolveHazard(event, false);
                        dialog.hide();
                        refreshFromController();
                        showStatusToast("[-] IMPACT SUSTAINED: DAMAGE RECORDED", CinematicTheme.STATUS_CRITICAL);
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
                content.add(desc).left().padBottom(12f).row();

                List<String> choices = res.getAvailableChoices();
                if (choices != null) {
                    for (int i = 0; i < choices.size(); i++) {
                        final int choiceIdx = i;
                        TextButton choiceBtn = new TextButton(choices.get(i), skin);
                        choiceBtn.getLabel().setFontScale(0.82f);
                        choiceBtn.addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent ev, float x, float y) {
                                String outcome = controller.resolveBeaconEncounter(event, choiceIdx);
                                dialog.hide();
                                refreshFromController();
                                showStatusToast("[+] " + outcome, CinematicTheme.STATUS_NOMINAL);
                            }
                        });
                        content.add(choiceBtn).width(380f).height(36f).padBottom(4f).row();
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
                content.add(desc).left().padBottom(12f).row();

                List<String> choices = res.getAvailableChoices();
                if (choices != null) {
                    for (int i = 0; i < choices.size(); i++) {
                        final int choiceIdx = i;
                        TextButton choiceBtn = new TextButton(choices.get(i), skin);
                        choiceBtn.getLabel().setFontScale(0.82f);
                        choiceBtn.addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent ev, float x, float y) {
                                String outcome = controller.resolveMonolithEncounter(event, choiceIdx);
                                dialog.hide();
                                refreshFromController();
                                showStatusToast("[+] " + outcome, CinematicTheme.STATUS_NOMINAL);
                            }
                        });
                        content.add(choiceBtn).width(380f).height(36f).padBottom(4f).row();
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
                dialog.getButtonTable().add(closeBtn).width(150f).height(36f);
                dialog.show(stage);
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

    private void handleSaveGame() {
        boolean saved = controller.saveGame();
        if (saved) {
            if (audioManager != null) audioManager.playConfirm();
            showStatusToast("[+] FLIGHT TELEMETRY SAVED TO saves/orbit_save.dat", CinematicTheme.STATUS_NOMINAL);
        } else {
            if (audioManager != null) audioManager.playAlert();
            showStatusToast("[-] SAVE FAILED: DISK IO ERROR", CinematicTheme.STATUS_CRITICAL);
        }
    }

    private void handleReturnToMainMenu() {
        if (audioManager != null) audioManager.playCancel();
        transitionOverlay.fadeOut(0.5f, () -> {
            game.showMainMenu();
        });
    }

    /**
     * Refreshes all gauges, labels, and badges from the authoritative OrbitController.
     */
    public void refreshFromController() {
        if (!controller.isGameActive()) return;

        Player player = controller.getPlayer();
        Spaceship ship = controller.getSpaceship();
        Planet location = controller.getCurrentLocation();
        Inventory inventory = controller.getInventory();

        // 1. Header Badges
        if (player != null) {
            commanderBadge.setText("CMDR: " + player.getCommanderName().toUpperCase());
            vesselBadge.setText("VESSEL: " + player.getSpaceshipName().toUpperCase() + " // TACTICAL BRIDGE");
        }
        if (location != null) {
            locationBadge.setText("LOC: " + location.getName().toUpperCase() + " ORBIT");
        }

        // 2. Vitals Gauges
        if (ship != null) {
            fuelGauge.setValue(ship.getFuel());
            o2Gauge.setValue(ship.getOxygen());
            energyGauge.setValue(ship.getEnergy());
            hullGauge.setValue(ship.getHullIntegrity());

            boolean isCritical = (ship.getFuel() <= Spaceship.CRITICAL_THRESHOLD
                    || ship.getOxygen() <= Spaceship.CRITICAL_THRESHOLD
                    || ship.getEnergy() <= Spaceship.CRITICAL_THRESHOLD
                    || ship.getHullIntegrity() <= Spaceship.CRITICAL_THRESHOLD);
            boolean isCaution = (ship.getFuel() <= Spaceship.CAUTION_THRESHOLD
                    || ship.getOxygen() <= Spaceship.CAUTION_THRESHOLD
                    || ship.getEnergy() <= Spaceship.CAUTION_THRESHOLD
                    || ship.getHullIntegrity() <= Spaceship.CAUTION_THRESHOLD);

            if (isCritical) {
                systemStatusBadge.setText("▲ SYSTEM CRITICAL");
                systemStatusBadge.setColor(CinematicTheme.STATUS_CRITICAL);
            } else if (isCaution) {
                systemStatusBadge.setText("▲ SYSTEM CAUTION");
                systemStatusBadge.setColor(CinematicTheme.STATUS_CAUTION);
            } else {
                systemStatusBadge.setText("● SYSTEMS NOMINAL");
                systemStatusBadge.setColor(CinematicTheme.STATUS_NOMINAL);
            }
        }

        // 3. Cargo Manifest Labels
        if (inventory != null) {
            metalLabel.setText(String.format("• Metal (Structural Repair)      : %2d units", inventory.getQuantity(Inventory.METAL)));
            energyCellsLabel.setText(String.format("• Energy Cells (Reactor Core)     : %2d units", inventory.getQuantity(Inventory.ENERGY_CELLS)));
            oxygenCanistersLabel.setText(String.format("• Oxygen Canisters (Life Support) : %2d units", inventory.getQuantity(Inventory.OXYGEN_CANISTERS)));
            fuelCellsLabel.setText(String.format("• Fuel Cells (Reaction Mass)      : %2d units", inventory.getQuantity(Inventory.FUEL_CELLS)));
            artifactsLabel.setText(String.format("• Alien Artifacts (Relics)        : %2d units", inventory.getQuantity(Inventory.ALIEN_ARTIFACTS)));
        }

        // 4. Mission Directives
        if (controller.getGameState() != null) {
            String stage = controller.getGameState().getCurrentStage();
            int progress = controller.getGameState().getMissionProgress();

            stageLabel.setText(stage.toUpperCase());
            progressLabel.setText(progress + "% COMPLETE");

            objectivesTable.clearChildren();
            List<MissionObjective> objectives = controller.getObjectives();
            if (objectives != null) {
                for (MissionObjective obj : objectives) {
                    if (obj.getStage().equalsIgnoreCase(stage)) {
                        Table row = new Table();
                        Label titleLbl = new Label("• " + obj.getTitle(), skin, "mono");
                        titleLbl.setColor(obj.isCompleted() ? CinematicTheme.TEXT_MUTED : CinematicTheme.TEXT_PRIMARY);
                        titleLbl.setFontScale(0.78f);

                        Label statusLbl = new Label(obj.isCompleted() ? "[DONE]" : "[IN PROGRESS]", skin, "mono");
                        statusLbl.setColor(obj.isCompleted() ? CinematicTheme.STATUS_NOMINAL : CinematicTheme.CYAN_AVIONICS);
                        statusLbl.setFontScale(0.75f);

                        row.add(titleLbl).left().expandX();
                        row.add(statusLbl).right();
                        objectivesTable.add(row).fillX().padBottom(3f).row();
                    }
                }
            }
        }

        // 5. Climax Button State
        if (exploreBtn != null) {
            if (controller.canInitiateFinalDirective()) {
                exploreBtn.setAccentColor(CinematicTheme.STATUS_CAUTION);
            } else {
                exploreBtn.setAccentColor(CinematicTheme.AMBER_WARM);
            }
        }
    }

    private void showStatusToast(String message, Color color) {
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
        System.out.println("[ORBIT-SCREEN] CommandCenterScreen active. Bridge avionics online.");
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
            transitionOverlay.fadeIn(0.5f, null);
        }
    }

    @Override
    public void render(float delta) {
        // 1. Update & Render 3D Bridge Environment (Interior consoles, struts, starfield, rotating planet)
        bridgeScene.update(delta);
        bridgeScene.render();

        // 2. Render 2D Diegetic Scene2D HUD Overlay
        stage.act(delta);
        stage.draw();

        frameCount++;
        logTimer += delta;
        if (logTimer >= 2.0f) {
            System.out.println(String.format("[ORBIT-RENDER] Bridge: %d frames | Target 60 FPS (Current: %d FPS)", frameCount, Gdx.graphics.getFramesPerSecond()));
            logTimer = 0f;
        }
    }

    @Override
    public void resize(int width, int height) {
        System.out.println(String.format("[ORBIT-SCREEN] Bridge window resized to %dx%d.", width, height));
        renderManager.update(width, height);
        bridgeScene.resize(width, height);
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

    public HoloButton getCargoButton() {
        return cargoBtn;
    }

    public HoloButton getExploreButton() {
        return exploreBtn;
    }

    public HoloButton getLogButton() {
        return logBtn;
    }

    public HoloButton getDirectivesButton() {
        return directivesBtn;
    }

    @Override
    public void dispose() {
        bridgeScene.dispose();
        stage.dispose();
    }
}
