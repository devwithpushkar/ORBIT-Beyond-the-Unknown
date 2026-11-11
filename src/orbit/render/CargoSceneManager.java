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
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import orbit.render.theme.CinematicTheme;

/**
 * CargoSceneManager renders the immersive 3D spacecraft cargo hold environment.
 *
 * Features:
 * - Industrial sci-fi architecture: heavy structural bulkhead ribs, magnetic deck rails,
 *   overhead gantry maintenance crane, and aft airlock pressure seals.
 * - 5 physical cargo containment pods/depots arranged on the deck:
 *   1. Reinforced Structural Metal Alloy Crate
 *   2. High-Capacity Reactor Energy Cells Array
 *   3. Cryogenic Life Support Oxygen Manifold
 *   4. Hypergolic Propellant Fuel Cells Magazine
 *   5. Alien Xenological Relic Stasis Vault with rotating floating artifact
 * - Dynamic dual-tone industrial lighting (warm ceiling floodlights + cool underfloor mag-rails).
 * - Organic microgravity camera sway and pulsing stasis field animations.
 */
public class CargoSceneManager implements Disposable {

    private final ModelBatch modelBatch;
    private final Environment environment;
    private final PerspectiveCamera camera;

    // 3D Cargo Hold Models & Instances
    private Model bayArchitectureModel;
    private ModelInstance bayInstance;

    private Model relicModel;
    private ModelInstance relicInstance;

    private Model stasisFieldModel;
    private ModelInstance stasisFieldInstance;

    private final Array<ModelInstance> instances = new Array<>();

    // Animation Timers
    private float cameraTime = 0f;
    private float animTime = 0f;
    private String highlightedCategory = null;

    public CargoSceneManager() {
        this.modelBatch = new ModelBatch();

        // 1. Perspective Camera overlooking the cargo deck
        this.camera = new PerspectiveCamera(62f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera.position.set(0f, 0.65f, 3.2f);
        this.camera.lookAt(0f, -0.25f, -4.5f);
        this.camera.near = 0.1f;
        this.camera.far = 120f;
        this.camera.update();

        // 2. Cinematic Lighting Environment
        this.environment = new Environment();
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.10f, 0.12f, 0.16f, 1.0f));

        // Primary Overhead Cargo Bay Floodlight (Industrial work-light)
        this.environment.add(new DirectionalLight().set(0.88f, 0.84f, 0.78f, -0.35f, -0.85f, -0.40f));
        // Underfloor Magnetic Rail Fill (Cyan avionics glow)
        this.environment.add(new DirectionalLight().set(0.12f, 0.32f, 0.48f, 0.0f, 1.0f, 0.3f));

