
package orbit.controllers;

import orbit.models.ExplorationEvent;
import orbit.models.GameState;
import orbit.models.Inventory;
import orbit.models.MissionLog;
import orbit.models.MissionObjective;
import orbit.models.Planet;
import orbit.models.Player;
import orbit.models.SaveManager;
import orbit.models.Spaceship;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * OrbitController is the headless session controller for ORBIT: Beyond the Unknown.
 * Manages the active GameState, session lifecycle (New Game, Save, Load),
 * and interplanetary travel independently of any user interface.
 */
public class OrbitController {

    private GameState gameState;

    /**
     * Initializes a new controller without an active game session.
     */
    public OrbitController() {
        this.gameState = null;
    }

    /**
     * Initializes a controller managing an existing GameState.
     *
     * @param gameState The initial active GameState
     */
    public OrbitController(GameState gameState) {
        this.gameState = gameState;
    }

    // ==========================================
    // Session Lifecycle & State Access
    // ==========================================

    /**
     * Starts a new game session for the specified commander name.
     * Initializes default Player, Spaceship, starter cargo, and Earth as the sole starting location.
     * Mars remains locked until the initial Navigation briefing is triggered.
     *
     * @param commanderName The commanding officer name
     * @return The newly initialized GameState
     * @throws IllegalArgumentException if commanderName is null or empty
     */
    public GameState startNewGame(String commanderName) {
        if (commanderName == null || commanderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Commander name cannot be null or empty.");
        }
        Player player = new Player(commanderName.trim());
        Spaceship spaceship = new Spaceship();
        this.gameState = new GameState(player, spaceship);

        return this.gameState;
    }

    /**
     * Triggers the initial mission navigation briefing from High Command, authorizing
     * Mars as a valid interplanetary destination.
     *
     * In the original flight progression, Mars is not unlocked at game start but rather
     * upon opening the Navigation console for the first time.
     *
     * If Mars is already unlocked, this operation is a safe no-op and prevents duplicate
     * unlocks or mission log entries.
     *
     * @return true if Mars was unlocked by this briefing, false if it was already unlocked or no game is active
     */
    public boolean triggerInitialNavigationBriefing() {
        if (!isGameActive()) {
            return false;
        }
        Planet mars = gameState.getPlanetByName("Mars");
        if (mars == null) {
            return false;
        }
        if (!mars.isDiscovered()) {
            mars.setDiscovered(true);
            if (!hasLoggedEntry("Mars orbital corridor authorized")) {
                gameState.getMissionLog().addEntry(
                    "DISCOVERY",
                    "Planetary coordinates decoded: Mars orbital corridor authorized."
                );
            }
            return true;
        }
        return false;
    }

    /**
     * Authorizes Mars as a discovered flight destination if not yet revealed.
     * Delegates to triggerInitialNavigationBriefing() to guarantee duplicate prevention.
     *
     * @return true if Mars was newly authorized, false if already authorized or no game active
     */
    public boolean authorizeMars() {
        return triggerInitialNavigationBriefing();
    }

    /**
     * Checks if Mars is currently authorized/unlocked for interplanetary travel.
     *
     * @return true if Mars is discovered, false otherwise
     */
    public boolean isMarsUnlocked() {
        if (!isGameActive()) return false;
        Planet mars = gameState.getPlanetByName("Mars");
        return mars != null && mars.isDiscovered();
    }

