package orbit.render.components;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import orbit.render.audio.AudioManager;
import orbit.render.theme.CinematicTheme;

/**
 * HoloDialog provides a diegetic modal spacecraft console.
 * Integrated with the audio subsystem for interactive confirmation tones.
 */
public class HoloDialog extends Dialog {

    private final AudioManager audio;

    public HoloDialog(String title, Skin skin, AudioManager audio) {
        super("", skin);
        this.audio = audio;

        setModal(true);
        setMovable(false);
        pad(16f, 22f, 16f, 22f);

        if (title != null && !title.isEmpty()) {
            Label titleLabel = new Label(title, skin, "subtitle");
            titleLabel.setColor(CinematicTheme.AMBER_WARM);
            getContentTable().add(titleLabel).left().padBottom(14f).row();
        }
    }

    @Override
    public Dialog show(Stage stage) {
        if (audio != null) audio.playConfirm();
        return super.show(stage);
    }

    @Override
    public void hide() {
        if (audio != null) audio.playCancel();
        super.hide(null); // Immediate clean dismissal to prevent lingering modal input capture
    }
}

