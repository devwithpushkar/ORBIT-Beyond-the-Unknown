package orbit.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
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
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import orbit.controllers.EndingResult;

import java.util.ArrayList;
import java.util.List;

/**
 * EndingSceneManager orchestrates the 3D cinematic visual presentation for ORBIT's
 * three campaign outcomes:
 *
 * 1. Ending A — Successful Return:
 *    Triumphant, sunlit low-Earth orbit. The operational Odyssey-07 glides gracefully
 *    along a deceleration vector with twin ion thruster plumes burning steadily,
 *    basking in solar gold and atmospheric cyan bounce light.
 *
 * 2. Ending B — Lost in Space:
 *    Devastating, cold interstellar void between star systems. Lifeless Odyssey-07
 *    tumbles without propulsion across multiple axes in near-pitch blackness, punctuated
 *    by a sharp red emergency distress beacon flashing into the deep void.
 *
 * 3. Ending C — The Unknown:
 *    Mysterious, transcendent close-proximity composition on Kepler-186f. An alien
 *    basalt spire anchors a shimmering trans-dimensional gateway flanked by spinning
 *    quantum rings, with the hovering Alien Artifact radiating ethereal violet energy
 *    particles toward the cosmic event horizon.
 */
public class EndingSceneManager implements Disposable {

    private static final int STAR_COUNT = 240;
    private static final int EXHAUST_COUNT = 32;
    private static final int AURA_COUNT = 48;

    private final AssetManager assetManager;
    private final PerspectiveCamera camera;
    private final ModelBatch modelBatch;
    private final Environment environment;

    private EndingResult.Type currentType = EndingResult.Type.ENDING_A_SUCCESSFUL_RETURN;

    // Disposables registry
    private final List<Model> allocatedModels = new ArrayList<>();

    // 3D Celestial & Architectural Models
    private Model shipModel;
    private ModelInstance shipInstance;

    // Ending A: Earth & Atmosphere
    private Model earthModel;
    private ModelInstance earthInstance;
    private Model earthCloudModel;
    private ModelInstance earthCloudInstance;

    // Ending B: Distant Cold Dwarf Star
    private Model distantStarModel;
    private ModelInstance distantStarInstance;

    // Ending C: Kepler-186f, Alien Spire, Quantum Rings & Relic
    private Model keplerModel;
    private ModelInstance keplerInstance;
    private Model keplerHaloModel;
    private ModelInstance keplerHaloInstance;
    private Model spireModel;
    private ModelInstance spireInstance;
    private Model ringOuterModel;
    private ModelInstance ringOuterInstance;
    private Model ringInnerModel;
    private ModelInstance ringInnerInstance;
    private Model artifactModel;
    private ModelInstance artifactInstance;

    // Dynamic Lighting
    private DirectionalLight sunLight;
    private DirectionalLight fillLight;
    private PointLight dynamicPointLight;

    // Dynamic Starfield
    private ImmediateModeRenderer20 starRenderer;
    private final Vector3[] starPositions = new Vector3[STAR_COUNT];
    private final Color[] starColors = new Color[STAR_COUNT];
    private final float[] starSpeeds = new float[STAR_COUNT];

    // Particle Systems
    private final Vector3[] exhaustPositions = new Vector3[EXHAUST_COUNT];
    private final float[] exhaustLifetimes = new float[EXHAUST_COUNT];
    private final Vector3[] auraPositions = new Vector3[AURA_COUNT];
    private final float[] auraSpeeds = new float[AURA_COUNT];
    private final float[] auraAngles = new float[AURA_COUNT];

    // Cinematic Choreography State
    private float cinematicTime = 0.0f;
    private float shipPitch = 0.0f;
    private float shipYaw = 0.0f;
    private float shipRoll = 0.0f;
    private float beaconTimer = 0.0f;
    private float beaconIntensity = 0.0f;

    public EndingSceneManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.modelBatch = new ModelBatch();

