package orbit.controllers;

import orbit.models.GameState;
import orbit.models.Inventory;
import orbit.models.MissionObjective;
import orbit.models.Planet;
import orbit.models.Player;
import orbit.models.Spaceship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the narrative outcome, campaign debriefing statistics,
 * and operational telemetry for ORBIT's three distinct endings:
 * - Ending A: Successful Return
 * - Ending B: Lost in Space
 * - Ending C: The Unknown
 */
public class EndingResult {

    public enum Type {
        ENDING_A_SUCCESSFUL_RETURN,
        ENDING_B_LOST_IN_SPACE,
        ENDING_C_THE_UNKNOWN
    }

    private final Type type;
    private final String endingTitle;
    private final String outcomeBanner;
    private final String narrative;
    private final String failureReason; // Only populated for Ending B

    // Debriefing telemetry snapshot
    private final String commanderName;
    private final String spaceshipName;
    private final int finalFuel;
    private final int finalOxygen;
    private final int finalEnergy;
    private final int finalHull;
    private final List<CargoSnapshot> cargoSnapshot;
    private final List<PlanetSnapshot> planetSnapshot;
    private final List<ObjectiveSnapshot> objectiveSnapshot;

    public static class CargoSnapshot {
        private final String name;
        private final int quantity;

        public CargoSnapshot(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    public static class PlanetSnapshot {
        private final String name;
        private final String description;
        private final boolean discovered;
        private final boolean explored;

        public PlanetSnapshot(String name, String description, boolean discovered, boolean explored) {
            this.name = name;
            this.description = description;
            this.discovered = discovered;
            this.explored = explored;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public boolean isDiscovered() {
            return discovered;
        }

        public boolean isExplored() {
            return explored;
        }
    }

    public static class ObjectiveSnapshot {
        private final String title;
        private final String stage;
        private final boolean completed;
        private final String progressInfo;

        public ObjectiveSnapshot(String title, String stage, boolean completed, String progressInfo) {
            this.title = title;
            this.stage = stage;
            this.completed = completed;
            this.progressInfo = progressInfo;
        }

        public String getTitle() {
            return title;
        }

        public String getStage() {
            return stage;
        }

        public boolean isCompleted() {
            return completed;
        }

        public String getProgressInfo() {
            return progressInfo;
        }
    }

    private EndingResult(
            Type type,
            String endingTitle,
            String outcomeBanner,
            String narrative,
            String failureReason,
            GameState gameState
    ) {
        this.type = type;
        this.endingTitle = endingTitle;
        this.outcomeBanner = outcomeBanner;
        this.narrative = narrative;
        this.failureReason = failureReason;

        Player player = (gameState != null) ? gameState.getPlayer() : null;
        Spaceship ship = (gameState != null) ? gameState.getSpaceship() : null;
        Inventory inv = (gameState != null) ? gameState.getInventory() : null;

        this.commanderName = (player != null) ? player.getCommanderName() : "COMMANDER";
        this.spaceshipName = (player != null) ? player.getSpaceshipName() : "ODYSSEY-07";

        this.finalFuel = (ship != null) ? ship.getFuel() : 0;
        this.finalOxygen = (ship != null) ? ship.getOxygen() : 0;
        this.finalEnergy = (ship != null) ? ship.getEnergy() : 0;
        this.finalHull = (ship != null) ? ship.getHullIntegrity() : 0;

        List<CargoSnapshot> cargo = new ArrayList<>();
        if (inv != null) {
            for (String item : Inventory.VALID_ITEMS) {
                cargo.add(new CargoSnapshot(item, inv.getQuantity(item)));
            }
        }
        this.cargoSnapshot = Collections.unmodifiableList(cargo);

        List<PlanetSnapshot> planets = new ArrayList<>();
        if (gameState != null) {
            for (Planet p : gameState.getPlanets()) {
                planets.add(new PlanetSnapshot(p.getName(), p.getDescription(), p.isDiscovered(), p.isExplored()));
            }
        }
        this.planetSnapshot = Collections.unmodifiableList(planets);

        List<ObjectiveSnapshot> objectives = new ArrayList<>();
        if (gameState != null) {
            for (MissionObjective o : gameState.getObjectives()) {
                objectives.add(new ObjectiveSnapshot(o.getTitle(), o.getStage(), o.isCompleted(), o.getProgressInfo()));
            }
        }
        this.objectiveSnapshot = Collections.unmodifiableList(objectives);
    }

    public static EndingResult endingA(GameState state) {
        String cmdr = (state != null && state.getPlayer() != null) ? state.getPlayer().getCommanderName().toUpperCase() : "COMMANDER";
        String vessel = (state != null && state.getPlayer() != null) ? state.getPlayer().getSpaceshipName() : "ODYSSEY-07";

        String narrative = "After weeks of silent cruising through the dark gulf between star systems, " + vessel + " decelerates into Earth's high orbital dock.\n\n"
                + "Planetary sensors erupt with celebratory telemetry as communication channels flood with praise from Mission High Command. "
                + "The mysterious Alien Artifact is safely delivered into the custody of the Global Scientific Coalition.\n\n"
                + "Its deciphered quantum networks reveal blueprints for deep-space propulsion, energy synthesis, and the coordinates of distant civilizations. "
                + "Humanity is propelled into an interstellar golden age.\n\n"
                + "Commander " + cmdr + " is forever honored as the voyager who ventured beyond the unknown and brought back the future of humankind.";

        return new EndingResult(
                Type.ENDING_A_SUCCESSFUL_RETURN,
                "Ending A: Successful Return",
                "VICTORY: HUMANITY'S FIRST INTERSTELLAR TRIUMPH",
                narrative,
                null,
                state
        );
    }

    public static EndingResult endingB(GameState state, String failureReason) {
        String cmdr = (state != null && state.getPlayer() != null) ? state.getPlayer().getCommanderName().toUpperCase() : "COMMANDER";
        String vessel = (state != null && state.getPlayer() != null) ? state.getPlayer().getSpaceshipName() : "ODYSSEY-07";
        String reason = (failureReason != null && !failureReason.isEmpty()) ? failureReason : "Vessel lost telemetry contact during deep-space transit.";

        String narrative = "Though the investigation of Kepler-186f succeeded and the alien origin was uncovered, the perilous voyage took an unforgiving toll.\n\n"
                + reason + "\n\n"
                + "As propulsion fires flicker out, " + vessel + " slips into the silent expanse between the solar system and Kepler's red dwarf star. "
                + "The Alien Artifact rests securely in its stasis chamber, untouched.\n\n"
                + "Centuries may pass before another human exploration fleet retraces your trajectory. "
                + "Until then, your emergency beacon continues its lonely call into the dark, commemorating Commander " + cmdr + "'s bravery.";

        return new EndingResult(
                Type.ENDING_B_LOST_IN_SPACE,
                "Ending B: Lost in Space",
                "OUTCOME: LOST AMONG THE SILENT STARS",
                narrative,
                reason,
                state
        );
    }

    public static EndingResult endingC(GameState state) {
        String cmdr = (state != null && state.getPlayer() != null) ? state.getPlayer().getCommanderName().toUpperCase() : "COMMANDER";
        String vessel = (state != null && state.getPlayer() != null) ? state.getPlayer().getSpaceshipName() : "ODYSSEY-07";

        String narrative = "Transmitting a final farewell telemetry stream to Earth Command, Commander " + cmdr + " inserts the Alien Artifact into the spire.\n\n"
                + "The megastructure's core pulses with impossible light. The basalt walls dissolve into a tapestry of nebulae, quantum strings, and timelines of civilizations spanning billions of years.\n\n"
                + "Choosing curiosity over the safety of home, the Commander steps across the threshold. "
                + "Physical form gives way to pure consciousness, joining the cosmic entity that first whispered across the stars.\n\n"
                + "Back on the surface of Kepler-186f, vessel " + vessel + " rests in eternal silent orbit—a beacon marking where humanity first touched the infinite.";

        return new EndingResult(
                Type.ENDING_C_THE_UNKNOWN,
                "Ending C: The Unknown",
                "TRANSCENDENCE: STEPPING BEYOND THE UNKNOWN",
                narrative,
                null,
                state
        );
    }

    public static EndingResult fromGameState(GameState state) {
        if (state == null || state.getCompletedEnding() == null) {
            return null;
        }
        String ending = state.getCompletedEnding();
        if (ending.contains("Ending A") || ending.toLowerCase().contains("successful")) {
            return endingA(state);
        } else if (ending.contains("Ending B") || ending.toLowerCase().contains("lost")) {
            return endingB(state, "Vessel lost telemetry contact during deep-space transit.");
        } else if (ending.contains("Ending C") || ending.toLowerCase().contains("unknown")) {
            return endingC(state);
        }
        return endingA(state);
    }

    public Type getType() {
        return type;
    }

    public String getEndingTitle() {
        return endingTitle;
    }

    public String getOutcomeBanner() {
        return outcomeBanner;
    }

    public String getNarrative() {
        return narrative;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getCommanderName() {
        return commanderName;
    }

    public String getSpaceshipName() {
        return spaceshipName;
    }

    public int getFinalFuel() {
        return finalFuel;
    }

    public int getFinalOxygen() {
        return finalOxygen;
    }

    public int getFinalEnergy() {
        return finalEnergy;
    }

    public int getFinalHull() {
        return finalHull;
    }

    public List<CargoSnapshot> getCargoSnapshot() {
        return cargoSnapshot;
    }

    public List<PlanetSnapshot> getPlanetSnapshot() {
        return planetSnapshot;
    }

    public List<ObjectiveSnapshot> getObjectiveSnapshot() {
        return objectiveSnapshot;
    }
}

