package orbit.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * ExplorationEvent represents a surface reconnaissance event encountered during
 * an expedition on a celestial body.
 *
 * Events are categorized into:
 * - Discovery: Abandoned equipment, old research facilities, unusual geology
 * - Resource: Natural deposits and salvage of Metal, Energy Cells, Oxygen Canisters, Fuel Cells
 * - Hazard: Environmental threats inflicting Hull damage, Energy drain, or Oxygen depletion
 * - Encounter: Unidentified harmonic transmissions and evidence of unknown civilizations
 */
public class ExplorationEvent {

    public static final String CATEGORY_DISCOVERY = "Discovery";
    public static final String CATEGORY_RESOURCE = "Resource";
    public static final String CATEGORY_HAZARD = "Hazard";
    public static final String CATEGORY_ENCOUNTER = "Encounter";

    private final String id;
    private final String title;
    private final String category;
    private final String description;
    private final Map<String, Integer> itemRewards;
    private final int hullPenalty;
    private final int energyPenalty;
    private final int oxygenPenalty;
    private final int fuelPenalty;

    public ExplorationEvent(
            String id,
            String title,
            String category,
            String description,
            Map<String, Integer> itemRewards,
            int hullPenalty,
            int energyPenalty,
            int oxygenPenalty
    ) {
        this(id, title, category, description, itemRewards, hullPenalty, energyPenalty, oxygenPenalty, 0);
    }