        // 1. Perspective Camera
        this.camera = new PerspectiveCamera(58.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera.near = 0.1f;
        this.camera.far = 180.0f;
        this.camera.position.set(0.0f, 0.8f, 6.5f);
        this.camera.lookAt(0.0f, 0.0f, 0.0f);
        this.camera.update();

        // 2. Cinematic Environment & Dynamic Lights
        this.environment = new Environment();
        this.sunLight = new DirectionalLight();
        this.fillLight = new DirectionalLight();
        this.dynamicPointLight = new PointLight();
        this.environment.add(sunLight);
        this.environment.add(fillLight);
        this.environment.add(dynamicPointLight);

        // 3. Procedural Starfield & Particle Systems
        initStarfield();
        initParticles();

        // 4. Build 3D Models
        initModels();

        // 5. Default initial configuration
        setupEnding(EndingResult.Type.ENDING_A_SUCCESSFUL_RETURN);
    }

    private void initStarfield() {
        this.starRenderer = new ImmediateModeRenderer20(false, true, 0);
        for (int i = 0; i < STAR_COUNT; i++) {
            float x = MathUtils.random(-65.0f, 65.0f);
            float y = MathUtils.random(-45.0f, 45.0f);
            float z = MathUtils.random(-90.0f, -12.0f);
            starPositions[i] = new Vector3(x, y, z);
            starSpeeds[i] = MathUtils.random(0.2f, 1.1f);

            float roll = MathUtils.random();
            if (roll < 0.28f) {
                starColors[i] = new Color(0.68f, 0.84f, 1.0f, MathUtils.random(0.4f, 0.95f)); // Cool blue-white
            } else if (roll < 0.50f) {
                starColors[i] = new Color(1.0f, 0.88f, 0.65f, MathUtils.random(0.4f, 0.90f)); // Warm solar gold
            } else {
                starColors[i] = new Color(0.95f, 0.96f, 1.0f, MathUtils.random(0.35f, 0.85f)); // Crisp white
            }
        }
    }

    private void initParticles() {
        for (int i = 0; i < EXHAUST_COUNT; i++) {
            exhaustPositions[i] = new Vector3();
            exhaustLifetimes[i] = MathUtils.random(0.0f, 1.0f);
        }
        for (int i = 0; i < AURA_COUNT; i++) {
            auraPositions[i] = new Vector3();
            auraSpeeds[i] = MathUtils.random(0.8f, 2.2f);
            auraAngles[i] = MathUtils.random(0.0f, MathUtils.PI2);
        }
    }

    private void initModels() {
        ModelBuilder mb = new ModelBuilder();
        long pAttr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        long texAttr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

        // A. Spacecraft Odyssey-07
        this.shipModel = createCruiserModel(mb);
        allocatedModels.add(shipModel);
        this.shipInstance = new ModelInstance(shipModel);

        // B. Earth & Concentric Cloud Sphere (Ending A)
        mb.begin();
        MeshPartBuilder earthBuilder = mb.part("earth", GL20.GL_TRIANGLES, texAttr,
                new Material(
                        TextureAttribute.createDiffuse(assetManager.getPlanetTextureByName("earth")),
                        ColorAttribute.createSpecular(0.40f, 0.55f, 0.70f, 1.0f)
                ));
        earthBuilder.sphere(7.2f, 7.2f, 7.2f, 44, 32);
        this.earthModel = mb.end();
        allocatedModels.add(earthModel);
        this.earthInstance = new ModelInstance(earthModel);

        mb.begin();
        MeshPartBuilder cloudBuilder = mb.part("clouds", GL20.GL_TRIANGLES, texAttr,
                new Material(
                        TextureAttribute.createDiffuse(assetManager.getCloudTexture()),
                        ColorAttribute.createSpecular(0.15f, 0.15f, 0.15f, 1.0f),
                        new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.55f)
                ));
        cloudBuilder.sphere(7.45f, 7.45f, 7.45f, 36, 26);
        this.earthCloudModel = mb.end();
        allocatedModels.add(earthCloudModel);
        this.earthCloudInstance = new ModelInstance(earthCloudModel);

