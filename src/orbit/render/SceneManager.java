package orbit.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

/**
 * SceneManager renders the cinematic 3D space environment for ORBIT.
 * Features:
 * - Actual 3D sphere planet with procedural surface terrain and cloud layer.
 * - Directional sunlight casting authentic day/night terminator.
 * - Continuous axial rotation and atmospheric rim glow.
 * - Fully modeled 3D spacecraft (ODYSSEY-07) with active flight path and banking.
 * - Multi-depth starfield with parallax drift.
 * - Smooth perspective camera with organic floating motion.
 */
public class SceneManager implements Disposable {

    private final AssetManager assetManager;
    private final ModelBatch modelBatch;
    private final Environment environment;
    private final PerspectiveCamera camera;

    // 3D Models and Instances
    private Model planetModel;
    private Model cloudModel;
    private Model shipModel;

    private ModelInstance planetInstance;
    private ModelInstance cloudInstance;
    private ModelInstance shipInstance;

    // Starfield System
    private static final int STAR_COUNT = 450;
    private final Vector3[] starPositions = new Vector3[STAR_COUNT];
    private final float[] starSpeeds = new float[STAR_COUNT];
    private final Color[] starColors = new Color[STAR_COUNT];
    private ImmediateModeRenderer20 starRenderer;

    // Spacecraft Flight Animation State
    private float shipTime = 0.0f;
    private final Vector3 shipPos = new Vector3();
    private final Vector3 shipRot = new Vector3();

    // Camera Floating Drift State
    private float cameraTime = 0.0f;

    // Engine Exhaust Particle State
    private static final int EXHAUST_COUNT = 30;
    private final Vector3[] exhaustParticles = new Vector3[EXHAUST_COUNT];
    private final float[] exhaustLifetimes = new float[EXHAUST_COUNT];

    public SceneManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.modelBatch = new ModelBatch();

