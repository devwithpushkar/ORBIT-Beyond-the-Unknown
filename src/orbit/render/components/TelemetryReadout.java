package orbit.render.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import orbit.render.theme.CinematicTheme;

/**
 * TelemetryReadout provides a diegetic instrument display for avionics,
 * mission parameters, spatial coordinates, and flight directives.
 */
public class TelemetryReadout extends Table {

    private final Label titleLabel;
    private final Label valueLabel;
    private final Label statusBadge;

    public TelemetryReadout(String title, String initialValue, Skin skin) {
        this(title, initialValue, null, skin);
    }

    public TelemetryReadout(String title, String initialValue, String status, Skin skin) {
        align(Align.left);
        pad(4f, 8f, 4f, 8f);

        // Header Label
        titleLabel = new Label(title, skin, "mono");
        titleLabel.setColor(CinematicTheme.TEXT_MUTED);
        titleLabel.setFontScale(0.75f);
        add(titleLabel).left().row();

        // Value & Status Row
        Table valueRow = new Table();
        valueRow.align(Align.left);

        valueLabel = new Label(initialValue, skin, "mono");
        valueLabel.setColor(CinematicTheme.CYAN_AVIONICS);
        valueLabel.setFontScale(0.95f);
        valueRow.add(valueLabel).left().expandX();

        statusBadge = new Label(status != null ? status : "", skin, "mono");
        statusBadge.setColor(CinematicTheme.STATUS_NOMINAL);
        statusBadge.setFontScale(0.75f);
        if (status != null && !status.isEmpty()) {
            valueRow.add(statusBadge).right().padLeft(8f);
        }

        add(valueRow).fillX().left().padTop(2f);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setValue(String value, Color color) {
        valueLabel.setText(value);
        valueLabel.setColor(color);
    }

    public void setStatus(String status, Color color) {
        statusBadge.setText(status);
        statusBadge.setColor(color);
    }
}

