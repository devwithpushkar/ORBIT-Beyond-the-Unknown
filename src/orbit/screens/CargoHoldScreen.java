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
import orbit.controllers.ItemUseResult;
import orbit.controllers.OrbitController;
import orbit.models.Inventory;
import orbit.models.Spaceship;
import orbit.render.AssetManager;
import orbit.render.CargoSceneManager;
import orbit.render.OrbitGame;
import orbit.render.RenderManager;
import orbit.render.audio.AudioManager;
import orbit.render.components.HoloPanel;
import orbit.render.components.ScreenTransitionOverlay;
import orbit.render.components.ShipResourceGauge;
import orbit.render.theme.CinematicTheme;

import java.util.HashMap;
import java.util.Map;

/**
 * CargoHoldScreen provides the dedicated 3D Cargo Hold experience in ORBIT.
 *
 * Implements:
 * - 3D industrial cargo-bay environment with physical storage pods and floating alien relic.
 * - Live vitals telemetry rack for all 4 authoritative ship resources.
 * - 5 interactive cargo containment bays:
 *   1. Metal (+20% Hull Integrity)
 *   2. Energy Cells (+25% Reactor Energy)
 *   3. Oxygen Canisters (+25% Life Support O2)
 *   4. Fuel Cells (+25% Propulsion Fuel)
 *   5. Alien Artifacts (Non-consumable scientific stasis vault)
 * - Immediate controller validation, consumption execution, and synchronized feedback.
 * - Direct return-to-bridge transition returning cleanly to Command Center.
 */
public class CargoHoldScreen implements Screen {

    private final OrbitGame game;
    private final OrbitController controller;
    private final AssetManager assetManager;
    private final RenderManager renderManager;
    private final AudioManager audioManager;

    private CargoSceneManager sceneManager;
    private Stage stage;
    private Skin skin;
    private ScreenTransitionOverlay transitionOverlay;

    // Vitals Gauges
    private ShipResourceGauge fuelGauge;
    private ShipResourceGauge o2Gauge;
    private ShipResourceGauge energyGauge;
    private ShipResourceGauge hullGauge;

    // Manifest UI References
    private final Map<String, Label> stockLabels = new HashMap<>();
    private final Map<String, Label> targetStatusLabels = new HashMap<>();
    private final Map<String, TextButton> actionButtons = new HashMap<>();

    private Label statusFeedbackLabel;
    private TextButton returnBtn;

    private int frameCount = 0;
    private float logTimer = 0f;

    public CargoHoldScreen(OrbitGame game, OrbitController controller, AssetManager assetManager, RenderManager renderManager, AudioManager audioManager) {
        this.game = game;
        this.controller = controller;
        this.assetManager = assetManager;
        this.renderManager = renderManager;
        this.audioManager = (audioManager != null) ? audioManager : new AudioManager();

        this.sceneManager = new CargoSceneManager();
        this.skin = assetManager.getUiSkin();
        this.stage = new Stage(renderManager.getUiViewport(), renderManager.getSpriteBatch());

        buildInterface();
        refreshFromController();
    }

