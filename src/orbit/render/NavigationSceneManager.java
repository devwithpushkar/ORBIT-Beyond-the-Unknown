package orbit.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;
import orbit.models.Planet;
import orbit.render.theme.CinematicTheme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NavigationSceneManager renders the dedicated 3D interactive holographic galaxy
 * emerging from the spacecraft's physical navigation console table.
 *
 * Provides:
 * - Physical metallic console table and holographic emitter in the foreground
 * - Volumetric-looking 3D planetary system with 5 distinct destinations (Earth, Mars, Europa, Titan, Kepler-186f)
 * - Animated orbital paths and celestial revolution
 * - Interactive orbit camera with mouse drag and zoom controls
 * - 3D ray-picking for direct planet selection
 * - Warp transit animation with starfield acceleration
 */
public class NavigationSceneManager implements Disposable {

    private final AssetManager assetManager;
    private final PerspectiveCamera camera;
    private final ModelBatch modelBatch;
    private final Environment environment;

    // Camera Orbit Controls
    private float camYaw = 45f;         // Degrees
    private float camPitch = 36f;       // Degrees
    private float camDistance = 21.0f;  // Units
    private final Vector3 camTarget = new Vector3(0f, 0.9f, 0f);
    private final Vector3 camPos = new Vector3();

    // 3D Models
    private Model holotableModel;
    private ModelInstance holotableInstance;

    private Model solModel;
    private ModelInstance solInstance;

    // Procedural 3D Holographic Orbit Ring & Grid Lines
    private Mesh orbitRingsMesh;
    private ShaderProgram hologramShader;

    // Planetary Nodes
    public static class PlanetVisualNode {
        public final String name;
        public final float orbitRadius;
        public final float scaleRadius;
        public final float orbitSpeed;
        public final float spinSpeed;
        public float currentAngle;
        public final Vector3 worldPos = new Vector3();
        public Model model;
        public ModelInstance instance;
        public Model cloudModel;
        public ModelInstance cloudInstance;
        public Model markerModel;
        public ModelInstance markerInstance;

        public PlanetVisualNode(String name, float orbitRadius, float scaleRadius, float orbitSpeed, float spinSpeed, float startAngle) {
            this.name = name;
            this.orbitRadius = orbitRadius;
            this.scaleRadius = scaleRadius;
            this.orbitSpeed = orbitSpeed;
            this.spinSpeed = spinSpeed;
            this.currentAngle = startAngle;
        }
    }

    private final Map<String, PlanetVisualNode> planetNodes = new HashMap<>();
    private final List<PlanetVisualNode> nodeList = new ArrayList<>();

    // Starfield
    private static final int STAR_COUNT = 900;
    private Mesh starMesh;
    private ShaderProgram starShader;
    private float starDriftX = 0f;
    private float starSpeed = 0.5f;

    // Warp Transit State
    private boolean isWarping = false;
    private float warpTimer = 0f;
    private float warpDuration = 2.4f;
    private Runnable warpCompleteCallback;
    private String transitDestinationName = "";

    // Selection & Telemetry State
    private String selectedPlanetName = "Mars";
    private String currentLocationName = "Earth";

    // Reusable math objects (Zero allocation in render/update)
    private final Vector3 tempVec1 = new Vector3();
    private final Vector3 tempVec2 = new Vector3();
    private final Vector3 intersection = new Vector3();
    private final Matrix4 tempMat = new Matrix4();

    public NavigationSceneManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.modelBatch = new ModelBatch();

