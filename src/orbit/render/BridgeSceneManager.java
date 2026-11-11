package orbit.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import orbit.render.theme.CinematicTheme;

/**
 * BridgeSceneManager renders the immersive 3D spacecraft bridge environment.
 *
 * Visual Components:
 * - Interior spacecraft bridge geometry: angled cockpit dashboard console,
 *   overhead bulkhead framing, and structural A-pillar observation window struts.
 * - Forward observation viewport looking out into deep space.
 * - Procedural 3D planetary sphere with rotational dynamics and atmospheric cloud shell.
 * - VRAM-cached multi-depth starfield with continuous parallax drift.
 * - Dynamic dual-source lighting (Sol sun exterior + subtle cockpit instrument fill).
 * - Organic micro-gravity camera sway.
 */
public class BridgeSceneManager implements Disposable {

    private static final int STAR_COUNT = 800;

    private final AssetManager assetManager;
    private final ModelBatch modelBatch;
    private final Environment environment;
    private final PerspectiveCamera camera;

    // 3D Bridge Structural Models
    private Model bridgeModel;
    private ModelInstance bridgeInstance;

    // 3D Celestial Scenery (Planet & Atmosphere)
    private Model planetModel;
    private Model cloudModel;
    private ModelInstance planetInstance;
    private ModelInstance cloudInstance;

    // Optimized VRAM Starfield
    private com.badlogic.gdx.graphics.Mesh starMesh;
    private ShaderProgram starShader;
    private final Matrix4 starTransform = new Matrix4();

    // Reusable Math Vectors (Zero Allocations in Render Loop)
    private final Vector3 camPos = new Vector3();
    private float cameraTime = 0f;
    private float starDriftX = 0f;

    public BridgeSceneManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.modelBatch = new ModelBatch();

