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
import orbit.controllers.OrbitController;
import orbit.models.MissionLog;
import orbit.render.AssetManager;
import orbit.render.OrbitGame;
import orbit.render.RenderManager;
import orbit.render.audio.AudioManager;
import orbit.render.components.HoloPanel;
import orbit.render.components.ScreenTransitionOverlay;
import orbit.render.theme.CinematicTheme;

import java.util.ArrayList;
import java.util.List;

/**
 * MissionLogScreen provides the dedicated holographic archival interface in ORBIT.
 *
 * Implements:
 * - Chronological and reverse-chronological telemetry audit trail directly from MissionLog.
 * - Categorical filtering (All, Discovery, Hazard, Navigation, Resource, Expedition, Mission).
 * - Clear visual distinction and diegetic badge color coding for each operational category.
 * - Smooth scrollable log stream with recessed panel chassis and monospace typography.
 * - Live entry counter telemetry and total log synchronization.
 * - Bidirectional return-to-bridge navigation.
 */
public class MissionLogScreen implements Screen {

    private final OrbitGame game;
    private final OrbitController controller;
    private final AssetManager assetManager;
    private final RenderManager renderManager;
    private final AudioManager audioManager;

    private Stage stage;
    private Skin skin;
    private ScreenTransitionOverlay transitionOverlay;

    // Filter & Sort State
    private String activeCategoryFilter = "ALL";
    private boolean chronological = true; // true = oldest first, false = newest first

    // UI Elements
    private Label totalEntriesBadge;
    private Label filterStatusLabel;
    private Table logRowsTable;
    private ScrollPane scrollPane;
    private TextButton sortToggleBtn;
    private TextButton returnBtn;

    private int frameCount = 0;
    private float logTimer = 0f;