        initBayArchitecture();
        initAlienRelic();
    }

    private void initBayArchitecture() {
        ModelBuilder mb = new ModelBuilder();
        mb.begin();

        long attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked;

        // --- Materials ---
        // Industrial Dark Titanium Deck
        Material deckMat = new Material(
                ColorAttribute.createDiffuse(0.06f, 0.08f, 0.12f, 1.0f),
                ColorAttribute.createSpecular(0.40f, 0.45f, 0.55f, 1.0f)
        );

        // Magnetic Transport Rails (Backlit Cyan)
        Material magRailMat = new Material(
                ColorAttribute.createDiffuse(0.15f, 0.75f, 0.90f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.45f, 0.75f, 0.95f, 1.0f)
        );

        // Heavy Structural Ribs / Bulkheads
        Material ribMat = new Material(
                ColorAttribute.createDiffuse(0.08f, 0.10f, 0.14f, 1.0f),
                ColorAttribute.createSpecular(0.35f, 0.40f, 0.50f, 1.0f)
        );

        // Hazard Chevron Yellow Striping
        Material hazardMat = new Material(
                ColorAttribute.createDiffuse(0.95f, 0.75f, 0.15f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.35f, 0.28f, 0.05f, 1.0f)
        );

        // Overhead Crane Gantry Rails
        Material gantryMat = new Material(
                ColorAttribute.createDiffuse(0.10f, 0.12f, 0.16f, 1.0f),
                ColorAttribute.createSpecular(0.25f, 0.30f, 0.35f, 1.0f)
        );

        // Container Materials
        Material metalCrateMat = new Material(
                ColorAttribute.createDiffuse(0.18f, 0.22f, 0.28f, 1.0f),
                ColorAttribute.createSpecular(0.55f, 0.60f, 0.70f, 1.0f)
        );
        Material energyCellMat = new Material(
                ColorAttribute.createDiffuse(0.08f, 0.45f, 0.85f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.25f, 0.65f, 0.95f, 1.0f)
        );
        Material oxygenTankMat = new Material(
                ColorAttribute.createDiffuse(0.25f, 0.65f, 0.70f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.15f, 0.45f, 0.50f, 1.0f)
        );
        Material fuelCellMat = new Material(
                ColorAttribute.createDiffuse(0.90f, 0.55f, 0.12f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.50f, 0.25f, 0.05f, 1.0f)
        );
        Material alienVaultMat = new Material(
                ColorAttribute.createDiffuse(0.05f, 0.06f, 0.09f, 1.0f),
                ColorAttribute.createSpecular(0.70f, 0.75f, 0.85f, 1.0f)
        );
        Material alienGoldMat = new Material(
                ColorAttribute.createDiffuse(0.95f, 0.78f, 0.20f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.65f, 0.48f, 0.10f, 1.0f)
        );

        // 1. Cargo Floor Deck (Heavy plating spanning the hold)
        MeshPartBuilder floorBuilder = mb.part("floor", GL20.GL_TRIANGLES, attr, deckMat);
        floorBuilder.box(0f, -0.95f, -5.0f, 12.0f, 0.35f, 18.0f);

        // 2. Magnetic Center Track Rails
        MeshPartBuilder railBuilder = mb.part("magRails", GL20.GL_TRIANGLES, attr, magRailMat);
        railBuilder.box(-0.65f, -0.76f, -5.0f, 0.08f, 0.04f, 17.5f);
        railBuilder.box(0.65f, -0.76f, -5.0f, 0.08f, 0.04f, 17.5f);

        // Central Walkway Strip
        MeshPartBuilder walkBuilder = mb.part("walkway", GL20.GL_TRIANGLES, attr, deckMat);
        walkBuilder.box(0f, -0.77f, -5.0f, 1.15f, 0.02f, 17.5f);

        // 3. Structural Bulkhead Ribs (4 Portal Arches)
        float[] ribZ = { 2.0f, -1.8f, -5.8f, -9.8f };
        for (int i = 0; i < ribZ.length; i++) {
            float z = ribZ[i];
            MeshPartBuilder ribBuilder = mb.part("rib_" + i, GL20.GL_TRIANGLES, attr, ribMat);
            // Left Pillar
            ribBuilder.box(-4.2f, 0.8f, z, 0.55f, 3.2f, 0.65f);
            // Right Pillar
            ribBuilder.box(4.2f, 0.8f, z, 0.55f, 3.2f, 0.65f);
            // Ceiling Cross-beam
            ribBuilder.box(0f, 2.35f, z, 8.8f, 0.45f, 0.65f);

            // Angled Trusses
            MeshPartBuilder hazardBuilder = mb.part("hazard_" + i, GL20.GL_TRIANGLES, attr, hazardMat);
            hazardBuilder.box(-3.5f, 2.05f, z, 0.65f, 0.12f, 0.66f);
            hazardBuilder.box(3.5f, 2.05f, z, 0.65f, 0.12f, 0.66f);
        }

        // 4. Overhead Maintenance Crane Gantry
        MeshPartBuilder gantryBuilder = mb.part("gantry", GL20.GL_TRIANGLES, attr, gantryMat);
        gantryBuilder.box(-1.8f, 2.2f, -5.0f, 0.22f, 0.28f, 18.0f);
        gantryBuilder.box(1.8f, 2.2f, -5.0f, 0.22f, 0.28f, 18.0f);
        // Traveling Hoist Beam
        gantryBuilder.box(0f, 2.1f, -4.2f, 3.8f, 0.18f, 0.45f);

        // 5. Aft Cargo Bay Pressure Airlock
        MeshPartBuilder airlockBuilder = mb.part("airlock", GL20.GL_TRIANGLES, attr, ribMat);
        airlockBuilder.box(0f, 0.6f, -13.8f, 6.5f, 2.8f, 0.5f);
        MeshPartBuilder sealBuilder = mb.part("airlockSeal", GL20.GL_TRIANGLES, attr, hazardMat);
        sealBuilder.box(0f, 0.6f, -13.5f, 3.2f, 2.2f, 0.15f);

        // --- 6. The 5 Physical Cargo Containment Pods ---
        // Pod 1: Reinforced Metal Plates (Port-Front: X=-2.4, Z=-2.0)
        MeshPartBuilder metalBuilder = mb.part("pod_metal", GL20.GL_TRIANGLES, attr, metalCrateMat);
        metalBuilder.box(-2.4f, -0.45f, -2.0f, 1.4f, 0.65f, 1.4f);
        // Metal Bracing Ribs
        MeshPartBuilder metalRibs = mb.part("pod_metal_ribs", GL20.GL_TRIANGLES, attr, hazardMat);
        metalRibs.box(-2.4f, -0.45f, -2.0f, 1.45f, 0.08f, 1.45f);

        // Pod 2: Energy Cells Matrix (Port-Mid: X=-2.2, Z=-5.0)
        MeshPartBuilder energyBase = mb.part("pod_energy_base", GL20.GL_TRIANGLES, attr, metalCrateMat);
        energyBase.box(-2.2f, -0.65f, -5.0f, 1.3f, 0.25f, 1.3f);
        MeshPartBuilder energyCores = mb.part("pod_energy_cores", GL20.GL_TRIANGLES, attr, energyCellMat);
        energyCores.box(-2.45f, -0.25f, -5.25f, 0.28f, 0.65f, 0.28f);
        energyCores.box(-1.95f, -0.25f, -5.25f, 0.28f, 0.65f, 0.28f);
        energyCores.box(-2.45f, -0.25f, -4.75f, 0.28f, 0.65f, 0.28f);
        energyCores.box(-1.95f, -0.25f, -4.75f, 0.28f, 0.65f, 0.28f);

        // Pod 3: Oxygen Reserves Manifold (Starboard-Mid: X=2.2, Z=-5.0)
        MeshPartBuilder o2Base = mb.part("pod_o2_base", GL20.GL_TRIANGLES, attr, metalCrateMat);
        o2Base.box(2.2f, -0.65f, -5.0f, 1.3f, 0.25f, 1.3f);
        MeshPartBuilder o2Tanks = mb.part("pod_o2_tanks", GL20.GL_TRIANGLES, attr, oxygenTankMat);
        o2Tanks.box(1.95f, -0.22f, -5.0f, 0.45f, 0.72f, 0.45f);
        o2Tanks.box(2.50f, -0.22f, -5.0f, 0.45f, 0.72f, 0.45f);

        // Pod 4: Fuel Cells Magazine (Starboard-Front: X=2.4, Z=-2.0)
        MeshPartBuilder fuelBase = mb.part("pod_fuel_base", GL20.GL_TRIANGLES, attr, metalCrateMat);
        fuelBase.box(2.4f, -0.65f, -2.0f, 1.4f, 0.25f, 1.4f);
        MeshPartBuilder fuelDrums = mb.part("pod_fuel_drums", GL20.GL_TRIANGLES, attr, fuelCellMat);
        fuelDrums.box(2.15f, -0.24f, -2.0f, 0.52f, 0.68f, 0.52f);
        fuelDrums.box(2.70f, -0.24f, -2.0f, 0.52f, 0.68f, 0.52f);

        // Pod 5: Alien Artifact Stasis Pedestal (Center-Back: X=0, Z=-7.5)
        MeshPartBuilder vaultBase = mb.part("pod_vault_base", GL20.GL_TRIANGLES, attr, alienVaultMat);
        vaultBase.box(0f, -0.60f, -7.5f, 1.8f, 0.35f, 1.8f);
        MeshPartBuilder vaultPylons = mb.part("pod_vault_pylons", GL20.GL_TRIANGLES, attr, alienGoldMat);
        vaultPylons.box(-0.75f, -0.25f, -8.25f, 0.12f, 0.45f, 0.12f);
        vaultPylons.box(0.75f, -0.25f, -8.25f, 0.12f, 0.45f, 0.12f);
        vaultPylons.box(-0.75f, -0.25f, -6.75f, 0.12f, 0.45f, 0.12f);
        vaultPylons.box(0.75f, -0.25f, -6.75f, 0.12f, 0.45f, 0.12f);

        bayArchitectureModel = mb.end();
        bayInstance = new ModelInstance(bayArchitectureModel);
        instances.add(bayInstance);
    }

    private void initAlienRelic() {
        ModelBuilder mb = new ModelBuilder();

        // 1. Floating Alien Relic Crystal (Rotating octahedral geometry)
        mb.begin();
        long attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked;
        Material crystalMat = new Material(
                ColorAttribute.createDiffuse(1.0f, 0.85f, 0.25f, 1.0f),
                new ColorAttribute(ColorAttribute.Emissive, 0.75f, 0.55f, 0.15f, 1.0f),
                ColorAttribute.createSpecular(1.0f, 0.95f, 0.60f, 1.0f)
        );
        MeshPartBuilder crystalBuilder = mb.part("relic", GL20.GL_TRIANGLES, attr, crystalMat);
        crystalBuilder.box(0f, 0f, 0f, 0.38f, 0.52f, 0.38f);
        relicModel = mb.end();

        relicInstance = new ModelInstance(relicModel);
        relicInstance.transform.setToTranslation(0f, -0.15f, -7.5f);
        instances.add(relicInstance);

        // 2. Translucent Stasis Containment Shield
        mb.begin();
        Material stasisMat = new Material(
                ColorAttribute.createDiffuse(0.2f, 0.8f, 1.0f, 0.35f),
                new ColorAttribute(ColorAttribute.Emissive, 0.15f, 0.45f, 0.75f, 0.35f),
                new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.35f)
        );
        MeshPartBuilder shieldBuilder = mb.part("stasis", GL20.GL_TRIANGLES, attr, stasisMat);
        shieldBuilder.cylinder(1.2f, 0.95f, 1.2f, 16);
        stasisFieldModel = mb.end();

        stasisFieldInstance = new ModelInstance(stasisFieldModel);
        stasisFieldInstance.transform.setToTranslation(0f, -0.15f, -7.5f);
        instances.add(stasisFieldInstance);
    }

    public void update(float delta) {
        cameraTime += delta * 0.5f;
        animTime += delta;

        // Subtle microgravity camera drift
        camera.position.set(
                MathUtils.sin(cameraTime) * 0.08f,
                0.65f + MathUtils.cos(cameraTime * 0.7f) * 0.04f,
                3.2f
        );
        camera.lookAt(0f, -0.25f, -4.5f);
        camera.update();

        // 3D Alien Relic Rotation & Levitation
        if (relicInstance != null) {
            float floatY = -0.15f + MathUtils.sin(animTime * 2.2f) * 0.06f;
            relicInstance.transform.setToTranslation(0f, floatY, -7.5f);
            relicInstance.transform.rotate(Vector3.Y, animTime * 45f);
            relicInstance.transform.rotate(Vector3.X, animTime * 20f);
        }

        // Stasis Shield subtle pulse
        if (stasisFieldInstance != null) {
            stasisFieldInstance.transform.setToTranslation(0f, -0.15f, -7.5f);
            stasisFieldInstance.transform.rotate(Vector3.Y, -animTime * 15f);
        }
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

    public void setHighlightedCategory(String category) {
        this.highlightedCategory = category;
    }

    public String getHighlightedCategory() {
        return highlightedCategory;
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        if (bayArchitectureModel != null) bayArchitectureModel.dispose();
        if (relicModel != null) relicModel.dispose();
        if (stasisFieldModel != null) stasisFieldModel.dispose();
    }
}

