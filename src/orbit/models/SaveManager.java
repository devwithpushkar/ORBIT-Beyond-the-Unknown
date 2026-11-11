package orbit.models;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SaveManager handles persistent file storage and restoration of active ORBIT game sessions.
 * Implements deterministic text-based serialization to and from the 'saves/' directory using Core Java.
 *
 * Preserves:
 * - Commander & Spaceship designations
 * - Spacecraft vitals (Fuel, Oxygen, Energy, Hull Integrity)
 * - Current celestial location
 * - Planetary exploration and discovery states
 * - Cargo hold inventory supplies
 * - Campaign mission objectives and progress
 * - Active campaign stage and decision flags
 * - Complete chronological mission log archive
 */
public class SaveManager {

    public static final String DEFAULT_SAVE_DIR = "saves";
    public static final String DEFAULT_SAVE_FILENAME = "orbit_save.dat";
    public static final String HEADER_SIGNATURE = "# ORBIT TELEMETRY SAVE FILE VERSION 1.0";

    public static File getDefaultSaveFile() {
        return new File(DEFAULT_SAVE_DIR, DEFAULT_SAVE_FILENAME);
    }

    /**
     * Checks if a valid persistent save file exists in the default location.
     */
    public static boolean hasSave() {
        return hasSave(getDefaultSaveFile());
    }

    /**
     * Checks if a save file exists and has content.
     */
    public static boolean hasSave(File file) {
        return file != null && file.exists() && file.isFile() && file.length() > 0;
    }

    /**
     * Retrieves high-level metadata from an existing save file for confirmation dialogues.
     */
    public static String getSaveSummary() {
        return getSaveSummary(getDefaultSaveFile());
    }

