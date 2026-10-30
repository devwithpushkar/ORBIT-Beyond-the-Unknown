package orbit.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GameState holds the active runtime session data for ORBIT:
 * the commander (Player), vessel status (Spaceship), current celestial location,
 * mission progress percentage, galaxy planetary catalog, cargo inventory,
 * campaign mission objectives, and chronological mission log.
 */
public class GameState {

    private Player player;
    private Spaceship spaceship;
    private Planet currentLocation;
    private int missionProgress;
    private final List<Planet> planets;
    private Inventory inventory;

    private final List<MissionObjective> objectives;
    private final MissionLog missionLog;
    private String currentStage;
    private boolean hasDepartedEarth;
    private boolean resourceCollectedDuringMission;
    private boolean transmissionEvidenceDiscovered;
    private boolean materialsCollectedDuringStage2;
    private boolean signalSourceDiscovered;
    private String completedEnding;

    /**
     * Initializes a new GameState for the given player and spaceship.
     * Sets up default starter supplies and the celestial catalog with Earth as the starting location.
     */
    public GameState(Player player, Spaceship spaceship) {
        this(player, spaceship, Inventory.createStartingInventory());
    }

    /**
     * Initializes a GameState with custom inventory supplies.
     */
    public GameState(Player player, Spaceship spaceship, Inventory inventory) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null.");
        }
        if (spaceship == null) {
            throw new IllegalArgumentException("Spaceship cannot be null.");
        }
        if (inventory == null) {
            throw new IllegalArgumentException("Inventory cannot be null.");
        }

        this.player = player;
        this.spaceship = spaceship;
        this.inventory = inventory;
        this.missionProgress = 0;
        this.planets = new ArrayList<>();

        this.objectives = MissionObjective.createCampaignObjectives();
        this.missionLog = new MissionLog();
        this.currentStage = MissionObjective.STAGE_1;
        this.hasDepartedEarth = false;
        this.resourceCollectedDuringMission = false;
        this.transmissionEvidenceDiscovered = false;
        this.materialsCollectedDuringStage2 = false;
        this.signalSourceDiscovered = false;
        this.completedEnding = null;

        initializeGalaxy();

        // Initial flight recorder logging
        this.missionLog.addEntry("MISSION", "Spacecraft " + player.getSpaceshipName()
                + " commissioned under Commander " + player.getCommanderName().toUpperCase() + ".");
        this.missionLog.addEntry("MISSION", "Primary campaign directive initiated: " + MissionObjective.STAGE_1 + ".");
        this.missionLog.addEntry("NAVIGATION", "Vessel deployed in Earth orbital parking station.");

        updateMissionObjectives();
    }

    /**
     * Populates the galaxy with initial destinations according to flight specifications.
     * Earth is the starting location and is the only initially discovered planet.
     */
    private void initializeGalaxy() {
        Planet earth = new Planet(
                "Earth",
                "Starting location and mission headquarters",
                "Temperate terrestrial biosphere with orbital docks and mission control",
                true,   // Discovered initially
                false   // Not yet fully explored
        );

        Planet mars = new Planet(
                "Mars",
                "Resource-rich planetary environment",
                "Arid desert world with iron-oxide dust storms and subterranean ice reserves",
                false,
                false
        );

        Planet europa = new Planet(
                "Europa",
                "Frozen moon containing unusual signals",
                "Cryogenic ice crust shielding a high-pressure subsurface ocean",
                false,
                false
        );

        Planet titan = new Planet(
                "Titan",
                "Hazardous environment with rare materials",
                "Dense nitrogen-methane atmosphere with cryogenic hydrocarbon seas",
                false,
                false
        );

        Planet kepler = new Planet(
                "Kepler-186f",
                "Unexplored destination associated with the mysterious signal",
                "Distant terrestrial exoplanet in the habitable zone emitting anomaly beacon",
                false,
                false
        );

        this.planets.add(earth);
        this.planets.add(mars);
        this.planets.add(europa);
        this.planets.add(titan);
        this.planets.add(kepler);

        // Earth is the initial starting location
        this.currentLocation = earth;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null.");
        }
        this.player = player;
    }

    public Spaceship getSpaceship() {
        return spaceship;
    }

    public void setSpaceship(Spaceship spaceship) {
        if (spaceship == null) {
            throw new IllegalArgumentException("Spaceship cannot be null.");
        }
        this.spaceship = spaceship;
    }

    public Planet getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Planet currentLocation) {
        if (currentLocation == null) {
            throw new IllegalArgumentException("Location cannot be null.");
        }
        this.currentLocation = currentLocation;
        if (!currentLocation.getName().equalsIgnoreCase("Earth")) {
            this.hasDepartedEarth = true;
        }
        updateMissionObjectives();
    }

    public int getMissionProgress() {
        return missionProgress;
    }

    public void setMissionProgress(int missionProgress) {
        this.missionProgress = Math.max(0, Math.min(100, missionProgress));
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        if (inventory == null) {
            throw new IllegalArgumentException("Inventory cannot be null.");
        }
        this.inventory = inventory;
    }

    public List<Planet> getPlanets() {
        return Collections.unmodifiableList(planets);
    }

    /**
     * Resolves a planet by its case-insensitive name.
     */
    public Planet getPlanetByName(String name) {
        if (name == null) return null;
        for (Planet planet : planets) {
            if (planet.getName().equalsIgnoreCase(name.trim())) {
                return planet;
            }
        }
        return null;
    }

    // ==========================================
    // Phase 6: Mission Objectives & Log Methods
    // ==========================================

    public List<MissionObjective> getObjectives() {
        return Collections.unmodifiableList(objectives);
    }

    public MissionObjective getObjectiveById(String id) {
        if (id == null) return null;
        for (MissionObjective obj : objectives) {
            if (obj.getId().equalsIgnoreCase(id.trim())) {
                return obj;
            }
        }
        return null;
    }

    public MissionLog getMissionLog() {
        return missionLog;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        if (currentStage != null && !currentStage.trim().isEmpty()) {
            this.currentStage = currentStage.trim();
        }
    }

    public boolean hasDepartedEarth() {
        return hasDepartedEarth;
    }

    public void setHasDepartedEarth(boolean hasDepartedEarth) {
        this.hasDepartedEarth = hasDepartedEarth;
        updateMissionObjectives();
    }

    public boolean isResourceCollectedDuringMission() {
        return resourceCollectedDuringMission;
    }

    public void setResourceCollectedDuringMission(boolean resourceCollectedDuringMission) {
        this.resourceCollectedDuringMission = resourceCollectedDuringMission;
        updateMissionObjectives();
    }

    public boolean isTransmissionEvidenceDiscovered() {
        return transmissionEvidenceDiscovered;
    }

    public void setTransmissionEvidenceDiscovered(boolean transmissionEvidenceDiscovered) {
        this.transmissionEvidenceDiscovered = transmissionEvidenceDiscovered;
        updateMissionObjectives();
    }

    public boolean isMaterialsCollectedDuringStage2() {
        return materialsCollectedDuringStage2;
    }

    public void setMaterialsCollectedDuringStage2(boolean materialsCollectedDuringStage2) {
        this.materialsCollectedDuringStage2 = materialsCollectedDuringStage2;
        updateMissionObjectives();
    }

    public boolean isSignalSourceDiscovered() {
        return signalSourceDiscovered;
    }

    public void setSignalSourceDiscovered(boolean signalSourceDiscovered) {
        this.signalSourceDiscovered = signalSourceDiscovered;
        updateMissionObjectives();
    }

    public String getCompletedEnding() {
        return completedEnding;
    }

    public void setCompletedEnding(String completedEnding) {
        this.completedEnding = completedEnding;
        updateMissionObjectives();
    }

    public boolean isCampaignCompleted() {
        return completedEnding != null && !completedEnding.trim().isEmpty();
    }

    /**
     * Evaluates current game state and updates mission objectives, stage progression,
     * and logs completion events automatically.
     */
    public synchronized void updateMissionObjectives() {
        Planet earth = getPlanetByName("Earth");
        Planet mars = getPlanetByName("Mars");
        Planet europa = getPlanetByName("Europa");
        Planet titan = getPlanetByName("Titan");
        Planet kepler = getPlanetByName("Kepler-186f");

        if (currentLocation != null && !currentLocation.getName().equalsIgnoreCase("Earth")) {
            this.hasDepartedEarth = true;
        }

        // ==========================================
        // STAGE 1 OBJECTIVES
        // ==========================================

        // Objective 1: Launch from Earth
        MissionObjective objLaunch = getObjectiveById("obj_launch_earth");
        if (objLaunch != null && !objLaunch.isCompleted() && this.hasDepartedEarth) {
            objLaunch.complete();
            missionLog.addEntry("MISSION", "Objective Completed: " + objLaunch.getTitle());
        }

        // Objective 2: Travel to Mars
        MissionObjective objTravelMars = getObjectiveById("obj_travel_mars");
        if (objTravelMars != null && !objTravelMars.isCompleted()
                && currentLocation != null && currentLocation.getName().equalsIgnoreCase("Mars")) {
            objTravelMars.complete();
            missionLog.addEntry("MISSION", "Objective Completed: " + objTravelMars.getTitle());
        }

        // Objective 3: Complete Initial Exploration
        MissionObjective objExplore = getObjectiveById("obj_explore_surface");
        if (objExplore != null && !objExplore.isCompleted()) {
            boolean exploredAny = (earth != null && earth.isExplored()) || (mars != null && mars.isExplored());
            if (exploredAny) {
                objExplore.complete();
                missionLog.addEntry("MISSION", "Objective Completed: " + objExplore.getTitle());
            }
        }

        // Objective 4: Collect Basic Resources
        MissionObjective objResources = getObjectiveById("obj_collect_resources");
        if (objResources != null && !objResources.isCompleted() && this.resourceCollectedDuringMission) {
            objResources.complete();
            missionLog.addEntry("MISSION", "Objective Completed: " + objResources.getTitle());
        }

        // Check Stage 1 completion and advancement to Stage 2
        if (currentStage.equals(MissionObjective.STAGE_1)) {
            boolean stage1Done = (objLaunch != null && objLaunch.isCompleted())
                    && (objTravelMars != null && objTravelMars.isCompleted())
                    && (objExplore != null && objExplore.isCompleted())
                    && (objResources != null && objResources.isCompleted());

            if (stage1Done) {
                this.currentStage = MissionObjective.STAGE_2;
                missionLog.addEntry("MISSION", "CAMPAIGN MILESTONE: Stage 1 (Departure) completed! Advanced to " + MissionObjective.STAGE_2 + ".");

                // Unlock outer moon corridors
                if (europa != null) europa.setDiscovered(true);
                if (titan != null) titan.setDiscovered(true);

                // Activate Stage 2 objectives
                for (MissionObjective obj : objectives) {
                    if (obj.getStage().equals(MissionObjective.STAGE_2) && obj.isPending()) {
                        obj.start();
                    }
                }
            }
        }

        // ==========================================
        // STAGE 2 OBJECTIVES
        // ==========================================

        // Objective 5: Explore Europa
        MissionObjective objEuropa = getObjectiveById("obj_explore_europa");
        if (objEuropa != null && !objEuropa.isCompleted() && europa != null && europa.isExplored()) {
            objEuropa.complete();
            missionLog.addEntry("MISSION", "Objective Completed: " + objEuropa.getTitle());
        }

        // Objective 6: Visit Titan
        MissionObjective objTitan = getObjectiveById("obj_visit_titan");
        if (objTitan != null && !objTitan.isCompleted()) {
            boolean titanVisited = (currentLocation != null && currentLocation.getName().equalsIgnoreCase("Titan"))
                    || (titan != null && titan.isExplored());
            if (titanVisited) {
                objTitan.complete();
                missionLog.addEntry("MISSION", "Objective Completed: " + objTitan.getTitle());
            }
        }

        // Objective 7: Collect Required Materials
        MissionObjective objMaterials = getObjectiveById("obj_collect_materials");
        if (objMaterials != null && !objMaterials.isCompleted()) {
            boolean outerMaterialHarvested = this.materialsCollectedDuringStage2
                    || (europa != null && europa.isExplored() && this.resourceCollectedDuringMission)
                    || (titan != null && titan.isExplored() && this.resourceCollectedDuringMission);
            if (outerMaterialHarvested) {
                objMaterials.complete();
                missionLog.addEntry("MISSION", "Objective Completed: " + objMaterials.getTitle());
            }
        }

        // Objective 8: Discover Unidentified Transmission
        MissionObjective objTransmission = getObjectiveById("obj_discover_transmission");
        if (objTransmission != null && !objTransmission.isCompleted() && this.transmissionEvidenceDiscovered) {
            if (this.currentStage.equals(MissionObjective.STAGE_2) || this.currentStage.equals(MissionObjective.STAGE_3)) {
                objTransmission.complete();
                missionLog.addEntry("MISSION", "Objective Completed: " + objTransmission.getTitle());
            } else {
                objTransmission.setProgressInfo("Signal Evidence Logged");
            }
        }

        // Check Stage 2 completion and advancement to Stage 3
        if (currentStage.equals(MissionObjective.STAGE_2)) {
            boolean stage2Done = (objEuropa != null && objEuropa.isCompleted())
                    && (objTitan != null && objTitan.isCompleted())
                    && (objMaterials != null && objMaterials.isCompleted())
                    && (objTransmission != null && objTransmission.isCompleted());

            if (stage2Done) {
                this.currentStage = MissionObjective.STAGE_3;
                missionLog.addEntry("MISSION", "CAMPAIGN MILESTONE: Stage 2 (Deep Space Exploration) completed! Advanced to " + MissionObjective.STAGE_3 + ".");

                // Unlock Kepler-186f
                if (kepler != null) {
                    kepler.setDiscovered(true);
                }

                // Activate Stage 3 objectives
                for (MissionObjective obj : objectives) {
                    if (obj.getStage().equals(MissionObjective.STAGE_3) && obj.isPending()) {
                        obj.start();
                    }
                }
            }
        }

        // ==========================================
        // STAGE 3 OBJECTIVES
        // ==========================================

        // Objective 9: Travel to Kepler-186f
        MissionObjective objTravelKepler = getObjectiveById("obj_travel_kepler");
        if (objTravelKepler != null && !objTravelKepler.isCompleted()
                && currentLocation != null && currentLocation.getName().equalsIgnoreCase("Kepler-186f")) {
            objTravelKepler.complete();
            missionLog.addEntry("MISSION", "Objective Completed: " + objTravelKepler.getTitle());
        }

        // Objective 10: Investigate the Signal
        MissionObjective objInvestigate = getObjectiveById("obj_investigate_signal");
        if (objInvestigate != null && !objInvestigate.isCompleted() && kepler != null && kepler.isExplored()) {
            objInvestigate.complete();
            missionLog.addEntry("MISSION", "Objective Completed: " + objInvestigate.getTitle());
        }

        // Objective 11: Discover Signal Source
        MissionObjective objSource = getObjectiveById("obj_discover_source");
        if (objSource != null && !objSource.isCompleted() && this.signalSourceDiscovered) {
            objSource.complete();
            missionLog.addEntry("MISSION", "Objective Completed: " + objSource.getTitle());
        }

        // Objective 12: Make Final Mission Decision
        MissionObjective objDecision = getObjectiveById("obj_final_decision");
        if (objDecision != null && !objDecision.isCompleted() && this.completedEnding != null) {
            objDecision.complete();
            objDecision.setProgressInfo(this.completedEnding);
            missionLog.addEntry("MISSION", "Objective Completed: " + objDecision.getTitle() + " (" + this.completedEnding + ")");
        }

        // Calculate overall mission progress percentage
        if (isCampaignCompleted()) {
            this.missionProgress = 100;
        } else {
            int completedCount = 0;
            for (MissionObjective obj : objectives) {
                if (obj.isCompleted()) {
                    completedCount++;
                }
            }
            this.missionProgress = (completedCount * 100) / objectives.size();
        }
    }
}
