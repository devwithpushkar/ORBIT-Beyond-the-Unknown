package orbit.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * RenderManager manages the multi-layer display pipeline, maintaining resolution
 * independence, 3D perspective projection, and 2D diegetic UI staging.
 */
public class RenderManager implements Disposable {

    public static final float VIRTUAL_WIDTH = 1280f;
    public static final float VIRTUAL_HEIGHT = 800f;

    private final Viewport uiViewport;
    private final SpriteBatch spriteBatch;

    public RenderManager() {
        this.spriteBatch = new SpriteBatch();
        this.uiViewport = new ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        this.uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    public void update(int width, int height) {
        uiViewport.update(width, height, true);
    }

    public Viewport getUiViewport() {
        return uiViewport;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public Camera getUiCamera() {
        return uiViewport.getCamera();
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
    }
}

