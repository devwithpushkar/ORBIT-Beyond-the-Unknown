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
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import orbit.models.Planet;

/**
 * ExplorationSceneManager renders the immersive 3D planetary reconnaissance environment.
 *
 * Visual Features:
 * - Close-orbital perspective of the currently orbited celestial body (Earth, Mars, Europa, Titan, Kepler-186f).
 * - Textured planetary sphere with rotational dynamics and axial tilt.
 * - Atmospheric cloud shell with independent rotational velocity.
 * - Holographic surface scan radar ring rotating around the planet with glowing telemetry markers.
 * - Starfield background in deep space.
 * - Dynamic dual-tone lighting (Sol sun exterior + orbital sensor suite fill).
 * - Organic sensor camera drift.
 */
public class ExplorationSceneManager implements Disposable {

    private final AssetManager assetManager;
    private final ModelBatch modelBatch;
    private final Environment environment;
    private final PerspectiveCamera camera;

    // 3D Celestial Models & Instances
    private Model planetModel;
    private Model cloudModel;
    private Model scanRingModel;

    private ModelInstance planetInstance;
    private ModelInstance cloudInstance;
    private ModelInstance scanRingInstance;

    private final Array<ModelInstance> instances = new Array<>();

    // Animation & Dynamics
    private float cameraTime = 0f;
    private float planetRotY = 0f;
    private float cloudRotY = 0f;
    private float scanRingRotZ = 0f;

    private String currentPlanetName = "earth";

    public ExplorationSceneManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.modelBatch = new ModelBatch();

        // 1. Perspective Camera
        this.camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera.position.set(0f, 0f, 0f);
        this.camera.lookAt(0f, 0f, -10f);
        this.camera.near = 0.1f;
        this.camera.far = 150f;
        this.camera.update();

        // 2. Lighting Environment
        this.environment = new Environment();
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.12f, 0.15f, 0.20f, 1.0f));

        // Sol Exterior Sunlight
        DirectionalLight sunLight = new DirectionalLight();
        sunLight.set(1.0f, 0.98f, 0.92f, -1.2f, -0.4f, -0.8f);
        this.environment.add(sunLight);

        // Orbital Sensor Fill Light (Cyan avionics accent)
        this.environment.add(new DirectionalLight().set(0.15f, 0.40f, 0.60f, 0.5f, 1.0f, 0.2f));

        initCelestialModels();
    }

    private void initCelestialModels() {
        ModelBuilder mb = new ModelBuilder();
        long attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

        // A. 3D Planet Sphere (Radius = 3.8f, positioned right-center at (3.2, -0.4, -9.0))
        mb.begin();
        MeshPartBuilder mpb = mb.part("planet", GL20.GL_TRIANGLES, attr,
                new Material(
                        TextureAttribute.createDiffuse(assetManager.getPlanetTextureByName(currentPlanetName)),
                        ColorAttribute.createSpecular(0.35f, 0.45f, 0.55f, 1.0f)
                ));
        mpb.sphere(7.6f, 7.6f, 7.6f, 48, 36);
        planetModel = mb.end();
        planetInstance = new ModelInstance(planetModel);
        instances.add(planetInstance);

        // B. Concentric Cloud Shell
        mb.begin();
        MeshPartBuilder cloudMpb = mb.part("clouds", GL20.GL_TRIANGLES, attr,
                new Material(
                        TextureAttribute.createDiffuse(assetManager.getCloudTexture()),
                        ColorAttribute.createSpecular(0.15f, 0.15f, 0.15f, 1.0f),
                        new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.40f)
                ));
        cloudMpb.sphere(7.85f, 7.85f, 7.85f, 40, 30);
        cloudModel = mb.end();
        cloudInstance = new ModelInstance(cloudModel);
        instances.add(cloudInstance);

        // C. Holographic Radar Scan Ring
        ModelBuilder ringMb = new ModelBuilder();
        ringMb.begin();
        long ringAttr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked;
        Material ringMat = new Material(
                ColorAttribute.createDiffuse(0.0f, 0.85f, 1.0f, 0.65f),
                new ColorAttribute(ColorAttribute.Emissive, 0.2f, 0.85f, 1.0f, 1.0f),
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.65f)
        );
        MeshPartBuilder ringBuilder = ringMb.part("scanRing", GL20.GL_TRIANGLES, ringAttr, ringMat);
        ringBuilder.box(0f, 0f, 0f, 9.2f, 0.08f, 9.2f);
        scanRingModel = ringMb.end();
        scanRingInstance = new ModelInstance(scanRingModel);
        instances.add(scanRingInstance);

        updateTransformMatrices();
    }

    public void updatePlanet(Planet planet) {
        if (planet == null) return;
        String name = planet.getName().toLowerCase();
        if (!name.equalsIgnoreCase(currentPlanetName)) {
            this.currentPlanetName = name;
            Material mat = planetInstance.materials.get(0);
            mat.set(TextureAttribute.createDiffuse(assetManager.getPlanetTextureByName(name)));
        }
    }

    private void updateTransformMatrices() {
        float posX = 3.2f;
        float posY = -0.4f;
        float posZ = -9.0f;

        // Planet Transform
        planetInstance.transform.setToTranslation(posX, posY, posZ);
        planetInstance.transform.rotate(Vector3.Z, -18.0f); // 18-deg axial tilt
        planetInstance.transform.rotate(Vector3.Y, planetRotY);

        // Cloud Transform
        cloudInstance.transform.setToTranslation(posX, posY, posZ);
        cloudInstance.transform.rotate(Vector3.Z, -18.0f);
        cloudInstance.transform.rotate(Vector3.Y, cloudRotY);

        // Radar Scan Ring Transform
        scanRingInstance.transform.setToTranslation(posX, posY, posZ);
        scanRingInstance.transform.rotate(Vector3.X, 42.0f);
        scanRingInstance.transform.rotate(Vector3.Z, scanRingRotZ);
    }

    public void update(float delta) {
        cameraTime += delta * 0.4f;
        planetRotY += delta * 3.5f;
        cloudRotY += delta * 4.8f;
        scanRingRotZ += delta * 15.0f;

        // Subtle microgravity sensor sway
        camera.position.set(
                MathUtils.sin(cameraTime) * 0.08f,
                MathUtils.cos(cameraTime * 0.8f) * 0.05f,
                0f
        );
        camera.lookAt(0f, 0f, -10f);
        camera.update();

        updateTransformMatrices();
    }

    public void render() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        if (planetModel != null) planetModel.dispose();
        if (cloudModel != null) cloudModel.dispose();
        if (scanRingModel != null) scanRingModel.dispose();
    }
}