    /**
     * Retrieves summary information (Commander, Vessel, Location, Stage) from the save file.
     */
    public static String getSaveSummary(File file) {
        if (!hasSave(file)) {
            return "No save file available";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            String commander = "Unknown";
            String ship = "Unknown";
            String location = "Unknown";
            String stage = "Unknown";
            String completedEnding = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("commander=")) {
                    commander = line.substring("commander=".length()).trim();
                } else if (line.startsWith("ship=")) {
                    ship = line.substring("ship=".length()).trim();
                } else if (line.startsWith("current=")) {
                    location = line.substring("current=".length()).trim();
                } else if (line.startsWith("stage=")) {
                    stage = line.substring("stage=".length()).trim();
                } else if (line.startsWith("completed_ending=")) {
                    String ending = line.substring("completed_ending=".length()).trim();
                    if (!ending.equalsIgnoreCase("NONE") && !ending.isEmpty()) {
                        completedEnding = ending;
                    }
                }
            }
            if (completedEnding != null) {
                return String.format("Commander %s | Ship %s | Location: %s | %s [%s]", commander, ship, location, stage, completedEnding);
            }
            return String.format("Commander %s | Ship %s | Location: %s | %s", commander, ship, location, stage);
        } catch (Exception e) {
            return "Save metadata corrupted";
        }
    }

    /**
     * Saves the complete active GameState to the default save file.
     */
    public static boolean saveGame(GameState state) throws IOException {
        return saveGame(state, getDefaultSaveFile());
    }

    /**
     * Serializes the GameState to a designated target file.
     */
    public static boolean saveGame(GameState state, File file) throws IOException {
        if (state == null) {
            throw new IllegalArgumentException("Cannot save null GameState.");
        }
        if (file == null) {
            throw new IllegalArgumentException("Target save file cannot be null.");
        }

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(HEADER_SIGNATURE);
            writer.newLine();

            // 1. Player Section
            writer.write("[PLAYER]");
            writer.newLine();
            writer.write("commander=" + state.getPlayer().getCommanderName());
            writer.newLine();
            writer.write("ship=" + state.getPlayer().getSpaceshipName());
            writer.newLine();
            writer.newLine();

            // 2. Spaceship Section
            Spaceship ship = state.getSpaceship();
            writer.write("[SPACESHIP]");
            writer.newLine();
            writer.write("fuel=" + ship.getFuel());
            writer.newLine();
            writer.write("oxygen=" + ship.getOxygen());
            writer.newLine();
            writer.write("energy=" + ship.getEnergy());
            writer.newLine();
            writer.write("hull=" + ship.getHullIntegrity());
            writer.newLine();
            writer.newLine();

            // 3. Current Location
            writer.write("[LOCATION]");
            writer.newLine();
            writer.write("current=" + state.getCurrentLocation().getName());
            writer.newLine();
            writer.newLine();

            // 4. Campaign State
            writer.write("[CAMPAIGN]");
            writer.newLine();
            writer.write("stage=" + state.getCurrentStage());
            writer.newLine();
            writer.write("progress=" + state.getMissionProgress());
            writer.newLine();
            writer.write("departed_earth=" + state.hasDepartedEarth());
            writer.newLine();
            writer.write("resource_collected=" + state.isResourceCollectedDuringMission());
            writer.newLine();
            writer.write("transmission_discovered=" + state.isTransmissionEvidenceDiscovered());
            writer.newLine();
            writer.write("materials_collected=" + state.isMaterialsCollectedDuringStage2());
            writer.newLine();
            writer.write("signal_source_discovered=" + state.isSignalSourceDiscovered());
            writer.newLine();
            writer.write("completed_ending=" + (state.getCompletedEnding() != null ? state.getCompletedEnding() : "NONE"));
            writer.newLine();
            writer.newLine();

            // 5. Planets Section
            writer.write("[PLANETS]");
            writer.newLine();
            for (Planet p : state.getPlanets()) {
                writer.write(p.getName() + "|" + p.isDiscovered() + "|" + p.isExplored());
                writer.newLine();
            }
            writer.newLine();

            // 6. Inventory Section
            writer.write("[INVENTORY]");
            writer.newLine();
            Inventory inv = state.getInventory();
            for (String item : Inventory.VALID_ITEMS) {
                writer.write(item + "=" + inv.getQuantity(item));
                writer.newLine();
            }
            writer.newLine();

            // 7. Objectives Section
            writer.write("[OBJECTIVES]");
            writer.newLine();
            for (MissionObjective obj : state.getObjectives()) {
                writer.write(obj.getId() + "|" + obj.getStatus().name() + "|" + obj.getProgressInfo());
                writer.newLine();
            }
            writer.newLine();

            // 8. Mission Log Section
            writer.write("[MISSION_LOG]");
            writer.newLine();
            for (MissionLog.LogEntry entry : state.getMissionLog().getEntries()) {
                // Escape vertical bars in message if any
                String safeDesc = entry.getDescription().replace("|", " - ");
                writer.write(entry.getTimestamp() + "|" + entry.getCategory() + "|" + safeDesc);
                writer.newLine();
            }

            writer.flush();
            return true;
        }
    }

    /**
     * Loads and restores a complete GameState from the default save file.
     */
    public static GameState loadGame() throws IOException {
        return loadGame(getDefaultSaveFile());
    }

    /**
     * Parses and restores a GameState from the specified file.
     */
    public static GameState loadGame(File file) throws IOException {
        if (!hasSave(file)) {
            throw new IOException("Save file does not exist or is empty: " + (file != null ? file.getPath() : "null"));
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();
            if (firstLine == null || !firstLine.trim().equals(HEADER_SIGNATURE)) {
                throw new IOException("Invalid save file header or corrupted save data.");
            }

            Map<String, String> playerMap = new HashMap<>();
            Map<String, String> shipMap = new HashMap<>();
            Map<String, String> locationMap = new HashMap<>();
            Map<String, String> campaignMap = new HashMap<>();
            Map<String, String> inventoryMap = new HashMap<>();
            List<String> planetLines = new ArrayList<>();
            List<String> objectiveLines = new ArrayList<>();
            List<String> logLines = new ArrayList<>();

            String currentSection = null;
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1).toUpperCase();
                    continue;
                }

                if (currentSection == null) continue;

                switch (currentSection) {
                    case "PLAYER" -> {
                        int eq = line.indexOf('=');
                        if (eq > 0) {
                            playerMap.put(line.substring(0, eq).trim().toLowerCase(), line.substring(eq + 1).trim());
                        }
                    }
                    case "SPACESHIP" -> {
                        int eq = line.indexOf('=');
                        if (eq > 0) {
                            shipMap.put(line.substring(0, eq).trim().toLowerCase(), line.substring(eq + 1).trim());
                        }
                    }
                    case "LOCATION" -> {
                        int eq = line.indexOf('=');
                        if (eq > 0) {
                            locationMap.put(line.substring(0, eq).trim().toLowerCase(), line.substring(eq + 1).trim());
                        }
                    }
                    case "CAMPAIGN" -> {
                        int eq = line.indexOf('=');
                        if (eq > 0) {
                            campaignMap.put(line.substring(0, eq).trim().toLowerCase(), line.substring(eq + 1).trim());
                        }
                    }
                    case "INVENTORY" -> {
                        int eq = line.indexOf('=');
                        if (eq > 0) {
                            inventoryMap.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
                        }
                    }
                    case "PLANETS" -> planetLines.add(line);
                    case "OBJECTIVES" -> objectiveLines.add(line);
                    case "MISSION_LOG" -> logLines.add(line);
                }
            }

            // Validate essential data
            String cmdrName = playerMap.getOrDefault("commander", "Commander");
            String shipName = playerMap.getOrDefault("ship", Player.DEFAULT_SPACESHIP_NAME);
            Player player = new Player(cmdrName, shipName);

            int fuel = Integer.parseInt(shipMap.getOrDefault("fuel", "100"));
            int oxygen = Integer.parseInt(shipMap.getOrDefault("oxygen", "100"));
            int energy = Integer.parseInt(shipMap.getOrDefault("energy", "100"));
            int hull = Integer.parseInt(shipMap.getOrDefault("hull", "100"));
            Spaceship spaceship = new Spaceship(fuel, oxygen, energy, hull);

            Inventory inventory = new Inventory();
            for (Map.Entry<String, String> entry : inventoryMap.entrySet()) {
                String itemName = Inventory.resolveCanonicalName(entry.getKey());
                if (itemName != null) {
                    try {
                        int qty = Integer.parseInt(entry.getValue());
                        inventory.setQuantity(itemName, Math.max(0, qty));
                    } catch (NumberFormatException ignored) {}
                }
            }

            GameState state = new GameState(player, spaceship, inventory);

            // Restore Planets
            for (String pLine : planetLines) {
                String[] parts = pLine.split("\\|");
                if (parts.length >= 3) {
                    String pName = parts[0].trim();
                    boolean discovered = Boolean.parseBoolean(parts[1].trim());
                    boolean explored = Boolean.parseBoolean(parts[2].trim());

                    Planet planet = state.getPlanetByName(pName);
                    if (planet != null) {
                        planet.setDiscovered(discovered);
                        planet.setExplored(explored);
                    }
                }
            }

            // Restore Location
            String locName = locationMap.getOrDefault("current", "Earth");
            Planet currentPlanet = state.getPlanetByName(locName);
            if (currentPlanet != null) {
                state.setCurrentLocation(currentPlanet);
            }

            // Restore Campaign flags
            String stage = campaignMap.getOrDefault("stage", MissionObjective.STAGE_1);
            state.setCurrentStage(stage);
            state.setHasDepartedEarth(Boolean.parseBoolean(campaignMap.getOrDefault("departed_earth", "false")));
            state.setResourceCollectedDuringMission(Boolean.parseBoolean(campaignMap.getOrDefault("resource_collected", "false")));
            state.setTransmissionEvidenceDiscovered(Boolean.parseBoolean(campaignMap.getOrDefault("transmission_discovered", "false")));
            state.setMaterialsCollectedDuringStage2(Boolean.parseBoolean(campaignMap.getOrDefault("materials_collected", "false")));
            state.setSignalSourceDiscovered(Boolean.parseBoolean(campaignMap.getOrDefault("signal_source_discovered", "false")));

            String ending = campaignMap.getOrDefault("completed_ending", "NONE");
            if (!ending.equalsIgnoreCase("NONE") && !ending.trim().isEmpty()) {
                state.setCompletedEnding(ending.trim());
            }

            // Restore Objectives
            for (String objLine : objectiveLines) {
                String[] parts = objLine.split("\\|", 3);
                if (parts.length >= 2) {
                    String objId = parts[0].trim();
                    String statusStr = parts[1].trim();
                    String progInfo = (parts.length >= 3) ? parts[2].trim() : "";

                    MissionObjective obj = state.getObjectiveById(objId);
                    if (obj != null) {
                        try {
                            MissionObjective.Status st = MissionObjective.Status.valueOf(statusStr);
                            obj.setStatus(st);
                            obj.setProgressInfo(progInfo);
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
            }

            // Restore Mission Log
            if (!logLines.isEmpty()) {
                state.getMissionLog().clear();
                for (String logLine : logLines) {
                    String[] parts = logLine.split("\\|", 3);
                    if (parts.length >= 3) {
                        state.getMissionLog().addLoadedEntry(parts[0].trim(), parts[1].trim(), parts[2].trim());
                    }
                }
            }

            if (campaignMap.containsKey("progress")) {
                try {
                    state.setMissionProgress(Integer.parseInt(campaignMap.get("progress")));
                } catch (NumberFormatException ignored) {}
            }

            return state;
        }
    }

    /**
     * Deletes the persistent save file if needed.
     */
    public static boolean deleteSave() {
        return deleteSave(getDefaultSaveFile());
    }

    /**
     * Deletes a specific save file.
     */
    public static boolean deleteSave(File file) {
        if (file != null && file.exists()) {
            return file.delete();
        }
        return false;
    }
}