    private void buildInterface() {
        stage.clear();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.pad(14f, 24f, 14f, 24f);

        // --- 1. Top Avionics & Environmental Header ---
        Table topBar = new Table();
        topBar.align(Align.left);

        Label deckBadge = new Label("SPACECRAFT ODYSSEY-07 // LOWER CARGO DECK - BAY 4", skin, "mono");
        deckBadge.setColor(CinematicTheme.AMBER_WARM);
        deckBadge.setFontScale(0.90f);

        Label envStatus = new Label("ATMOSPHERE: 1.0 ATM (O2/N2)  |  MAG-LOCKS: SECURED  |  GRAVITY: 0.88G", skin, "mono");
        envStatus.setColor(CinematicTheme.STATUS_NOMINAL);
        envStatus.setFontScale(0.80f);

        topBar.add(deckBadge).left().expandX();
        topBar.add(envStatus).right();
        rootTable.add(topBar).fillX().top().padBottom(8f).row();

        // --- 2. Live Vitals Telemetry Rack ---
        Table vitalsRack = new Table();
        vitalsRack.align(Align.center);
        vitalsRack.pad(4f, 8f, 4f, 8f);

        fuelGauge = new ShipResourceGauge(ShipResourceGauge.ResourceType.FUEL, skin);
        o2Gauge = new ShipResourceGauge(ShipResourceGauge.ResourceType.OXYGEN, skin);
        energyGauge = new ShipResourceGauge(ShipResourceGauge.ResourceType.ENERGY, skin);
        hullGauge = new ShipResourceGauge(ShipResourceGauge.ResourceType.HULL, skin);

        float gaugeW = 285f;
        vitalsRack.add(fuelGauge).width(gaugeW).padRight(12f);
        vitalsRack.add(o2Gauge).width(gaugeW).padRight(12f);
        vitalsRack.add(energyGauge).width(gaugeW).padRight(12f);
        vitalsRack.add(hullGauge).width(gaugeW);

        rootTable.add(vitalsRack).fillX().padBottom(10f).row();

        // --- 3. Center Workspace: 5 Cargo Manifest Bay Cards ---
        Table baysGrid = new Table();
        baysGrid.align(Align.top);

        baysGrid.add(createCargoModule(Inventory.METAL, "[CRG-M01]", "REINFORCED ALLOY", "+20% Hull Integrity", CinematicTheme.STEEL_BLUE)).width(235f).padRight(8f);
        baysGrid.add(createCargoModule(Inventory.ENERGY_CELLS, "[CRG-E02]", "ENERGY CELLS", "+25% Reactor Energy", CinematicTheme.SOLAR_GOLD)).width(235f).padRight(8f);
        baysGrid.add(createCargoModule(Inventory.OXYGEN_CANISTERS, "[CRG-O03]", "O2 CANISTERS", "+25% Life Support O2", CinematicTheme.OXYGEN_AQUA)).width(235f).padRight(8f);
        baysGrid.add(createCargoModule(Inventory.FUEL_CELLS, "[CRG-F04]", "FUEL CELLS", "+25% Reaction Fuel", CinematicTheme.AMBER_WARM)).width(235f).padRight(8f);
        baysGrid.add(createCargoModule(Inventory.ALIEN_ARTIFACTS, "[CRG-X05]", "ALIEN ARTIFACTS", "Relic Data / Scientific Anomaly", CinematicTheme.SOLAR_GOLD)).width(235f);

        rootTable.add(baysGrid).expand().fill().padBottom(10f).row();

        // --- 4. Bottom Operations Rail ---
        Table bottomRail = new Table();
        statusFeedbackLabel = new Label("CARGO DISPENSER ONLINE // SELECT STORAGE POD TO TRANSFER SUPPLIES", skin, "mono");
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

    private Table createCargoModule(String itemName, String tag, String title, String effectDesc, Color accent) {
        HoloPanel panel = new HoloPanel(tag, skin);
        panel.setBracketColor(accent);
        Table c = panel.getContentTable();
        c.align(Align.topLeft);

        // Title
        Label titleLbl = new Label(title, skin, "subtitle");
        titleLbl.setColor(CinematicTheme.TEXT_PRIMARY);
        titleLbl.setFontScale(0.88f);
        c.add(titleLbl).left().padBottom(3f).row();

        // Effect
        Label effectLbl = new Label("• " + effectDesc, skin, "mono");
        effectLbl.setColor(accent);
        effectLbl.setFontScale(0.76f);
        c.add(effectLbl).left().padBottom(10f).row();

        // Stock Badge Box
        Table stockBox = new Table();
        stockBox.align(Align.left);
        Label stockLbl = new Label("STOCK: 0 UNITS", skin, "mono");
        stockLbl.setFontScale(0.85f);
        stockBox.add(stockLbl).left();
        stockLabels.put(itemName, stockLbl);
        c.add(stockBox).left().padBottom(6f).row();

        // Target Resource Status
        Label targetStatusLbl = new Label("STATUS: OPTIMAL", skin, "mono");
        targetStatusLbl.setColor(CinematicTheme.TEXT_MUTED);
        targetStatusLbl.setFontScale(0.76f);
        targetStatusLabels.put(itemName, targetStatusLbl);
        c.add(targetStatusLbl).left().padBottom(14f).row();

        // Dispense / Inspect Action Button
        TextButton btn = new TextButton(itemName.equals(Inventory.ALIEN_ARTIFACTS) ? "STASIS SEALED" : "DISPENSE", skin);
        btn.getLabel().setFontScale(0.82f);
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleDispenseItem(itemName);
            }
        });
        actionButtons.put(itemName, btn);
        c.add(btn).fillX().height(36f).bottom();

        return panel;
    }

    public void handleDispenseItem(String itemName) {
        if (!controller.isGameActive()) return;

        // Alien Artifacts are non-consumable relics
        if (Inventory.ALIEN_ARTIFACTS.equalsIgnoreCase(itemName)) {
            if (audioManager != null) audioManager.playAlert();
            int qty = controller.getInventory() != null ? controller.getInventory().getQuantity(Inventory.ALIEN_ARTIFACTS) : 0;
            showStatusFeedback(
                    String.format("[!] XENOLOGICAL STASIS: %d Artifacts preserved in stasis vault. Non-consumable scientific anomaly.", qty),
                    CinematicTheme.SOLAR_GOLD
            );
            sceneManager.setHighlightedCategory(Inventory.ALIEN_ARTIFACTS);
            return;
        }

        // Check availability before consumption
        Inventory inv = controller.getInventory();
        int qty = (inv != null) ? inv.getQuantity(itemName) : 0;
        if (qty <= 0) {
            if (audioManager != null) audioManager.playAlert();
            showStatusFeedback("[-] INSUFFICIENT SUPPLIES: 0 " + itemName.toUpperCase() + " REMAINING IN HOLD", CinematicTheme.STATUS_CRITICAL);
            return;
        }

        // Execute item consumption via OrbitController
        ItemUseResult res = controller.useItem(itemName);
        if (res.isSuccess()) {
            if (audioManager != null) audioManager.playConfirm();
            showStatusFeedback(
                    String.format("[+] DISPENSED 1 %s: %s RESTORED TO %d%%",
                            itemName.toUpperCase(), res.getTargetResource().toUpperCase(), res.getNewValue()),
                    CinematicTheme.STATUS_NOMINAL
            );
            sceneManager.setHighlightedCategory(itemName);
        } else {
            if (audioManager != null) audioManager.playAlert();
            showStatusFeedback("[-] DISPENSE REJECTED: " + res.getMessage(), CinematicTheme.STATUS_CAUTION);
        }

        // Refresh all gauges and cards immediately
        refreshFromController();
    }

    public void handleReturnToBridge() {
        if (audioManager != null) audioManager.playCancel();
        transitionOverlay.fadeOut(0.4f, () -> {
            game.showCommandCenter();
        });
    }

    public void refreshFromController() {
        if (!controller.isGameActive()) return;

        Spaceship ship = controller.getSpaceship();
        Inventory inv = controller.getInventory();

        // 1. Update Live Telemetry Gauges
        if (ship != null) {
            fuelGauge.setValue(ship.getFuel());
            o2Gauge.setValue(ship.getOxygen());
            energyGauge.setValue(ship.getEnergy());
            hullGauge.setValue(ship.getHullIntegrity());
        }

        // 2. Update Manifest Cards
        updateModuleCard(Inventory.METAL, inv, ship != null ? ship.getHullIntegrity() : 100, "+20% HULL");
        updateModuleCard(Inventory.ENERGY_CELLS, inv, ship != null ? ship.getEnergy() : 100, "+25% ENG");
        updateModuleCard(Inventory.OXYGEN_CANISTERS, inv, ship != null ? ship.getOxygen() : 100, "+25% O2");
        updateModuleCard(Inventory.FUEL_CELLS, inv, ship != null ? ship.getFuel() : 100, "+25% FUEL");

        // Alien Artifacts
        int artQty = (inv != null) ? inv.getQuantity(Inventory.ALIEN_ARTIFACTS) : 0;
        Label artStock = stockLabels.get(Inventory.ALIEN_ARTIFACTS);
        if (artStock != null) {
            artStock.setText(String.format("IN STASIS: %2d UNITS", artQty));
            artStock.setColor(artQty > 0 ? CinematicTheme.SOLAR_GOLD : CinematicTheme.TEXT_MUTED);
        }
        Label artStatus = targetStatusLabels.get(Inventory.ALIEN_ARTIFACTS);
        if (artStatus != null) {
            artStatus.setText("CLASSIFICATION: EXOTIC");
            artStatus.setColor(CinematicTheme.SOLAR_GOLD);
        }
        TextButton artBtn = actionButtons.get(Inventory.ALIEN_ARTIFACTS);
        if (artBtn != null) {
            artBtn.setText("XENOLOGICAL VAULT");
            artBtn.setDisabled(false); // Can be clicked to view scientific relic advisory
        }
    }

    private void updateModuleCard(String itemName, Inventory inv, int currentResourceVal, String effectShort) {
        int qty = (inv != null) ? inv.getQuantity(itemName) : 0;
        boolean canAbsorb = (currentResourceVal < Spaceship.MAX_RESOURCE);
        boolean canUse = (qty > 0 && canAbsorb);

        // Stock Label
        Label stockLbl = stockLabels.get(itemName);
        if (stockLbl != null) {
            stockLbl.setText(String.format("STOCK: %2d UNITS", qty));
            stockLbl.setColor(qty > 0 ? CinematicTheme.TEXT_PRIMARY : CinematicTheme.STATUS_CRITICAL);
        }

        // Target Status
        Label statusLbl = targetStatusLabels.get(itemName);
        if (statusLbl != null) {
            if (currentResourceVal >= 100) {
                statusLbl.setText("TARGET: 100% [OPTIMAL]");
                statusLbl.setColor(CinematicTheme.STATUS_NOMINAL);
            } else {
                statusLbl.setText(String.format("TARGET: %d%% (%s)", currentResourceVal, effectShort));
                statusLbl.setColor(CinematicTheme.AMBER_WARM);
            }
        }

        // Button State
        TextButton btn = actionButtons.get(itemName);
        if (btn != null) {
            if (qty <= 0) {
                btn.setText("DEPLETED (0)");
                btn.setDisabled(true);
            } else if (!canAbsorb) {
                btn.setText("SYSTEM AT 100%");
                btn.setDisabled(true);
            } else {
                btn.setText("DISPENSE (" + effectShort + ")");
                btn.setDisabled(false);
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
        System.out.println("[ORBIT-SCREEN] CargoHoldScreen active. 3D cargo deck avionics online.");
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
        // 1. Update & Render 3D Cargo Hold Environment
        sceneManager.update(delta);
        sceneManager.render();

        // 2. Render 2D Diegetic HUD Overlay
        stage.act(delta);
        stage.draw();

        frameCount++;
        logTimer += delta;
        if (logTimer >= 2.0f) {
            System.out.println(String.format("[ORBIT-RENDER] Cargo: %d frames | Target 60 FPS (Current: %d FPS)", frameCount, Gdx.graphics.getFramesPerSecond()));
            logTimer = 0f;
        }
    }

    @Override
    public void resize(int width, int height) {
        System.out.println(String.format("[ORBIT-SCREEN] Cargo bay window resized to %dx%d.", width, height));
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

    public TextButton getReturnButton() {
        return returnBtn;
    }

    public TextButton getDispenseButton(String itemName) {
        return actionButtons.get(itemName);
    }

    public CargoSceneManager getSceneManager() {
        return sceneManager;
    }

    @Override
    public void dispose() {
        if (sceneManager != null) sceneManager.dispose();
        if (stage != null) stage.dispose();
    }
}