        // 1. Perspective Camera inside the spacecraft bridge
        this.camera = new PerspectiveCamera(70f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera.position.set(0f, 0f, 0f);
        this.camera.lookAt(0f, 0f, -10f);
        this.camera.near = 0.1f;
        this.camera.far = 300f;
        this.camera.update();

        // 2. Dynamic Lighting Environment
        this.environment = new Environment();
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.08f, 0.11f, 0.16f, 1.0f));
        // Primary Exterior Sol Sunlight (casts sharp terminator on celestial bodies)
        this.environment.add(new DirectionalLight().set(1.0f, 0.96f, 0.90f, -0.95f, -0.35f, -0.80f));
        // Subtle Interior Cockpit Instrument Fill Light
        this.environment.add(new DirectionalLight().set(0.18f, 0.28f, 0.38f, 0.0f, 1.0f, 0.5f));

        initBridgeGeometry();
        initCelestialBodies();
        initOptimizedStarfield();
    }

    /**
     * Builds the procedural 3D structural elements of the spacecraft bridge.
     */
    private void initBridgeGeometry() {
        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        long attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked;

        // Dark metallic titanium chassis material for bridge consoles
        Material chassisMat = new Material(
                ColorAttribute.createDiffuse(0.05f, 0.07f, 0.11f, 1.0f),
                ColorAttribute.createSpecular(0.35f, 0.40f, 0.50f, 1.0f)
        );

        // Angled Lower Dashboard Console (Spans across the bottom of the viewport)
        com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder dashBuilder = mb.part("dashboard", GL20.GL_TRIANGLES, attr, chassisMat);
        dashBuilder.box(0f, -1.15f, -2.4f, 8.8f, 0.65f, 2.2f);

        // Angled Instrument Wedge Surface (Tilted toward the commanding officer)
        Material wedgeMat = new Material(
                ColorAttribute.createDiffuse(0.08f, 0.12f, 0.18f, 1.0f),
                ColorAttribute.createSpecular(0.6f, 0.65f, 0.75f, 1.0f)
        );
        com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder wedgeBuilder = mb.part("wedge", GL20.GL_TRIANGLES, attr, wedgeMat);
        wedgeBuilder.box(0f, -0.78f, -1.85f, 7.8f, 0.28f, 0.85f);

        // Glowing Instrument Bezel Accent Strips (Warm amber / cyan avionics strips)
        Material stripMat = new Material(
                ColorAttribute.createDiffuse(0.96f, 0.65f, 0.14f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.7f, 0.45f, 0.10f, 1.0f)
        );
        com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder stripBuilder = mb.part("strips", GL20.GL_TRIANGLES, attr, stripMat);
        stripBuilder.box(0f, -0.62f, -1.75f, 7.2f, 0.035f, 0.08f);

        // Overhead Ceiling Bulkhead Frame
        Material ceilingMat = new Material(
                ColorAttribute.createDiffuse(0.04f, 0.06f, 0.09f, 1.0f),
                ColorAttribute.createSpecular(0.2f, 0.25f, 0.3f, 1.0f)
        );
        com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder ceilingBuilder = mb.part("ceiling", GL20.GL_TRIANGLES, attr, ceilingMat);
        ceilingBuilder.box(0f, 1.55f, -2.4f, 8.8f, 0.55f, 2.2f);

        // Side Structural Window Struts / A-Pillars (Framing the observation viewport)
        Material strutMat = new Material(
                ColorAttribute.createDiffuse(0.06f, 0.09f, 0.13f, 1.0f),
                ColorAttribute.createSpecular(0.4f, 0.45f, 0.55f, 1.0f)
        );
        com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder strutBuilder = mb.part("struts", GL20.GL_TRIANGLES, attr, strutMat);
        // Port A-Pillar
        strutBuilder.box(-3.5f, 0.2f, -2.3f, 0.65f, 2.5f, 1.8f);
        // Starboard A-Pillar
        strutBuilder.box(3.5f, 0.2f, -2.3f, 0.65f, 2.5f, 1.8f);

        bridgeModel = mb.end();
        bridgeInstance = new ModelInstance(bridgeModel);
    }

    /**
     * Builds the procedural 3D planet sphere and atmospheric cloud shell outside the viewport.
     */
    private void initCelestialBodies() {
        ModelBuilder mb = new ModelBuilder();
        long attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

        // 1. Planetary Sphere (Earth coordinates in forward view)
        Material planetMat = new Material(
                TextureAttribute.createDiffuse(assetManager.getPlanetTexture()),
                ColorAttribute.createSpecular(0.25f, 0.35f, 0.5f, 1.0f)
        );
        planetModel = mb.createSphere(7.4f, 7.4f, 7.4f, 40, 40, planetMat, attr);
        planetInstance = new ModelInstance(planetModel);
        // Positioned outside the forward observation viewport
        planetInstance.transform.setToTranslation(2.4f, -0.6f, -11.5f);
        planetInstance.transform.rotate(Vector3.X, -14f);

        // 2. Atmospheric Cloud Shell
        Material cloudMat = new Material(
                TextureAttribute.createDiffuse(assetManager.getCloudTexture()),
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.85f)
        );
        cloudModel = mb.createSphere(7.55f, 7.55f, 7.55f, 40, 40, cloudMat, attr);
        cloudInstance = new ModelInstance(cloudModel);
        cloudInstance.transform.setToTranslation(2.4f, -0.6f, -11.5f);
        cloudInstance.transform.rotate(Vector3.X, -14f);
    }

    /**
     * Builds an optimized VRAM-cached static Mesh for the multi-depth starfield.
     * Replaces CPU-side ImmediateModeRenderer buffer uploads for 60 FPS performance.
     */
    private void initOptimizedStarfield() {
        starMesh = new com.badlogic.gdx.graphics.Mesh(true, STAR_COUNT, 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE)
        );

        float[] vertices = new float[STAR_COUNT * 4];
        int idx = 0;
        for (int i = 0; i < STAR_COUNT; i++) {
            float x = MathUtils.random(-60f, 60f);
            float y = MathUtils.random(-40f, 40f);
            float z = MathUtils.random(-80f, -12f);

            float depthNorm = MathUtils.clamp((-z - 12f) / 68f, 0f, 1f);
            float brightness = MathUtils.lerp(1.0f, 0.35f, depthNorm) * MathUtils.random(0.7f, 1.0f);

            Color c;
            float temp = MathUtils.random();
            if (temp < 0.25f) {
                c = new Color(0.7f, 0.85f, 1.0f, brightness);
            } else if (temp < 0.45f) {
                c = new Color(1.0f, 0.90f, 0.7f, brightness);
            } else {
                c = new Color(0.95f, 0.97f, 1.0f, brightness);
            }

            vertices[idx++] = x;
            vertices[idx++] = y;
            vertices[idx++] = z;
            vertices[idx++] = c.toFloatBits();
        }
        starMesh.setVertices(vertices);

        // Simple hardware vertex/fragment point shader
        String vertexShader = "attribute vec4 a_position;\n"
                + "attribute vec4 a_color;\n"
                + "uniform mat4 u_projTrans;\n"
                + "varying vec4 v_color;\n"
                + "void main() {\n"
                + "    v_color = a_color;\n"
                + "    gl_Position = u_projTrans * a_position;\n"
                + "    gl_PointSize = 2.0;\n"
                + "}\n";

        String fragmentShader = "#ifdef GL_ES\n"
                + "precision mediump float;\n"
                + "#endif\n"
                + "varying vec4 v_color;\n"
                + "void main() {\n"
                + "    gl_FragColor = v_color;\n"
                + "}\n";

        starShader = new ShaderProgram(vertexShader, fragmentShader);
        if (!starShader.isCompiled()) {
            System.out.println("[ORBIT-RENDER] Star shader compilation failed: " + starShader.getLog());
        }
    }

    /**
     * Updates celestial rotations and camera organic breathing each frame.
     */
    public void update(float delta) {
        // 1. Continuous Planetary Axial Rotation
        float spin = 3.6f * delta;
        planetInstance.transform.rotate(Vector3.Y, spin);
        cloudInstance.transform.rotate(Vector3.Y, spin * 1.25f);

        // 2. Starfield Continuous Depth Parallax Drift (Matrix-based, zero memory allocation)
        starDriftX -= delta * 0.45f;
        if (starDriftX < -120f) starDriftX = 0f;
        starTransform.setToTranslation(starDriftX, 0f, 0f);

        // 3. Subtle Organic Micro-Gravity Camera Breathing (Standing on bridge)
        cameraTime += delta * 0.40f;
        float swayX = MathUtils.sin(cameraTime * 0.8f) * 0.035f;
        float swayY = MathUtils.cos(cameraTime * 0.6f) * 0.025f;
        camPos.set(swayX, swayY, 0f);

        camera.position.set(camPos);
        camera.lookAt(0.3f, -0.05f, -10f);
        camera.update();
    }

    /**
     * Renders the complete 3D bridge scene: starfield, planet, clouds, and cockpit geometry.
     */
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.012f, 0.022f, 0.045f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // 1. Render VRAM-cached Starfield through the observation viewport
        renderStarfield();

        // 2. Render 3D Celestial Scenery and Bridge Cockpit Architecture
        modelBatch.begin(camera);
        // Celestial bodies outside the viewport
        modelBatch.render(planetInstance, environment);
        modelBatch.render(cloudInstance, environment);
        // Spacecraft cockpit structural interior (framed around the viewport)
        modelBatch.render(bridgeInstance, environment);
        modelBatch.end();
    }

    private void renderStarfield() {
        if (starShader == null || !starShader.isCompiled() || starMesh == null) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

        starShader.bind();
        starShader.setUniformMatrix("u_projTrans", camera.combined);
        starMesh.render(starShader, GL20.GL_POINTS);

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        planetModel.dispose();
        cloudModel.dispose();
        bridgeModel.dispose();
        if (starMesh != null) starMesh.dispose();
        if (starShader != null) starShader.dispose();
    }
}

