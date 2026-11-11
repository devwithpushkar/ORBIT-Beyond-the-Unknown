package orbit.render.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import orbit.render.theme.CinematicTheme;

/**
 * ScreenTransitionOverlay provides smooth cinematic fades and scanline
 * transitions between game screens.
 */
public class ScreenTransitionOverlay extends Table {

    private final Drawable whiteDrawable;
    private Color overlayColor = new Color(CinematicTheme.VOID_BLACK);

    public ScreenTransitionOverlay(Drawable whiteDrawable) {
        this.whiteDrawable = whiteDrawable;
        this.overlayColor.a = 0f;
        setFillParent(true);
        setTouchable(Touchable.disabled);
        setVisible(false);
    }

    public void fadeIn(float duration, Runnable onComplete) {
        setVisible(true);
        clearActions();
        overlayColor.a = 1.0f;
        getColor().a = 1.0f;
        addAction(Actions.sequence(
                Actions.fadeOut(duration, CinematicTheme.EASING_SMOOTH),
                Actions.run(() -> {
                    setVisible(false);
                    if (onComplete != null) onComplete.run();
                })
        ));
    }

    public void fadeOut(float duration, Runnable onComplete) {
        setVisible(true);
        clearActions();
        overlayColor.a = 1.0f;
        getColor().a = 0.0f;
        addAction(Actions.sequence(
                Actions.fadeIn(duration, CinematicTheme.EASING_SMOOTH),
                Actions.run(() -> {
                    if (onComplete != null) onComplete.run();
                })
        ));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!isVisible() || getColor().a <= 0f) return;

        batch.setColor(overlayColor.r, overlayColor.g, overlayColor.b, overlayColor.a * getColor().a * parentAlpha);
        whiteDrawable.draw(batch, getX(), getY(), getWidth(), getHeight());
        batch.setColor(Color.WHITE);
    }
}

