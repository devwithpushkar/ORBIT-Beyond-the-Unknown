package orbit.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

/**
 * AssetManager creates and manages all GPU textures, materials, procedural
 * planet maps, and custom Scene2D sci-fi UI skins for ORBIT.
 */
public class AssetManager implements Disposable {

    // Theme Palette Colors
    public static final Color CYAN_ELECTRIC = new Color(0.0f, 0.95f, 1.0f, 1.0f);
    public static final Color CYAN_DIM = new Color(0.0f, 0.55f, 0.65f, 0.85f);
    public static final Color BLUE_VOID = new Color(0.04f, 0.07f, 0.15f, 0.92f);
    public static final Color BLUE_PANEL = new Color(0.06f, 0.10f, 0.20f, 0.85f);
    public static final Color AMBER_WARN = new Color(1.0f, 0.72f, 0.0f, 1.0f);
    public static final Color GREEN_OK = new Color(0.13f, 0.85f, 0.45f, 1.0f);
    public static final Color RED_ALERT = new Color(1.0f, 0.22f, 0.35f, 1.0f);

    private final List<Disposable> disposables = new ArrayList<>();

    private Texture planetTexture;
    private Texture marsTexture;
    private Texture europaTexture;
    private Texture titanTexture;
    private Texture keplerTexture;
    private Texture cloudTexture;
    private Texture atmosphereGlowTexture;
    private Texture starParticleTexture;
    private Texture engineExhaustTexture;
    private Texture whitePixelTexture;

    private BitmapFont titleFont;
    private BitmapFont subtitleFont;
    private BitmapFont bodyFont;
    private BitmapFont monoFont;

    private Skin uiSkin;

    public AssetManager() {
        initTextures();
        initFonts();
        initSkin();
    }

    private void initTextures() {
        // 1. Procedural Seamless Planetary Texture Maps (5 Distinct Celestial Destinations)
        this.planetTexture = generateProceduralPlanetTexture(1024, 512); // Earth
        this.marsTexture = generateMarsTexture(512, 256);
        this.europaTexture = generateEuropaTexture(512, 256);
        this.titanTexture = generateTitanTexture(512, 256);
        this.keplerTexture = generateKeplerTexture(512, 256);

        this.cloudTexture = generateProceduralCloudTexture(1024, 512);
        this.atmosphereGlowTexture = generateRadialGlowTexture(128, new Color(0.0f, 0.85f, 1.0f, 0.9f));
        this.starParticleTexture = generateRadialGlowTexture(32, Color.WHITE);
        this.engineExhaustTexture = generateRadialGlowTexture(64, new Color(0.2f, 0.6f, 1.0f, 1.0f));

        // 1x1 White Texture for borders and procedural drawing
        Pixmap singlePixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        singlePixel.setColor(Color.WHITE);
        singlePixel.fill();
        this.whitePixelTexture = new Texture(singlePixel);
        singlePixel.dispose();

        disposables.add(planetTexture);
        disposables.add(marsTexture);
        disposables.add(europaTexture);
        disposables.add(titanTexture);
        disposables.add(keplerTexture);
        disposables.add(cloudTexture);
        disposables.add(atmosphereGlowTexture);
        disposables.add(starParticleTexture);
        disposables.add(engineExhaustTexture);
        disposables.add(whitePixelTexture);
    }

    private void initFonts() {
        // Generate crisp vector-styled bitmap fonts for different UI hierarchies
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(2.2f);
        this.titleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        this.subtitleFont = new BitmapFont();
        this.subtitleFont.getData().setScale(1.25f);
        this.subtitleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        this.bodyFont = new BitmapFont();
        this.bodyFont.getData().setScale(1.0f);
        this.bodyFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        this.monoFont = new BitmapFont();
        this.monoFont.getData().setScale(0.85f);
        this.monoFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        disposables.add(titleFont);
        disposables.add(subtitleFont);
        disposables.add(bodyFont);
        disposables.add(monoFont);
    }