    /**
     * Checks whether a log entry with the given text fragment already exists in the mission log.
     *
     * @param fragment Text fragment to match
     * @return true if found, false otherwise
     */
    public boolean hasLoggedEntry(String fragment) {
        if (!isGameActive() || gameState.getMissionLog() == null || fragment == null) {
            return false;
        }
        for (MissionLog.LogEntry entry : gameState.getMissionLog().getEntries()) {
            if (entry.getDescription().contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the narrative text of the initial Mars authorization briefing from High Command.
     * Can be presented by UI implementations (terminal or Swing) upon unlocking Mars.
     *
     * @return Formatted mission briefing narrative
     */
    public String getInitialNavigationBriefingText() {
        return "[PRIORITY TRANSMISSION: HIGH-COMMAND // SOL-EARTH]\n\n"
                + "Commander, long-range deep space telemetry has verified an initial\n"
                + "sub-space transmission vector matching the anomalous signal.\n\n"
                + "The primary telemetry beacon triangulates to the outer Martian perimeter.\n"
                + "High Command has officially AUTHORIZED destination: MARS.\n\n"
                + "Interplanetary burn trajectory: 20 FUEL.\n"
                + "All navigation locks for Sector MARS are now cleared.";
    }

    /**
     * Checks if the Outer Moons (Europa and Titan) Stage 2 authorization briefing should be presented.
     * True when in Stage 2/3, Europa is discovered, neither Europa nor Titan has been explored,
     * and the briefing has not yet been recorded in the mission log.
     */
    public boolean needsOuterMoonsNavigationBriefing() {
        if (!isGameActive()) return false;
        Planet europa = gameState.getPlanetByName("Europa");
        Planet titan = gameState.getPlanetByName("Titan");
        return (gameState.getCurrentStage().equals(MissionObjective.STAGE_2) || gameState.getCurrentStage().equals(MissionObjective.STAGE_3))
                && europa != null && europa.isDiscovered() && !europa.isExplored() && !titan.isExplored()
                && !hasLoggedEntry("Outer system corridors authorized");
    }

    /**
     * Records the Stage 2 Outer Moons authorization briefing in the flight recorder with duplicate protection.
     *
     * @return true if the briefing was newly recorded, false if not eligible or already logged
     */
    public boolean triggerOuterMoonsNavigationBriefing() {
        if (!needsOuterMoonsNavigationBriefing()) {
            return false;
        }
        gameState.getMissionLog().addEntry(
            "DISCOVERY",
            "Outer system corridors authorized: Navigation unlocked to Europa and Titan."
        );
        return true;
    }

    /**
     * Returns the verbatim narrative text of the Stage 2 Outer Moons authorization briefing from High Command.
     */
    public String getOuterMoonsNavigationBriefingText() {
        return "[PRIORITY TRANSMISSION: HIGH-COMMAND // STAGE 2 DEEP VOYAGER]\n\n"
                + "Commander, Stage 1 departure milestones have been fully validated.\n"
                + "High Command has cleared outer system navigation locks.\n\n"
                + "Destinations EUROPA and TITAN are now AUTHORIZED.\n"
                + "Interplanetary burn trajectories: 25 FUEL each.\n"
                + "Survey cryogenic environments and locate further transmission vectors.";
    }

    /**
     * Checks if the Kepler-186f Stage 3 extrasolar authorization briefing should be presented.
     * True when in Stage 3, Kepler-186f is discovered, and the briefing has not yet been logged.
     */
    public boolean needsKeplerNavigationBriefing() {
        if (!isGameActive()) return false;
        Planet kepler = gameState.getPlanetByName("Kepler-186f");
        return gameState.getCurrentStage().equals(MissionObjective.STAGE_3)
                && kepler != null && kepler.isDiscovered()
                && !hasLoggedEntry("Extrasolar corridor authorized");
    }

    /**
     * Records the Stage 3 Kepler-186f extrasolar authorization briefing in the flight recorder with duplicate protection.
     *
     * @return true if the briefing was newly recorded, false if not eligible or already logged
     */
    public boolean triggerKeplerNavigationBriefing() {
        if (!needsKeplerNavigationBriefing()) {
            return false;
        }
        gameState.getMissionLog().addEntry(
            "DISCOVERY",
            "Extrasolar corridor authorized: Kepler-186f jump trajectory unlocked."
        );
        return true;
    }

    /**
     * Returns the verbatim narrative text of the Stage 3 Kepler-186f extrasolar trajectory directive from High Command.
     */
    public String getKeplerNavigationBriefingText() {
        return "[PRIORITY TRANSMISSION: HIGH-COMMAND // STAGE 3 BEYOND THE UNKNOWN]\n\n"
                + "Commander, transmission telemetry analyzed across the outer moons is conclusive.\n"
                + "The unidentified sub-space carrier originates from exoplanet KEPLER-186F.\n\n"
                + "Deep-space interstellar jump coordinates calculated. Burn requirement: 30 FUEL.\n"
                + "You are authorized to proceed beyond the Sol system to the anomaly origin.";
    }

    /**
     * Checks whether an active game session is currently loaded in memory.
     *
     * @return true if an active GameState exists, false otherwise
     */
    public boolean isGameActive() {
        return this.gameState != null;
    }

    /**
     * Convenience alias for isGameActive().
     *
     * @return true if an active GameState exists, false otherwise
     */
    public boolean hasActiveGame() {
        return isGameActive();
    }

    /**
     * Returns the active GameState.
     *
     * @return The active GameState, or null if no session is active
     */
    public GameState getGameState() {
        return this.gameState;
    }

    /**
     * Sets or replaces the active GameState.
     *
     * @param gameState The new GameState to manage
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    // ==========================================
    // Persistence (Save / Load)
    // ==========================================

    /**
     * Checks if a persistent save file exists in the default saves directory.
     *
     * @return true if a save file exists with content
     */
    public boolean hasSave() {
        return SaveManager.hasSave();
    }

    /**
     * Checks if a persistent save file exists at the given file path.
     *
     * @param file Target save file
     * @return true if the file exists with content
     */
    public boolean hasSave(File file) {
        return SaveManager.hasSave(file);
    }

    /**
     * Retrieves summary metadata from the default save file.
     *
     * @return Metadata description string, or advisory if unavailable
     */
    public String getSaveSummary() {
        return SaveManager.getSaveSummary();
    }

    /**
     * Retrieves summary metadata from a specific save file.
     *
     * @param file Target save file
     * @return Metadata description string
     */
    public String getSaveSummary(File file) {
        return SaveManager.getSaveSummary(file);
    }

    /**
     * Saves the active game state to the default save repository (saves/orbit_save.dat).
     *
     * @return true if saving succeeded, false if no game is active or an I/O error occurred
     */
    public boolean saveGame() {
        if (!isGameActive()) {
            return false;
        }
        return saveGame(SaveManager.getDefaultSaveFile());
    }

    /**
     * Saves the active game state to the specified file.
     *
     * @param file Target file
     * @return true if saving succeeded, false otherwise
     */
    public boolean saveGame(File file) {
        if (!isGameActive() || file == null) {
            return false;
        }
        try {
            gameState.getMissionLog().addEntry(
                "MISSION",
                "Flight telemetry archived to " + file.getPath().replace('\\', '/') + "."
            );
            return SaveManager.saveGame(this.gameState, file);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Loads and restores a game session from the default save file.
     * Gracefully returns false if the file is missing or corrupted.
     *
     * @return true if loading succeeded and GameState was restored, false otherwise
     */
    public boolean continueGame() {
        return loadGame();
    }

    /**
     * Loads and restores a game session from the default save file.
     *
     * @return true if loading succeeded, false if file is missing or corrupted
     */
    public boolean loadGame() {
        return loadGame(SaveManager.getDefaultSaveFile());
    }

    /**
     * Loads and restores a game session from the specified save file.
     *
     * @param file Target save file
     * @return true if loading succeeded, false if file is missing or corrupted
     */
    public boolean loadGame(File file) {
        if (!hasSave(file)) {
            return false;
        }
        try {
            GameState loaded = SaveManager.loadGame(file);
            if (loaded != null) {
                this.gameState = loaded;
                return true;
            }
        } catch (Exception e) {
            // Gracefully handle missing or corrupted save file
            return false;
        }
        return false;
    }

    // ==========================================
    // Interplanetary Navigation
    // ==========================================

    /**
     * Calculates the standard propulsion fuel burn required to reach a destination.
     *
     * @param target The target planetary destination
     * @return Fuel required (20 for Earth/Mars, 25 for Europa/Titan, 30 for Kepler-186f, or 0 if null)
     */
    public int getFuelCost(Planet target) {
        if (target == null) return 0;
        return getFuelCost(target.getName());
    }

    /**
     * Calculates the fuel burn required to reach a planet by name.
     *
     * @param planetName Target planet name
     * @return Fuel required
     */
    public int getFuelCost(String planetName) {
        if (planetName == null) return 0;
        return switch (planetName.trim().toLowerCase()) {
            case "earth", "mars" -> 20;
            case "europa", "titan" -> 25;
            case "kepler-186f", "kepler" -> 30;
            default -> 20;
        };
    }

    /**
     * Checks if a destination is currently unlocked and authorized for travel.
     *
     * @param target Target planet
     * @return true if discovered/authorized, false if locked or null
     */
    public boolean isDestinationUnlocked(Planet target) {
        return target != null && target.isDiscovered();
    }

    /**
     * Checks if travel to the given planet is currently possible (active game, valid target,
     * different from current location, unlocked, and sufficient fuel).
     *
     * @param target Target planet
     * @return true if travel would succeed, false otherwise
     */
    public boolean canTravelTo(Planet target) {
        if (!isGameActive() || target == null) return false;
        Planet current = gameState.getCurrentLocation();
        if (target.getName().equalsIgnoreCase(current.getName())) return false;
        if (!target.isDiscovered()) return false;
        return gameState.getSpaceship().getFuel() >= getFuelCost(target);
    }

    /**
     * Checks if travel to the given planet by name is currently possible.
     *
     * @param planetName Target planet name
     * @return true if travel would succeed, false otherwise
     */
    public boolean canTravelTo(String planetName) {
        if (!isGameActive() || planetName == null) return false;
        Planet target = gameState.getPlanetByName(planetName);
        return canTravelTo(target);
    }

    /**
     * Navigates to a planet by name.
     *
     * @param planetName Planetary designation
     * @return Structured NavigationResult
     */
    public NavigationResult travelTo(String planetName) {
        if (!isGameActive()) {
            return NavigationResult.invalidDestination("No active game session.");
        }
        if (planetName == null || planetName.trim().isEmpty()) {
            return NavigationResult.invalidDestination("Navigation failed: Destination name cannot be null or empty.");
        }
        Planet target = gameState.getPlanetByName(planetName);
        if (target == null) {
            return NavigationResult.invalidDestination("Navigation failed: Unknown destination '" + planetName + "'.");
        }
        return travelTo(target);
    }

    /**
     * Executes interplanetary travel to the target destination.
     *
     * Validates:
     * 1. Active session exists
     * 2. Target is not null
     * 3. Target is not the current location
     * 4. Target is discovered/unlocked in the current campaign stage
     * 5. Spacecraft has sufficient fuel for the trajectory burn
     *
     * On success:
     * - Deducts fuel from spaceship
     * - Records propulsion burn in mission log
     * - Updates current celestial location
     * - Records orbital insertion in mission log
     * - Updates campaign mission objectives
     *
     * On failure:
     * - GameState remains completely unmodified.
     *
     * @param target The target planetary destination
     * @return Structured NavigationResult indicating outcome and user-facing message
     */
    public NavigationResult travelTo(Planet target) {
        if (!isGameActive()) {
            return NavigationResult.invalidDestination("No active game session.");
        }
        if (target == null) {
            return NavigationResult.invalidDestination("Navigation failed: Invalid or unknown planetary destination.");
        }

        if (gameState.isCampaignCompleted()) {
            return NavigationResult.invalidDestination("Navigation rejected: Campaign has concluded (" + gameState.getCompletedEnding() + "). Flight operations suspended.");
        }

        Planet current = gameState.getCurrentLocation();
        if (target.getName().equalsIgnoreCase(current.getName())) {
            return NavigationResult.alreadyAtDestination(
                target,
                "Navigation rejected: Vessel " + gameState.getPlayer().getSpaceshipName()
                    + " is already in orbit around " + target.getName() + "."
            );
        }

        if (!target.isDiscovered()) {
            return NavigationResult.destinationLocked(
                target,
                "Navigation locked: Route coordinates to " + target.getName()
                    + " are not authorized in this operational phase."
            );
        }

        int fuelCost = getFuelCost(target);
        Spaceship ship = gameState.getSpaceship();

        if (ship.getFuel() < fuelCost) {
            return NavigationResult.insufficientFuel(
                target,
                fuelCost,
                ship.getFuel(),
                "Insufficient fuel: Interplanetary trajectory to " + target.getName()
                    + " requires " + fuelCost + " Fuel, but reserves are at "
                    + ship.getFuel() + "%. Travel aborted."
            );
        }

        // Apply state changes:
        ship.consumeFuel(fuelCost);

        gameState.getMissionLog().addEntry(
            "NAVIGATION",
            "Propulsion burn initiated: Transiting from " + current.getName()
                + " to " + target.getName() + " (-" + fuelCost + " Fuel)."
        );

        gameState.setCurrentLocation(target);

        gameState.getMissionLog().addEntry(
            "NAVIGATION",
            "Orbital insertion confirmed: Established parking orbit at " + target.getName() + "."
        );

        gameState.updateMissionObjectives();

        return NavigationResult.success(
            target,
            fuelCost,
            "Orbital insertion confirmed: Established parking orbit at " + target.getName() + "."
        );
    }

    // ==========================================
    // Cargo & Supply Consumption
    // ==========================================

    /**
     * Checks if a specific cargo supply item can currently be dispensed and consumed.
     * Item must be recognized, consumable, available in hold (qty > 0), and its target
     * spaceship resource must be below maximum capacity (< 100%).
     *
     * @param itemName Name of item to check
     * @return true if consumption is permissible, false otherwise
     */
    public boolean canUseItem(String itemName) {
        if (!isGameActive() || itemName == null || gameState.isCampaignCompleted()) {
            return false;
        }
        String canonical = Inventory.resolveCanonicalName(itemName);
        if (canonical == null || !Inventory.isConsumable(canonical)) {
            return false;
        }
        Inventory inv = gameState.getInventory();
        if (inv == null || inv.getQuantity(canonical) <= 0) {
            return false;
        }
        Spaceship ship = gameState.getSpaceship();
        if (ship == null) {
            return false;
        }
        int currentVal = getTargetResourceValue(canonical, ship);
        return currentVal < Spaceship.MAX_RESOURCE;
    }

    /**
     * Dispenses and consumes one unit of an inventory supply item, applying its exact
     * restoration effect to the spacecraft's systems in accordance with flight rules.
     *
     * Item restoration mappings:
     * - Metal           -> Repairs Hull Integrity (+20 Hull)
     * - Energy Cells    -> Restores Reactor Energy (+25 Energy)
     * - Oxygen Canisters-> Replenishes Life Support (+25 Oxygen)
     * - Fuel Cells      -> Restores Propellant Reserves (+25 Fuel)
     * - Alien Artifacts -> Non-consumable discovery (rejected)
     *
     * @param itemName Name of the item to consume
     * @return ItemUseResult indicating success or failure rationale
     */
    public ItemUseResult useItem(String itemName) {
        if (!isGameActive()) {
            return ItemUseResult.failure(itemName, "No active flight session.");
        }
        if (gameState.isCampaignCompleted()) {
            return ItemUseResult.failure(itemName, "Campaign concluded (" + gameState.getCompletedEnding() + "). Resource dispensing suspended.");
        }
        if (itemName == null || itemName.trim().isEmpty()) {
            return ItemUseResult.failure("", "Invalid supply designation: item name cannot be null or empty.");
        }
        String canonical = Inventory.resolveCanonicalName(itemName);
        if (canonical == null) {
            return ItemUseResult.failure(itemName, "Unrecognized cargo item: '" + itemName + "'.");
        }
        if (!Inventory.isConsumable(canonical)) {
            return ItemUseResult.failure(canonical, canonical + " is an anomaly artifact and cannot be consumed.");
        }

        Inventory inv = gameState.getInventory();
        if (inv.getQuantity(canonical) <= 0) {
            return ItemUseResult.failure(canonical, "Insufficient supplies: 0 " + canonical + " available in cargo hold.");
        }

        Spaceship ship = gameState.getSpaceship();
        String resourceName = getTargetResourceName(canonical);
        int currentVal = getTargetResourceValue(canonical, ship);
        int restorationAmount = Inventory.getRestorationAmount(canonical);

        if (currentVal >= Spaceship.MAX_RESOURCE) {
            return ItemUseResult.failure(
                canonical,
                "Consumption rejected: Spacecraft " + resourceName + " is already at maximum capacity (100%). Item was not consumed."
            );
        }

        // Deduct 1 unit from cargo hold
        boolean removed = inv.remove(canonical, 1);
        if (!removed) {
            return ItemUseResult.failure(canonical, "Failed to dispense " + canonical + ": insufficient cargo quantity.");
        }

        // Apply exact restoration to Spaceship
        switch (canonical) {
            case Inventory.METAL -> ship.repairHull(restorationAmount);
            case Inventory.ENERGY_CELLS -> ship.restoreEnergy(restorationAmount);
            case Inventory.OXYGEN_CANISTERS -> ship.restoreOxygen(restorationAmount);
            case Inventory.FUEL_CELLS -> ship.restoreFuel(restorationAmount);
        }

        int newVal = getTargetResourceValue(canonical, ship);
        int remainingQty = inv.getQuantity(canonical);

        return ItemUseResult.success(
            canonical,
            resourceName,
            restorationAmount,
            currentVal,
            newVal,
            remainingQty,
            "1 " + canonical + " dispensed from cargo hold. " + resourceName + " restored (" + currentVal + "% -> " + newVal + "%)."
        );
    }

    private String getTargetResourceName(String canonicalItem) {
        return switch (canonicalItem) {
            case Inventory.METAL -> "Hull Integrity";
            case Inventory.ENERGY_CELLS -> "Reactor Energy";
            case Inventory.OXYGEN_CANISTERS -> "Life Support Oxygen";
            case Inventory.FUEL_CELLS -> "Propulsion Fuel";
            default -> "System Resource";
        };
    }

    private int getTargetResourceValue(String canonicalItem, Spaceship ship) {
        return switch (canonicalItem) {
            case Inventory.METAL -> ship.getHullIntegrity();
            case Inventory.ENERGY_CELLS -> ship.getEnergy();
            case Inventory.OXYGEN_CANISTERS -> ship.getOxygen();
            case Inventory.FUEL_CELLS -> ship.getFuel();
            default -> 0;
        };
    }

    // ==========================================
    // Planetary Surface Exploration
    // ==========================================

    private final Random random = new Random();

    /**
     * Checks if surface exploration expeditions are supported on the given planet.
     * In Stage 1: Earth and Mars.
     * In Stage 2: Europa and Titan (when discovered).
     * In Stage 3: Kepler-186f (when discovered).
     *
     * @param planet Target planet
     * @return true if exploration is supported, false otherwise
     */
    public boolean isExplorationSupported(Planet planet) {
        if (planet == null) return false;
        String name = planet.getName();
        if (name.equalsIgnoreCase("Earth") || name.equalsIgnoreCase("Mars")) {
            return true;
        }
        if (name.equalsIgnoreCase("Europa") || name.equalsIgnoreCase("Titan")) {
            return planet.isDiscovered();
        }
        if (name.equalsIgnoreCase("Kepler-186f")) {
            return planet.isDiscovered();
        }
        return false;
    }

    public boolean isExplorationSupported() {
        return isGameActive() && isExplorationSupported(gameState.getCurrentLocation());
    }

    /**
     * Checks if the vessel has sufficient vitals and is operational to launch an expedition.
     * Requires:
     * - Active session & supported planet
     * - Spaceship operational (hull > 0 && oxygen > 0)
     * - Energy >= 10 and Oxygen >= 5
     *
     * @param planet Target planet
     * @return true if expedition can be launched, false otherwise
     */
    public boolean canLaunchExpedition(Planet planet) {
        if (!isGameActive() || planet == null || gameState.isCampaignCompleted()) return false;
        if (!isExplorationSupported(planet)) return false;
        Spaceship ship = gameState.getSpaceship();
        if (ship == null || !ship.isOperational()) return false;
        return ship.getEnergy() >= 10 && ship.getOxygen() >= 5;
    }

    public boolean canLaunchExpedition() {
        return isGameActive() && canLaunchExpedition(gameState.getCurrentLocation());
    }

    public String getExpeditionReadinessMessage(Planet planet) {
        if (!isGameActive() || planet == null) return "No active mission session.";
        if (!isExplorationSupported(planet)) return "[HAZARDOUS // SURFACE DESCENT RESTRICTED]";
        Spaceship ship = gameState.getSpaceship();
        if (!ship.isOperational()) return "[SPACECRAFT CASUALTY // DEPLOYMENT INHIBITED]";
        if (ship.getEnergy() < 10 || ship.getOxygen() < 5) return "[VITALS INSUFFICIENT // DEPLOYMENT INHIBITED]";
        return "[EXPEDITION READY // VITALS SUFFICIENT]";
    }

    /**
     * Initiates a planetary surface excursion rover deployment.
     * Validates readiness, deducts 10 Energy and 5 Oxygen, marks planet explored,
     * and generates the exploration event or Kepler climax sequence.
     *
     * @return ExpeditionResult containing event details, choices, or applied effects
     */
    public ExpeditionResult startExpedition() {
        return startExpedition(this.random);
    }

    public ExpeditionResult startExpedition(Random rng) {
        if (!isGameActive()) {
            return ExpeditionResult.launchInhibited("No active flight session.");
        }
        if (gameState.isCampaignCompleted()) {
            return ExpeditionResult.launchInhibited("Campaign concluded (" + gameState.getCompletedEnding() + "). Further surface exploration suspended.");
        }
        Planet planet = gameState.getCurrentLocation();
        if (!isExplorationSupported(planet)) {
            return ExpeditionResult.unsupportedPlanet("Atmospheric and radiation hazards prohibit surface landings on " + planet.getName() + " in this operational phase.");
        }
        Spaceship ship = gameState.getSpaceship();
        if (!ship.isOperational()) {
            return ExpeditionResult.launchInhibited("Spacecraft casualty alert: ODYSSEY-07 is no longer operational (Hull or Oxygen depleted).");
        }
        if (ship.getEnergy() < 10 || ship.getOxygen() < 5) {
            return ExpeditionResult.launchInhibited("Resource deficit: Excursion requires 10 Energy (available: " + ship.getEnergy() + "%) and 5 Oxygen (available: " + ship.getOxygen() + "%).");
        }

        // Deduct expedition costs
        ship.consumeEnergy(10);
        ship.consumeOxygen(5);

        // Mark planet explored
        planet.setExplored(true);
        gameState.updateMissionObjectives();

        // Check if Kepler-186f climax applies
        if (planet.getName().equalsIgnoreCase("Kepler-186f") && !gameState.isSignalSourceDiscovered()) {
            gameState.getInventory().add(Inventory.ALIEN_ARTIFACTS, 1);
            gameState.setSignalSourceDiscovered(true);
            gameState.getMissionLog().addEntry("DISCOVERY", "Signal source discovered on Kepler-186f: Extrasolar Megastructure cataloged & Alien Artifact secured.");
            gameState.getMissionLog().addEntry("EXPEDITION", "Surface reconnaissance excursion completed on Kepler-186f.");
            gameState.updateMissionObjectives();

            List<String> effectLog = List.of(
                "[+] EXTRASOLAR RECOVERY: +1 ALIEN ARTIFACT secured into stasis hold.",
                "[i] SIGNAL TELEMETRY: Carrier frequency 1420.405 MHz triangulated to megastructure core."
            );
            return ExpeditionResult.keplerClimax("Subterranean basalt caverns breached. Ancient monolithic megastructure cataloged and Alien Artifact secured!", effectLog);
        }

        // Select random event
        Random r = (rng != null) ? rng : this.random;
        ExplorationEvent event = ExplorationEvent.getRandomEvent(r);

        // On outer celestial bodies, record materials milestone
        if (planet.getName().equalsIgnoreCase("Europa") || planet.getName().equalsIgnoreCase("Titan")) {
            gameState.setMaterialsCollectedDuringStage2(true);
        }

        // Dispatch by category
        switch (event.getCategory()) {
            case ExplorationEvent.CATEGORY_HAZARD -> {
                String counterItem = event.getCountermeasureItem();
                int availableCount = gameState.getInventory().getQuantity(counterItem);
                List<String> choices = new ArrayList<>();
                if (availableCount > 0) {
                    choices.add("DEPLOY COUNTERMEASURE (-1 " + counterItem + ")");
                }
                choices.add("BRACE FOR IMPACT & ACCEPT DAMAGE");
                return ExpeditionResult.hazardChoice(event, "Hazard encountered: " + event.getTitle() + " (" + event.getPrimaryPenaltyDescription() + ")", choices);
            }
            case ExplorationEvent.CATEGORY_ENCOUNTER -> {
                if (event.getId().equalsIgnoreCase("enc_unidentified_signal")) {
                    List<String> choices = List.of(
                        "Triangulate Transmission Vector",
                        "Scan Local Topography",
                        "Log Spectral Harmonics & Archive"
                    );
                    return ExpeditionResult.beaconChoice(event, "Sub-space anomalous beacon detected oscillating at carrier frequency 1420.405 MHz.", choices);
                } else if (event.getId().equalsIgnoreCase("enc_ancient_civilization")) {
                    List<String> choices = List.of(
                        "Optical Glyph Deciphering",
                        "Electromagnetic Field Probe",
                        "Establish Exclusion Zone"
                    );
                    return ExpeditionResult.monolithChoice(event, "Ancient xenological monolith spire uncovered etched with mathematical glyphs.", choices);
                } else {
                    List<String> effectLog = event.applyEffects(ship, gameState.getInventory());
                    gameState.getMissionLog().addEntry("EXPEDITION", "Surface reconnaissance excursion completed on " + planet.getName() + ".");
                    gameState.updateMissionObjectives();
                    return ExpeditionResult.autoResolved(event, "Encounter completed on " + planet.getName() + ".", effectLog);
                }
            }
            case ExplorationEvent.CATEGORY_DISCOVERY -> {
                List<String> effectLog = event.applyEffects(ship, gameState.getInventory());
                if (event.hasRewards()) {
                    gameState.setResourceCollectedDuringMission(true);
                }
                gameState.getMissionLog().addEntry("DISCOVERY", "Surface discovery on " + planet.getName() + ": " + event.getTitle() + ".");
                gameState.getMissionLog().addEntry("EXPEDITION", "Surface reconnaissance excursion completed on " + planet.getName() + ".");
                gameState.updateMissionObjectives();
                return ExpeditionResult.autoResolved(event, "Surface discovery cataloged: " + event.getTitle() + ".", effectLog);
            }
            case ExplorationEvent.CATEGORY_RESOURCE -> {
                List<String> effectLog = event.applyEffects(ship, gameState.getInventory());
                gameState.setResourceCollectedDuringMission(true);
                gameState.getMissionLog().addEntry("RESOURCE", "Resource salvage recovered on " + planet.getName() + ": " + event.getTitle() + ".");
                gameState.getMissionLog().addEntry("EXPEDITION", "Surface reconnaissance excursion completed on " + planet.getName() + ".");
                gameState.updateMissionObjectives();
                return ExpeditionResult.autoResolved(event, "Resource cache salvaged: " + event.getTitle() + ".", effectLog);
            }
            default -> {
                List<String> effectLog = event.applyEffects(ship, gameState.getInventory());
                gameState.getMissionLog().addEntry("EXPEDITION", "Surface reconnaissance excursion completed on " + planet.getName() + ".");
                gameState.updateMissionObjectives();
                return ExpeditionResult.autoResolved(event, "Excursion completed: " + event.getTitle() + ".", effectLog);
            }
        }
    }

    /**
     * Resolves a pending hazard event, either by deploying a countermeasure supply or accepting the damage.
     *
     * @param event Hazard event
     * @param useCountermeasure true to consume 1 countermeasure item to negate all damage, false to sustain penalties
     * @return Outcome summary string
     */
    public List<String> resolveHazard(ExplorationEvent event, boolean useCountermeasure) {
        if (!isGameActive() || event == null) {
            return Collections.emptyList();
        }
        Planet planet = gameState.getCurrentLocation();
        Spaceship ship = gameState.getSpaceship();
        Inventory inv = gameState.getInventory();
        String counterItem = event.getCountermeasureItem();

        List<String> log = new ArrayList<>();
        if (useCountermeasure && counterItem != null && inv.getQuantity(counterItem) > 0) {
            inv.remove(counterItem, 1);
            gameState.getMissionLog().addEntry("HAZARD", "Hazard countered on " + planet.getName() + ": Used 1 " + counterItem + " to mitigate " + event.getTitle() + ".");
            log.add("[+] COUNTERMEASURE DEPLOYED: Expended 1 " + counterItem + " (Remaining: " + inv.getQuantity(counterItem) + " units).");
            log.add("[+] THREAT NEUTRALIZED: Structural shielding absorbed impact. Zero damage sustained!");
        } else {
            List<String> effects = event.applyEffects(ship, inv);
            gameState.getMissionLog().addEntry("HAZARD", "Hazard sustained on " + planet.getName() + ": " + event.getTitle() + " (" + event.getPrimaryPenaltyDescription() + ").");
            log.addAll(effects);
        }

        gameState.getMissionLog().addEntry("EXPEDITION", "Surface reconnaissance excursion completed on " + planet.getName() + ".");
        gameState.updateMissionObjectives();
        return log;
    }

    /**
     * Resolves the Unidentified Beacon encounter with tactical player decision.
     */
    public String resolveBeaconEncounter(ExplorationEvent event, int choiceIndex) {
        if (!isGameActive() || event == null) return "";
        Planet planet = gameState.getCurrentLocation();

        String actionLabel;
        String narrativeOutcome;
        switch (choiceIndex) {
            case 0 -> {
                actionLabel = "Vector Triangulation";
                narrativeOutcome = "Excursion arrays lock onto carrier oscillation at 1420.405 MHz. Vector coordinates triangulate along a deep-space axis leading beyond the Sol system.";
            }
            case 1 -> {
                actionLabel = "Perimeter Topography Scan";
                narrativeOutcome = "High-gain radar sweeps confirm no artificial terrestrial structures or repeaters. The rhythmic pulse originates from an extrasolar trajectory.";
            }
            default -> {
                actionLabel = "Spectral Waveform Logging";
                narrativeOutcome = "Continuous harmonic waveforms digitized and mirrored to the spacecraft flight recorder for deep-space signal correlation.";
            }
        }

        gameState.setTransmissionEvidenceDiscovered(true);
        gameState.getMissionLog().addEntry("SIGNAL", "Unidentified deep-space transmission analyzed on " + planet.getName() + " (" + actionLabel + ")!");
        gameState.getMissionLog().addEntry("EXPEDITION", "Surface reconnaissance excursion completed on " + planet.getName() + ".");
        gameState.updateMissionObjectives();

        return narrativeOutcome;
    }

    /**
     * Resolves the Ancient Monolith encounter with tactical player decision.
     */
    public String resolveMonolithEncounter(ExplorationEvent event, int choiceIndex) {
        if (!isGameActive() || event == null) return "";
        Planet planet = gameState.getCurrentLocation();

        String actionLabel;
        String narrativeOutcome;
        switch (choiceIndex) {
            case 0 -> {
                actionLabel = "Glyph Deciphering";
                narrativeOutcome = "Linguistic sensors register prime number sequences and stellar constellations. The mathematics imply a map, though no physical artifacts are disturbed.";
            }
            case 1 -> {
                actionLabel = "Field Resonance Sampling";
                narrativeOutcome = "Spectrometry detects dormant sub-space energy humming within the alloy matrix. The spire remains inert, safe from contamination.";
            }
            default -> {
                actionLabel = "Exclusion Zone Tagging";
                narrativeOutcome = "Perimeter beacon deployed. Sector coordinates registered with high command as a protected xenological anomaly site.";
            }
        }

        gameState.getMissionLog().addEntry("ENCOUNTER", "Xenological monolith investigated on " + planet.getName() + " (" + actionLabel + ").");
        gameState.getMissionLog().addEntry("EXPEDITION", "Surface reconnaissance excursion completed on " + planet.getName() + ".");
        gameState.updateMissionObjectives();

        return narrativeOutcome;
    }

    // ==========================================
    // Delegate State Accessors
    // ==========================================

    public Player getPlayer() {
        return isGameActive() ? gameState.getPlayer() : null;
    }

    public Spaceship getSpaceship() {
        return isGameActive() ? gameState.getSpaceship() : null;
    }

    public Planet getCurrentLocation() {
        return isGameActive() ? gameState.getCurrentLocation() : null;
    }

    public Inventory getInventory() {
        return isGameActive() ? gameState.getInventory() : null;
    }

    public MissionLog getMissionLog() {
        return isGameActive() ? gameState.getMissionLog() : null;
    }

    public List<Planet> getPlanets() {
        return isGameActive() ? gameState.getPlanets() : Collections.emptyList();
    }

    public Planet getPlanetByName(String name) {
        return isGameActive() ? gameState.getPlanetByName(name) : null;
    }

    public NavigationResult navigate(Planet target) {
        return travelTo(target);
    }

    public List<MissionObjective> getObjectives() {
        return isGameActive() ? gameState.getObjectives() : Collections.emptyList();
    }

    // ==========================================
    // Campaign Progression & Endings
    // ==========================================

    public boolean isCampaignCompleted() {
        return isGameActive() && gameState.isCampaignCompleted();
    }

    public String getCompletedEnding() {
        return isGameActive() ? gameState.getCompletedEnding() : null;
    }

    public String getCurrentStage() {
        return isGameActive() ? gameState.getCurrentStage() : MissionObjective.STAGE_1;
    }

    public int getMissionProgress() {
        return isGameActive() ? gameState.getMissionProgress() : 0;
    }

    public String getStageMissionTitle(String stage) {
        if (stage == null) return "OPERATION DEEP SPACE EXPLORATION";
        return switch (stage) {
            case MissionObjective.STAGE_1 -> "OPERATION SOL DEPARTURE // FIRST INTERPLANETARY FLIGHT";
            case MissionObjective.STAGE_2 -> "PROJECT DEEP VOYAGER // OUTER SYSTEM RECONNAISSANCE";
            case MissionObjective.STAGE_3 -> "BEYOND THE UNKNOWN // KEPLER-186F ANOMALY RESOLUTION";
            default -> "OPERATION DEEP SPACE EXPLORATION";
        };
    }

    public String getNextDirectiveAdvice() {
        if (!isGameActive()) {
            return "No active flight mission in progress.";
        }
        if (gameState.isCampaignCompleted()) {
            return "Campaign directives resolved (" + gameState.getCompletedEnding() + "). Telemetry archived.";
        }

        if (gameState.getCurrentStage().equals(MissionObjective.STAGE_1)) {
            MissionObjective launch = gameState.getObjectiveById("obj_launch_earth");
            MissionObjective travel = gameState.getObjectiveById("obj_travel_mars");
            MissionObjective explore = gameState.getObjectiveById("obj_explore_surface");
            MissionObjective resources = gameState.getObjectiveById("obj_collect_resources");

            if (launch != null && !launch.isCompleted()) {
                return "Disengage mooring clamps and burn propulsion vector to Mars via Navigation Console.";
            } else if (travel != null && !travel.isCompleted()) {
                return "Complete interplanetary transit to Mars and achieve stable orbital insertion.";
            } else if (explore != null && !explore.isCompleted()) {
                return "Deploy surface reconnaissance excursion on Mars to survey local geography.";
            } else if (resources != null && !resources.isCompleted()) {
                return "Conduct planetary surface surveys to locate and harvest vital cargo resources.";
            } else {
                return "Stage 1 directives fulfilled. Stand by for Deep Space Exploration authorization.";
            }
        } else if (gameState.getCurrentStage().equals(MissionObjective.STAGE_2)) {
            MissionObjective objEuropa = gameState.getObjectiveById("obj_explore_europa");
            MissionObjective objTitan = gameState.getObjectiveById("obj_visit_titan");
            MissionObjective objMat = gameState.getObjectiveById("obj_collect_materials");
            MissionObjective objSig = gameState.getObjectiveById("obj_discover_transmission");

            if (objEuropa != null && !objEuropa.isCompleted()) {
                return "Navigate to Europa and deploy surface reconnaissance to explore the subsurface ocean.";
            } else if (objTitan != null && !objTitan.isCompleted()) {
                return "Navigate to Titan and survey its cryogenic hydrocarbon seas.";
            } else if (objMat != null && !objMat.isCompleted()) {
                return "Harvest outer system salvage materials on Europa or Titan.";
            } else if (objSig != null && !objSig.isCompleted()) {
                return "Scan planetary anomalies for evidence of the mysterious sub-space transmission.";
            } else {
                return "Stage 2 directives fulfilled. Kepler-186f deep space corridor authorized.";
            }
        } else {
            if (gameState.getCurrentLocation() == null || !gameState.getCurrentLocation().getName().equalsIgnoreCase("Kepler-186f")) {
                return "Initiate deep-space burn (30 Fuel) to navigate to extrasolar destination Kepler-186f.";
            } else if (!gameState.isSignalSourceDiscovered()) {
                return "Conduct surface exploration on Kepler-186f to pinpoint the transmission source.";
            } else {
                return "Signal source cataloged! Execute FINAL MISSION DIRECTIVE to determine mission climax.";
            }
        }
    }

    public boolean canInitiateFinalDirective() {
        if (!isGameActive()) {
            return false;
        }
        Planet loc = gameState.getCurrentLocation();
        return loc != null && loc.getName().equalsIgnoreCase("Kepler-186f")
                && gameState.isSignalSourceDiscovered()
                && !gameState.isCampaignCompleted();
    }

    public boolean canPerformEmergencyRefuel() {
        if (!isGameActive()) {
            return false;
        }
        return gameState.getSpaceship().getFuel() < 20
                && gameState.getInventory().getQuantity(Inventory.FUEL_CELLS) > 0;
    }

    public int getEmergencyRefuelCellsNeeded() {
        if (!canPerformEmergencyRefuel()) {
            return 0;
        }
        Spaceship ship = gameState.getSpaceship();
        Inventory inv = gameState.getInventory();
        int neededCells = (int) Math.ceil((20.0 - ship.getFuel()) / 20.0);
        int availableCells = inv.getQuantity(Inventory.FUEL_CELLS);
        return Math.min(neededCells, availableCells);
    }

    public int performEmergencyRefuel() {
        if (!canPerformEmergencyRefuel()) {
            return 0;
        }
        Spaceship ship = gameState.getSpaceship();
        Inventory inv = gameState.getInventory();
        int neededCells = (int) Math.ceil((20.0 - ship.getFuel()) / 20.0);
        int availableCells = inv.getQuantity(Inventory.FUEL_CELLS);
        int cellsToUse = Math.min(neededCells, availableCells);
        if (cellsToUse > 0) {
            inv.remove(Inventory.FUEL_CELLS, cellsToUse);
            ship.restoreFuel(cellsToUse * 20);
            gameState.getMissionLog().addEntry("INVENTORY", "Emergency fuel cells transferred: +" + (cellsToUse * 20) + " Fuel.");
        }
        return cellsToUse;
    }

    public EndingResult resolveReturnEnding() {
        if (!canInitiateFinalDirective()) {
            return null;
        }
        Spaceship ship = gameState.getSpaceship();
        Inventory inv = gameState.getInventory();

        boolean hasArtifact = inv.getQuantity(Inventory.ALIEN_ARTIFACTS) >= 1;
        boolean operational = ship.isOperational();
        boolean sufficientFuel = ship.getFuel() >= 20;

        if (hasArtifact && operational && sufficientFuel) {
            ship.consumeFuel(20);
            String endingTitle = "Ending A: Successful Return";
            gameState.setCompletedEnding(endingTitle);
            Planet earth = gameState.getPlanetByName("Earth");
            if (earth != null) {
                gameState.setCurrentLocation(earth);
            }
            gameState.getMissionLog().addEntry("MISSION", "CAMPAIGN CONCLUDED: Ending A - Successful Return. Vessel returned to Earth with Alien Artifact.");
            gameState.updateMissionObjectives();
            saveGame();
            return EndingResult.endingA(gameState);
        } else {
            String reason;
            if (!operational) {
                reason = "Spacecraft sustained catastrophic structural casualty during transit.";
            } else if (!sufficientFuel) {
                reason = "Propulsion fuel depleted in interstellar void (Available: " + ship.getFuel() + "%, Required: 20%).";
            } else {
                reason = "Vessel lost telemetry contact during deep-space transit.";
            }
            String endingTitle = "Ending B: Lost in Space";
            gameState.setCompletedEnding(endingTitle);
            gameState.getMissionLog().addEntry("MISSION", "CAMPAIGN CONCLUDED: Ending B - Lost in Space. " + reason);
            gameState.updateMissionObjectives();
            saveGame();
            return EndingResult.endingB(gameState, reason);
        }
    }

    public EndingResult resolveUnknownEnding() {
        if (!canInitiateFinalDirective()) {
            return null;
        }
        String endingTitle = "Ending C: The Unknown";
        gameState.setCompletedEnding(endingTitle);
        gameState.getMissionLog().addEntry("MISSION", "CAMPAIGN CONCLUDED: Ending C - The Unknown. Commander entered the alien gateway on Kepler-186f.");
        gameState.updateMissionObjectives();
        saveGame();
        return EndingResult.endingC(gameState);
    }

    public EndingResult getCompletedEndingResult() {
        if (!isCampaignCompleted()) {
            return null;
        }
        return EndingResult.fromGameState(gameState);
    }
}