        // C. Distant Cold Dwarf Star (Ending B)
        mb.begin();
        Material starMat = new Material(
                ColorAttribute.createDiffuse(0.9f, 0.25f, 0.15f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 1.0f, 0.35f, 0.15f, 1.0f)
        );
        MeshPartBuilder starBuilder = mb.part("star", GL20.GL_TRIANGLES, pAttr, starMat);
        starBuilder.sphere(1.2f, 1.2f, 1.2f, 16, 16);
        this.distantStarModel = mb.end();
        allocatedModels.add(distantStarModel);
        this.distantStarInstance = new ModelInstance(distantStarModel);
        this.distantStarInstance.transform.setToTranslation(-18.0f, 9.0f, -48.0f);

        // D. Kepler-186f Celestial Sphere (Ending C)
        mb.begin();
        MeshPartBuilder keplerBuilder = mb.part("kepler", GL20.GL_TRIANGLES, texAttr,
                new Material(
                        TextureAttribute.createDiffuse(assetManager.getPlanetTextureByName("kepler-186f")),
                        ColorAttribute.createSpecular(0.50f, 0.30f, 0.65f, 1.0f)
                ));
        keplerBuilder.sphere(7.8f, 7.8f, 7.8f, 44, 32);
        this.keplerModel = mb.end();
        allocatedModels.add(keplerModel);
        this.keplerInstance = new ModelInstance(keplerModel);

