package orbit.render.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import orbit.render.audio.AudioManager;
import orbit.render.theme.CinematicTheme;

/**
 * HoloButton is a diegetic spacecraft flight switch.
 *
 * Replaces generic rectangular flat buttons with an authentic avionics flight control:
 * - Left edge active-state indicator pip.
 * - Hardware index/callsign micro-stencil tag (e.g. [EXP-01]).
 * - Bold primary operational title.
 * - Monospace explanatory mission descriptor subtitle.
 * - Dynamic tactile hover/press animations and integrated sound triggers.
 */
public class HoloButton extends Table {

    private final String tag;
    private final String title;
    private final String subtitle;
    private final AudioManager audio;

    private boolean disabled = false;
    private boolean hovered = false;
    private boolean pressed = false;

    private Label tagLabel;
    private Label titleLabel;
    private Label subtitleLabel;
    private Label chevronLabel;
    private Table indicatorPip;

    private Color accentColor = CinematicTheme.AMBER_WARM;
    private Runnable clickAction;

    public HoloButton(String tag, String title, String subtitle, Skin skin, AudioManager audio, Runnable onClick) {
        this.tag = tag;
        this.title = title;
        this.subtitle = subtitle;
        this.audio = audio;
        this.clickAction = onClick;

        buildWidget(skin);
        setupInteractions();
    }

    public HoloButton setAccentColor(Color color) {
        this.accentColor = (color != null) ? color : CinematicTheme.AMBER_WARM;
        updateVisualState();
        return this;
    }

    private void buildWidget(Skin skin) {
        clearChildren();
        setTouchable(Touchable.enabled);
        align(Align.left);
        pad(6f, 10f, 6f, 14f);

        // Background chassis plate
        Drawable bg = skin.getDrawable("white");
        setBackground(bg);
        setColor(CinematicTheme.CHASSIS_PLATE);

        // 1. Left Edge Indicator Pip (Vertical status pip)
        indicatorPip = new Table();
        indicatorPip.setBackground(skin.getDrawable("white"));
        indicatorPip.setColor(CinematicTheme.BEZEL_EDGE);
        indicatorPip.setTouchable(Touchable.disabled);
        add(indicatorPip).width(4f).fillY().padRight(12f);

        // 2. Center Column: Tag, Title, Subtitle
        Table textColumn = new Table();
        textColumn.align(Align.left);
        textColumn.setTouchable(Touchable.disabled);

        tagLabel = new Label(tag, skin, "mono");
        tagLabel.setColor(CinematicTheme.TEXT_MUTED);
        tagLabel.setFontScale(0.85f);
        tagLabel.setTouchable(Touchable.disabled);

        titleLabel = new Label(title, skin, "subtitle");
        titleLabel.setColor(CinematicTheme.TEXT_PRIMARY);
        titleLabel.setTouchable(Touchable.disabled);

        subtitleLabel = new Label(subtitle, skin, "mono");
        subtitleLabel.setColor(CinematicTheme.TEXT_MUTED);
        subtitleLabel.setFontScale(0.80f);
        subtitleLabel.setTouchable(Touchable.disabled);

        textColumn.add(tagLabel).left().row();
        textColumn.add(titleLabel).left().padTop(1f).padBottom(2f).row();
        textColumn.add(subtitleLabel).left();

        add(textColumn).expandX().left();

        // 3. Right Flight Vector Chevron
        chevronLabel = new Label("›", skin, "subtitle");
        chevronLabel.setColor(CinematicTheme.TEXT_DIM);
        chevronLabel.setTouchable(Touchable.disabled);
        add(chevronLabel).right().padRight(4f);
    }

    private void setupInteractions() {
        addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                if (!disabled && pointer == -1) {
                    hovered = true;
                    updateVisualState();
                    if (audio != null) audio.playHover();
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (!disabled && pointer == -1) {
                    hovered = false;
                    pressed = false;
                    updateVisualState();
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (disabled) return false;
                boolean handled = super.touchDown(event, x, y, pointer, button);
                if (handled) {
                    pressed = true;
                    updateVisualState();
                }
                return handled;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if (!disabled) {
                    pressed = false;
                    updateVisualState();
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!disabled && clickAction != null) {
                    if (audio != null) audio.playSelect();
                    // Subtle click tactile feedback animation
                    clearActions();
                    addAction(Actions.sequence(
                            Actions.color(accentColor, 0.05f),
                            Actions.color(CinematicTheme.PANEL_GLASS, 0.12f)
                    ));
                    clickAction.run();
                }
            }
        });
    }

    private void updateVisualState() {
        if (disabled) {
            setColor(new Color(0.04f, 0.06f, 0.08f, 0.45f));
            indicatorPip.setColor(new Color(0.15f, 0.20f, 0.25f, 0.3f));
            tagLabel.setColor(CinematicTheme.TEXT_DIM);
            titleLabel.setColor(CinematicTheme.TEXT_DIM);
            subtitleLabel.setColor(CinematicTheme.TEXT_DIM);
            chevronLabel.setColor(CinematicTheme.TEXT_DIM);
            return;
        }

        if (pressed) {
            setColor(new Color(0.12f, 0.18f, 0.26f, 0.95f));
            indicatorPip.setColor(accentColor);
            titleLabel.setColor(accentColor);
            chevronLabel.setColor(accentColor);
        } else if (hovered) {
            setColor(new Color(0.08f, 0.13f, 0.20f, 0.95f));
            indicatorPip.setColor(accentColor);
            titleLabel.setColor(accentColor);
            chevronLabel.setColor(CinematicTheme.TEXT_PRIMARY);
            tagLabel.setColor(CinematicTheme.TEXT_SECONDARY);
            subtitleLabel.setColor(CinematicTheme.TEXT_SECONDARY);
        } else {
            setColor(CinematicTheme.CHASSIS_PLATE);
            indicatorPip.setColor(CinematicTheme.BEZEL_EDGE);
            titleLabel.setColor(CinematicTheme.TEXT_PRIMARY);
            chevronLabel.setColor(CinematicTheme.TEXT_DIM);
            tagLabel.setColor(CinematicTheme.TEXT_MUTED);
            subtitleLabel.setColor(CinematicTheme.TEXT_MUTED);
        }
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        setTouchable(disabled ? Touchable.disabled : Touchable.enabled);
        updateVisualState();
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setClickAction(Runnable action) {
        this.clickAction = action;
    }

    public Runnable getClickAction() {
        return clickAction;
    }
}