        // 1. Perspective Camera
        this.camera = new PerspectiveCamera(62f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera.near = 0.1f;
        this.camera.far = 350f;
        updateCameraPosition();

        // 2. Lighting Environment
        this.environment = new Environment();
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.12f, 0.16f, 0.22f, 1.0f));
        // Sol light emission from central stellar anchor
        this.environment.add(new DirectionalLight().set(1.0f, 0.95f, 0.88f, -0.6f, -0.4f, -0.7f));
        // Upward holographic projector wash
        this.environment.add(new DirectionalLight().set(0.15f, 0.45f, 0.70f, 0.0f, 1.0f, 0.0f));

        initHolotableGeometry();
        initSolStellarCore();
        initPlanetaryNodes();
        initHolographicOrbitsAndGrid();
        initOptimizedStarfield();
    }

    /**
     * Constructs the physical command holotable base and emitter rings in the foreground.
     */
    private void initHolotableGeometry() {
        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        long attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked;

        // 1. Heavy Titanium Console Table Pedestal & Chamfered Top
        Material chassisMat = new Material(
                ColorAttribute.createDiffuse(0.06f, 0.09f, 0.14f, 1.0f),
                ColorAttribute.createSpecular(0.45f, 0.50f, 0.60f, 1.0f)
        );

        MeshPartBuilder baseBuilder = mb.part("pedestal", GL20.GL_TRIANGLES, attr, chassisMat);
        baseBuilder.cylinder(8.4f, 1.2f, 8.4f, 24);

        MeshPartBuilder topBuilder = mb.part("tableTop", GL20.GL_TRIANGLES, attr, chassisMat);
        topBuilder.cylinder(11.2f, 0.45f, 11.2f, 24);

        // 2. Outer Glowing Holographic Emitter Ring (Electric Cyan)
        Material cyanRingMat = new Material(
                ColorAttribute.createDiffuse(0.0f, 0.85f, 1.0f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.0f, 0.75f, 0.95f, 1.0f)
        );
        MeshPartBuilder cyanRing = mb.part("cyanRing", GL20.GL_TRIANGLES, attr, cyanRingMat);
        cyanRing.cylinder(10.6f, 0.06f, 10.6f, 24);

        // 3. Inner Focusing Bezel (Warm Amber Avionics Ring)
        Material amberRingMat = new Material(
                ColorAttribute.createDiffuse(0.95f, 0.65f, 0.12f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.85f, 0.55f, 0.10f, 1.0f)
        );
        MeshPartBuilder amberRing = mb.part("amberRing", GL20.GL_TRIANGLES, attr, amberRingMat);
        amberRing.cylinder(7.8f, 0.08f, 7.8f, 24);

        // 4. Central Projector Iris Plate
        Material irisMat = new Material(
                ColorAttribute.createDiffuse(0.03f, 0.05f, 0.09f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.1f, 0.3f, 0.5f, 1.0f)
        );
        MeshPartBuilder iris = mb.part("iris", GL20.GL_TRIANGLES, attr, irisMat);
        iris.cylinder(4.2f, 0.10f, 4.2f, 24);

        holotableModel = mb.end();
        holotableInstance = new ModelInstance(holotableModel);
        holotableInstance.transform.setToTranslation(0f, -1.8f, 0f);
    }

    /**
     * Central stellar anchor representing the host star (Sol).
     */
    private void initSolStellarCore() {
        ModelBuilder mb = new ModelBuilder();
        long attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

        Material solMat = new Material(
                ColorAttribute.createDiffuse(1.0f, 0.85f, 0.35f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.95f, 0.75f, 0.25f, 1.0f)
        );
        solModel = mb.createSphere(1.1f, 1.1f, 1.1f, 24, 24, solMat, attr);
        solInstance = new ModelInstance(solModel);
        solInstance.transform.setToTranslation(0f, 1.2f, 0f);
    }

    /**
     * Initializes the 5 distinct planetary nodes with customized 3D meshes,
     * procedural textures, and orbital configurations.
     */
    private void initPlanetaryNodes() {
        ModelBuilder mb = new ModelBuilder();
        long attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;
        long markerAttr = VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked;

        // Generic marker geometry (Diamond targeting reticle)
        Material markerMat = new Material(
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE),
                ColorAttribute.createDiffuse(0.0f, 0.95f, 1.0f, 0.9f)
        );
        Model sharedMarkerModel = mb.createCone(0.5f, 0.7f, 0.5f, 6, markerMat, markerAttr);

        // --- 1. Earth (Sol-01: Temperate Biosphere) ---
        PlanetVisualNode earth = new PlanetVisualNode("Earth", 3.8f, 0.85f, 0.18f, 12f, 0.4f);
        Material earthMat = new Material(
                TextureAttribute.createDiffuse(assetManager.getEarthTexture()),
                ColorAttribute.createSpecular(0.35f, 0.45f, 0.6f, 1.0f)
        );
        earth.model = mb.createSphere(earth.scaleRadius * 2f, earth.scaleRadius * 2f, earth.scaleRadius * 2f, 32, 32, earthMat, attr);
        earth.instance = new ModelInstance(earth.model);

        Material earthCloudMat = new Material(
                TextureAttribute.createDiffuse(assetManager.getCloudTexture()),
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.75f)
        );
        earth.cloudModel = mb.createSphere(earth.scaleRadius * 2.06f, earth.scaleRadius * 2.06f, earth.scaleRadius * 2.06f, 28, 28, earthCloudMat, attr);
        earth.cloudInstance = new ModelInstance(earth.cloudModel);
        earth.markerModel = sharedMarkerModel;
        earth.markerInstance = new ModelInstance(sharedMarkerModel);
        addNode(earth);

        // --- 2. Mars (Sol-02: Arid Desert Red Planet) ---
        PlanetVisualNode mars = new PlanetVisualNode("Mars", 5.8f, 0.55f, 0.12f, 14f, 1.8f);
        Material marsMat = new Material(
                TextureAttribute.createDiffuse(assetManager.getMarsTexture()),
                ColorAttribute.createSpecular(0.2f, 0.15f, 0.1f, 1.0f)
        );
        mars.model = mb.createSphere(mars.scaleRadius * 2f, mars.scaleRadius * 2f, mars.scaleRadius * 2f, 28, 28, marsMat, attr);
        mars.instance = new ModelInstance(mars.model);
        mars.markerModel = sharedMarkerModel;
        mars.markerInstance = new ModelInstance(sharedMarkerModel);
        addNode(mars);

        // --- 3. Europa (Jov-01: Cryogenic Ice Moon with Lineae Cracks) ---
        PlanetVisualNode europa = new PlanetVisualNode("Europa", 8.2f, 0.42f, 0.08f, 18f, 3.4f);
        Material europaMat = new Material(
                TextureAttribute.createDiffuse(assetManager.getEuropaTexture()),
                ColorAttribute.createSpecular(0.7f, 0.8f, 0.95f, 1.0f) // High icy specular albedo
        );
        europa.model = mb.createSphere(europa.scaleRadius * 2f, europa.scaleRadius * 2f, europa.scaleRadius * 2f, 24, 24, europaMat, attr);
        europa.instance = new ModelInstance(europa.model);
        europa.markerModel = sharedMarkerModel;
        europa.markerInstance = new ModelInstance(sharedMarkerModel);
        addNode(europa);

        // --- 4. Titan (Sat-01: Dense Amber Nitrogen Smog & Methane Seas) ---
        PlanetVisualNode titan = new PlanetVisualNode("Titan", 10.8f, 0.58f, 0.055f, 10f, 4.9f);
        Material titanMat = new Material(
                TextureAttribute.createDiffuse(assetManager.getTitanTexture()),
                ColorAttribute.createSpecular(0.15f, 0.12f, 0.08f, 1.0f)
        );
        titan.model = mb.createSphere(titan.scaleRadius * 2f, titan.scaleRadius * 2f, titan.scaleRadius * 2f, 28, 28, titanMat, attr);
        titan.instance = new ModelInstance(titan.model);

        // Amber smog haze shell
        Material titanHazeMat = new Material(
                ColorAttribute.createDiffuse(0.95f, 0.65f, 0.18f, 0.45f),
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE, 0.40f)
        );
        titan.cloudModel = mb.createSphere(titan.scaleRadius * 2.12f, titan.scaleRadius * 2.12f, titan.scaleRadius * 2.12f, 24, 24, titanHazeMat, attr);
        titan.cloudInstance = new ModelInstance(titan.cloudModel);
        titan.markerModel = sharedMarkerModel;
        titan.markerInstance = new ModelInstance(sharedMarkerModel);
        addNode(titan);

        // --- 5. Kepler-186f (Exo-01: Habitable Zone Alien Exoplanet) ---
        PlanetVisualNode kepler = new PlanetVisualNode("Kepler-186f", 14.2f, 0.90f, 0.035f, 8f, 0.9f);
        Material keplerMat = new Material(
                TextureAttribute.createDiffuse(assetManager.getKeplerTexture()),
                ColorAttribute.createSpecular(0.5f, 0.3f, 0.65f, 1.0f)
        );
        kepler.model = mb.createSphere(kepler.scaleRadius * 2f, kepler.scaleRadius * 2f, kepler.scaleRadius * 2f, 32, 32, keplerMat, attr);
        kepler.instance = new ModelInstance(kepler.model);

        // Exotic violet/auroral luminescence shell
        Material keplerHaloMat = new Material(
                ColorAttribute.createDiffuse(0.75f, 0.25f, 0.85f, 0.40f),
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE, 0.35f)
        );
        kepler.cloudModel = mb.createSphere(kepler.scaleRadius * 2.08f, kepler.scaleRadius * 2.08f, kepler.scaleRadius * 2.08f, 28, 28, keplerHaloMat, attr);
        kepler.cloudInstance = new ModelInstance(kepler.cloudModel);
        kepler.markerModel = sharedMarkerModel;
        kepler.markerInstance = new ModelInstance(sharedMarkerModel);
        addNode(kepler);
    }

    private void addNode(PlanetVisualNode node) {
        planetNodes.put(node.name.toLowerCase().trim(), node);
        nodeList.add(node);
    }

    /**
     * Generates 3D holographic orbit rings and radial grid markers in the projection volume.
     */
    private void initHolographicOrbitsAndGrid() {
        int segmentsPerCircle = 64;
        int totalCircles = nodeList.size();
        int totalRays = 8;
        int totalVertices = (totalCircles * segmentsPerCircle * 2) + (totalRays * 2);

        float[] vertices = new float[totalVertices * 4]; // pos(3), colorPacked(1)
        int idx = 0;

        Color cyan = new Color(0.0f, 0.85f, 1.0f, 0.45f);
        Color gold = new Color(1.0f, 0.72f, 0.15f, 0.50f);

        // 1. Orbital Ring Loops
        for (PlanetVisualNode node : nodeList) {
            float r = node.orbitRadius;
            Color c = node.name.equalsIgnoreCase("Kepler-186f") ? gold : cyan;
            float cBits = c.toFloatBits();

            for (int i = 0; i < segmentsPerCircle; i++) {
                float a1 = ((float) i / segmentsPerCircle) * MathUtils.PI2;
                float a2 = ((float) (i + 1) / segmentsPerCircle) * MathUtils.PI2;

                float x1 = MathUtils.cos(a1) * r;
                float z1 = MathUtils.sin(a1) * r;
                float x2 = MathUtils.cos(a2) * r;
                float z2 = MathUtils.sin(a2) * r;

                vertices[idx++] = x1;
                vertices[idx++] = 1.2f;
                vertices[idx++] = z1;
                vertices[idx++] = cBits;

                vertices[idx++] = x2;
                vertices[idx++] = 1.2f;
                vertices[idx++] = z2;
                vertices[idx++] = cBits;
            }
        }

        // 2. Radial Quadrant Lines
        Color dimRayColor = new Color(0.0f, 0.45f, 0.65f, 0.25f);
        float rayBits = dimRayColor.toFloatBits();
        for (int i = 0; i < totalRays; i++) {
            float angle = ((float) i / totalRays) * MathUtils.PI2;
            float x2 = MathUtils.cos(angle) * 16.5f;
            float z2 = MathUtils.sin(angle) * 16.5f;

            vertices[idx++] = 0f;
            vertices[idx++] = 1.2f;
            vertices[idx++] = 0f;
            vertices[idx++] = rayBits;

            vertices[idx++] = x2;
            vertices[idx++] = 1.2f;
            vertices[idx++] = z2;
            vertices[idx++] = rayBits;
        }

        orbitRingsMesh = new Mesh(true, totalVertices, 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE)
        );
        orbitRingsMesh.setVertices(vertices);

        // Hardware Hologram Line Shader
        String vs = "attribute vec4 a_position;\n"
                + "attribute vec4 a_color;\n"
                + "uniform mat4 u_projTrans;\n"
                + "varying vec4 v_color;\n"
                + "void main() {\n"
                + "    v_color = a_color;\n"
                + "    gl_Position = u_projTrans * a_position;\n"
                + "}\n";

        String fs = "#ifdef GL_ES\nprecision mediump float;\n#endif\n"
                + "varying vec4 v_color;\n"
                + "void main() {\n"
                + "    gl_FragColor = v_color;\n"
                + "}\n";

        hologramShader = new ShaderProgram(vs, fs);
        if (!hologramShader.isCompiled()) {
            System.out.println("[ORBIT-NAV] Hologram shader compile log: " + hologramShader.getLog());
        }
    }

    /**
     * Initializes background starfield mesh.
     */
    private void initOptimizedStarfield() {
        starMesh = new Mesh(true, STAR_COUNT, 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE)
        );

        float[] vertices = new float[STAR_COUNT * 4];
        int idx = 0;
        for (int i = 0; i < STAR_COUNT; i++) {
            float x = MathUtils.random(-120f, 120f);
            float y = MathUtils.random(-80f, 80f);
            float z = MathUtils.random(-140f, -25f);

            float depthNorm = MathUtils.clamp((-z - 25f) / 115f, 0f, 1f);
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

        String vs = "attribute vec4 a_position;\n"
                + "attribute vec4 a_color;\n"
                + "uniform mat4 u_projTrans;\n"
                + "varying vec4 v_color;\n"
                + "void main() {\n"
                + "    v_color = a_color;\n"
                + "    gl_Position = u_projTrans * a_position;\n"
                + "    gl_PointSize = 2.0;\n"
                + "}\n";

        String fs = "#ifdef GL_ES\nprecision mediump float;\n#endif\n"
                + "varying vec4 v_color;\n"
                + "void main() {\n"
                + "    gl_FragColor = v_color;\n"
                + "}\n";

        starShader = new ShaderProgram(vs, fs);
    }

    /**
     * Updates orbit rotations, celestial axial spin, and camera motion.
     */
    public void update(float delta) {
        // 1. Planetary Orbital Revolutions and Axial Rotations
        for (PlanetVisualNode node : nodeList) {
            node.currentAngle += node.orbitSpeed * delta;
            if (node.currentAngle > MathUtils.PI2) node.currentAngle -= MathUtils.PI2;

            float x = MathUtils.cos(node.currentAngle) * node.orbitRadius;
            float z = MathUtils.sin(node.currentAngle) * node.orbitRadius;
            float y = 1.2f;

            node.worldPos.set(x, y, z);

            // Update Planet 3D Transform
            node.instance.transform.setToTranslation(x, y, z);
            node.instance.transform.rotate(Vector3.Y, node.currentAngle * 25f + node.spinSpeed * delta);

            // Update Cloud / Atmosphere Haze Shell
            if (node.cloudInstance != null) {
                node.cloudInstance.transform.setToTranslation(x, y, z);
                node.cloudInstance.transform.rotate(Vector3.Y, node.currentAngle * 30f);
            }

            // Update Holographic Marker Float Beacon
            if (node.markerInstance != null) {
                float markerY = y + node.scaleRadius + 0.45f + MathUtils.sin(node.currentAngle * 4f) * 0.08f;
                node.markerInstance.transform.setToTranslation(x, markerY, z);
                node.markerInstance.transform.rotate(Vector3.Y, delta * 90f);
            }
        }

        // 2. Rotate Sol Core
        solInstance.transform.rotate(Vector3.Y, delta * 8f);

        // 3. Starfield Parallax Drift / Warp Streaks
        if (isWarping) {
            warpTimer += delta;
            starSpeed = MathUtils.lerp(starSpeed, 42.0f, delta * 4f);
            camDistance = MathUtils.lerp(camDistance, 9.5f, delta * 2.5f);

            // Warp completion trigger
            if (warpTimer >= warpDuration) {
                isWarping = false;
                starSpeed = 0.5f;
                if (warpCompleteCallback != null) {
                    warpCompleteCallback.run();
                }
            }
        } else {
            starSpeed = MathUtils.lerp(starSpeed, 0.5f, delta * 2f);
        }

        starDriftX -= delta * starSpeed;
        if (starDriftX < -120f) starDriftX = 0f;

        // 4. Update Camera Projection
        updateCameraPosition();
    }

    /**
     * Renders the complete 3D scene: starfield, holotable, central Sol core,
     * holographic orbit paths, and 5 celestial destinations.
     */
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.012f, 0.018f, 0.032f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // 1. Background Starfield
        renderStarfield();

        // 2. 3D Physical Table and Celestial Spheres
        modelBatch.begin(camera);
        modelBatch.render(holotableInstance, environment);
        modelBatch.render(solInstance, environment);

        for (PlanetVisualNode node : nodeList) {
            modelBatch.render(node.instance, environment);
            if (node.cloudInstance != null) {
                modelBatch.render(node.cloudInstance, environment);
            }
            if (node.markerInstance != null) {
                modelBatch.render(node.markerInstance, environment);
            }
        }
        modelBatch.end();

        // 3. Holographic Orbit Lines and Quadrant Grid (Additive blend)
        renderHolographicOrbits();
    }

    private void renderStarfield() {
        if (starShader == null || !starShader.isCompiled() || starMesh == null) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

        tempMat.set(camera.combined).translate(starDriftX, 0f, 0f);

        starShader.bind();
        starShader.setUniformMatrix("u_projTrans", tempMat);
        starMesh.render(starShader, GL20.GL_POINTS);

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    private void renderHolographicOrbits() {
        if (hologramShader == null || !hologramShader.isCompiled() || orbitRingsMesh == null) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        Gdx.gl.glDepthMask(false);

        hologramShader.bind();
        hologramShader.setUniformMatrix("u_projTrans", camera.combined);
        orbitRingsMesh.render(hologramShader, GL20.GL_LINES);

        Gdx.gl.glDepthMask(true);
    }

    /**
     * Ray-picks the celestial body intersecting the 2D mouse coordinate.
     * Uses bounding sphere tests against each planet's current world position.
     *
     * @param screenX Mouse X in screen coordinates
     * @param screenY Mouse Y in screen coordinates
     * @param planets The list of game planets from controller
     * @return The picked Planet or null if background clicked
     */
    public Planet getPlanetAtScreen(float screenX, float screenY, List<Planet> planets) {
        if (planets == null || planets.isEmpty()) return null;

        Ray pickRay = camera.getPickRay(screenX, screenY);
        Planet closestPlanet = null;
        float closestDistance = Float.MAX_VALUE;

        for (Planet p : planets) {
            PlanetVisualNode node = planetNodes.get(p.getName().toLowerCase().trim());
            if (node == null) continue;

            // Generous pick radius for effortless user interaction
            float pickRadius = node.scaleRadius + 0.65f;

            if (Intersector.intersectRaySphere(pickRay, node.worldPos, pickRadius, intersection)) {
                float dist = pickRay.origin.dst2(intersection);
                if (dist < closestDistance) {
                    closestDistance = dist;
                    closestPlanet = p;
                }
            }
        }
        return closestPlanet;
    }

    /**
     * Gets the visual node for a named planet.
     */
    public PlanetVisualNode getNode(String planetName) {
        if (planetName == null) return null;
        return planetNodes.get(planetName.toLowerCase().trim());
    }

    // --- Camera Controls & Input Helpers ---

    public void rotateCamera(float deltaYaw, float deltaPitch) {
        this.camYaw += deltaYaw;
        this.camPitch = MathUtils.clamp(this.camPitch + deltaPitch, 15f, 80f);
        updateCameraPosition();
    }

    public void zoomCamera(float deltaDistance) {
        this.camDistance = MathUtils.clamp(this.camDistance + deltaDistance, 7.5f, 32.0f);
        updateCameraPosition();
    }

    private void updateCameraPosition() {
        float pitchRad = camPitch * MathUtils.degreesToRadians;
        float yawRad = camYaw * MathUtils.degreesToRadians;

        float cosPitch = MathUtils.cos(pitchRad);
        float sinPitch = MathUtils.sin(pitchRad);

        float x = camDistance * cosPitch * MathUtils.sin(yawRad);
        float y = camTarget.y + camDistance * sinPitch;
        float z = camDistance * cosPitch * MathUtils.cos(yawRad);

        camPos.set(x, y, z);
        camera.position.set(camPos);
        camera.lookAt(camTarget);
        camera.update();
    }

    /**
     * Initiates the cinematic warp transit acceleration toward a new planetary destination.
     */
    public void startTransitBurn(Planet target, Runnable onComplete) {
        this.isWarping = true;
        this.warpTimer = 0f;
        this.transitDestinationName = (target != null) ? target.getName() : "";
        this.warpCompleteCallback = onComplete;
    }

    public boolean isWarping() {
        return isWarping;
    }

    public float getWarpProgress() {
        return MathUtils.clamp(warpTimer / warpDuration, 0f, 1f);
    }

    public void setSelectedPlanet(String name) {
        this.selectedPlanetName = (name != null) ? name : "";
    }

    public void setCurrentLocation(String name) {
        this.currentLocationName = (name != null) ? name : "";
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        holotableModel.dispose();
        solModel.dispose();

        for (PlanetVisualNode node : nodeList) {
            if (node.model != null) node.model.dispose();
            if (node.cloudModel != null) node.cloudModel.dispose();
        }

        if (orbitRingsMesh != null) orbitRingsMesh.dispose();
        if (hologramShader != null) hologramShader.dispose();
        if (starMesh != null) starMesh.dispose();
        if (starShader != null) starShader.dispose();
    }
}