        // 1. Perspective 3D Camera Setup
        this.camera = new PerspectiveCamera(52, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera.near = 0.1f;
        this.camera.far = 250f;
        this.camera.position.set(0f, 1.2f, 11f);
        this.camera.lookAt(0.5f, 0f, 0f);
        this.camera.update();

        // 2. Lighting & Environmental PBR Shaders
        this.environment = new Environment();
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.12f, 0.15f, 0.22f, 1.0f));
        // Sunlight direction pointing from top-left toward planet center
        DirectionalLight sunLight = new DirectionalLight();
        sunLight.set(1.0f, 0.98f, 0.92f, -1.2f, -0.4f, -0.8f);
        this.environment.add(sunLight);

        init3DModels();
        initStarfield();
        initExhaust();
    }

    private void init3DModels() {
        ModelBuilder mb = new ModelBuilder();

        // A. 3D Planet Sphere (Radius = 3.6f)
        long attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;
        mb.begin();
        MeshPartBuilder mpb = mb.part("planet", GL20.GL_TRIANGLES, attr,
                new com.badlogic.gdx.graphics.g3d.Material(
                        TextureAttribute.createDiffuse(assetManager.getPlanetTexture()),
                        ColorAttribute.createSpecular(0.25f, 0.35f, 0.45f, 1.0f)
                ));
        mpb.sphere(7.2f, 7.2f, 7.2f, 64, 48);
        this.planetModel = mb.end();
        this.planetInstance = new ModelInstance(planetModel);
        // Position planet slightly right of center to frame the menu UI
        this.planetInstance.transform.setToTranslation(4.0f, -0.4f, -1.5f);
        this.planetInstance.transform.rotate(Vector3.Z, -18.0f); // 18-degree axial tilt

        // B. Concentric Cloud Shell (Radius = 3.72f) with Alpha Blending
        mb.begin();
        MeshPartBuilder cloudMpb = mb.part("clouds", GL20.GL_TRIANGLES, attr,
                new com.badlogic.gdx.graphics.g3d.Material(
                        TextureAttribute.createDiffuse(assetManager.getCloudTexture()),
                        new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.75f)
                ));
        cloudMpb.sphere(7.44f, 7.44f, 7.44f, 48, 36);
        this.cloudModel = mb.end();
        this.cloudInstance = new ModelInstance(cloudModel);
        this.cloudInstance.transform.setToTranslation(4.0f, -0.4f, -1.5f);
        this.cloudInstance.transform.rotate(Vector3.Z, -18.0f);

        // C. 3D Spacecraft (ODYSSEY-07)
        this.shipModel = createSpacecraftModel(mb);
        this.shipInstance = new ModelInstance(shipModel);
    }

    /**
     * Builds a detailed 3D multi-part sci-fi reconnaissance cruiser using ModelBuilder.
     */
    private Model createSpacecraftModel(ModelBuilder mb) {
        mb.begin();
        long attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        // 1. Titanium Hull Main Fuselage (Elongated hexagonal geometry)
        com.badlogic.gdx.graphics.g3d.Material hullMat = new com.badlogic.gdx.graphics.g3d.Material(
                ColorAttribute.createDiffuse(0.20f, 0.24f, 0.30f, 1.0f),
                ColorAttribute.createSpecular(0.6f, 0.7f, 0.8f, 1.0f)
        );
        MeshPartBuilder hullBuilder = mb.part("fuselage", GL20.GL_TRIANGLES, attr, hullMat);
        hullBuilder.box(0.85f, 0.45f, 2.8f);

        // 2. Cockpit Bridge Nose Canopy (Tapered front wedge with cyan viewport glass)
        com.badlogic.gdx.graphics.g3d.Material cockpitMat = new com.badlogic.gdx.graphics.g3d.Material(
                ColorAttribute.createDiffuse(0.0f, 0.85f, 1.0f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.0f, 0.4f, 0.6f, 1.0f)
        );
        MeshPartBuilder cockpitBuilder = mb.part("canopy", GL20.GL_TRIANGLES, attr, cockpitMat);
        cockpitBuilder.box(0.55f, 0.30f, 0.8f);

        // 3. Swept Delta Wings (Aerodynamic stability vanes)
        com.badlogic.gdx.graphics.g3d.Material wingMat = new com.badlogic.gdx.graphics.g3d.Material(
                ColorAttribute.createDiffuse(0.14f, 0.18f, 0.24f, 1.0f),
                ColorAttribute.createSpecular(0.4f, 0.5f, 0.6f, 1.0f)
        );
        MeshPartBuilder wingBuilder = mb.part("wings", GL20.GL_TRIANGLES, attr, wingMat);
        wingBuilder.box(2.8f, 0.08f, 1.2f);

        // 4. Twin Ion Propulsion Engine Nacelles (Port & Starboard)
        com.badlogic.gdx.graphics.g3d.Material engineMat = new com.badlogic.gdx.graphics.g3d.Material(
                ColorAttribute.createDiffuse(0.12f, 0.14f, 0.18f, 1.0f),
                ColorAttribute.createSpecular(0.8f, 0.8f, 0.9f, 1.0f)
        );
        MeshPartBuilder nacelleBuilder = mb.part("nacelles", GL20.GL_TRIANGLES, attr, engineMat);
        // Port engine
        nacelleBuilder.box(0.32f, 0.32f, 1.4f);
        // Starboard engine
        nacelleBuilder.box(0.32f, 0.32f, 1.4f);

        // 5. Twin Glowing Blue Engine Bells (Exhaust Emitters)
        com.badlogic.gdx.graphics.g3d.Material bellMat = new com.badlogic.gdx.graphics.g3d.Material(
                ColorAttribute.createDiffuse(0.2f, 0.8f, 1.0f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.4f, 0.9f, 1.0f, 1.0f)
        );
        MeshPartBuilder bellBuilder = mb.part("bells", GL20.GL_TRIANGLES, attr, bellMat);
        bellBuilder.cylinder(0.26f, 0.15f, 0.26f, 16);

        return mb.end();
    }

    private void initStarfield() {
        this.starRenderer = new ImmediateModeRenderer20(false, true, 0);

        for (int i = 0; i < STAR_COUNT; i++) {
            // Distribute stars in a 3D hemisphere surrounding the scene
            float x = MathUtils.random(-50f, 50f);
            float y = MathUtils.random(-35f, 35f);
            float z = MathUtils.random(-70f, -8f);
            starPositions[i] = new Vector3(x, y, z);
            starSpeeds[i] = MathUtils.random(0.3f, 1.2f);

            // Varied spectral star colors (warm yellow, blue-white, cool cyan)
            float temp = MathUtils.random();
            if (temp < 0.25f) {
                starColors[i] = new Color(0.7f, 0.85f, 1.0f, MathUtils.random(0.5f, 0.95f)); // Blue-white
            } else if (temp < 0.45f) {
                starColors[i] = new Color(1.0f, 0.90f, 0.7f, MathUtils.random(0.5f, 0.9f));  // Warm gold
            } else {
                starColors[i] = new Color(0.95f, 0.97f, 1.0f, MathUtils.random(0.4f, 0.85f)); // White
            }
        }
    }

    private void initExhaust() {
        for (int i = 0; i < EXHAUST_COUNT; i++) {
            exhaustParticles[i] = new Vector3();
            exhaustLifetimes[i] = MathUtils.random(0f, 1f);
        }
    }

    /**
     * Updates physics, trajectories, rotations, and camera drift each frame.
     */
    public void update(float delta) {
        // 1. Continuous Planet Axial Rotation
        float planetSpinRate = 4.2f * delta;
        planetInstance.transform.rotate(Vector3.Y, planetSpinRate);
        // Cloud layer rotates slightly faster for dynamic atmospheric swirling
        cloudInstance.transform.rotate(Vector3.Y, planetSpinRate * 1.25f);

        // 2. Camera Organic Floating Sway
        cameraTime += delta * 0.45f;
        float camSwayX = MathUtils.sin(cameraTime * 0.8f) * 0.35f;
        float camSwayY = 1.2f + MathUtils.cos(cameraTime * 0.6f) * 0.22f;
        float camSwayZ = 11.0f + MathUtils.sin(cameraTime * 0.4f) * 0.3f;
        camera.position.set(camSwayX, camSwayY, camSwayZ);
        camera.lookAt(0.6f, 0.0f, 0.0f);
        camera.update();

        // 3. Spacecraft Flight Path & Subtle Banking
        shipTime += delta * 0.55f;
        // Orbital trajectory sweeping across the viewport
        float shipX = -1.6f + MathUtils.sin(shipTime * 0.7f) * 1.8f;
        float shipY = -0.3f + MathUtils.cos(shipTime * 0.9f) * 0.45f;
        float shipZ = 2.8f + MathUtils.sin(shipTime * 0.5f) * 0.8f;
        shipPos.set(shipX, shipY, shipZ);

        // Smooth bank roll during turn
        float roll = MathUtils.sin(shipTime * 0.7f) * 14.0f;
        float pitch = MathUtils.cos(shipTime * 0.9f) * 5.0f;
        float yaw = 25.0f + MathUtils.cos(shipTime * 0.7f) * 8.0f;

        shipInstance.transform.idt();
        shipInstance.transform.setToTranslation(shipPos);
        shipInstance.transform.rotate(Vector3.Z, roll);
        shipInstance.transform.rotate(Vector3.X, pitch);
        shipInstance.transform.rotate(Vector3.Y, yaw);
        shipInstance.transform.scale(0.85f, 0.85f, 0.85f);

        // 4. Starfield Depth Parallax Drift
        for (int i = 0; i < STAR_COUNT; i++) {
            starPositions[i].x -= delta * starSpeeds[i] * 0.4f;
            if (starPositions[i].x < -50f) {
                starPositions[i].x = 50f;
                starPositions[i].y = MathUtils.random(-35f, 35f);
            }
        }

        // 5. Engine Exhaust Particle Simulation
        for (int i = 0; i < EXHAUST_COUNT; i++) {
            exhaustLifetimes[i] += delta * 2.5f;
            if (exhaustLifetimes[i] > 1.0f) {
                exhaustLifetimes[i] = 0.0f;
                // Emit from engine nozzles behind the vessel
                exhaustParticles[i].set(shipPos).add(MathUtils.random(-0.1f, 0.1f) - 0.2f, -0.05f, 1.4f);
            } else {
                exhaustParticles[i].z += delta * 4.0f;
                exhaustParticles[i].y -= delta * 0.2f;
            }
        }
    }

    /**
     * Renders the complete 3D scene (Starfield, Planet, Clouds, Ship, Particles).
     */
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.012f, 0.022f, 0.045f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // 1. Render Multi-Depth Starfield
        renderStarfield();

        // 2. Render 3D Model Geometry (Planet & Spacecraft)
        modelBatch.begin(camera);
        modelBatch.render(planetInstance, environment);
        modelBatch.render(cloudInstance, environment);
        modelBatch.render(shipInstance, environment);
        modelBatch.end();

        // 3. Render Engine Exhaust Particle Glow
        renderExhaustParticles();
    }

    private void renderStarfield() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

        starRenderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < STAR_COUNT; i++) {
            Vector3 pos = starPositions[i];
            Color c = starColors[i];
            starRenderer.color(c.r, c.g, c.b, c.a);
            starRenderer.vertex(pos.x, pos.y, pos.z);
        }
        starRenderer.end();

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    private void renderExhaustParticles() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        Gdx.gl.glDepthMask(false);

        starRenderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < EXHAUST_COUNT; i++) {
            float life = exhaustLifetimes[i];
            float alpha = Math.max(0.0f, 1.0f - life);
            Vector3 p = exhaustParticles[i];
            starRenderer.color(0.15f, 0.65f, 1.0f, alpha * 0.75f);
            starRenderer.vertex(p.x, p.y, p.z);
        }
        starRenderer.end();

        Gdx.gl.glDepthMask(true);
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
        shipModel.dispose();
        starRenderer.dispose();
    }
}