    public MissionLogScreen(OrbitGame game, OrbitController controller, AssetManager assetManager, RenderManager renderManager, AudioManager audioManager) {
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

        // --- 1. Top Avionics & Archive Header ---
        Table topBar = new Table();
        topBar.align(Align.left);

        Label deckBadge = new Label("SPACECRAFT ODYSSEY-07 // CHRONOLOGICAL FLIGHT RECORDER & MISSION ARCHIVE", skin, "mono");
        deckBadge.setColor(CinematicTheme.CYAN_AVIONICS);
        deckBadge.setFontScale(0.92f);

        totalEntriesBadge = new Label("ARCHIVE BUS: SYNCHRONIZED  |  TOTAL RECORDS: 0", skin, "mono");
        totalEntriesBadge.setColor(CinematicTheme.STATUS_NOMINAL);
        totalEntriesBadge.setFontScale(0.80f);

        topBar.add(deckBadge).left().expandX();
        topBar.add(totalEntriesBadge).right();
        rootTable.add(topBar).fillX().top().padBottom(10f).row();

        // --- 2. Filter & Sort Rail ---
        Table controlRail = new Table();
        controlRail.align(Align.left);

        Table filterButtons = new Table();
        filterButtons.align(Align.left);

        filterButtons.add(createFilterButton("ALL RECORDS", "ALL")).height(28f).padRight(6f);
        filterButtons.add(createFilterButton("DISCOVERY", "DISCOVERY")).height(28f).padRight(6f);
        filterButtons.add(createFilterButton("HAZARDS", "HAZARD")).height(28f).padRight(6f);
        filterButtons.add(createFilterButton("NAVIGATION", "NAVIGATION")).height(28f).padRight(6f);
        filterButtons.add(createFilterButton("RESOURCES", "RESOURCE")).height(28f).padRight(6f);
        filterButtons.add(createFilterButton("EXPEDITIONS", "EXPEDITION")).height(28f).padRight(6f);
        filterButtons.add(createFilterButton("MISSION", "MISSION")).height(28f);

        sortToggleBtn = new TextButton("ORDER: CHRONOLOGICAL [OLD -> NEW]", skin);
        sortToggleBtn.getLabel().setFontScale(0.75f);
        sortToggleBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (audioManager != null) audioManager.playSelect();
                chronological = !chronological;
                sortToggleBtn.setText(chronological ? "ORDER: CHRONOLOGICAL [OLD -> NEW]" : "ORDER: REVERSE [NEW -> OLD]");
                refreshFromController();
            }
        });

        controlRail.add(filterButtons).left().expandX();
        controlRail.add(sortToggleBtn).right().height(28f);
        rootTable.add(controlRail).fillX().padBottom(8f).row();

        // --- 3. Center Holographic Archival Stream HoloPanel ---
        Table centerWorkspace = new Table();
        centerWorkspace.align(Align.topLeft);

        HoloPanel archivePanel = new HoloPanel("HISTORICAL TELEMETRY AUDIT TRAIL", skin);
        archivePanel.setBracketColor(CinematicTheme.CYAN_AVIONICS);
        Table panelContent = archivePanel.getContentTable();
        panelContent.align(Align.topLeft);

        logRowsTable = new Table();
        logRowsTable.align(Align.topLeft);

        scrollPane = new ScrollPane(logRowsTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        panelContent.add(scrollPane).width(1200f).height(460f).fill().row();

        centerWorkspace.add(archivePanel).width(1228f).height(510f);
        rootTable.add(centerWorkspace).expand().fill().padBottom(10f).row();

        // --- 4. Bottom Operations Rail ---
        Table bottomRail = new Table();
        filterStatusLabel = new Label("FILTER: ALL RECORDS // DISPLAYING ALL TELEMETRY ENTRIES", skin, "mono");
        filterStatusLabel.setColor(CinematicTheme.TEXT_MUTED);
        filterStatusLabel.setFontScale(0.80f);

        returnBtn = new TextButton("RETURN TO BRIDGE", skin);
        returnBtn.getLabel().setFontScale(0.82f);
        returnBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleReturnToBridge();
            }
        });

        bottomRail.add(filterStatusLabel).left().expandX();
        bottomRail.add(returnBtn).width(190f).height(38f).right();
        rootTable.add(bottomRail).fillX().bottom();

        stage.addActor(rootTable);

        // Screen Transition Overlay
        transitionOverlay = new ScreenTransitionOverlay(skin.getDrawable("white"));
        transitionOverlay.setFillParent(true);
        stage.addActor(transitionOverlay);
        transitionOverlay.fadeIn(0.4f, null);
    }

    private TextButton createFilterButton(String label, String filterVal) {
        TextButton btn = new TextButton(label, skin);
        btn.getLabel().setFontScale(0.75f);
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (audioManager != null) audioManager.playSelect();
                activeCategoryFilter = filterVal;
                refreshFromController();
            }
        });
        return btn;
    }

    public void refreshFromController() {
        if (!controller.isGameActive()) return;

        MissionLog missionLog = controller.getMissionLog();
        List<MissionLog.LogEntry> allEntries = (missionLog != null) ? missionLog.getEntries() : List.of();
        int totalCount = allEntries.size();

        totalEntriesBadge.setText(String.format("ARCHIVE BUS: SYNCHRONIZED  |  TOTAL RECORDS: %d", totalCount));

        // Filter and sort entries
        List<MissionLog.LogEntry> displayList = new ArrayList<>();
        if (chronological) {
            for (MissionLog.LogEntry entry : allEntries) {
                if (matchesFilter(entry)) {
                    displayList.add(entry);
                }
            }
        } else {
            List<MissionLog.LogEntry> reversed = (missionLog != null) ? missionLog.getEntriesNewestFirst() : List.of();
            for (MissionLog.LogEntry entry : reversed) {
                if (matchesFilter(entry)) {
                    displayList.add(entry);
                }
            }
        }

        filterStatusLabel.setText(String.format("FILTER: %s // DISPLAYING %d OF %d TOTAL ARCHIVE RECORDS",
                activeCategoryFilter.toUpperCase(), displayList.size(), totalCount));

        // Rebuild log rows
        logRowsTable.clear();

        if (displayList.isEmpty()) {
            Table emptyRow = new Table();
            emptyRow.align(Align.left);
            Label emptyLbl = new Label("No historical telemetry entries match the current filter criteria.", skin, "mono");
            emptyLbl.setColor(CinematicTheme.TEXT_MUTED);
            emptyLbl.setFontScale(0.80f);
            emptyRow.add(emptyLbl).left().pad(14f);
            logRowsTable.add(emptyRow).fillX().row();
            return;
        }

        for (int i = 0; i < displayList.size(); i++) {
            MissionLog.LogEntry entry = displayList.get(i);
            Table row = new Table();
            row.align(Align.left);
            row.pad(5f, 8f, 5f, 8f);

            // Alternating subtle zebra striping
            Color rowBg = (i % 2 == 0) ? CinematicTheme.CHASSIS_PLATE : CinematicTheme.PANEL_GLASS;
            row.setBackground(skin.newDrawable("white", rowBg));

            // 1. Timestamp badge
            Label timeLbl = new Label("[" + entry.getTimestamp() + "]", skin, "mono");
            timeLbl.setColor(CinematicTheme.CYAN_DIM);
            timeLbl.setFontScale(0.76f);
            row.add(timeLbl).width(95f).left();

            // 2. Category badge
            Label catLbl = new Label("[" + entry.getCategory() + "]", skin, "mono");
            catLbl.setColor(getCategoryColor(entry.getCategory()));
            catLbl.setFontScale(0.78f);
            row.add(catLbl).width(135f).left();

            // 3. Narrative description
            Label descLbl = new Label(entry.getDescription(), skin, "mono");
            descLbl.setColor(CinematicTheme.TEXT_PRIMARY);
            descLbl.setFontScale(0.78f);
            descLbl.setWrap(true);
            row.add(descLbl).width(960f).left().expandX();

            logRowsTable.add(row).fillX().padBottom(3f).row();
        }
    }

    private boolean matchesFilter(MissionLog.LogEntry entry) {
        if (entry == null) return false;
        if (activeCategoryFilter.equals("ALL")) return true;
        String cat = entry.getCategory().toUpperCase();
        if (activeCategoryFilter.equalsIgnoreCase("MISSION")) {
            return cat.equals("MISSION") || cat.equals("SIGNAL") || cat.equals("ENCOUNTER");
        }
        return cat.equalsIgnoreCase(activeCategoryFilter);
    }

    private Color getCategoryColor(String cat) {
        if (cat == null) return CinematicTheme.TEXT_MUTED;
        return switch (cat.toUpperCase()) {
            case "DISCOVERY" -> CinematicTheme.AMBER_WARM;
            case "HAZARD" -> CinematicTheme.STATUS_CRITICAL;
            case "NAVIGATION" -> CinematicTheme.CYAN_AVIONICS;
            case "RESOURCE", "INVENTORY" -> CinematicTheme.SOLAR_GOLD;
            case "EXPEDITION" -> CinematicTheme.STATUS_NOMINAL;
            case "SIGNAL", "ENCOUNTER" -> CinematicTheme.CYAN_AVIONICS;
            case "MISSION" -> CinematicTheme.SOLAR_GOLD;
            default -> CinematicTheme.TEXT_SECONDARY;
        };
    }

    public void handleReturnToBridge() {
        if (audioManager != null) audioManager.playCancel();
        transitionOverlay.fadeOut(0.4f, () -> {
            game.showCommandCenter();
        });
    }

    @Override
    public void show() {
        System.out.println("[ORBIT-SCREEN] MissionLogScreen active. Historical archive online.");
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
        Gdx.gl.glClearColor(0.024f, 0.035f, 0.055f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        frameCount++;
        logTimer += delta;
        if (logTimer >= 2.0f) {
            System.out.println(String.format("[ORBIT-RENDER] MissionLog: %d frames | Target 60 FPS (Current: %d FPS)", frameCount, Gdx.graphics.getFramesPerSecond()));
            logTimer = 0f;
        }
    }

    @Override
    public void resize(int width, int height) {
        System.out.println(String.format("[ORBIT-SCREEN] Mission Log window resized to %dx%d.", width, height));
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

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public TextButton getSortToggleButton() {
        return sortToggleBtn;
    }

    public String getActiveCategoryFilter() {
        return activeCategoryFilter;
    }

    public void setActiveCategoryFilter(String filter) {
        this.activeCategoryFilter = (filter != null) ? filter : "ALL";
        refreshFromController();
    }

    public boolean isChronological() {
        return chronological;
    }

    public void setChronological(boolean chrono) {
        this.chronological = chrono;
        if (sortToggleBtn != null) {
            sortToggleBtn.setText(chrono ? "ORDER: CHRONOLOGICAL [OLD -> NEW]" : "ORDER: REVERSE [NEW -> OLD]");
        }
        refreshFromController();
    }

    public int getDisplayedEntryCount() {
        if (!controller.isGameActive() || controller.getMissionLog() == null) return 0;
        int count = 0;
        for (MissionLog.LogEntry entry : controller.getMissionLog().getEntries()) {
            if (matchesFilter(entry)) count++;
        }
        return count;
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }
}