    private void initSkin() {
        this.uiSkin = new Skin();

        // Register Fonts
        uiSkin.add("title-font", titleFont);
        uiSkin.add("subtitle-font", subtitleFont);
        uiSkin.add("body-font", bodyFont);
        uiSkin.add("mono-font", monoFont);

        // Register Drawables
        TextureRegionDrawable whiteDrawable = new TextureRegionDrawable(new TextureRegion(whitePixelTexture));
        uiSkin.add("white", whiteDrawable, Drawable.class);
        uiSkin.add("white", whiteDrawable);

        // Procedural Button Backgrounds
        Texture btnNormalTex = createButtonTexture(320, 48, BLUE_PANEL, CYAN_DIM, 1);
        Texture btnHoverTex = createButtonTexture(320, 48, new Color(0.08f, 0.16f, 0.32f, 0.95f), CYAN_ELECTRIC, 2);
        Texture btnPressedTex = createButtonTexture(320, 48, new Color(0.0f, 0.4f, 0.55f, 0.95f), CYAN_ELECTRIC, 2);
        Texture btnDisabledTex = createButtonTexture(320, 48, new Color(0.04f, 0.06f, 0.10f, 0.6f), new Color(0.2f, 0.25f, 0.35f, 0.4f), 1);

        disposables.add(btnNormalTex);
        disposables.add(btnHoverTex);
        disposables.add(btnPressedTex);
        disposables.add(btnDisabledTex);

        TextureRegionDrawable btnNormal = new TextureRegionDrawable(new TextureRegion(btnNormalTex));
        TextureRegionDrawable btnHover = new TextureRegionDrawable(new TextureRegion(btnHoverTex));
        TextureRegionDrawable btnPressed = new TextureRegionDrawable(new TextureRegion(btnPressedTex));
        TextureRegionDrawable btnDisabled = new TextureRegionDrawable(new TextureRegion(btnDisabledTex));

        // TextButton Style
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = subtitleFont;
        btnStyle.up = btnNormal;
        btnStyle.over = btnHover;
        btnStyle.down = btnPressed;
        btnStyle.disabled = btnDisabled;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.overFontColor = CYAN_ELECTRIC;
        btnStyle.downFontColor = Color.WHITE;
        btnStyle.disabledFontColor = new Color(0.5f, 0.55f, 0.65f, 0.5f);
        uiSkin.add("default", btnStyle);

        // Label Styles
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, CYAN_ELECTRIC);
        Label.LabelStyle subtitleStyle = new Label.LabelStyle(subtitleFont, Color.WHITE);
        Label.LabelStyle bodyStyle = new Label.LabelStyle(bodyFont, new Color(0.85f, 0.9f, 0.95f, 1f));
        Label.LabelStyle monoStyle = new Label.LabelStyle(monoFont, new Color(0.6f, 0.75f, 0.85f, 0.85f));

        uiSkin.add("title", titleStyle);
        uiSkin.add("subtitle", subtitleStyle);
        uiSkin.add("default", bodyStyle);
        uiSkin.add("mono", monoStyle);

        // Modal Window Style
        Texture winBgTex = createButtonTexture(500, 320, BLUE_VOID, CYAN_ELECTRIC, 2);
        disposables.add(winBgTex);
        TextureRegionDrawable winBg = new TextureRegionDrawable(new TextureRegion(winBgTex));

        Window.WindowStyle winStyle = new Window.WindowStyle();
        winStyle.background = winBg;
        winStyle.titleFont = subtitleFont;
        winStyle.titleFontColor = CYAN_ELECTRIC;
        uiSkin.add("default", winStyle);

        // TextField Style
        Texture fieldBgTex = createButtonTexture(300, 38, new Color(0.02f, 0.04f, 0.08f, 0.95f), CYAN_DIM, 1);
        disposables.add(fieldBgTex);
        TextureRegionDrawable fieldBg = new TextureRegionDrawable(new TextureRegion(fieldBgTex));

        TextField.TextFieldStyle fieldStyle = new TextField.TextFieldStyle();
        fieldStyle.font = subtitleFont;
        fieldStyle.fontColor = Color.WHITE;
        fieldStyle.background = fieldBg;
        fieldStyle.cursor = whiteDrawable;
        fieldStyle.selection = new TextureRegionDrawable(new TextureRegion(whitePixelTexture)).tint(new Color(0.0f, 0.6f, 0.8f, 0.5f));
        uiSkin.add("default", fieldStyle);