    public ExplorationEvent(
            String id,
            String title,
            String category,
            String description,
            Map<String, Integer> itemRewards,
            int hullPenalty,
            int energyPenalty,
            int oxygenPenalty,
            int fuelPenalty
    ) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be null or empty.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Event title cannot be null or empty.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Event category cannot be null or empty.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Event description cannot be null or empty.");
        }

        this.id = id.trim();
        this.title = title.trim();
        this.category = category.trim();
        this.description = description.trim();
        this.itemRewards = (itemRewards != null) ? new LinkedHashMap<>(itemRewards) : Collections.emptyMap();
        this.hullPenalty = Math.max(0, hullPenalty);
        this.energyPenalty = Math.max(0, energyPenalty);
        this.oxygenPenalty = Math.max(0, oxygenPenalty);
        this.fuelPenalty = Math.max(0, fuelPenalty);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Integer> getItemRewards() {
        return Collections.unmodifiableMap(itemRewards);
    }

    public int getHullPenalty() {
        return hullPenalty;
    }

    public int getEnergyPenalty() {
        return energyPenalty;
    }

    public int getOxygenPenalty() {
        return oxygenPenalty;
    }

    public int getFuelPenalty() {
        return fuelPenalty;
    }

    public boolean hasRewards() {
        return !itemRewards.isEmpty();
    }

    public boolean hasPenalties() {
        return hullPenalty > 0 || energyPenalty > 0 || oxygenPenalty > 0 || fuelPenalty > 0;
    }

    public String getCountermeasureItem() {
        if (hullPenalty > 0) return Inventory.METAL;
        if (energyPenalty > 0) return Inventory.ENERGY_CELLS;
        if (oxygenPenalty > 0) return Inventory.OXYGEN_CANISTERS;
        if (fuelPenalty > 0) return Inventory.FUEL_CELLS;
        return null;
    }

    public String getPrimaryPenaltyDescription() {
        if (hullPenalty > 0) return "-" + hullPenalty + " Hull Integrity";
        if (energyPenalty > 0) return "-" + energyPenalty + " Reactor Energy";
        if (oxygenPenalty > 0) return "-" + oxygenPenalty + " Life Support Oxygen";
        if (fuelPenalty > 0) return "-" + fuelPenalty + " Propulsion Fuel";
        return "None";
    }

    public String getCountermeasureDescription() {
        if (hullPenalty > 0) return "Deploy sacrificial high-tensile alloy plating to absorb physical debris impact.";
        if (energyPenalty > 0) return "Discharge capacitor bypass shunt to isolate reactor manifolds from electrical surges.";
        if (oxygenPenalty > 0) return "Inject emergency atmospheric filter cartridge to neutralize corrosive gas infiltration.";
        if (fuelPenalty > 0) return "Hot-swap auxiliary propellant seal ring to arrest high-pressure cryogenic fuel purge.";
        return "";
    }

    /**
     * Applies the rewards and penalties of this event directly to the active session models.
     * All resource values and inventory quantities are safely bounded.
     *
     * @param spaceship The active spaceship instance
     * @param inventory The active inventory instance
     * @return User-facing summary of effects applied
     */
    public List<String> applyEffects(Spaceship spaceship, Inventory inventory) {
        List<String> effectLog = new ArrayList<>();

        // Apply item rewards
        for (Map.Entry<String, Integer> entry : itemRewards.entrySet()) {
            String item = entry.getKey();
            int amount = entry.getValue();
            if (amount > 0 && Inventory.isValidItem(item)) {
                inventory.add(item, amount);
                effectLog.add("[+] REWARD: Secured +" + amount + " " + item + " (Cargo: " + inventory.getQuantity(item) + ")");
            }
        }

        // Apply penalties
        if (hullPenalty > 0 && spaceship != null) {
            spaceship.applyDamage(hullPenalty);
            effectLog.add("[-] HAZARD: Sustained -" + hullPenalty + " Hull Integrity (Current: " + spaceship.getHullIntegrity() + "%)");
        }
        if (energyPenalty > 0 && spaceship != null) {
            spaceship.consumeEnergy(energyPenalty);
            effectLog.add("[-] HAZARD: Dissipated -" + energyPenalty + " Reactor Energy (Current: " + spaceship.getEnergy() + "%)");
        }
        if (oxygenPenalty > 0 && spaceship != null) {
            spaceship.consumeOxygen(oxygenPenalty);
            effectLog.add("[-] HAZARD: Expended -" + oxygenPenalty + " Life Support Oxygen (Current: " + spaceship.getOxygen() + "%)");
        }
        if (fuelPenalty > 0 && spaceship != null) {
            spaceship.consumeFuel(fuelPenalty);
            effectLog.add("[-] HAZARD: Vented -" + fuelPenalty + " Propulsion Fuel (Current: " + spaceship.getFuel() + "%)");
        }

        if (effectLog.isEmpty()) {
            effectLog.add("[i] TELEMETRY: Exploration telemetry and anomalous sensor records archived.");
        }

        return effectLog;
    }

    // ==========================================
    // Predefined 12 Events Pool
    // ==========================================

    private static final List<ExplorationEvent> PREDEFINED_EVENTS = new ArrayList<>();

    static {
        // --- 1. DISCOVERY EVENTS ---
        Map<String, Integer> eqRewards = new LinkedHashMap<>();
        eqRewards.put(Inventory.METAL, 1);
        eqRewards.put(Inventory.ENERGY_CELLS, 1);
        PREDEFINED_EVENTS.add(new ExplorationEvent(
                "disc_abandoned_equipment",
                "Abandoned Equipment Cache",
                CATEGORY_DISCOVERY,
                "The excursion rover uncovers a half-buried surveyor pod left behind by an early uncrewed mission. Salvaging intact structural brackets and battery components yields valuable supplies.",
                eqRewards,
                0, 0, 0
        ));

        Map<String, Integer> stRewards = new LinkedHashMap<>();
        stRewards.put(Inventory.OXYGEN_CANISTERS, 1);
        PREDEFINED_EVENTS.add(new ExplorationEvent(
                "disc_research_station",
                "Derelict Research Outpost",
                CATEGORY_DISCOVERY,
                "The team identifies an obsolete subterranean research outpost sealed decades ago inside a rock shelter. Sealed emergency life support reservoirs remain pressurized inside the airlock.",
                stRewards,
                0, 0, 0
        ));

        Map<String, Integer> geoRewards = new LinkedHashMap<>();
        geoRewards.put(Inventory.METAL, 2);
        PREDEFINED_EVENTS.add(new ExplorationEvent(
                "disc_geological_formation",
                "Anomalous Mineral Formations",
                CATEGORY_DISCOVERY,
                "Spectral sensors detect crystalline basalt spires rich in refined structural ore veins along a tectonic fault line. The team extracts high-grade alloy fragments.",
                geoRewards,
                0, 0, 0
        ));

        // --- 2. RESOURCE EVENTS ---
        Map<String, Integer> metalRewards = new LinkedHashMap<>();
        metalRewards.put(Inventory.METAL, 2);
        PREDEFINED_EVENTS.add(new ExplorationEvent(
                "res_metal_deposit",
                "Subterranean Metallic Vein",
                CATEGORY_RESOURCE,
                "Surface drill sensors detect an extensive vein of high-tensile titanium-carbide ore beneath the topsoil. Automated excavators harvest workable structural plating materials.",
                metalRewards,
                0, 0, 0
        ));

        Map<String, Integer> energyRewards = new LinkedHashMap<>();
        energyRewards.put(Inventory.ENERGY_CELLS, 2);
        PREDEFINED_EVENTS.add(new ExplorationEvent(
                "res_energy_cells",
                "Geothermal Capacitor Bank",
                CATEGORY_RESOURCE,
                "The rover discovers an intact thermal energy station tapping a natural fissure. Its high-capacity plasma storage cells are fully charged and ready for use.",
                energyRewards,
                0, 0, 0
        ));

        Map<String, Integer> o2Rewards = new LinkedHashMap<>();
        o2Rewards.put(Inventory.OXYGEN_CANISTERS, 2);
        PREDEFINED_EVENTS.add(new ExplorationEvent(
                "res_oxygen_canisters",
                "Pressurized Oxygen Reserve",
                CATEGORY_RESOURCE,
                "Sensors locate a sealed supply pod containing emergency atmospheric canisters. The liquid oxygen cylinders remain in pristine, usable condition.",
                o2Rewards,
                0, 0, 0
        ));

        Map<String, Integer> fuelRewards = new LinkedHashMap<>();
        fuelRewards.put(Inventory.FUEL_CELLS, 2);
        PREDEFINED_EVENTS.add(new ExplorationEvent(
                "res_fuel_cells",
                "Pre-Staged Fuel Depot",
                CATEGORY_RESOURCE,
                "The excursion team excavates an auxiliary booster tank jettisoned during an earlier orbital burn. Sealed deuterium propellant rods are successfully recovered.",
                fuelRewards,
                0, 0, 0
        ));

        // --- 3. HAZARD EVENTS ---
        PREDEFINED_EVENTS.add(new ExplorationEvent(
                "haz_hull_damage",
                "Seismic Rockfall & Debris Impact",
                CATEGORY_HAZARD,
                "Unstable crater walls collapse without warning, triggering a violent rockslide. Heavy boulders strike the landing struts and exterior armor plating of the vessel.",
                Collections.emptyMap(),
                15, 0, 0
        ));

        PREDEFINED_EVENTS.add(new ExplorationEvent(
                "haz_energy_drain",
                "Electromagnetic Ionic Storm",
                CATEGORY_HAZARD,
                "A sudden localized atmospheric ion discharge sweeps across the valley, inducing severe back-EMF surges that drain power from the spacecraft's reactor manifold.",
                Collections.emptyMap(),
                0, 15, 0
        ));

        PREDEFINED_EVENTS.add(new ExplorationEvent(
                "haz_oxygen_consumption",
                "Toxic Corrosive Outgassing",
                CATEGORY_HAZARD,
                "A volcanic fissure erupts with corrosive acidic vapors. The ship's environmental control system must run atmospheric scrubbers at maximum overdrive, consuming excess oxygen.",
                Collections.emptyMap(),
                0, 0, 15
        ));

        PREDEFINED_EVENTS.add(new ExplorationEvent(
                "haz_fuel_leak",
                "Cryogenic Fuel Conduit Fissure",
                CATEGORY_HAZARD,
                "Micro-meteorite regolith impact shears an exterior cryogenic propellant line, venting volatile deuterium fuel into the planetary vacuum.",
                Collections.emptyMap(),
                0, 0, 0, 15
        ));

        // --- 4. ENCOUNTER EVENTS ---
        PREDEFINED_EVENTS.add(new ExplorationEvent(
                "enc_unidentified_signal",
                "Unidentified Deep-Space Beacon",
                CATEGORY_ENCOUNTER,
                "The excursion array intercepts a rhythmic radio pulse oscillating in resonance with the unknown deep-space signal. Vector telemetry and frequency patterns are logged.",
                Collections.emptyMap(),
                0, 0, 0
        ));

        PREDEFINED_EVENTS.add(new ExplorationEvent(
                "enc_ancient_civilization",
                "Ancient Xenological Monolith",
                CATEGORY_ENCOUNTER,
                "Protruding from the surface regolith, the team uncovers a polished monolithic spire etched with unknown mathematical glyphs that defy natural erosion. It hums faintly with dormant energy.",
                Collections.emptyMap(),
                0, 0, 0
        ));
    }

    /**
     * Returns an unmodifiable list of all 12 predefined exploration events.
     */
    public static List<ExplorationEvent> getAllEvents() {
        return Collections.unmodifiableList(PREDEFINED_EVENTS);
    }

    /**
     * Selects one event randomly from the pool using the provided Random instance.
     */
    public static ExplorationEvent getRandomEvent(Random random) {
        if (random == null) {
            random = new Random();
        }
        int index = random.nextInt(PREDEFINED_EVENTS.size());
        return PREDEFINED_EVENTS.get(index);
    }

    /**
     * Retrieves an event by its unique ID.
     */
    public static ExplorationEvent getEventById(String id) {
        if (id == null) return null;
        for (ExplorationEvent event : PREDEFINED_EVENTS) {
            if (event.getId().equalsIgnoreCase(id.trim())) {
                return event;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ExplorationEvent{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
