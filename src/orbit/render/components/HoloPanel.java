package orbit.render.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import orbit.render.theme.CinematicTheme;

/**
 * HoloPanel is a diegetic spacecraft chassis frame.
 *
 * Provides a translucent backlit avionics housing with hardware corner brackets,
 * metallic border accents, and optional header stencils.
 */
public class HoloPanel extends Table {

    private final Drawable whiteDrawable;
    private Color bracketColor = CinematicTheme.CYAN_DIM;
    private Color panelColor = CinematicTheme.PANEL_GLASS;
    private final Table contentTable;

    public HoloPanel(Skin skin) {
        this(null, skin);
    }

    public HoloPanel(String title, Skin skin) {
        this.whiteDrawable = skin.getDrawable("white");

        align(Align.topLeft);
        pad(10f, 12f, 10f, 12f);

        if (title != null && !title.isEmpty()) {
            Table header = new Table();
            header.align(Align.left);

            Label titleLabel = new Label(title, skin, "mono");
            titleLabel.setColor(CinematicTheme.CYAN_AVIONICS);
            titleLabel.setFontScale(0.82f);
            header.add(titleLabel).left().expandX();

            Label hardwarePip = new Label("● BUS: LOCKED", skin, "mono");
            hardwarePip.setColor(CinematicTheme.STATUS_NOMINAL);
            hardwarePip.setFontScale(0.70f);
            header.add(hardwarePip).right();

            add(header).fillX().padBottom(8f).row();
        }

        contentTable = new Table();
        contentTable.align(Align.topLeft);
        add(contentTable).expand().fill();
    }

    public Table getContentTable() {
        return contentTable;
    }

    public HoloPanel setBracketColor(Color color) {
        this.bracketColor = (color != null) ? color : CinematicTheme.CYAN_DIM;
        return this;
    }

    public HoloPanel setPanelColor(Color color) {
        this.panelColor = (color != null) ? color : CinematicTheme.PANEL_GLASS;
        return this;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float x = getX();
        float y = getY();
        float w = getWidth();
        float h = getHeight();

        // 1. Draw Translucent Dark Glass Backplane
        batch.setColor(panelColor.r, panelColor.g, panelColor.b, panelColor.a * parentAlpha);
        whiteDrawable.draw(batch, x, y, w, h);

        // 2. Draw Subtle Chassis Border Lines
        batch.setColor(CinematicTheme.BEZEL_EDGE.r, CinematicTheme.BEZEL_EDGE.g, CinematicTheme.BEZEL_EDGE.b, CinematicTheme.BEZEL_EDGE.a * parentAlpha);
        whiteDrawable.draw(batch, x, y, w, 1f);          // Bottom edge
        whiteDrawable.draw(batch, x, y + h - 1f, w, 1f); // Top edge
        whiteDrawable.draw(batch, x, y, 1f, h);          // Left edge
        whiteDrawable.draw(batch, x + w - 1f, y, 1f, h); // Right edge

        // 3. Draw Hardware Stencil Corner Brackets (6px tick marks)
        batch.setColor(bracketColor.r, bracketColor.g, bracketColor.b, bracketColor.a * parentAlpha);
        float tick = 8f;
        // Top-Left
        whiteDrawable.draw(batch, x, y + h - 2f, tick, 2f);
        whiteDrawable.draw(batch, x, y + h - tick, 2f, tick);
        // Top-Right
        whiteDrawable.draw(batch, x + w - tick, y + h - 2f, tick, 2f);
        whiteDrawable.draw(batch, x + w - 2f, y + h - tick, 2f, tick);
        // Bottom-Left
        whiteDrawable.draw(batch, x, y, tick, 2f);
        whiteDrawable.draw(batch, x, y, 2f, tick);
        // Bottom-Right
        whiteDrawable.draw(batch, x + w - tick, y, tick, 2f);
        whiteDrawable.draw(batch, x + w - 2f, y, 2f, tick);

        // Reset batch
        batch.setColor(Color.WHITE);

        // Draw children
        super.draw(batch, parentAlpha);
    }
}