        // ScrollPane Style
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.vScroll = whiteDrawable.tint(new Color(0.06f, 0.10f, 0.18f, 0.85f));
        scrollStyle.vScrollKnob = whiteDrawable.tint(new Color(0.18f, 0.85f, 1.0f, 0.85f));
        scrollStyle.hScroll = whiteDrawable.tint(new Color(0.06f, 0.10f, 0.18f, 0.85f));
        scrollStyle.hScrollKnob = whiteDrawable.tint(new Color(0.18f, 0.85f, 1.0f, 0.85f));
        uiSkin.add("default", scrollStyle);
    }

    /**
     * Generates a 3D procedural spherical landmass texture map using spherical noise projection.
     */
    private Texture generateProceduralPlanetTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int y = 0; y < height; y++) {
            float v = (float) y / height;
            float lat = (v - 0.5f) * MathUtils.PI;
            float cosLat = MathUtils.cos(lat);
            float sinLat = MathUtils.sin(lat);

            for (int x = 0; x < width; x++) {
                float u = (float) x / width;
                float lon = u * MathUtils.PI2;

                // 3D Cartesian coordinates on sphere surface
                float nx = cosLat * MathUtils.cos(lon);
                float ny = sinLat;
                float nz = cosLat * MathUtils.sin(lon);

                // Multi-octave harmonic noise for continents
                float n = MathUtils.sin(nx * 2.5f + nz * 1.5f) * 0.5f
                        + MathUtils.cos(ny * 3.5f + nx * 2.0f) * 0.3f
                        + MathUtils.sin(nz * 6.0f + ny * 5.0f) * 0.15f
                        + MathUtils.cos((nx + nz) * 12.0f) * 0.05f;

                // Ocean vs Land coloring
                if (n < -0.05f) {
                    // Deep ocean
                    float depth = MathUtils.clamp((-n - 0.05f) * 2.0f, 0f, 1f);
                    float r = MathUtils.lerp(0.04f, 0.01f, depth);
                    float g = MathUtils.lerp(0.20f, 0.08f, depth);
                    float b = MathUtils.lerp(0.55f, 0.35f, depth);
                    pixmap.setColor(r, g, b, 1.0f);
                } else if (n < 0.04f) {
                    // Shallow coastal waters
                    pixmap.setColor(0.08f, 0.45f, 0.65f, 1.0f);
                } else if (n < 0.18f) {
                    // Lowland coastal vegetation
                    pixmap.setColor(0.12f, 0.48f, 0.22f, 1.0f);
                } else if (n < 0.35f) {
                    // Highland / forest
                    pixmap.setColor(0.28f, 0.42f, 0.18f, 1.0f);
                } else {
                    // Mountain ridges / snow caps
                    float t = MathUtils.clamp((n - 0.35f) * 4.0f, 0f, 1f);
                    float r = MathUtils.lerp(0.45f, 0.90f, t);
                    float g = MathUtils.lerp(0.38f, 0.92f, t);
                    float b = MathUtils.lerp(0.32f, 0.95f, t);
                    pixmap.setColor(r, g, b, 1.0f);
                }

                // Polar ice caps
                if (Math.abs(lat) > 1.25f) {
                    float ice = (Math.abs(lat) - 1.25f) / (MathUtils.PI * 0.5f - 1.25f);
                    pixmap.setColor(0.92f, 0.95f, 0.98f, MathUtils.clamp(ice * 1.5f, 0.5f, 1.0f));
                }

                pixmap.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    /**
     * Generates a semi-transparent procedural cloud pattern for planetary atmosphere.
     */
    private Texture generateProceduralCloudTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int y = 0; y < height; y++) {
            float v = (float) y / height;
            float lat = (v - 0.5f) * MathUtils.PI;
            float cosLat = MathUtils.cos(lat);
            float sinLat = MathUtils.sin(lat);

            for (int x = 0; x < width; x++) {
                float u = (float) x / width;
                float lon = u * MathUtils.PI2;

                float nx = cosLat * MathUtils.cos(lon);
                float ny = sinLat;
                float nz = cosLat * MathUtils.sin(lon);

                float cloudNoise = MathUtils.sin(nx * 4.0f + ny * 6.0f) * 0.4f
                        + MathUtils.cos(nz * 8.0f - nx * 3.0f) * 0.3f
                        + MathUtils.sin((nx + nz) * 16.0f) * 0.2f;

                if (cloudNoise > 0.15f) {
                    float alpha = MathUtils.clamp((cloudNoise - 0.15f) * 2.2f, 0f, 0.85f);
                    pixmap.setColor(0.98f, 0.99f, 1.0f, alpha);
                    pixmap.drawPixel(x, y);
                } else {
                    pixmap.setColor(0, 0, 0, 0);
                    pixmap.drawPixel(x, y);
                }
            }
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    /**
     * Generates a radial glow texture for atmospheric fringes and engine plumes.
     */
    private Texture generateRadialGlowTexture(int size, Color tint) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float center = size / 2.0f;
        float maxDist = center;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float dy = y - center;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float norm = MathUtils.clamp(1.0f - (dist / maxDist), 0.0f, 1.0f);
                float alpha = norm * norm * tint.a;

                pixmap.setColor(tint.r, tint.g, tint.b, alpha);
                pixmap.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    /**
     * Creates a high-tech sci-fi button background with illuminated border and corner accents.
     */
    private Texture createButtonTexture(int width, int height, Color bgColor, Color borderColor, int borderWidth) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Fill semi-transparent background
        pixmap.setColor(bgColor);
        pixmap.fill();

        // Outer border
        pixmap.setColor(borderColor);
        for (int i = 0; i < borderWidth; i++) {
            pixmap.drawRectangle(i, i, width - (i * 2), height - (i * 2));
        }

        // Tech corner brackets (6px notches)
        int notch = 8;
        // Top-left
        pixmap.drawLine(0, notch, notch, 0);
        // Top-right
        pixmap.drawLine(width - notch, 0, width, notch);
        // Bottom-left
        pixmap.drawLine(0, height - notch, notch, height);
        // Bottom-right
        pixmap.drawLine(width - notch, height, width, height - notch);

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private Texture generateMarsTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        for (int y = 0; y < height; y++) {
            float v = (float) y / height;
            float lat = (v - 0.5f) * MathUtils.PI;
            float cosLat = MathUtils.cos(lat);
            float sinLat = MathUtils.sin(lat);

            for (int x = 0; x < width; x++) {
                float u = (float) x / width;
                float lon = u * MathUtils.PI2;

                float nx = cosLat * MathUtils.cos(lon);
                float ny = sinLat;
                float nz = cosLat * MathUtils.sin(lon);

                float n = MathUtils.sin(nx * 2.8f + nz * 2.1f) * 0.5f
                        + MathUtils.cos(ny * 3.8f + nx * 2.3f) * 0.3f
                        + MathUtils.sin(nz * 7.0f + ny * 6.0f) * 0.15f
                        + MathUtils.cos((nx + nz) * 14.0f) * 0.05f;

                float r, g, b;
                if (n < -0.12f) {
                    // Dark basalt lowlands / canyon trenches (Valles Marineris)
                    r = 0.32f + n * 0.1f;
                    g = 0.16f + n * 0.05f;
                    b = 0.10f;
                } else if (n < 0.22f) {
                    // Classic rust-red iron oxide plains
                    r = 0.72f + n * 0.25f;
                    g = 0.32f + n * 0.15f;
                    b = 0.14f + n * 0.08f;
                } else {
                    // Elevated bright silicate highlands & Olympus volcano shields
                    float t = MathUtils.clamp((n - 0.22f) * 3.0f, 0f, 1f);
                    r = MathUtils.lerp(0.85f, 0.95f, t);
                    g = MathUtils.lerp(0.48f, 0.62f, t);
                    b = MathUtils.lerp(0.24f, 0.38f, t);
                }

                // Polar carbon-dioxide dry-ice caps
                if (Math.abs(lat) > 1.32f) {
                    float ice = (Math.abs(lat) - 1.32f) / (MathUtils.PI * 0.5f - 1.32f);
                    r = MathUtils.lerp(r, 0.94f, Math.min(1f, ice * 2.0f));
                    g = MathUtils.lerp(g, 0.92f, Math.min(1f, ice * 2.0f));
                    b = MathUtils.lerp(b, 0.88f, Math.min(1f, ice * 2.0f));
                }

                pixmap.setColor(r, g, b, 1.0f);
                pixmap.drawPixel(x, y);
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private Texture generateEuropaTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        for (int y = 0; y < height; y++) {
            float v = (float) y / height;
            float lat = (v - 0.5f) * MathUtils.PI;
            float cosLat = MathUtils.cos(lat);
            float sinLat = MathUtils.sin(lat);

            for (int x = 0; x < width; x++) {
                float u = (float) x / width;
                float lon = u * MathUtils.PI2;

                float nx = cosLat * MathUtils.cos(lon);
                float ny = sinLat;
                float nz = cosLat * MathUtils.sin(lon);

                // Base glacial smooth noise
                float n = MathUtils.sin(nx * 3.0f + nz * 2.0f) * 0.5f
                        + MathUtils.cos(ny * 4.0f + nx * 3.0f) * 0.3f
                        + MathUtils.sin(nz * 8.0f) * 0.2f;

                // Tectonic fracture lines (lineae)
                float c1 = Math.abs(MathUtils.sin(nx * 15.0f + ny * 9.0f + nz * 7.0f));
                float c2 = Math.abs(MathUtils.cos(nx * 8.0f - ny * 14.0f + nz * 18.0f));
                float c3 = Math.abs(MathUtils.sin(ny * 18.0f + nz * 12.0f));
                float crack = Math.min(Math.min(c1, c2), c3);

                float r = 0.88f + n * 0.08f;
                float g = 0.92f + n * 0.06f;
                float b = 0.98f;

                // Subtle chaotic brown cryovolcanic patches
                if (n < -0.2f) {
                    float t = MathUtils.clamp((-n - 0.2f) * 2.5f, 0f, 1f);
                    r = MathUtils.lerp(r, 0.76f, t);
                    g = MathUtils.lerp(g, 0.65f, t);
                    b = MathUtils.lerp(b, 0.52f, t);
                }

                // Dark reddish-brown fractures
                if (crack < 0.08f) {
                    float intensity = 1.0f - (crack / 0.08f);
                    r = MathUtils.lerp(r, 0.52f, intensity);
                    g = MathUtils.lerp(g, 0.24f, intensity);
                    b = MathUtils.lerp(b, 0.16f, intensity);
                }

                pixmap.setColor(r, g, b, 1.0f);
                pixmap.drawPixel(x, y);
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private Texture generateTitanTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        for (int y = 0; y < height; y++) {
            float v = (float) y / height;
            float lat = (v - 0.5f) * MathUtils.PI;
            float cosLat = MathUtils.cos(lat);
            float sinLat = MathUtils.sin(lat);

            for (int x = 0; x < width; x++) {
                float u = (float) x / width;
                float lon = u * MathUtils.PI2;

                float nx = cosLat * MathUtils.cos(lon);
                float ny = sinLat;
                float nz = cosLat * MathUtils.sin(lon);

                // Dense atmospheric banded smog
                float band = MathUtils.sin(lat * 10.0f) * 0.12f
                           + MathUtils.cos(lat * 4.0f + nx * 0.5f) * 0.15f
                           + MathUtils.sin((nx + nz) * 6.0f) * 0.05f;

                float r = MathUtils.clamp(0.86f + band, 0.6f, 1.0f);
                float g = MathUtils.clamp(0.50f + band * 0.7f, 0.35f, 0.8f);
                float b = MathUtils.clamp(0.12f + band * 0.3f, 0.05f, 0.35f);

                // Polar liquid hydrocarbon seas (Kraken & Ligeia Mare)
                if (Math.abs(lat) > 1.15f) {
                    float seaNoise = MathUtils.sin(nx * 8.0f + nz * 10.0f) * 0.5f + MathUtils.cos(ny * 12.0f) * 0.5f;
                    if (seaNoise > 0.15f) {
                        float depth = MathUtils.clamp((seaNoise - 0.15f) * 3.0f, 0f, 1f);
                        r = MathUtils.lerp(r, 0.08f, depth);
                        g = MathUtils.lerp(g, 0.12f, depth);
                        b = MathUtils.lerp(b, 0.18f, depth);
                    }
                }

                pixmap.setColor(r, g, b, 1.0f);
                pixmap.drawPixel(x, y);
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private Texture generateKeplerTexture(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        for (int y = 0; y < height; y++) {
            float v = (float) y / height;
            float lat = (v - 0.5f) * MathUtils.PI;
            float cosLat = MathUtils.cos(lat);
            float sinLat = MathUtils.sin(lat);

            for (int x = 0; x < width; x++) {
                float u = (float) x / width;
                float lon = u * MathUtils.PI2;

                float nx = cosLat * MathUtils.cos(lon);
                float ny = sinLat;
                float nz = cosLat * MathUtils.sin(lon);

                float n = MathUtils.sin(nx * 2.8f + nz * 2.2f) * 0.5f
                        + MathUtils.cos(ny * 3.2f + nx * 2.5f) * 0.3f
                        + MathUtils.sin(nz * 6.5f + ny * 5.0f) * 0.15f
                        + MathUtils.cos((nx + nz) * 12.0f) * 0.05f;

                float r, g, b;
                if (n < -0.06f) {
                    // Deep luminescent geothermal ocean (cyan / teal)
                    float depth = MathUtils.clamp((-n - 0.06f) * 2.5f, 0f, 1f);
                    r = MathUtils.lerp(0.04f, 0.01f, depth);
                    g = MathUtils.lerp(0.48f, 0.28f, depth);
                    b = MathUtils.lerp(0.62f, 0.40f, depth);
                } else if (n < 0.05f) {
                    // Bioluminescent coastal shoreline (vivid cyan/green glow)
                    r = 0.10f;
                    g = 0.85f;
                    b = 0.80f;
                } else if (n < 0.25f) {
                    // Exotic retinal/infrared-absorbing alien vegetation (deep violet / magenta)
                    r = 0.48f + n * 0.4f;
                    g = 0.10f + n * 0.1f;
                    b = 0.52f + n * 0.4f;
                } else {
                    // High strange crystal ridges (luminescent purple/silver)
                    float t = MathUtils.clamp((n - 0.25f) * 3.5f, 0f, 1f);
                    r = MathUtils.lerp(0.68f, 0.88f, t);
                    g = MathUtils.lerp(0.25f, 0.65f, t);
                    b = MathUtils.lerp(0.72f, 0.95f, t);
                }

                pixmap.setColor(r, g, b, 1.0f);
                pixmap.drawPixel(x, y);
            }
        }
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    public Texture getEarthTexture() {
        return planetTexture;
    }

    public Texture getMarsTexture() {
        return marsTexture;
    }

    public Texture getEuropaTexture() {
        return europaTexture;
    }

    public Texture getTitanTexture() {
        return titanTexture;
    }

    public Texture getKeplerTexture() {
        return keplerTexture;
    }

    public Texture getPlanetTextureByName(String name) {
        if (name == null) return planetTexture;
        return switch (name.toLowerCase().trim()) {
            case "earth" -> planetTexture;
            case "mars" -> marsTexture;
            case "europa" -> europaTexture;
            case "titan" -> titanTexture;
            case "kepler-186f", "kepler" -> keplerTexture;
            default -> planetTexture;
        };
    }

    public Texture getPlanetTexture() {
        return planetTexture;
    }

    public Texture getCloudTexture() {
        return cloudTexture;
    }

    public Texture getAtmosphereGlowTexture() {
        return atmosphereGlowTexture;
    }

    public Texture getStarParticleTexture() {
        return starParticleTexture;
    }

    public Texture getEngineExhaustTexture() {
        return engineExhaustTexture;
    }

    public Skin getUiSkin() {
        return uiSkin;
    }

    @Override
    public void dispose() {
        for (Disposable d : disposables) {
            if (d != null) {
                d.dispose();
            }
        }
        disposables.clear();
    }
}

