package orbit.render.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import orbit.render.theme.CinematicTheme;

/**
 * ShipResourceGauge is an authentic diegetic telemetry instrument for ORBIT's
 * four authoritative ship resources: Fuel, Oxygen, Energy, and Hull Integrity.
 *
 * Features:
 * - Segmented optical LED bar (20 hardware segments @ 5% each).
 * - Smooth animated numeric interpolation (prevents jarring value jumps).
 * - Multi-tier telemetry alerts (Nominal >= 50%, Caution 25-49%, Critical < 25%).
 * - Flashing strobe effect for critical resource warnings.
 */
public class ShipResourceGauge extends Widget {

    public enum ResourceType {
        FUEL("FUEL // REACTION MASS", CinematicTheme.AMBER_WARM),
        OXYGEN("O2 // LIFE SUPPORT", CinematicTheme.OXYGEN_AQUA),
        ENERGY("ENERGY // FUSION CORE", CinematicTheme.SOLAR_GOLD),
        HULL("HULL // TITANIUM MATRIX", CinematicTheme.STEEL_BLUE);

        public final String label;
        public final Color primaryColor;

        ResourceType(String label, Color color) {
            this.label = label;
            this.primaryColor = color;
        }
    }

    private final ResourceType type;
    private final Drawable whiteDrawable;
    private final BitmapFont titleFont;
    private final BitmapFont monoFont;
    private final GlyphLayout layout = new GlyphLayout();

    private int targetValue = 100;
    private float displayValue = 100f;
    private float strobeTimer = 0f;

    public ShipResourceGauge(ResourceType type, Skin skin) {
        this.type = type;
        this.whiteDrawable = skin.getDrawable("white");
        this.titleFont = skin.getFont("mono-font");
        this.monoFont = skin.getFont("mono-font");
    }

    public void setValue(int value) {
        this.targetValue = MathUtils.clamp(value, 0, 100);
    }

    public int getValue() {
        return targetValue;
    }

    @Override
    public float getPrefWidth() {
        return 340f;
    }

    @Override
    public float getPrefHeight() {
        return 36f;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // Smooth numeric interpolation towards target value
        if (Math.abs(displayValue - targetValue) > 0.1f) {
            displayValue += (targetValue - displayValue) * Math.min(1.0f, delta * 9.0f);
        } else {
            displayValue = targetValue;
        }

        // Strobe animation for low resource warnings
        if (targetValue < 25) {
            strobeTimer += delta * 4.0f;
        } else {
            strobeTimer = 0f;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();

        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        // 1. Draw Top Header: Resource Label (Left) and Percentage (Right)
        String headerText = type.label;
        int currentPercent = Math.round(displayValue);
        String percentText = currentPercent + "%";

        Color activeColor = getActiveColor();

        // Header Label
        float origScaleX = monoFont.getData().scaleX;
        float origScaleY = monoFont.getData().scaleY;
        monoFont.setColor(CinematicTheme.TEXT_MUTED);
        monoFont.getData().setScale(0.78f);
        monoFont.draw(batch, headerText, x, y + height);

        // Percentage Readout
        monoFont.setColor(activeColor);
        layout.setText(monoFont, percentText);
        monoFont.draw(batch, percentText, x + width - layout.width, y + height);
        monoFont.getData().setScale(origScaleX, origScaleY); // Restore exact original scale

        // 2. Draw Chassis Track Frame for the Segmented Bar
        float trackY = y + 4f;
        float trackHeight = 12f;

        // Dim background track
        batch.setColor(0.04f, 0.07f, 0.11f, 0.70f * parentAlpha);
        whiteDrawable.draw(batch, x, trackY, width, trackHeight);

        // 3. Draw 20 Hardware LED Segments
        int totalSegments = 20;
        float gap = 2.0f;
        float totalGaps = (totalSegments - 1) * gap;
        float segmentWidth = (width - totalGaps) / totalSegments;

        int litSegments = Math.round((displayValue / 100f) * totalSegments);

        // Strobe multiplier for critical state
        float alphaMod = 1.0f;
        if (targetValue < 25) {
            alphaMod = (MathUtils.sin(strobeTimer) > 0f) ? 1.0f : 0.25f;
        }

        for (int i = 0; i < totalSegments; i++) {
            float segX = x + i * (segmentWidth + gap);

            if (i < litSegments) {
                // Lit segment
                batch.setColor(activeColor.r, activeColor.g, activeColor.b, activeColor.a * parentAlpha * alphaMod);
                whiteDrawable.draw(batch, segX, trackY + 1f, segmentWidth, trackHeight - 2f);
            } else {
                // Unlit segment slot
                batch.setColor(0.08f, 0.12f, 0.18f, 0.40f * parentAlpha);
                whiteDrawable.draw(batch, segX, trackY + 1f, segmentWidth, trackHeight - 2f);
            }
        }

        // Reset batch color
        batch.setColor(Color.WHITE);
    }

    private Color getActiveColor() {
        if (targetValue < 25) {
            return CinematicTheme.STATUS_CRITICAL;
        } else if (targetValue < 50) {
            return CinematicTheme.STATUS_CAUTION;
        } else {
            return type.primaryColor;
        }
    }
}