        mb.begin();
        Material haloMat = new Material(
                ColorAttribute.createDiffuse(0.65f, 0.25f, 0.85f, 0.45f),
                new ColorAttribute(ColorAttribute.Emissive, 0.40f, 0.15f, 0.60f, 0.45f),
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.45f)
        );
        MeshPartBuilder haloBuilder = mb.part("halo", GL20.GL_TRIANGLES, pAttr, haloMat);
        haloBuilder.sphere(8.15f, 8.15f, 8.15f, 32, 24);
        this.keplerHaloModel = mb.end();
        allocatedModels.add(keplerHaloModel);
        this.keplerHaloInstance = new ModelInstance(keplerHaloModel);

        // E. Alien Megastructure Spire & Gateway (Ending C)
        mb.begin();
        Material spireMat = new Material(
                ColorAttribute.createDiffuse(0.08f, 0.07f, 0.12f, 1.0f),
                ColorAttribute.createSpecular(0.6f, 0.5f, 0.9f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.05f, 0.02f, 0.10f, 1.0f)
        );
        MeshPartBuilder spireBuilder = mb.part("spire", GL20.GL_TRIANGLES, pAttr, spireMat);
        spireBuilder.cylinder(0.9f, 3.8f, 0.9f, 8); // Hexagonal basalt monolith column
        this.spireModel = mb.end();
        allocatedModels.add(spireModel);
        this.spireInstance = new ModelInstance(spireModel);

        // F. Concentric Quantum Gateway Rings
        mb.begin();
        Material outerRingMat = new Material(
                ColorAttribute.createDiffuse(0.75f, 0.35f, 0.95f, 0.70f),
                new ColorAttribute(ColorAttribute.Emissive, 0.65f, 0.25f, 0.90f, 1.0f),
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.70f)
        );
        MeshPartBuilder ringOuterBuilder = mb.part("ringOuter", GL20.GL_TRIANGLES, pAttr, outerRingMat);
        ringOuterBuilder.cylinder(2.6f, 0.06f, 2.6f, 28);
        this.ringOuterModel = mb.end();
        allocatedModels.add(ringOuterModel);
        this.ringOuterInstance = new ModelInstance(ringOuterModel);

        mb.begin();
        Material innerRingMat = new Material(
                ColorAttribute.createDiffuse(0.15f, 0.85f, 1.0f, 0.75f),
                new ColorAttribute(ColorAttribute.Emissive, 0.25f, 0.90f, 1.0f, 1.0f),
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.75f)
        );
        MeshPartBuilder ringInnerBuilder = mb.part("ringInner", GL20.GL_TRIANGLES, pAttr, innerRingMat);
        ringInnerBuilder.cylinder(1.7f, 0.06f, 1.7f, 24);
        this.ringInnerModel = mb.end();
        allocatedModels.add(ringInnerModel);
        this.ringInnerInstance = new ModelInstance(ringInnerModel);

        // G. Floating Alien Artifact Octahedral Relic
        mb.begin();
        Material artifactMat = new Material(
                ColorAttribute.createDiffuse(1.0f, 0.90f, 0.40f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.95f, 0.55f, 1.0f, 1.0f),
                ColorAttribute.createSpecular(1.0f, 1.0f, 0.8f, 1.0f)
        );
        MeshPartBuilder artBuilder = mb.part("relic", GL20.GL_TRIANGLES, pAttr, artifactMat);
        artBuilder.box(0.48f, 0.65f, 0.48f);
        this.artifactModel = mb.end();
        allocatedModels.add(artifactModel);
        this.artifactInstance = new ModelInstance(artifactModel);
    }

    private Model createCruiserModel(ModelBuilder mb) {
        mb.begin();
        long attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        // Fuselage
        Material hullMat = new Material(
                ColorAttribute.createDiffuse(0.22f, 0.26f, 0.32f, 1.0f),
                ColorAttribute.createSpecular(0.6f, 0.7f, 0.8f, 1.0f)
        );
        MeshPartBuilder hullBuilder = mb.part("fuselage", GL20.GL_TRIANGLES, attr, hullMat);
        hullBuilder.box(0.9f, 0.48f, 3.0f);

        // Cockpit Nose
        Material cockpitMat = new Material(
                ColorAttribute.createDiffuse(0.0f, 0.88f, 1.0f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.1f, 0.5f, 0.7f, 1.0f)
        );
        MeshPartBuilder cockpitBuilder = mb.part("cockpit", GL20.GL_TRIANGLES, attr, cockpitMat);
        cockpitBuilder.box(0.6f, 0.32f, 0.85f);

        // Swept Wings
        Material wingMat = new Material(
                ColorAttribute.createDiffuse(0.16f, 0.20f, 0.26f, 1.0f),
                ColorAttribute.createSpecular(0.45f, 0.55f, 0.65f, 1.0f)
        );
        MeshPartBuilder wingBuilder = mb.part("wings", GL20.GL_TRIANGLES, attr, wingMat);
        wingBuilder.box(3.0f, 0.08f, 1.3f);

        // Engine Nacelles
        Material nacelleMat = new Material(
                ColorAttribute.createDiffuse(0.14f, 0.16f, 0.20f, 1.0f),
                ColorAttribute.createSpecular(0.8f, 0.8f, 0.9f, 1.0f)
        );
        MeshPartBuilder nacelleBuilder = mb.part("nacelles", GL20.GL_TRIANGLES, attr, nacelleMat);
        nacelleBuilder.box(0.34f, 0.34f, 1.5f);

        // Engine Bells (Exhaust Ports)
        Material bellMat = new Material(
                ColorAttribute.createDiffuse(0.2f, 0.8f, 1.0f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.4f, 0.9f, 1.0f, 1.0f)
        );
        MeshPartBuilder bellBuilder = mb.part("bells", GL20.GL_TRIANGLES, attr, bellMat);
        bellBuilder.cylinder(0.28f, 0.18f, 0.28f, 16);

        return mb.end();
    }

    /**
     * Reconfigures lighting, models, and camera parameters for the target ending outcome.
     */
    public void setupEnding(EndingResult.Type type) {
        this.currentType = (type != null) ? type : EndingResult.Type.ENDING_A_SUCCESSFUL_RETURN;
        this.cinematicTime = 0.0f;
        this.beaconTimer = 0.0f;
        this.beaconIntensity = 0.0f;
        this.shipPitch = 0.0f;
        this.shipYaw = 0.0f;
        this.shipRoll = 0.0f;

        switch (currentType) {
            case ENDING_A_SUCCESSFUL_RETURN -> {
                // Ambient & Directional Warm Sunlight
                environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.08f, 0.10f, 0.14f, 1.0f));
                sunLight.set(1.0f, 0.95f, 0.85f, -1.0f, -0.6f, -0.8f);
                fillLight.set(0.15f, 0.45f, 0.70f, 0.8f, 0.4f, 0.5f);
                dynamicPointLight.set(0f, 0f, 0f, 0f, 0f, 0f, 0f);

                // Spacecraft initial posture: Controlled orbital approach
                shipInstance.transform.idt();
                shipInstance.transform.setToTranslation(-1.2f, 0.25f, -2.2f);
                shipInstance.transform.rotate(Vector3.Y, 24.0f);
                shipInstance.transform.rotate(Vector3.Z, 12.0f);
                shipInstance.transform.scale(0.85f, 0.85f, 0.85f);

                // Earth positioning: Framed proudly in background-right
                earthInstance.transform.idt();
                earthInstance.transform.setToTranslation(2.8f, -0.5f, -7.2f);
                earthInstance.transform.rotate(Vector3.Z, -18.0f);

                earthCloudInstance.transform.idt();
                earthCloudInstance.transform.setToTranslation(2.8f, -0.5f, -7.2f);
                earthCloudInstance.transform.rotate(Vector3.Z, -18.0f);
            }
            case ENDING_B_LOST_IN_SPACE -> {
                // Stark cold void lighting
                environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.02f, 0.025f, 0.04f, 1.0f));
                sunLight.set(0.18f, 0.22f, 0.35f, -0.8f, -0.3f, -0.5f); // Dim distant starlight
                fillLight.set(0.04f, 0.05f, 0.09f, 0.5f, 0.8f, 0.2f);
                dynamicPointLight.set(Color.RED, 0.0f, 0.2f, -3.2f, 8.0f);

                // Spacecraft initial posture: Adrift, lifeless
                shipInstance.transform.idt();
                shipInstance.transform.setToTranslation(-0.4f, -0.1f, -3.2f);
                shipInstance.transform.scale(0.85f, 0.85f, 0.85f);
            }
            case ENDING_C_THE_UNKNOWN -> {
                // Mystical violet & transcendent magenta environment
                environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.12f, 0.08f, 0.20f, 1.0f));
                sunLight.set(0.85f, 0.40f, 0.70f, -1.0f, -0.5f, -0.8f);
                fillLight.set(0.20f, 0.80f, 1.0f, 0.8f, 0.5f, 0.4f);
                dynamicPointLight.set(0.85f, 0.45f, 1.0f, -0.6f, 1.2f, -4.5f, 12.0f);

                // Kepler-186f in midground-right
                keplerInstance.transform.idt();
                keplerInstance.transform.setToTranslation(3.6f, -0.6f, -8.8f);
                keplerHaloInstance.transform.idt();
                keplerHaloInstance.transform.setToTranslation(3.6f, -0.6f, -8.8f);

                // Spire Monolith & Quantum Rings at center-left
                spireInstance.transform.idt();
                spireInstance.transform.setToTranslation(-0.6f, -0.9f, -4.5f);

                ringOuterInstance.transform.idt();
                ringOuterInstance.transform.setToTranslation(-0.6f, 1.1f, -4.5f);

                ringInnerInstance.transform.idt();
                ringInnerInstance.transform.setToTranslation(-0.6f, 1.1f, -4.5f);

                artifactInstance.transform.idt();
                artifactInstance.transform.setToTranslation(-0.6f, 1.1f, -4.5f);

                // Vessel in high observant parking orbit
                shipInstance.transform.idt();
                shipInstance.transform.setToTranslation(-2.6f, 1.6f, -6.8f);
                shipInstance.transform.rotate(Vector3.Y, 55.0f);
                shipInstance.transform.rotate(Vector3.X, 10.0f);
                shipInstance.transform.scale(0.70f, 0.70f, 0.70f);
            }
        }
    }

    public void resetCinematic() {
        setupEnding(currentType);
    }

    public void update(float delta) {
        cinematicTime += delta;

        // 1. Starfield Continuous Parallax
        for (int i = 0; i < STAR_COUNT; i++) {
            float speedMult = (currentType == EndingResult.Type.ENDING_B_LOST_IN_SPACE) ? 0.15f : 0.40f;
            starPositions[i].x -= delta * starSpeeds[i] * speedMult;
            if (starPositions[i].x < -65.0f) {
                starPositions[i].x = 65.0f;
                starPositions[i].y = MathUtils.random(-45.0f, 45.0f);
            }
        }

        // 2. Branch Sequence Choreography
        switch (currentType) {
            case ENDING_A_SUCCESSFUL_RETURN -> updateEndingA(delta);
            case ENDING_B_LOST_IN_SPACE -> updateEndingB(delta);
            case ENDING_C_THE_UNKNOWN -> updateEndingC(delta);
        }
    }

    private void updateEndingA(float delta) {
        // A. Earth Rotation
        float earthSpin = 2.5f * delta;
        earthInstance.transform.rotate(Vector3.Y, earthSpin);
        earthCloudInstance.transform.rotate(Vector3.Y, earthSpin * 1.3f);

        // B. Spacecraft Gentle Orbital Drift & Banking
        float swayY = MathUtils.sin(cinematicTime * 0.7f) * 0.12f;
        float swayX = MathUtils.cos(cinematicTime * 0.5f) * 0.10f;
        shipInstance.transform.idt();
        shipInstance.transform.setToTranslation(-1.2f + swayX, 0.25f + swayY, -2.2f);
        shipInstance.transform.rotate(Vector3.Y, 24.0f + MathUtils.cos(cinematicTime * 0.5f) * 4.0f);
        shipInstance.transform.rotate(Vector3.Z, 12.0f + MathUtils.sin(cinematicTime * 0.7f) * 3.0f);
        shipInstance.transform.scale(0.85f, 0.85f, 0.85f);

        // C. Camera Majestic Orbital Pan
        float camX = -0.4f + MathUtils.sin(cinematicTime * 0.25f) * 0.35f;
        float camY = 0.75f + MathUtils.cos(cinematicTime * 0.20f) * 0.18f;
        float camZ = 6.2f - Math.min(cinematicTime * 0.12f, 1.2f);
        camera.position.set(camX, camY, camZ);
        camera.lookAt(0.3f, 0.0f, -2.0f);
        camera.update();

        // D. Twin Engine Exhaust Particles
        for (int i = 0; i < EXHAUST_COUNT; i++) {
            exhaustLifetimes[i] += delta * 2.8f;
            if (exhaustLifetimes[i] > 1.0f) {
                exhaustLifetimes[i] = 0.0f;
                // Emit from engine bells behind vessel
                float offsetSide = (i % 2 == 0) ? -0.32f : 0.32f;
                exhaustPositions[i].set(-1.2f + offsetSide, 0.22f + swayY, -1.0f);
            } else {
                exhaustPositions[i].z += delta * 4.8f;
                exhaustPositions[i].y -= delta * 0.15f;
            }
        }
    }

    private void updateEndingB(float delta) {
        // A. Spacecraft Tumbling Adrift (No engine power, multi-axial rotation)
        shipPitch += 5.5f * delta;
        shipYaw += 3.8f * delta;
        shipRoll += 7.2f * delta;

        float driftZ = -3.2f - (cinematicTime * 0.08f);
        float driftX = -0.4f + MathUtils.sin(cinematicTime * 0.2f) * 0.15f;
        float driftY = -0.1f - MathUtils.cos(cinematicTime * 0.15f) * 0.10f;

        shipInstance.transform.idt();
        shipInstance.transform.setToTranslation(driftX, driftY, driftZ);
        shipInstance.transform.rotate(Vector3.X, shipPitch);
        shipInstance.transform.rotate(Vector3.Y, shipYaw);
        shipInstance.transform.rotate(Vector3.Z, shipRoll);
        shipInstance.transform.scale(0.85f, 0.85f, 0.85f);

        // B. Emergency Distress Beacon Strobe
        beaconTimer += delta;
        if (beaconTimer >= 1.9f) {
            beaconTimer = 0.0f;
            beaconIntensity = 1.0f; // Sharp red strobe flash
        } else {
            beaconIntensity = Math.max(0.0f, beaconIntensity - delta * 2.5f); // Fast decay
        }

        // Modulate point light color based on beacon intensity
        dynamicPointLight.setColor(beaconIntensity, 0.05f * beaconIntensity, 0.05f * beaconIntensity, 1.0f);
        dynamicPointLight.setPosition(driftX, driftY + 0.3f, driftZ);
        dynamicPointLight.setIntensity(beaconIntensity * 14.0f);

        // C. Camera Solitary Pull-Back Drift (Isolating the drifting vessel)
        float camZ = 5.8f + Math.min(cinematicTime * 0.14f, 2.4f);
        float camX = 0.15f + MathUtils.sin(cinematicTime * 0.12f) * 0.20f;
        camera.position.set(camX, 0.4f, camZ);
        camera.lookAt(driftX, driftY, driftZ);
        camera.update();
    }

    private void updateEndingC(float delta) {
        // A. Kepler-186f Slow Axial Rotation
        keplerInstance.transform.rotate(Vector3.Y, 1.8f * delta);
        keplerHaloInstance.transform.rotate(Vector3.Y, 2.4f * delta);

        // B. Spire & Concentric Quantum Gateway Rings Rotation
        ringOuterInstance.transform.rotate(Vector3.X, 42.0f * delta);
        ringOuterInstance.transform.rotate(Vector3.Y, 25.0f * delta);

        ringInnerInstance.transform.rotate(Vector3.Z, -55.0f * delta);
        ringInnerInstance.transform.rotate(Vector3.Y, 38.0f * delta);

        // C. Floating Alien Artifact Levitation & Spin
        float hoverY = 1.1f + MathUtils.sin(cinematicTime * 1.8f) * 0.18f;
        artifactInstance.transform.idt();
        artifactInstance.transform.setToTranslation(-0.6f, hoverY, -4.5f);
        artifactInstance.transform.rotate(Vector3.Y, cinematicTime * 60.0f);
        artifactInstance.transform.rotate(Vector3.X, MathUtils.sin(cinematicTime * 1.2f) * 15.0f);

        // D. Transcendent Aura Particle Swirl
        for (int i = 0; i < AURA_COUNT; i++) {
            auraAngles[i] += delta * auraSpeeds[i];
            float rad = 0.9f + (i % 4) * 0.35f;
            float px = -0.6f + MathUtils.cos(auraAngles[i]) * rad;
            float py = hoverY + MathUtils.sin(auraAngles[i] * 1.5f) * 0.45f;
            float pz = -4.5f + MathUtils.sin(auraAngles[i]) * rad;
            auraPositions[i].set(px, py, pz);
        }

        // Dynamic Ethereal Point Light Oscillating
        float lightIntensity = 8.0f + MathUtils.sin(cinematicTime * 2.5f) * 3.5f;
        dynamicPointLight.setPosition(-0.6f, hoverY, -4.5f);
        dynamicPointLight.setIntensity(lightIntensity);

        // E. Camera Ascending Spiral Choreography
        float spiralAngle = cinematicTime * 0.22f;
        float camX = -0.6f + MathUtils.sin(spiralAngle) * 4.6f;
        float camZ = -4.5f + MathUtils.cos(spiralAngle) * 4.6f;
        float camY = 0.8f + MathUtils.sin(cinematicTime * 0.3f) * 0.6f;
        camera.position.set(camX, camY, camZ);
        camera.lookAt(-0.6f, hoverY, -4.5f);
        camera.update();
    }

    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Background void clear color based on outcome
        if (currentType == EndingResult.Type.ENDING_B_LOST_IN_SPACE) {
            Gdx.gl.glClearColor(0.005f, 0.008f, 0.015f, 1.0f);
        } else if (currentType == EndingResult.Type.ENDING_C_THE_UNKNOWN) {
            Gdx.gl.glClearColor(0.02f, 0.015f, 0.038f, 1.0f);
        } else {
            Gdx.gl.glClearColor(0.012f, 0.022f, 0.045f, 1.0f);
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // 1. Render Dynamic Starfield
        renderStarfield();

        // 2. Render 3D Model Batch
        modelBatch.begin(camera);

        switch (currentType) {
            case ENDING_A_SUCCESSFUL_RETURN -> {
                modelBatch.render(earthInstance, environment);
                modelBatch.render(earthCloudInstance, environment);
                modelBatch.render(shipInstance, environment);
            }
            case ENDING_B_LOST_IN_SPACE -> {
                modelBatch.render(distantStarInstance, environment);
                modelBatch.render(shipInstance, environment);
            }
            case ENDING_C_THE_UNKNOWN -> {
                modelBatch.render(keplerInstance, environment);
                modelBatch.render(keplerHaloInstance, environment);
                modelBatch.render(spireInstance, environment);
                modelBatch.render(ringOuterInstance, environment);
                modelBatch.render(ringInnerInstance, environment);
                modelBatch.render(artifactInstance, environment);
                modelBatch.render(shipInstance, environment);
            }
        }

        modelBatch.end();

        // 3. Render Particles (Thrusters, Aura, or Beacon Strobe)
        if (currentType == EndingResult.Type.ENDING_A_SUCCESSFUL_RETURN) {
            renderExhaustParticles();
        } else if (currentType == EndingResult.Type.ENDING_C_THE_UNKNOWN) {
            renderAuraParticles();
        }
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
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void renderExhaustParticles() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

        starRenderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < EXHAUST_COUNT; i++) {
            float alpha = 1.0f - exhaustLifetimes[i];
            Vector3 p = exhaustPositions[i];
            starRenderer.color(0.2f, 0.85f, 1.0f, alpha * 0.85f);
            starRenderer.vertex(p.x, p.y, p.z);
        }
        starRenderer.end();

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void renderAuraParticles() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

        starRenderer.begin(camera.combined, GL20.GL_POINTS);
        for (int i = 0; i < AURA_COUNT; i++) {
            Vector3 p = auraPositions[i];
            float glow = 0.5f + MathUtils.sin(cinematicTime * 3.0f + i) * 0.4f;
            if (i % 2 == 0) {
                starRenderer.color(0.85f, 0.4f, 1.0f, glow * 0.9f); // Transcendent magenta
            } else {
                starRenderer.color(0.25f, 0.9f, 1.0f, glow * 0.9f); // Quantum cyan
            }
            starRenderer.vertex(p.x, p.y, p.z);
        }
        starRenderer.end();

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public EndingResult.Type getCurrentType() {
        return currentType;
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    public ModelInstance getShipInstance() {
        return shipInstance;
    }

    public ModelInstance getEarthInstance() {
        return earthInstance;
    }

    public ModelInstance getDistantStarInstance() {
        return distantStarInstance;
    }

    public ModelInstance getKeplerInstance() {
        return keplerInstance;
    }

    public ModelInstance getSpireInstance() {
        return spireInstance;
    }

    public ModelInstance getArtifactInstance() {
        return artifactInstance;
    }

    public float getCinematicTime() {
        return cinematicTime;
    }

    public float getBeaconIntensity() {
        return beaconIntensity;
    }

    public void resize(int width, int height) {
        if (camera != null) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }
    }

    @Override
    public void dispose() {
        if (modelBatch != null) modelBatch.dispose();
        if (starRenderer != null) starRenderer.dispose();
        for (Model m : allocatedModels) {
            if (m != null) m.dispose();
        }
        allocatedModels.clear();
    }
}

