package orbit;

import orbit.models.ExplorationEvent;
import orbit.models.GameState;
import orbit.models.Inventory;
import orbit.models.MissionLog;
import orbit.models.MissionObjective;
import orbit.models.Planet;
import orbit.models.Player;
import orbit.models.SaveManager;
import orbit.models.Spaceship;
import orbit.ui.TerminalUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Game controls the overall lifecycle of ORBIT: Beyond the Unknown.
 * Orchestrates the startup sequence, main menu loop, and active game session
 * Command Center operations, including interplanetary navigation, galaxy cartography,
 * cargo inventory management, and planetary surface exploration expeditions.
 */
public class Game {

    private boolean running;
    private GameState gameState;
    private final Random random;

    public Game() {
        this(new Random());
    }

    public Game(Random random) {
        this.running = false;
        this.gameState = null;
        this.random = (random != null) ? random : new Random();
    }

    /**
     * Starts the ORBIT application lifecycle.
     */
    public void start() {
        this.running = true;

        // Display atmospheric startup sequence
        showStartupSequence();

        // Enter main menu interaction loop
        while (running) {
            displayMainMenu();
            int choice = TerminalUI.readMenuOption("  COMMAND > ", 1, 5);

            if (choice == -1) {
                // Stream ended (EOF), terminate cleanly
                running = false;
                break;
            }

            switch (choice) {
                case 1 -> handleNewGame();
                case 2 -> handleContinueGame();
                case 3 -> handleHowToPlay();
                case 4 -> handleCredits();
                case 5 -> handleExit();
                default -> TerminalUI.printWarning("Invalid command option.");
            }
        }
    }

    /**
     * Renders the atmospheric spacecraft startup sequence.
     */
    private void showStartupSequence() {
        TerminalUI.clearScreen();

        // Step 1: Cold boot BIOS diagnostic
        TerminalUI.printDoubleBorder();
        TerminalUI.printCenteredBoxLine(TerminalUI.brightWhite(TerminalUI.bold("SPACECRAFT ODYSSEY-07 - COLD BOOT SEQUENCE")));
        TerminalUI.printCenteredBoxLine(TerminalUI.gray("QUANTUM AVIONICS FIRMWARE v4.19 // BUS: ONLINE"));
        TerminalUI.printDoubleBorder();
        System.out.println();

        TerminalUI.sleep(150);
        TerminalUI.renderSpinner("SCANNING MAIN HARDWARE INTERFACES", 350);
        TerminalUI.renderProgressAnimation("INITIALIZING TELEMETRY MATRIX", 400);
        TerminalUI.sleep(200);

        // Step 2: Clear and display the primary ORBIT Title Logo
        TerminalUI.clearScreen();
        TerminalUI.printDoubleBorder();
        TerminalUI.printEmptyBoxLine();

        String[] logoLines = {
            "      ____  ____  ____  ________ ",
            "     / __ \\/ __ \\/ __ )/  _/_  __/",
            "    / / / / /_/ / __  |/ /  / /   ",
            "   / /_/ / _, _/ /_/ // /  / /    ",
            "   \\____/_/ |_/_____/___/ /_/     "
        };

        for (String line : logoLines) {
            TerminalUI.printCenteredBoxLine(TerminalUI.brightCyan(TerminalUI.bold(line)));
        }

        TerminalUI.printEmptyBoxLine();
        TerminalUI.printCenteredBoxLine(TerminalUI.brightWhite(TerminalUI.bold("B E Y O N D   T H E   U N K N O W N")));
        TerminalUI.printCenteredBoxLine(TerminalUI.blue("-= Deep Space Exploration & Survival Terminal =-"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        // Step 3: Subsystem initialization diagnostics
        TerminalUI.renderProgressAnimation("POWERING SPACECRAFT SUBSYSTEMS", 450);
        System.out.println();

        String[] subsystems = {
            "FLIGHT COMPUTER",
            "NAVIGATION SYSTEM",
            "LIFE SUPPORT",
            "COMMUNICATIONS",
            "QUANTUM REACTOR CORE",
            "LONG-RANGE SENSOR ARRAY"
        };

        String[] statuses = {
            "ONLINE",
            "ONLINE",
            "ONLINE",
            "ONLINE",
            "STABLE",
            "CALIBRATED"
        };

        for (int i = 0; i < subsystems.length; i++) {
            TerminalUI.printSubsystemStatus(subsystems[i], statuses[i]);
            TerminalUI.sleep(60);
        }

        System.out.println();
        TerminalUI.printSingleBorder();
        TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("ALL PRIMARY SYSTEMS NOMINAL // STANDBY FOR COMMAND"));
        TerminalUI.printSingleBorder();
        System.out.println();

        System.out.print("  " + TerminalUI.brightCyan("Press [ENTER] to access Command Deck..."));
        System.out.flush();
        TerminalUI.readLine("");
        System.out.println();
    }

    /**
     * Renders the interactive Main Menu dashboard.
     */
    private void displayMainMenu() {
        TerminalUI.clearScreen();
        TerminalUI.printHeaderWithSubtitle(
            "ORBIT: BEYOND THE UNKNOWN",
            "SPACECRAFT ODYSSEY-07 // COMMAND DECK"
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[1]") + " " + TerminalUI.brightWhite("NEW GAME")
                + TerminalUI.gray("        - Begin a new space exploration mission"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[2]") + " " + TerminalUI.brightWhite("CONTINUE GAME")
                + TerminalUI.gray("   - Resume saved exploration flight"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[3]") + " " + TerminalUI.brightWhite("HOW TO PLAY")
                + TerminalUI.gray("     - Review spacecraft operations & flight manual"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[4]") + " " + TerminalUI.brightWhite("CREDITS")
                + TerminalUI.gray("         - View mission development team"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[5]") + " " + TerminalUI.brightWhite("EXIT")
                + TerminalUI.gray("            - Terminate terminal session & power down"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printCenteredBoxLine(
            TerminalUI.green("SYS: READY") + TerminalUI.gray(" | ")
            + TerminalUI.cyan("SECTOR: 00-SOL") + TerminalUI.gray(" | ")
            + TerminalUI.yellow("REG: EX-7749-ODYSSEY")
        );
        TerminalUI.printDoubleBorder();
        System.out.println();
    }

    /**
     * Handles [1] NEW GAME:
     * Prompts for commander name, initializes Player, Spaceship, and GameState,
     * then enters the Command Center.
     */
    private void handleNewGame() {
        TerminalUI.clearScreen();
        TerminalUI.printHeader("MISSION ENROLLMENT // COMMANDER PROFILE");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Welcome aboard the " + TerminalUI.brightCyan("ODYSSEY-07") + ".");
        TerminalUI.printBoxLine("  Please enter your name to register command authorization credentials.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        String commanderName = "";
        while (commanderName.isEmpty()) {
            commanderName = TerminalUI.readLine("  COMMANDER NAME > ");
            if (commanderName.isEmpty()) {
                TerminalUI.printWarning("Commander name cannot be empty. Please enter your name.");
            }
        }

        // Initialize core models
        Player player = new Player(commanderName);
        Spaceship spaceship = new Spaceship();
        this.gameState = new GameState(player, spaceship);

        System.out.println();
        TerminalUI.printSuccess("CREDENTIALS REGISTERED FOR COMMANDER: " + player.getCommanderName().toUpperCase());
        TerminalUI.renderProgressAnimation("CONFIGURING COMMAND CENTER", 350);
        TerminalUI.sleep(200);

        // Enter interactive Command Center dashboard
        runCommandCenter();
    }

    /**
     * Handles [2] CONTINUE GAME:
     * Resumes the active session or loads the persistent save from disk if present.
     */
    private void handleContinueGame() {
        if (!SaveManager.hasSave() && this.gameState == null) {
            TerminalUI.clearScreen();
            TerminalUI.printHeader("FLIGHT TELEMETRY ARCHIVE // RESTORE SESSION");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("STORAGE REPOSITORY : ") + TerminalUI.brightCyan("saves/orbit_save.dat"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("TELEMETRY STATUS   : ") + TerminalUI.brightYellow("NO SAVED GAME DETECTED"));
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printInnerBorder();
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightYellow("[!] NOTICE:"));
            TerminalUI.printBoxLine("  No prior flight logs or save states were discovered in the local");
            TerminalUI.printBoxLine("  mission archive directory (saves/orbit_save.dat).");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  Please start a " + TerminalUI.brightCyan("NEW GAME") + " to initialize a flight mission.");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printDoubleBorder();

            TerminalUI.pressEnterToContinue();
            return;
        }

        // If a save file exists on disk, offer to load it
        if (SaveManager.hasSave()) {
            TerminalUI.clearScreen();
            TerminalUI.printHeader("FLIGHT TELEMETRY ARCHIVE // RESTORE SESSION");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("SAVE REPOSITORY   : ") + TerminalUI.brightCyan("saves/orbit_save.dat"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("ARCHIVE METADATA  : ") + TerminalUI.brightGreen(SaveManager.getSaveSummary()));
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printInnerBorder();
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("RESTORATION OPTIONS:"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[1] ") + TerminalUI.brightWhite("LOAD SAVED FLIGHT MISSION") + TerminalUI.gray(" - Resume operations from archive"));
            if (this.gameState != null) {
                TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[2] ") + TerminalUI.brightWhite("RESUME ACTIVE IN-MEMORY SESSION") + TerminalUI.gray(" - Continue current session"));
                TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[3] ") + TerminalUI.brightWhite("RETURN TO MAIN MENU      ") + TerminalUI.gray(" - Abort"));
            } else {
                TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[2] ") + TerminalUI.brightWhite("RETURN TO MAIN MENU      ") + TerminalUI.gray(" - Abort"));
            }
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printDoubleBorder();
            System.out.println();

            int maxOpt = (this.gameState != null) ? 3 : 2;
            int choice = TerminalUI.readMenuOption("  ARCHIVE COMMAND > ", 1, maxOpt);

            if (choice == 1) {
                try {
                    TerminalUI.renderProgressAnimation("DECRYPTING FLIGHT TELEMETRY ARCHIVE", 350);
                    GameState loadedState = SaveManager.loadGame();
                    if (loadedState != null) {
                        this.gameState = loadedState;
                        TerminalUI.clearScreen();
                        TerminalUI.printSuccess("TELEMETRY RESTORED FOR COMMANDER: " + loadedState.getPlayer().getCommanderName().toUpperCase());
                        TerminalUI.sleep(250);

                        if (loadedState.isCampaignCompleted()) {
                            showCompletedCampaignReviewScreen(loadedState);
                        } else {
                            runCommandCenter();
                        }
                    }
                } catch (Exception e) {
                    TerminalUI.clearScreen();
                    TerminalUI.printHeader("RESTORE SESSION ERROR // CORRUPTED ARCHIVE");
                    TerminalUI.printEmptyBoxLine();
                    TerminalUI.printBoxLine("  " + TerminalUI.brightRed("[!] ERROR: Corrupted or invalid save file encountered."));
                    TerminalUI.printBoxLine("  Details: " + TerminalUI.white(e.getMessage()));
                    TerminalUI.printEmptyBoxLine();
                    TerminalUI.printBoxLine("  Please start a " + TerminalUI.brightCyan("NEW GAME") + " or inspect the save file.");
                    TerminalUI.printEmptyBoxLine();
                    TerminalUI.printDoubleBorder();
                    TerminalUI.pressEnterToContinue();
                }
                return;
            } else if (this.gameState != null && choice == 2) {
                TerminalUI.clearScreen();
                TerminalUI.printSuccess("RESUMING ACTIVE FLIGHT SESSION: " + gameState.getPlayer().getCommanderName().toUpperCase());
                TerminalUI.sleep(200);
                if (gameState.isCampaignCompleted()) {
                    showCompletedCampaignReviewScreen(gameState);
                } else {
                    runCommandCenter();
                }
                return;
            } else {
                return;
            }
        }

        // Only active session in memory
        if (this.gameState != null) {
            TerminalUI.clearScreen();
            TerminalUI.printSuccess("RESUMING ACTIVE FLIGHT SESSION: " + gameState.getPlayer().getCommanderName().toUpperCase());
            TerminalUI.sleep(200);
            if (gameState.isCampaignCompleted()) {
                showCompletedCampaignReviewScreen(gameState);
            } else {
                runCommandCenter();
            }
        }
    }

    /**
     * Interactive loop for the Command Center dashboard.
     */
    private void runCommandCenter() {
        boolean inCommandCenter = true;

        while (inCommandCenter && running) {
            displayCommandCenter();
            int choice = TerminalUI.readMenuOption("  COMMAND > ", 1, 9);

            if (choice == -1) {
                running = false;
                break;
            }

            switch (choice) {
                case 1 -> handleNavigation();
                case 2 -> handleGalaxyMap();
                case 3 -> handleShipStatus();
                case 4 -> handleInventory();
                case 5 -> handleExplorePlanet();
                case 6 -> handleMissionObjectives();
                case 7 -> handleMissionLog();
                case 8 -> handleSaveGame();
                case 9 -> {
                    // Return to main menu
                    inCommandCenter = false;
                }
                default -> TerminalUI.printWarning("Invalid command option.");
            }
        }
    }

    /**
     * Renders the Command Center dashboard.
     */
    private void displayCommandCenter() {
        TerminalUI.clearScreen();
        Player player = gameState.getPlayer();
        Spaceship ship = gameState.getSpaceship();

        TerminalUI.printHeaderWithSubtitle(
            "ORBIT COMMAND CENTER",
            "SPACECRAFT " + player.getSpaceshipName() + " // FLIGHT OPERATIONS"
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("COMMANDER : ") + TerminalUI.brightCyan(player.getCommanderName().toUpperCase()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("VESSEL    : ") + TerminalUI.brightYellow(player.getSpaceshipName()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("LOCATION  : ") + TerminalUI.brightGreen(gameState.getCurrentLocation().getName().toUpperCase()) + TerminalUI.gray(" (SOL SYSTEM)"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("MISSION   : ") + TerminalUI.white("DEEP SPACE EXPLORATION ") + TerminalUI.brightCyan("[PROGRESS: " + gameState.getMissionProgress() + "%]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("SPACECRAFT VITALS:"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("FUEL      : ") + TerminalUI.formatProgressBar(ship.getFuel(), 100, 20));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("OXYGEN    : ") + TerminalUI.formatProgressBar(ship.getOxygen(), 100, 20));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("ENERGY    : ") + TerminalUI.formatProgressBar(ship.getEnergy(), 100, 20));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("HULL      : ") + TerminalUI.formatProgressBar(ship.getHullIntegrity(), 100, 20));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("COMMAND OPERATIONS:"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[1] ") + TerminalUI.brightWhite("NAVIGATION         ") + TerminalUI.gray("- Interplanetary Flight Trajectories"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[2] ") + TerminalUI.brightWhite("GALAXY MAP         ") + TerminalUI.gray("- Star System Coordinates & Orbital Grid"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[3] ") + TerminalUI.brightWhite("SHIP STATUS        ") + TerminalUI.gray("- Telemetry & Subsystem Diagnostics"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[4] ") + TerminalUI.brightWhite("INVENTORY          ") + TerminalUI.gray("- Cargo Manifest & Supplies Management"));
        if (gameState.getCurrentLocation().getName().equalsIgnoreCase("Kepler-186f") && gameState.isSignalSourceDiscovered() && !gameState.isCampaignCompleted()) {
            TerminalUI.printBoxLine("  " + TerminalUI.brightYellow("[5] ") + TerminalUI.brightYellow("FINAL MISSION DIRECTIVE ") + TerminalUI.yellow("- Resolve Climax & Determine Ending"));
        } else {
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[5] ") + TerminalUI.brightWhite("EXPLORE PLANET     ") + TerminalUI.gray("- Planetary Surface Reconnaissance & Expeditions"));
        }
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[6] ") + TerminalUI.brightWhite("MISSION OBJECTIVES ") + TerminalUI.gray("- Flight Directives & Campaign Goals"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[7] ") + TerminalUI.brightWhite("MISSION LOG        ") + TerminalUI.gray("- Flight Recorder & Event Chronicle"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[8] ") + TerminalUI.brightWhite("SAVE GAME          ") + TerminalUI.gray("- Persist Flight Telemetry to saves/orbit_save.dat"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[9] ") + TerminalUI.brightWhite("RETURN TO MAIN MENU") + TerminalUI.gray("- Suspend Operations to Main Deck"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();
    }

    /**
     * Handles [8] SAVE GAME:
     * Saves active game state to saves/orbit_save.dat with overwrite confirmation.
     */
    private void handleSaveGame() {
        TerminalUI.clearScreen();
        TerminalUI.printHeader("MISSION TELEMETRY STORAGE // SAVE GAME");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("SAVE REPOSITORY   : ") + TerminalUI.brightCyan("saves/orbit_save.dat"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("CURRENT COMMANDER : ") + TerminalUI.brightYellow(gameState.getPlayer().getCommanderName().toUpperCase()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("CURRENT LOCATION  : ") + TerminalUI.brightGreen(gameState.getCurrentLocation().getName().toUpperCase()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("CAMPAIGN STAGE    : ") + TerminalUI.brightWhite(gameState.getCurrentStage()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("MISSION PROGRESS  : ") + TerminalUI.brightCyan(gameState.getMissionProgress() + "%"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();

        if (SaveManager.hasSave()) {
            TerminalUI.printBoxLine("  " + TerminalUI.brightYellow("[!] NOTICE: An existing mission archive was found on disk:"));
            TerminalUI.printBoxLine("  " + TerminalUI.gray(SaveManager.getSaveSummary()));
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  Overwriting will replace the previous telemetry archive.");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printDoubleBorder();
            System.out.println();

            String confirm = TerminalUI.readLine("  Confirm overwriting existing save file? [Y/N] > ");
            if (!confirm.equalsIgnoreCase("Y") && !confirm.equalsIgnoreCase("YES")) {
                System.out.println();
                TerminalUI.printWarning("Save operation cancelled. Existing save file was preserved.");
                TerminalUI.pressEnterToContinue("return to Command Center");
                return;
            }
        } else {
            TerminalUI.printBoxLine("  " + TerminalUI.brightGreen("[+] Initializing new mission telemetry archive file..."));
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printDoubleBorder();
            System.out.println();
        }

        try {
            TerminalUI.renderProgressAnimation("ENCRYPTING AND WRITING TELEMETRY ARCHIVE", 350);
            gameState.getMissionLog().addEntry("MISSION", "Flight telemetry archived to saves/orbit_save.dat.");
            boolean saved = SaveManager.saveGame(gameState);
            if (saved) {
                System.out.println();
                TerminalUI.printSuccess("MISSION TELEMETRY SUCCESSFULLY SAVED TO saves/orbit_save.dat");
            } else {
                System.out.println();
                TerminalUI.printError("Failed to persist save data to disk.");
            }
        } catch (Exception e) {
            System.out.println();
            TerminalUI.printError("Save error: " + e.getMessage());
        }

        TerminalUI.pressEnterToContinue("return to Command Center");
    }



    // ==========================================
    // Phase 4: Inventory Management System
    // ==========================================

    /**
     * Handles [4] INVENTORY:
     * Manages cargo supplies, using items, and inspecting inventory manifest.
     */
    private void handleInventory() {
        boolean inInventory = true;

        while (inInventory && running) {
            displayInventoryScreen();
            int choice = TerminalUI.readMenuOption("  CARGO COMMAND > ", 1, 3);

            if (choice == -1) {
                running = false;
                break;
            }

            switch (choice) {
                case 1 -> handleUseItem();
                case 2 -> handleViewInventoryDetails();
                case 3 -> inInventory = false;
                default -> TerminalUI.printWarning("Invalid cargo command option.");
            }
        }
    }

    /**
     * Renders the primary Cargo Hold / Inventory screen.
     */
    private void displayInventoryScreen() {
        TerminalUI.clearScreen();
        Inventory inv = gameState.getInventory();
        Player player = gameState.getPlayer();

        TerminalUI.printHeaderWithSubtitle(
            "CARGO HOLD // RESOURCE INVENTORY",
            "SPACECRAFT " + player.getSpaceshipName() + " // STORAGE BAY-01"
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("VESSEL DESIGNATION : ") + TerminalUI.brightCyan(player.getSpaceshipName()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("COMMANDING OFFICER : ") + TerminalUI.brightYellow(player.getCommanderName().toUpperCase()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("TOTAL CARGO UNITS  : ") + TerminalUI.brightWhite(inv.getTotalItemCount() + " / 50 UNITS ALLOCATED"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("STORED SUPPLY MANIFEST:"));

        // Display all 5 items
        printInventoryItemRow(Inventory.METAL, inv.getQuantity(Inventory.METAL));
        printInventoryItemRow(Inventory.ENERGY_CELLS, inv.getQuantity(Inventory.ENERGY_CELLS));
        printInventoryItemRow(Inventory.OXYGEN_CANISTERS, inv.getQuantity(Inventory.OXYGEN_CANISTERS));
        printInventoryItemRow(Inventory.FUEL_CELLS, inv.getQuantity(Inventory.FUEL_CELLS));
        printInventoryItemRow(Inventory.ALIEN_ARTIFACTS, inv.getQuantity(Inventory.ALIEN_ARTIFACTS));

        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("AVAILABLE CARGO ACTIONS:"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[1] ") + TerminalUI.brightWhite("USE AN ITEM           ") + TerminalUI.gray("- Consume supplies to restore ship vitals"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[2] ") + TerminalUI.brightWhite("VIEW INVENTORY DETAILS") + TerminalUI.gray("- Detailed manifest specifications & lore"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[3] ") + TerminalUI.brightWhite("RETURN TO COMMAND DECK") + TerminalUI.gray("- Back to Command Center dashboard"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();
    }

    private void printInventoryItemRow(String itemName, int quantity) {
        String desc = Inventory.getItemDescription(itemName);
        String qtyStr = String.format("%2d QTY", quantity);
        String nameStr = String.format("%-17s", itemName);

        TerminalUI.printBoxLine("  * " + TerminalUI.brightWhite(nameStr) + ": " 
                + TerminalUI.brightCyan(qtyStr) + "  " + TerminalUI.gray(desc));
    }

    /**
     * Handles using consumable items to restore spaceship resources.
     */
    private void handleUseItem() {
        Inventory inv = gameState.getInventory();
        Spaceship ship = gameState.getSpaceship();

        // Filter to only usable items that currently have available quantity > 0
        List<String> usableItems = new ArrayList<>();
        if (inv.getQuantity(Inventory.METAL) > 0) usableItems.add(Inventory.METAL);
        if (inv.getQuantity(Inventory.ENERGY_CELLS) > 0) usableItems.add(Inventory.ENERGY_CELLS);
        if (inv.getQuantity(Inventory.OXYGEN_CANISTERS) > 0) usableItems.add(Inventory.OXYGEN_CANISTERS);
        if (inv.getQuantity(Inventory.FUEL_CELLS) > 0) usableItems.add(Inventory.FUEL_CELLS);

        if (usableItems.isEmpty()) {
            TerminalUI.clearScreen();
            TerminalUI.printHeader("DISPENSE SHIP SUPPLIES // CARGO HOLD");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightYellow("[!] NO USABLE SUPPLIES AVAILABLE"));
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  All consumable supply reserves in the cargo hold are currently at 0.");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printDoubleBorder();

            TerminalUI.pressEnterToContinue("return to Inventory Menu");
            return;
        }

        TerminalUI.clearScreen();
        TerminalUI.printHeader("DISPENSE SHIP SUPPLIES // CARGO HOLD");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("CURRENT SPACECRAFT VITALS:"));
        TerminalUI.printBoxLine("  * Fuel: " + ship.getFuel() + "% | Oxygen: " + ship.getOxygen() 
                + "% | Energy: " + ship.getEnergy() + "% | Hull: " + ship.getHullIntegrity() + "%");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("SELECT AN ITEM TO USE:"));

        for (int i = 0; i < usableItems.size(); i++) {
            String item = usableItems.get(i);
            int qty = inv.getQuantity(item);
            int restoreAmount = Inventory.getRestorationAmount(item);
            String target = getShortResourceTarget(item);

            String opt = String.format("  [%d] ", i + 1);
            String itemPadded = String.format("%-16s", item);
            String qtyPadded = String.format("(Qty: %d)", qty);
            String effectStr = String.format("-> +%d %s", restoreAmount, target);

            TerminalUI.printBoxLine(TerminalUI.brightCyan(opt) + TerminalUI.brightWhite(itemPadded)
                    + " " + TerminalUI.brightYellow(qtyPadded) + "  " + TerminalUI.gray(effectStr));
        }

        int cancelChoice = usableItems.size() + 1;
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[" + cancelChoice + "] ") + TerminalUI.brightWhite("CANCEL & RETURN TO INVENTORY"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        int choice = TerminalUI.readMenuOption("  SELECT ITEM TO USE > ", 1, cancelChoice);
        if (choice == -1 || choice == cancelChoice) {
            // Cancelled
            return;
        }

        String selectedItem = usableItems.get(choice - 1);
        String resourceName = getTargetResourceName(selectedItem);
        int currentVal = getTargetResourceValue(selectedItem, ship);
        int restoreAmount = Inventory.getRestorationAmount(selectedItem);

        // Rule: If corresponding resource is already at 100, do not consume the item
        if (currentVal >= 100) {
            System.out.println();
            TerminalUI.printWarning("Consumption rejected: Spacecraft " + resourceName 
                    + " is already at maximum capacity (100%). Item was not consumed.");
            TerminalUI.pressEnterToContinue("return to Inventory Menu");
            return;
        }

        // Confirmation prompt
        TerminalUI.clearScreen();
        TerminalUI.printHeader("CONFIRM RESOURCE DISPENSATION");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("ITEM SELECTED   : ") + TerminalUI.brightCyan(selectedItem));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("QUANTITY TO USE : ") + TerminalUI.brightYellow("1 unit"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("TARGET RESOURCE : ") + TerminalUI.brightWhite(resourceName));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("CURRENT LEVEL   : ") + TerminalUI.brightWhite(currentVal + "%"));
        int projected = Math.min(100, currentVal + restoreAmount);
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("PROJECTED LEVEL : ") + TerminalUI.brightGreen(projected + "%") + " (+" + (projected - currentVal) + "% applied)");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        String confirm = TerminalUI.readLine("  Confirm supply consumption? [Y/N] > ");
        if (!confirm.equalsIgnoreCase("Y") && !confirm.equalsIgnoreCase("YES")) {
            System.out.println();
            TerminalUI.printWarning("Consumption cancelled by Commander. No items consumed.");
            TerminalUI.pressEnterToContinue("return to Inventory Menu");
            return;
        }

        // Consume item
        boolean removed = inv.remove(selectedItem, 1);
        if (!removed) {
            System.out.println();
            TerminalUI.printError("Failed to dispense item: insufficient quantity.");
            TerminalUI.pressEnterToContinue("return to Inventory Menu");
            return;
        }

        // Apply restoration immediately to Spaceship
        switch (selectedItem) {
            case Inventory.METAL -> ship.repairHull(20);
            case Inventory.ENERGY_CELLS -> ship.restoreEnergy(25);
            case Inventory.OXYGEN_CANISTERS -> ship.restoreOxygen(25);
            case Inventory.FUEL_CELLS -> ship.restoreFuel(25);
        }

        int newVal = getTargetResourceValue(selectedItem, ship);

        TerminalUI.clearScreen();
        TerminalUI.printHeader("SUPPLY APPLIED SUCCESSFULLY");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightGreen("[+] 1 " + selectedItem.toUpperCase() + " consumed from cargo hold."));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("Remaining in Cargo: ") + TerminalUI.brightCyan(inv.getQuantity(selectedItem) + " units"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("RESOURCE TELEMETRY UPDATE:"));
        TerminalUI.printBoxLine("  * " + resourceName + ": " + currentVal + "% -> " 
                + TerminalUI.brightGreen(newVal + "%") + " " + TerminalUI.formatResourceStatus(newVal));
        TerminalUI.printBoxLine("  * Status: " + TerminalUI.formatProgressBar(newVal, 100, 20));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("return to Inventory Menu");
    }

    private String getTargetResourceName(String item) {
        return switch (item) {
            case Inventory.METAL -> "Hull Integrity";
            case Inventory.ENERGY_CELLS -> "Reactor Energy";
            case Inventory.OXYGEN_CANISTERS -> "Life Support Oxygen";
            case Inventory.FUEL_CELLS -> "Propulsion Fuel";
            default -> "System Resource";
        };
    }

    private String getShortResourceTarget(String item) {
        return switch (item) {
            case Inventory.METAL -> "Hull";
            case Inventory.ENERGY_CELLS -> "Energy";
            case Inventory.OXYGEN_CANISTERS -> "Oxygen";
            case Inventory.FUEL_CELLS -> "Fuel";
            default -> "Resource";
        };
    }

    private int getTargetResourceValue(String item, Spaceship ship) {
        return switch (item) {
            case Inventory.METAL -> ship.getHullIntegrity();
            case Inventory.ENERGY_CELLS -> ship.getEnergy();
            case Inventory.OXYGEN_CANISTERS -> ship.getOxygen();
            case Inventory.FUEL_CELLS -> ship.getFuel();
            default -> 0;
        };
    }

    /**
     * Renders detailed technical specifications and lore for all inventory items.
     */
    private void handleViewInventoryDetails() {
        TerminalUI.clearScreen();
        Inventory inv = gameState.getInventory();

        TerminalUI.printHeader("CARGO HOLD // COMPREHENSIVE SUPPLY LOG");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("DETAILED CARGO SPECIFICATIONS & PURPOSES:"));
        TerminalUI.printEmptyBoxLine();

        TerminalUI.printBoxLine("  1. " + TerminalUI.brightWhite("METAL (Hull Plating)") + " - Qty: " + inv.getQuantity(Inventory.METAL));
        TerminalUI.printBoxLine("     High-density titanium-carbide alloy plates deployed by automated");
        TerminalUI.printBoxLine("     repair welders to patch exterior hull breaches. Restores 20 Hull.");
        TerminalUI.printEmptyBoxLine();

        TerminalUI.printBoxLine("  2. " + TerminalUI.brightWhite("ENERGY CELLS (Plasma Battery)") + " - Qty: " + inv.getQuantity(Inventory.ENERGY_CELLS));
        TerminalUI.printBoxLine("     High-capacity fusion plasma capacitors that channel reserve power");
        TerminalUI.printBoxLine("     into the ship's reactor manifold. Restores 25 Energy.");
        TerminalUI.printEmptyBoxLine();

        TerminalUI.printBoxLine("  3. " + TerminalUI.brightWhite("OXYGEN CANISTERS (Cryo-O2 Flask)") + " - Qty: " + inv.getQuantity(Inventory.OXYGEN_CANISTERS));
        TerminalUI.printBoxLine("     Pressurized liquid-oxygen cylinders that replenish life-support");
        TerminalUI.printBoxLine("     atmospheric scrubbers. Restores 25 Oxygen.");
        TerminalUI.printEmptyBoxLine();

        TerminalUI.printBoxLine("  4. " + TerminalUI.brightWhite("FUEL CELLS (Deuterium Rods)") + " - Qty: " + inv.getQuantity(Inventory.FUEL_CELLS));
        TerminalUI.printBoxLine("     Encapsulated sub-light and impulse propellant cells fueling");
        TerminalUI.printBoxLine("     interplanetary burn maneuvers. Restores 25 Fuel.");
        TerminalUI.printEmptyBoxLine();

        TerminalUI.printBoxLine("  5. " + TerminalUI.brightWhite("ALIEN ARTIFACTS (Xenological Relic)") + " - Qty: " + inv.getQuantity(Inventory.ALIEN_ARTIFACTS));
        TerminalUI.printBoxLine("     Enigmatic crystalline materials of non-human origin emitting low");
        TerminalUI.printBoxLine("     electromagnetic resonance. Inactive; cannot be consumed.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("return to Inventory Menu");
    }

    // ==========================================
    // Phase 5: Planetary Exploration & Events
    // ==========================================

    /**
     * Checks if surface exploration expeditions are supported on the given planet.
     * In Stage 1: Earth and Mars.
     * In Stage 2: Europa and Titan (when discovered).
     * In Stage 3: Kepler-186f (when discovered).
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

    /**
     * Handles [5] EXPLORE PLANET:
     * Manages planetary surface reconnaissance, checks destination support,
     * renders environment profiles, and launches exploration expeditions.
     */
    private void handleExplorePlanet() {
        Planet current = gameState.getCurrentLocation();
        if (!isExplorationSupported(current)) {
            displayExplorationRestrictedScreen(current);
            return;
        }

        boolean inExploration = true;
        while (inExploration && running) {
            boolean hasFinalDirective = current.getName().equalsIgnoreCase("Kepler-186f")
                    && gameState.isSignalSourceDiscovered()
                    && !gameState.isCampaignCompleted();

            displayExplorationScreen(current);
            int maxChoice = hasFinalDirective ? 3 : 2;
            int choice = TerminalUI.readMenuOption("  EXPEDITION COMMAND > ", 1, maxChoice);

            if (choice == -1) {
                running = false;
                break;
            }

            if (hasFinalDirective) {
                switch (choice) {
                    case 1 -> {
                        handleFinalMissionDecision();
                        if (gameState.isCampaignCompleted()) {
                            inExploration = false;
                        }
                    }
                    case 2 -> launchExpedition(current);
                    case 3 -> inExploration = false;
                    default -> TerminalUI.printWarning("Invalid expedition command.");
                }
            } else {
                switch (choice) {
                    case 1 -> launchExpedition(current);
                    case 2 -> inExploration = false;
                    default -> TerminalUI.printWarning("Invalid expedition command.");
                }
            }
        }
    }

    /**
     * Displays a restriction screen when attempting to explore locked planets.
     */
    private void displayExplorationRestrictedScreen(Planet planet) {
        TerminalUI.clearScreen();
        TerminalUI.printHeaderWithSubtitle(
            "PLANETARY EXPLORATION // DESCENT PROTOCOL",
            "SURFACE LANDING INHIBITED // " + planet.getName().toUpperCase()
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("CURRENT SECTOR   : ") + TerminalUI.brightCyan(planet.getName().toUpperCase() + " ORBIT"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("DESCENT TELEMETRY: ") + TerminalUI.brightRed("[HAZARDOUS // SURFACE DESCENT RESTRICTED]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightYellow("[!] HIGH-COMMAND FLIGHT ADVISORY:"));
        TerminalUI.printBoxLine("  Atmospheric, cryogenic, or radiation telemetry for " + planet.getName());
        TerminalUI.printBoxLine("  indicates severe planetary hazards. Surface landings and manned");
        TerminalUI.printBoxLine("  expeditions to this celestial body are not authorized in this operational stage.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("Current authorized campaign stage : ") + TerminalUI.brightGreen(gameState.getCurrentStage()));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("return to Command Center");
    }

    /**
     * Renders the primary planetary exploration interface.
     */
    private void displayExplorationScreen(Planet planet) {
        TerminalUI.clearScreen();
        Player player = gameState.getPlayer();
        Spaceship ship = gameState.getSpaceship();

        TerminalUI.printHeaderWithSubtitle(
            "PLANETARY RECONNAISSANCE // SURFACE EXPEDITION",
            "CELESTIAL BODY: " + planet.getName().toUpperCase() + " // ORBITAL DESCENT HUB"
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("VESSEL DESIGNATION : ") + TerminalUI.brightCyan(player.getSpaceshipName()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("COMMANDING OFFICER : ") + TerminalUI.brightYellow(player.getCommanderName().toUpperCase()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("TARGET PLANET      : ") + TerminalUI.brightGreen(planet.getName().toUpperCase()));

        String statusBadge = planet.isExplored()
                ? TerminalUI.brightGreen("[SURFACE SURVEYED / EXPLORED]")
                : TerminalUI.brightYellow("[UNEXPLORED / SURVEY PENDING]");
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("EXPLORATION STATUS : ") + statusBadge);
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("ENVIRONMENTAL PROFILE:"));
        printWrappedBoxText(planet.getEnvironmentDescription());
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("SPACECRAFT VITALS:"));
        TerminalUI.printBoxLine("  * Fuel   : " + String.format("%3d / 100  ", ship.getFuel()) + TerminalUI.formatProgressBar(ship.getFuel(), 100, 18) + " " + TerminalUI.formatResourceStatus(ship.getFuel()));
        TerminalUI.printBoxLine("  * Oxygen : " + String.format("%3d / 100  ", ship.getOxygen()) + TerminalUI.formatProgressBar(ship.getOxygen(), 100, 18) + " " + TerminalUI.formatResourceStatus(ship.getOxygen()));
        TerminalUI.printBoxLine("  * Energy : " + String.format("%3d / 100  ", ship.getEnergy()) + TerminalUI.formatProgressBar(ship.getEnergy(), 100, 18) + " " + TerminalUI.formatResourceStatus(ship.getEnergy()));
        TerminalUI.printBoxLine("  * Hull   : " + String.format("%3d / 100  ", ship.getHullIntegrity()) + TerminalUI.formatProgressBar(ship.getHullIntegrity(), 100, 18) + " " + TerminalUI.formatResourceStatus(ship.getHullIntegrity()));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("EXPEDITION OPERATIONAL REQUIREMENTS:"));
        TerminalUI.printBoxLine("  * " + TerminalUI.brightWhite("ENERGY CONSUMPTION : ") + TerminalUI.brightYellow("10 Energy per excursion"));
        TerminalUI.printBoxLine("  * " + TerminalUI.brightWhite("OXYGEN CONSUMPTION : ") + TerminalUI.brightYellow("5 Oxygen per excursion"));
        boolean operational = ship.isOperational();
        boolean vitalsOk = ship.getEnergy() >= 10 && ship.getOxygen() >= 5;
        String readiness;
        if (!operational) {
            readiness = TerminalUI.brightRed("[SPACECRAFT CASUALTY / DEPLOYMENT INHIBITED]");
        } else if (!vitalsOk) {
            readiness = TerminalUI.brightRed("[VITALS INSUFFICIENT / DEPLOYMENT INHIBITED]");
        } else {
            readiness = TerminalUI.brightGreen("[EXPEDITION READY / VITALS SUFFICIENT]");
        }
        TerminalUI.printBoxLine("  * " + TerminalUI.brightWhite("EXCURSION READINESS: ") + readiness);
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printEmptyBoxLine();
        boolean hasFinalDirective = planet.getName().equalsIgnoreCase("Kepler-186f")
                && gameState.isSignalSourceDiscovered()
                && !gameState.isCampaignCompleted();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("AVAILABLE ACTIONS:"));
        if (hasFinalDirective) {
            TerminalUI.printBoxLine("  " + TerminalUI.brightYellow("[1] ") + TerminalUI.brightYellow("RESOLVE FINAL MISSION DIRECTIVE") + TerminalUI.yellow(" - Determine Campaign Ending & Return"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[2] ") + TerminalUI.brightWhite("CONDUCT SURFACE SURVEY EXPEDITION") + TerminalUI.gray(" - Deploy surface excursion (-10 Energy, -5 O2)"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[3] ") + TerminalUI.brightWhite("RETURN TO COMMAND CENTER    ") + TerminalUI.gray(" - Back to main flight operations deck"));
        } else {
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[1] ") + TerminalUI.brightWhite("BEGIN EXPLORATION EXPEDITION") + TerminalUI.gray(" - Deploy surface excursion rover (-10 Energy, -5 O2)"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[2] ") + TerminalUI.brightWhite("RETURN TO COMMAND CENTER    ") + TerminalUI.gray(" - Back to main flight operations deck"));
        }
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();
    }

    /**
     * Executes a surface exploration expedition on the target planet.
     * Consumes exactly 10 Energy and 5 Oxygen upon confirmed launch,
     * marks the planet as explored, selects exactly one random event,
     * and applies its rewards or penalties.
     */
    private void launchExpedition(Planet planet) {
        Spaceship ship = gameState.getSpaceship();

        // 1. Verify operational status
        if (!ship.isOperational()) {
            TerminalUI.clearScreen();
            TerminalUI.printHeader("EXPEDITION LAUNCH INHIBITED // CASUALTY ALERT");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightRed("[!] CATASTROPHIC STATUS WARNING:"));
            TerminalUI.printBoxLine("  Vessel ODYSSEY-07 is no longer operational. Expeditions cannot proceed:");
            if (ship.getHullIntegrity() <= 0) {
                TerminalUI.printBoxLine("  * Hull Integrity : " + TerminalUI.brightRed("0% (HULL BREACHED / DESTROYED)"));
            }
            if (ship.getOxygen() <= 0) {
                TerminalUI.printBoxLine("  * Life Support O2: " + TerminalUI.brightRed("0% (LIFE SUPPORT DEPLETED)"));
            }
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  Please repair structural hull or restore oxygen via Inventory before launch.");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printDoubleBorder();
            TerminalUI.pressEnterToContinue("return to Exploration Menu");
            return;
        }

        // 2. Verify sufficient resources
        if (ship.getEnergy() < 10 || ship.getOxygen() < 5) {
            TerminalUI.clearScreen();
            TerminalUI.printHeader("EXPEDITION LAUNCH INHIBITED // INSUFFICIENT VITALS");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightRed("[!] RESOURCE DEFICIT WARNING:"));
            TerminalUI.printBoxLine("  Surface excursion requires minimum life-support and reactor reserves:");
            TerminalUI.printBoxLine("  * Energy Required: 10 Energy  | Available: " + ship.getEnergy() + "%");
            TerminalUI.printBoxLine("  * Oxygen Required:  5 Oxygen  | Available: " + ship.getOxygen() + "%");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  Please use cargo supplies in the Inventory or restore vitals before launch.");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printDoubleBorder();

            TerminalUI.pressEnterToContinue("return to Exploration Menu");
            return;
        }

        // 3. Pre-launch confirmation prompt
        TerminalUI.clearScreen();
        TerminalUI.printHeader("CONFIRM EXPEDITION DEPLOYMENT // " + planet.getName().toUpperCase());
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("EXPEDITION DESTINATION : ") + TerminalUI.brightCyan(planet.getName().toUpperCase() + " SURFACE"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("ENVIRONMENT PROFILE    : ") + TerminalUI.white(planet.getEnvironmentDescription()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("EXPEDITION COST        : ") + TerminalUI.brightYellow("10 Energy, 5 Oxygen"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("TELEMETRY PROJECTION:"));
        TerminalUI.printBoxLine("  * Reactor Energy : " + ship.getEnergy() + "% -> " + TerminalUI.brightGreen((ship.getEnergy() - 10) + "%"));
        TerminalUI.printBoxLine("  * Life Support O2: " + ship.getOxygen() + "% -> " + TerminalUI.brightGreen((ship.getOxygen() - 5) + "%"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        String confirm = TerminalUI.readLine("  Confirm expedition launch? [Y/N] > ");
        if (!confirm.equalsIgnoreCase("Y") && !confirm.equalsIgnoreCase("YES")) {
            System.out.println();
            TerminalUI.printWarning("Expedition deployment aborted by Commander. No resources consumed.");
            TerminalUI.pressEnterToContinue("return to Exploration Menu");
            return;
        }

        // 4. Deduct expedition costs only after confirmation
        ship.consumeEnergy(10);
        ship.consumeOxygen(5);

        // 5. Descent sequence animation
        TerminalUI.clearScreen();
        TerminalUI.printHeader("SURFACE EXPEDITION DEPLOYMENT IN PROGRESS");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Disengaging surface reconnaissance shuttle... " + TerminalUI.brightGreen("[ OK ]"));
        TerminalUI.printBoxLine("  Entering atmospheric descent vector on " + planet.getName() + "... " + TerminalUI.brightGreen("[ OK ]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        TerminalUI.renderProgressAnimation("DESCENDING TO PLANETARY SURFACE (-10 ENERGY, -5 OXYGEN)", 400);
        TerminalUI.renderSpinner("CONDUCTING SURFACE SURVEY & ANOMALY SCAN", 400);

        // 6. Update planet explored status
        planet.setExplored(true);

        // 7. Check if special Kepler-186f climax event applies
        if (planet.getName().equalsIgnoreCase("Kepler-186f") && !gameState.isSignalSourceDiscovered()) {
            handleKeplerSignalSourceClimax(ship, planet);
        } else {
            // Generate exactly one random event
            ExplorationEvent event = ExplorationEvent.getRandomEvent(this.random);

            // Dispatch event by classification
            switch (event.getCategory()) {
                case ExplorationEvent.CATEGORY_HAZARD -> handleHazardEvent(event, ship, planet);
                case ExplorationEvent.CATEGORY_ENCOUNTER -> handleEncounterEvent(event, ship, planet);
                case ExplorationEvent.CATEGORY_DISCOVERY -> handleDiscoveryEvent(event, ship, planet);
                case ExplorationEvent.CATEGORY_RESOURCE -> handleResourceEvent(event, ship, planet);
                default -> handleGenericEvent(event, ship, planet);
            }
        }

        // On outer celestial bodies, record materials milestone
        if (planet.getName().equalsIgnoreCase("Europa") || planet.getName().equalsIgnoreCase("Titan")) {
            gameState.setMaterialsCollectedDuringStage2(true);
        }

        // 9. Flight recorder audit trail & objective progression
        gameState.getMissionLog().addEntry("EXPEDITION", "Surface reconnaissance excursion completed on " + planet.getName() + ".");
        gameState.updateMissionObjectives();

        // 10. Post-expedition operational casualty check
        if (!ship.isOperational()) {
            displayCasualtyWarning(ship);
        }
    }

    /**
     * Executes the special Kepler-186f signal source discovery climax sequence,
     * securing the extrasolar Alien Artifact.
     */
    private void handleKeplerSignalSourceClimax(Spaceship ship, Planet planet) {
        TerminalUI.clearScreen();
        TerminalUI.printHeaderWithSubtitle(
            "EXTRASOLAR RECONNAISSANCE // SIGNAL SOURCE DISCOVERED",
            "KEPLER-186F SURFACE // SUB-CRUSTAL ANOMALY 01"
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightMagenta("[PRIORITY SENSOR TELEMETRY: CARRIER FREQUENCY 1420.405 MHz]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  After traversing subterranean basalt caverns, excursion sensors detect");
        TerminalUI.printBoxLine("  a colossal extraterrestrial structure humming with harmonic power.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  At the central dais of the monolithic spire rests an intact,");
        TerminalUI.printBoxLine("  glowing geometric object: the " + TerminalUI.brightYellow("EXTRASOLAR ALIEN ARTIFACT") + ".");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("FIELD OPERATIONS & RECOVERY:"));
        TerminalUI.printBoxLine("  * Harmonic oscillation mapped to deep-space archives: " + TerminalUI.brightGreen("[ VERIFIED ]"));
        TerminalUI.printBoxLine("  * Extraterrestrial artifact extracted into stasis container: " + TerminalUI.brightGreen("[ +1 ALIEN ARTIFACT ]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("MISSION DIRECTIVE STATUS: ") + TerminalUI.brightGreen("[SIGNAL SOURCE CATALOGED // READY FOR RESOLUTION]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        gameState.getInventory().add(Inventory.ALIEN_ARTIFACTS, 1);
        gameState.setSignalSourceDiscovered(true);
        gameState.getMissionLog().addEntry("DISCOVERY", "Signal source discovered on Kepler-186f: Extrasolar Megastructure cataloged & Alien Artifact secured.");
        gameState.updateMissionObjectives();

        // Milestone save
        try {
            SaveManager.saveGame(gameState);
        } catch (Exception ignored) {}

        TerminalUI.pressEnterToContinue("proceed to mission directive review");
    }

    /**
     * Handles an active hazard event with warnings, threat telemetry,
     * and interactive damage mitigation options.
     */
    private void handleHazardEvent(ExplorationEvent event, Spaceship ship, Planet planet) {
        String counterItem = event.getCountermeasureItem();
        Inventory inv = gameState.getInventory();
        int availableCount = (counterItem != null) ? inv.getQuantity(counterItem) : 0;
        String penaltyDesc = event.getPrimaryPenaltyDescription();

        boolean resolved = false;

        while (!resolved && running) {
            TerminalUI.clearScreen();
            TerminalUI.printHeaderWithSubtitle(
                "SURFACE EXPEDITION // HAZARD ALERT",
                planet.getName().toUpperCase() + " SURFACE // " + event.getTitle().toUpperCase()
            );
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("EVENT CLASSIFICATION : ") + TerminalUI.brightRed("[HAZARD EVENT - CODE RED]"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("INCIDENT DESIGNATION : ") + TerminalUI.brightWhite(event.getTitle()));
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printInnerBorder();
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("FIELD THREAT NARRATIVE:"));
            printWrappedBoxText(event.getDescription());
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printInnerBorder();
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightRed("PREDICTED HAZARD CONSEQUENCE:"));
            TerminalUI.printBoxLine("  * Threat Impact   : " + TerminalUI.brightRed(penaltyDesc));
            TerminalUI.printBoxLine("  * Current Status  : " + formatCurrentHazardTelemetry(event, ship));
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printSingleBorder();
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("DAMAGE MITIGATION RESPONSE:"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("Countermeasure Supply : ") + TerminalUI.brightYellow("1 " + (counterItem != null ? counterItem : "None")));
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("Available in Cargo    : ")
                    + (availableCount > 0 ? TerminalUI.brightGreen(availableCount + " units") : TerminalUI.brightRed("0 units (DEPLETED)")));
            TerminalUI.printEmptyBoxLine();

            if (availableCount > 0) {
                TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[1] ") + TerminalUI.brightGreen("DEPLOY COUNTERMEASURE (-1 " + counterItem + ")"));
                TerminalUI.printBoxLine("      " + TerminalUI.gray(event.getCountermeasureDescription()));
                TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[2] ") + TerminalUI.brightYellow("BRACE FOR IMPACT & ACCEPT DAMAGE"));
                TerminalUI.printBoxLine("      " + TerminalUI.gray("Preserve cargo inventory supplies and sustain " + penaltyDesc + "."));
                TerminalUI.printEmptyBoxLine();
                TerminalUI.printDoubleBorder();
                System.out.println();

                int choice = TerminalUI.readMenuOption("  HAZARD COMMAND > ", 1, 2);
                if (choice == -1) {
                    running = false;
                    break;
                }

                if (choice == 1) {
                    System.out.println();
                    String confirm = TerminalUI.readLine("  Confirm dispensing 1 " + counterItem + " for emergency mitigation? [Y/N] > ");
                    if (!confirm.equalsIgnoreCase("Y") && !confirm.equalsIgnoreCase("YES")) {
                        System.out.println();
                        TerminalUI.printWarning("Countermeasure deployment cancelled. No supplies consumed.");
                        TerminalUI.sleep(300);
                        continue;
                    }

                    boolean removed = inv.remove(counterItem, 1);
                    if (removed) {
                        resolved = true;
                        gameState.getMissionLog().addEntry("HAZARD", "Hazard countered on " + planet.getName() + ": Used 1 " + counterItem + " to mitigate " + event.getTitle() + ".");
                        showMitigationSuccessScreen(event, planet, counterItem);
                    } else {
                        TerminalUI.printError("Failed to dispense item: insufficient quantity.");
                        TerminalUI.sleep(300);
                    }
                } else {
                    resolved = true;
                    applyHazardDamage(event, ship, planet, penaltyDesc);
                }
            } else {
                TerminalUI.printBoxLine("  " + TerminalUI.brightYellow("[!] NOTICE: No countermeasure items available in cargo hold."));
                TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[1] ") + TerminalUI.brightYellow("BRACE FOR IMPACT & ACCEPT DAMAGE"));
                TerminalUI.printBoxLine("      " + TerminalUI.gray("Emergency shields offline; sustain " + penaltyDesc + "."));
                TerminalUI.printEmptyBoxLine();
                TerminalUI.printDoubleBorder();
                System.out.println();

                int choice = TerminalUI.readMenuOption("  HAZARD COMMAND > ", 1, 1);
                if (choice == -1) {
                    running = false;
                    break;
                }
                resolved = true;
                applyHazardDamage(event, ship, planet, penaltyDesc);
            }
        }
    }

    private String formatCurrentHazardTelemetry(ExplorationEvent event, Spaceship ship) {
        if (event.getHullPenalty() > 0) {
            int proj = Math.max(0, ship.getHullIntegrity() - event.getHullPenalty());
            return "Hull: " + ship.getHullIntegrity() + "% -> " + TerminalUI.brightRed(proj + "%");
        }
        if (event.getEnergyPenalty() > 0) {
            int proj = Math.max(0, ship.getEnergy() - event.getEnergyPenalty());
            return "Energy: " + ship.getEnergy() + "% -> " + TerminalUI.brightRed(proj + "%");
        }
        if (event.getOxygenPenalty() > 0) {
            int proj = Math.max(0, ship.getOxygen() - event.getOxygenPenalty());
            return "Oxygen: " + ship.getOxygen() + "% -> " + TerminalUI.brightRed(proj + "%");
        }
        if (event.getFuelPenalty() > 0) {
            int proj = Math.max(0, ship.getFuel() - event.getFuelPenalty());
            return "Fuel: " + ship.getFuel() + "% -> " + TerminalUI.brightRed(proj + "%");
        }
        return "Normal";
    }

    private void showMitigationSuccessScreen(ExplorationEvent event, Planet planet, String item) {
        TerminalUI.clearScreen();
        Spaceship ship = gameState.getSpaceship();
        Inventory inv = gameState.getInventory();

        TerminalUI.printHeaderWithSubtitle(
            "SURFACE EXPEDITION REPORT // MITIGATION SUCCESS",
            planet.getName().toUpperCase() + " SURFACE // " + event.getTitle().toUpperCase()
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("EVENT CLASSIFICATION : ") + TerminalUI.brightGreen("[HAZARD COUNTERED - THREAT NEUTRALIZED]"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("INCIDENT DESIGNATION : ") + TerminalUI.brightWhite(event.getTitle()));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightGreen("[+] EMERGENCY COUNTERMEASURE SUCCESSFUL:"));
        TerminalUI.printBoxLine("  Expended 1 " + item + " from cargo hold to neutralize the environmental hazard.");
        TerminalUI.printBoxLine("  Structural shielding absorbed all kinetic and thermal energy. Zero damage sustained!");
        TerminalUI.printBoxLine("  Remaining in Cargo: " + TerminalUI.brightCyan(inv.getQuantity(item) + " units"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("UPDATED SPACECRAFT STATUS:"));
        TerminalUI.printBoxLine("  * Fuel: " + ship.getFuel() + "% | Oxygen: " + ship.getOxygen() 
                + "% | Energy: " + ship.getEnergy() + "% | Hull: " + ship.getHullIntegrity() + "%");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("return to Exploration Menu");
    }

    private void applyHazardDamage(ExplorationEvent event, Spaceship ship, Planet planet, String penaltyDesc) {
        List<String> effectLog = event.applyEffects(ship, gameState.getInventory());
        gameState.getMissionLog().addEntry("HAZARD", "Hazard sustained on " + planet.getName() + ": " + event.getTitle() + " (" + penaltyDesc + ").");

        TerminalUI.clearScreen();
        TerminalUI.printHeaderWithSubtitle(
            "SURFACE EXPEDITION REPORT // HAZARD CASUALTY",
            planet.getName().toUpperCase() + " SURFACE // " + event.getTitle().toUpperCase()
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("EVENT CLASSIFICATION : ") + TerminalUI.brightRed("[HAZARD IMPACT SUSTAINED]"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("INCIDENT DESIGNATION : ") + TerminalUI.brightWhite(event.getTitle()));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("FIELD THREAT NARRATIVE:"));
        printWrappedBoxText(event.getDescription());
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightRed("DAMAGE TELEMETRY & SYSTEM EFFECTS:"));
        for (String logEntry : effectLog) {
            TerminalUI.printBoxLine("  * " + colorizeLogEntry(logEntry));
        }
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("UPDATED SPACECRAFT STATUS:"));
        TerminalUI.printBoxLine("  * Fuel: " + ship.getFuel() + "% | Oxygen: " + ship.getOxygen() 
                + "% | Energy: " + ship.getEnergy() + "% | Hull: " + ship.getHullIntegrity() + "%");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("return to Exploration Menu");
    }

    private void handleEncounterEvent(ExplorationEvent event, Spaceship ship, Planet planet) {
        if (event.getId().equalsIgnoreCase("enc_unidentified_signal")) {
            handleBeaconEncounter(event, ship, planet);
        } else if (event.getId().equalsIgnoreCase("enc_ancient_civilization")) {
            handleMonolithEncounter(event, ship, planet);
        } else {
            handleGenericEvent(event, ship, planet);
        }
    }

    private void handleBeaconEncounter(ExplorationEvent event, Spaceship ship, Planet planet) {
        TerminalUI.clearScreen();
        TerminalUI.printHeaderWithSubtitle(
            "SURFACE EXPEDITION // DEEP-SPACE TRANSMISSION",
            planet.getName().toUpperCase() + " SURFACE // " + event.getTitle().toUpperCase()
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("EVENT CLASSIFICATION : ") + TerminalUI.brightMagenta("[COSMIC ENCOUNTER - ANOMALOUS BEACON]"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("INCIDENT DESIGNATION : ") + TerminalUI.brightWhite(event.getTitle()));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("SUB-SPACE SIGNAL INTERCEPT:"));
        printWrappedBoxText(event.getDescription());
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("TACTICAL RESPONSE OPTIONS:"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[1] ") + TerminalUI.brightWhite("TRIANGULATE TRANSMISSION VECTOR  ") + TerminalUI.gray("- Decode frequency harmonics and determine origin vector"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[2] ") + TerminalUI.brightWhite("SCAN LOCAL TOPOGRAPHY           ") + TerminalUI.gray("- Deploy terrain sensors to search for physical relays"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[3] ") + TerminalUI.brightWhite("LOG SPECTRAL HARMONICS & ARCHIVE") + TerminalUI.gray("- Record signal telemetry directly into navigation archives"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        int choice = TerminalUI.readMenuOption("  TACTICAL COMMAND > ", 1, 3);
        if (choice == -1) choice = 1;

        String actionLabel;
        String narrativeOutcome;
        switch (choice) {
            case 1 -> {
                actionLabel = "Vector Triangulation";
                narrativeOutcome = "Excursion arrays lock onto carrier oscillation at 1420.405 MHz. Vector coordinates triangulate along a deep-space axis leading beyond the Sol system.";
            }
            case 2 -> {
                actionLabel = "Perimeter Topography Scan";
                narrativeOutcome = "High-gain radar sweeps confirm no artificial terrestrial structures or repeaters. The rhythmic pulse originates from an extrasolar trajectory.";
            }
            default -> {
                actionLabel = "Spectral Waveform Logging";
                narrativeOutcome = "Continuous harmonic waveforms digitized and mirrored to the spacecraft flight recorder for deep-space signal correlation.";
            }
        }

        // Discovery state updated (preserves Stage 1 restrictions via GameState.updateMissionObjectives)
        gameState.setTransmissionEvidenceDiscovered(true);
        gameState.getMissionLog().addEntry("SIGNAL", "Unidentified deep-space transmission analyzed on " + planet.getName() + " (" + actionLabel + ")!");
        gameState.updateMissionObjectives();

        // Display outcome screen
        TerminalUI.clearScreen();
        TerminalUI.printHeaderWithSubtitle(
            "SURFACE EXPEDITION REPORT // SIGNAL TELEMETRY",
            planet.getName().toUpperCase() + " // " + actionLabel.toUpperCase()
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("OPERATION      : ") + TerminalUI.brightCyan(actionLabel));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("SIGNAL STATUS  : ") + TerminalUI.brightGreen("BEACON TELEMETRY ARCHIVED"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("FIELD ANALYSIS LOG:"));
        printWrappedBoxText(narrativeOutcome);
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("UPDATED SPACECRAFT STATUS:"));
        TerminalUI.printBoxLine("  * Fuel: " + ship.getFuel() + "% | Oxygen: " + ship.getOxygen() 
                + "% | Energy: " + ship.getEnergy() + "% | Hull: " + ship.getHullIntegrity() + "%");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("return to Exploration Menu");
    }

    private void handleMonolithEncounter(ExplorationEvent event, Spaceship ship, Planet planet) {
        TerminalUI.clearScreen();
        TerminalUI.printHeaderWithSubtitle(
            "SURFACE EXPEDITION // XENOLOGICAL DISCOVERY",
            planet.getName().toUpperCase() + " SURFACE // " + event.getTitle().toUpperCase()
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("EVENT CLASSIFICATION : ") + TerminalUI.brightYellow("[XENOLOGICAL ENCOUNTER]"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("INCIDENT DESIGNATION : ") + TerminalUI.brightWhite(event.getTitle()));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("EXPEDITION REPORT:"));
        printWrappedBoxText(event.getDescription());
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("TACTICAL PROTOCOL OPTIONS:"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[1] ") + TerminalUI.brightWhite("OPTICAL GLYPH DECIPHERING   ") + TerminalUI.gray("- Scan geometric inscriptions without physical contact"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[2] ") + TerminalUI.brightWhite("ELECTROMAGNETIC FIELD PROBE ") + TerminalUI.gray("- Sample the dormant harmonic resonance field"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[3] ") + TerminalUI.brightWhite("ESTABLISH EXCLUSION ZONE    ") + TerminalUI.gray("- Tag sector coordinates and archive expedition log"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        int choice = TerminalUI.readMenuOption("  TACTICAL COMMAND > ", 1, 3);
        if (choice == -1) choice = 1;

        String actionLabel;
        String narrativeOutcome;
        switch (choice) {
            case 1 -> {
                actionLabel = "Glyph Deciphering";
                narrativeOutcome = "Linguistic sensors register prime number sequences and stellar constellations. The mathematics imply a map, though no physical artifacts are disturbed.";
            }
            case 2 -> {
                actionLabel = "Field Resonance Sampling";
                narrativeOutcome = "Spectrometry detects dormant sub-space energy humming within the alloy matrix. The spire remains inert, safe from contamination.";
            }
            default -> {
                actionLabel = "Exclusion Zone Tagging";
                narrativeOutcome = "Perimeter beacon deployed. Sector coordinates registered with high command as a protected xenological anomaly site.";
            }
        }

        gameState.getMissionLog().addEntry("ENCOUNTER", "Xenological monolith investigated on " + planet.getName() + " (" + actionLabel + ").");
        gameState.updateMissionObjectives();

        // Display outcome screen
        TerminalUI.clearScreen();
        TerminalUI.printHeaderWithSubtitle(
            "SURFACE EXPEDITION REPORT // XENOLOGICAL ANALYSIS",
            planet.getName().toUpperCase() + " // " + actionLabel.toUpperCase()
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("OPERATION      : ") + TerminalUI.brightYellow(actionLabel));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("SITE STATUS    : ") + TerminalUI.brightGreen("SCIENTIFIC DATA CATALOGED (0 ARTIFACTS DISTURBED)"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("ANALYSIS FINDINGS:"));
        printWrappedBoxText(narrativeOutcome);
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("UPDATED SPACECRAFT STATUS:"));
        TerminalUI.printBoxLine("  * Fuel: " + ship.getFuel() + "% | Oxygen: " + ship.getOxygen() 
                + "% | Energy: " + ship.getEnergy() + "% | Hull: " + ship.getHullIntegrity() + "%");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("return to Exploration Menu");
    }

    private void handleDiscoveryEvent(ExplorationEvent event, Spaceship ship, Planet planet) {
        List<String> effectLog = event.applyEffects(ship, gameState.getInventory());
        if (event.hasRewards()) {
            gameState.setResourceCollectedDuringMission(true);
        }
        gameState.getMissionLog().addEntry("DISCOVERY", "Surface discovery on " + planet.getName() + ": " + event.getTitle() + ".");
        gameState.updateMissionObjectives();

        TerminalUI.clearScreen();
        TerminalUI.printHeaderWithSubtitle(
            "SURFACE EXPEDITION REPORT // ANOMALY DISCOVERY",
            planet.getName().toUpperCase() + " SURFACE // " + event.getTitle().toUpperCase()
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("EVENT CLASSIFICATION : ") + TerminalUI.brightCyan("[DISCOVERY EVENT]"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("INCIDENT DESIGNATION : ") + TerminalUI.brightWhite(event.getTitle()));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("FIELD RECONNAISSANCE FINDINGS:"));
        printWrappedBoxText(event.getDescription());
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("RECOVERED EQUIPMENT & TELEMETRY:"));
        for (String logEntry : effectLog) {
            TerminalUI.printBoxLine("  * " + colorizeLogEntry(logEntry));
        }
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("UPDATED SPACECRAFT STATUS:"));
        TerminalUI.printBoxLine("  * Fuel: " + ship.getFuel() + "% | Oxygen: " + ship.getOxygen() 
                + "% | Energy: " + ship.getEnergy() + "% | Hull: " + ship.getHullIntegrity() + "%");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("return to Exploration Menu");
    }

    private void handleResourceEvent(ExplorationEvent event, Spaceship ship, Planet planet) {
        List<String> effectLog = event.applyEffects(ship, gameState.getInventory());
        gameState.setResourceCollectedDuringMission(true);
        gameState.getMissionLog().addEntry("RESOURCE", "Resource salvage recovered on " + planet.getName() + ": " + event.getTitle() + ".");
        gameState.updateMissionObjectives();

        TerminalUI.clearScreen();
        TerminalUI.printHeaderWithSubtitle(
            "SURFACE EXPEDITION REPORT // RESOURCE HARVEST",
            planet.getName().toUpperCase() + " SURFACE // " + event.getTitle().toUpperCase()
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("EVENT CLASSIFICATION : ") + TerminalUI.brightGreen("[RESOURCE RECOVERY EVENT]"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("INCIDENT DESIGNATION : ") + TerminalUI.brightWhite(event.getTitle()));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("FIELD SURVEY SUMMARY:"));
        printWrappedBoxText(event.getDescription());
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightGreen("SECURED CARGO MANIFEST:"));
        for (String logEntry : effectLog) {
            TerminalUI.printBoxLine("  * " + colorizeLogEntry(logEntry));
        }
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("UPDATED SPACECRAFT STATUS:"));
        TerminalUI.printBoxLine("  * Fuel: " + ship.getFuel() + "% | Oxygen: " + ship.getOxygen() 
                + "% | Energy: " + ship.getEnergy() + "% | Hull: " + ship.getHullIntegrity() + "%");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("return to Exploration Menu");
    }

    private void handleGenericEvent(ExplorationEvent event, Spaceship ship, Planet planet) {
        List<String> effectLog = event.applyEffects(ship, gameState.getInventory());
        TerminalUI.clearScreen();
        TerminalUI.printHeaderWithSubtitle(
            "SURFACE EXPEDITION REPORT",
            planet.getName().toUpperCase() + " SURFACE // " + event.getTitle().toUpperCase()
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("EVENT CLASSIFICATION : ") + getCategoryBadge(event.getCategory()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("INCIDENT DESIGNATION : ") + TerminalUI.brightWhite(event.getTitle()));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("FIELD LOG NARRATIVE:"));
        printWrappedBoxText(event.getDescription());
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("EXPEDITION TELEMETRY & EFFECTS:"));
        for (String logEntry : effectLog) {
            TerminalUI.printBoxLine("  * " + colorizeLogEntry(logEntry));
        }
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("UPDATED SPACECRAFT STATUS:"));
        TerminalUI.printBoxLine("  * Fuel: " + ship.getFuel() + "% | Oxygen: " + ship.getOxygen() 
                + "% | Energy: " + ship.getEnergy() + "% | Hull: " + ship.getHullIntegrity() + "%");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("return to Exploration Menu");
    }

    private void displayCasualtyWarning(Spaceship ship) {
        TerminalUI.clearScreen();
        TerminalUI.printHeader("CRITICAL SPACECRAFT CASUALTY // IMMEDIATE ACTION REQUIRED");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printCenteredBoxLine(TerminalUI.brightRed("[!] DANGER: VESSEL ODYSSEY-07 HAS SUSTAINED CATASTROPHIC DAMAGE [!]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Spacecraft operational integrity has fallen below safe limits:");
        if (ship.getHullIntegrity() <= 0) {
            TerminalUI.printBoxLine("  * Hull Integrity : " + TerminalUI.brightRed("0% (HULL BREACHED)"));
        }
        if (ship.getOxygen() <= 0) {
            TerminalUI.printBoxLine("  * Life Support O2: " + TerminalUI.brightRed("0% (LIFE SUPPORT DEPLETED)"));
        }
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  All further surface expeditions and interplanetary burns are INHIBITED.");
        TerminalUI.printBoxLine("  Access Command Center " + TerminalUI.brightCyan("[4] INVENTORY") + " to apply emergency repair supplies.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("return to Exploration Menu");
    }

    private String getCategoryBadge(String category) {
        return switch (category) {
            case ExplorationEvent.CATEGORY_DISCOVERY -> TerminalUI.brightCyan("[DISCOVERY EVENT]");
            case ExplorationEvent.CATEGORY_RESOURCE -> TerminalUI.brightGreen("[RESOURCE EVENT]");
            case ExplorationEvent.CATEGORY_HAZARD -> TerminalUI.brightRed("[HAZARD EVENT]");
            case ExplorationEvent.CATEGORY_ENCOUNTER -> TerminalUI.brightYellow("[ENCOUNTER EVENT]");
            default -> TerminalUI.brightWhite("[" + category.toUpperCase() + "]");
        };
    }

    private String colorizeLogEntry(String entry) {
        if (entry.startsWith("[+]")) {
            return TerminalUI.brightGreen(entry);
        } else if (entry.startsWith("[-]")) {
            return TerminalUI.brightRed(entry);
        } else {
            return TerminalUI.cyan(entry);
        }
    }

    /**
     * Splits and wraps text cleanly to fit within box boundaries.
     */
    private void printWrappedBoxText(String text) {
        if (text == null || text.isEmpty()) return;
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder("  ");
        int maxLineLen = TerminalUI.DEFAULT_WIDTH - 6;

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLineLen) {
                TerminalUI.printBoxLine(currentLine.toString());
                currentLine = new StringBuilder("  ").append(word).append(" ");
            } else {
                currentLine.append(word).append(" ");
            }
        }
        if (currentLine.length() > 2) {
            TerminalUI.printBoxLine(currentLine.toString());
        }
    }

    // ==========================================
    // Phase 3: Navigation System
    // ==========================================

    /**
     * Handles [1] NAVIGATION:
     * Unlocks Mars with an authorization briefing on first access,
     * displays available travel routes and fuel costs,
     * and executes confirmed interplanetary journeys.
     */
    private void handleNavigation() {
        // Scripted unlock: check if Mars needs initial mission authorization
        Planet mars = gameState.getPlanetByName("Mars");
        if (mars != null && !mars.isDiscovered()) {
            showMarsAuthorizationBriefing();
            mars.setDiscovered(true);
            gameState.getMissionLog().addEntry("DISCOVERY", "Planetary coordinates decoded: Mars orbital corridor authorized.");
        }

        // Scripted unlock: check if outer planets Europa and Titan need Stage 2 authorization briefing
        Planet europa = gameState.getPlanetByName("Europa");
        Planet titan = gameState.getPlanetByName("Titan");
        if ((gameState.getCurrentStage().equals(MissionObjective.STAGE_2) || gameState.getCurrentStage().equals(MissionObjective.STAGE_3))
                && europa != null && europa.isDiscovered() && !europa.isExplored() && !titan.isExplored()
                && !hasLoggedEntry("Outer system corridors authorized")) {
            showOuterMoonsAuthorizationBriefing();
            gameState.getMissionLog().addEntry("DISCOVERY", "Outer system corridors authorized: Navigation unlocked to Europa and Titan.");
        }

        // Scripted unlock: check if Kepler-186f needs Stage 3 authorization briefing
        Planet kepler = gameState.getPlanetByName("Kepler-186f");
        if (gameState.getCurrentStage().equals(MissionObjective.STAGE_3) && kepler != null && kepler.isDiscovered()
                && !hasLoggedEntry("Extrasolar corridor authorized")) {
            showKeplerAuthorizationBriefing();
            gameState.getMissionLog().addEntry("DISCOVERY", "Extrasolar corridor authorized: Kepler-186f jump trajectory unlocked.");
        }

        boolean inNavigation = true;
        while (inNavigation && running) {
            displayNavigationMenu();
            int choice = TerminalUI.readMenuOption("  NAVIGATION COMMAND > ", 1, 6);

            if (choice == -1) {
                running = false;
                break;
            }

            switch (choice) {
                case 1 -> {
                    if (attemptTravel(gameState.getPlanetByName("Earth"), 20)) {
                        inNavigation = false;
                    }
                }
                case 2 -> {
                    if (attemptTravel(gameState.getPlanetByName("Mars"), 20)) {
                        inNavigation = false;
                    }
                }
                case 3 -> {
                    if (europa != null && europa.isDiscovered()) {
                        if (attemptTravel(europa, 25)) {
                            inNavigation = false;
                        }
                    } else {
                        handleLockedDestination(europa);
                    }
                }
                case 4 -> {
                    if (titan != null && titan.isDiscovered()) {
                        if (attemptTravel(titan, 25)) {
                            inNavigation = false;
                        }
                    } else {
                        handleLockedDestination(titan);
                    }
                }
                case 5 -> {
                    if (kepler != null && kepler.isDiscovered()) {
                        if (attemptTravel(kepler, 30)) {
                            inNavigation = false;
                        }
                    } else {
                        handleLockedDestination(kepler);
                    }
                }
                case 6 -> {
                    inNavigation = false;
                }
                default -> TerminalUI.printWarning("Invalid navigation selection.");
            }
        }
    }

    private boolean hasLoggedEntry(String fragment) {
        if (gameState == null || gameState.getMissionLog() == null) return false;
        for (MissionLog.LogEntry entry : gameState.getMissionLog().getEntries()) {
            if (entry.getDescription().contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Renders the mission briefing authorizing Mars as the initial travel destination.
     */
    private void showMarsAuthorizationBriefing() {
        TerminalUI.clearScreen();
        TerminalUI.printHeader("MISSION CONTROL // FLIGHT AUTHORIZATION DIRECTIVE");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightYellow("[PRIORITY TRANSMISSION: HIGH-COMMAND // SOL-EARTH]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  \"Commander, long-range deep space telemetry has verified an initial");
        TerminalUI.printBoxLine("  sub-space transmission vector matching the anomalous signal.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  The primary telemetry beacon triangulates to the outer Martian perimeter.");
        TerminalUI.printBoxLine("  High Command has officially AUTHORIZED destination: " + TerminalUI.brightCyan("MARS") + ".");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Interplanetary burn trajectory: " + TerminalUI.brightYellow("20 FUEL") + ".");
        TerminalUI.printBoxLine("  All navigation locks for Sector MARS are now cleared.\"");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("[+] DESTINATION UNLOCKED: MARS"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("open Navigation Console");
    }

    /**
     * Renders mission briefing unlocking Europa and Titan for Stage 2.
     */
    private void showOuterMoonsAuthorizationBriefing() {
        TerminalUI.clearScreen();
        TerminalUI.printHeader("MISSION CONTROL // FLIGHT AUTHORIZATION DIRECTIVE");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightYellow("[PRIORITY TRANSMISSION: HIGH-COMMAND // STAGE 2 DEEP VOYAGER]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  \"Commander, Stage 1 departure milestones have been fully validated.");
        TerminalUI.printBoxLine("  High Command has cleared outer system navigation locks.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Destinations " + TerminalUI.brightCyan("EUROPA") + " and " + TerminalUI.brightCyan("TITAN") + " are now AUTHORIZED.");
        TerminalUI.printBoxLine("  Interplanetary burn trajectories: " + TerminalUI.brightYellow("25 FUEL") + " each.");
        TerminalUI.printBoxLine("  Survey cryogenic environments and locate further transmission vectors.\"");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("[+] DESTINATIONS UNLOCKED: EUROPA & TITAN"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("open Navigation Console");
    }

    /**
     * Renders mission briefing unlocking Kepler-186f for Stage 3.
     */
    private void showKeplerAuthorizationBriefing() {
        TerminalUI.clearScreen();
        TerminalUI.printHeader("MISSION CONTROL // EXTRASOLAR TRAJECTORY DIRECTIVE");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightMagenta("[PRIORITY TRANSMISSION: HIGH-COMMAND // STAGE 3 BEYOND THE UNKNOWN]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  \"Commander, transmission telemetry analyzed across the outer moons is conclusive.");
        TerminalUI.printBoxLine("  The unidentified sub-space carrier originates from exoplanet " + TerminalUI.brightYellow("KEPLER-186F") + ".");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Deep-space interstellar jump coordinates calculated. Burn requirement: " + TerminalUI.brightYellow("30 FUEL") + ".");
        TerminalUI.printBoxLine("  You are authorized to proceed beyond the Sol system to the anomaly origin.\"");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("[+] DESTINATION UNLOCKED: KEPLER-186F"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("open Navigation Console");
    }

    /**
     * Renders the interactive Navigation menu showing available routes and fuel reserves.
     */
    private void displayNavigationMenu() {
        TerminalUI.clearScreen();
        Planet current = gameState.getCurrentLocation();
        Spaceship ship = gameState.getSpaceship();
        Planet europa = gameState.getPlanetByName("Europa");
        Planet titan = gameState.getPlanetByName("Titan");
        Planet kepler = gameState.getPlanetByName("Kepler-186f");

        TerminalUI.printHeaderWithSubtitle(
            "INTERPLANETARY NAVIGATION SYSTEM",
            "SPACECRAFT " + gameState.getPlayer().getSpaceshipName() + " // TRAJECTORY PLOTTER"
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("CURRENT LOCATION : ") + TerminalUI.brightGreen(current.getName().toUpperCase()) + TerminalUI.gray(" (SOL SYSTEM)"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("FUEL RESERVES    : ") + TerminalUI.formatProgressBar(ship.getFuel(), 100, 20) + " (" + ship.getFuel() + " / 100)");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("AVAILABLE DESTINATIONS:"));

        // Option 1: Earth
        String earthStatus = current.getName().equalsIgnoreCase("Earth")
            ? TerminalUI.brightGreen("[CURRENT LOCATION]")
            : TerminalUI.brightCyan("[20 FUEL]");
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[1] ") + TerminalUI.brightWhite("EARTH       ")
                + TerminalUI.gray("- Starting headquarters       ") + earthStatus);

        // Option 2: Mars
        String marsStatus = current.getName().equalsIgnoreCase("Mars")
            ? TerminalUI.brightGreen("[CURRENT LOCATION]")
            : TerminalUI.brightCyan("[20 FUEL]");
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[2] ") + TerminalUI.brightWhite("MARS        ")
                + TerminalUI.gray("- Resource-rich desert world  ") + marsStatus);

        // Option 3: Europa
        String europaStatus;
        if (current.getName().equalsIgnoreCase("Europa")) {
            europaStatus = TerminalUI.brightGreen("[CURRENT LOCATION]");
        } else if (europa != null && europa.isDiscovered()) {
            europaStatus = TerminalUI.brightCyan("[25 FUEL]");
        } else {
            europaStatus = TerminalUI.brightYellow("[LOCKED - STAGE 2]");
        }
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[3] ") + TerminalUI.brightWhite("EUROPA      ")
                + TerminalUI.gray("- Frozen subsurface ocean     ") + europaStatus);

        // Option 4: Titan
        String titanStatus;
        if (current.getName().equalsIgnoreCase("Titan")) {
            titanStatus = TerminalUI.brightGreen("[CURRENT LOCATION]");
        } else if (titan != null && titan.isDiscovered()) {
            titanStatus = TerminalUI.brightCyan("[25 FUEL]");
        } else {
            titanStatus = TerminalUI.brightYellow("[LOCKED - STAGE 2]");
        }
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[4] ") + TerminalUI.brightWhite("TITAN       ")
                + TerminalUI.gray("- Cryogenic methane seas      ") + titanStatus);

        // Option 5: Kepler-186f
        String keplerStatus;
        if (current.getName().equalsIgnoreCase("Kepler-186f")) {
            keplerStatus = TerminalUI.brightGreen("[CURRENT LOCATION]");
        } else if (kepler != null && kepler.isDiscovered()) {
            keplerStatus = TerminalUI.brightCyan("[30 FUEL]");
        } else {
            keplerStatus = TerminalUI.brightRed("[LOCKED - STAGE 3]");
        }
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[5] ") + TerminalUI.brightWhite("KEPLER-186F ")
                + TerminalUI.gray("- Deep space anomaly origin   ") + keplerStatus);

        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[6] ") + TerminalUI.brightWhite("RETURN TO COMMAND CENTER"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();
    }

    /**
     * Validates, confirms, and executes interplanetary travel.
     * Deducts fuel only upon confirmation.
     *
     * @return true if travel succeeded, false if aborted, cancelled, or rejected
     */
    private boolean attemptTravel(Planet target, int fuelCost) {
        if (target == null) return false;

        Planet current = gameState.getCurrentLocation();

        // Rule: Travelling to the current location must be rejected
        if (target.getName().equalsIgnoreCase(current.getName())) {
            System.out.println();
            TerminalUI.printWarning("Navigation rejected: Vessel ODYSSEY-07 is already in orbit around " + target.getName() + ".");
            TerminalUI.pressEnterToContinue("return to Navigation");
            return false;
        }

        Spaceship ship = gameState.getSpaceship();

        // Rule: Require sufficient fuel before allowing travel
        if (ship.getFuel() < fuelCost) {
            System.out.println();
            TerminalUI.printError("Insufficient fuel: Interplanetary trajectory to " + target.getName()
                    + " requires " + fuelCost + " Fuel, but reserves are at " + ship.getFuel() + "%. Travel aborted.");
            TerminalUI.pressEnterToContinue("return to Navigation");
            return false;
        }

        // Confirmation prompt
        TerminalUI.clearScreen();
        TerminalUI.printHeader("FLIGHT VECTOR CONFIRMATION");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("DEPARTURE POINT : ") + TerminalUI.brightCyan(current.getName().toUpperCase()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("DESTINATION     : ") + TerminalUI.brightGreen(target.getName().toUpperCase()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("PROPULSION BURN : ") + TerminalUI.brightYellow(fuelCost + " FUEL"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("CURRENT FUEL    : ") + TerminalUI.brightWhite(ship.getFuel() + " FUEL"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("REMAINING AFTER : ") + TerminalUI.brightWhite((ship.getFuel() - fuelCost) + " FUEL"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        String confirm = TerminalUI.readLine("  Confirm burn sequence? [Y/N] > ");
        if (!confirm.equalsIgnoreCase("Y") && !confirm.equalsIgnoreCase("YES")) {
            System.out.println();
            TerminalUI.printWarning("Flight burn sequence cancelled by Commander. No fuel consumed.");
            TerminalUI.pressEnterToContinue("return to Navigation");
            return false;
        }

        // Rule: Deduct fuel only after confirmation
        ship.consumeFuel(fuelCost);

        // Record propulsion burn in mission log
        gameState.getMissionLog().addEntry("NAVIGATION", "Propulsion burn initiated: Transiting from " + current.getName() + " to " + target.getName() + " (-" + fuelCost + " Fuel).");

        // Departure sequence animation
        TerminalUI.clearScreen();
        TerminalUI.printHeader("INTERPLANETARY FLIGHT IN PROGRESS");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Disengaging orbital mooring clamps... " + TerminalUI.brightGreen("[ OK ]"));
        TerminalUI.printBoxLine("  Plotting burn vector from " + current.getName() + " to " + target.getName() + "... " + TerminalUI.brightGreen("[ OK ]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        TerminalUI.renderProgressAnimation("EXECUTING PROPULSION BURN (-" + fuelCost + " FUEL)", 450);
        TerminalUI.renderSpinner("TRANSITING INTERPLANETARY SPACE", 400);

        // Rule: Update spacecraft current location after successful travel
        gameState.setCurrentLocation(target);

        // Record orbital insertion in mission log
        gameState.getMissionLog().addEntry("NAVIGATION", "Orbital insertion confirmed: Established parking orbit at " + target.getName() + ".");
        gameState.updateMissionObjectives();

        // Arrival message
        TerminalUI.clearScreen();
        TerminalUI.printHeader("ORBITAL INSERTION CONFIRMED // ARRIVAL");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightGreen("[+] SUCCESSFUL ARRIVAL: ") + TerminalUI.brightWhite(target.getName().toUpperCase()));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Vessel ODYSSEY-07 has established a stable parking orbit around " + target.getName() + ".");
        TerminalUI.printBoxLine("  Fuel reserves remaining: " + TerminalUI.brightWhite(ship.getFuel() + "%") + " " + TerminalUI.formatResourceStatus(ship.getFuel()));
        TerminalUI.printBoxLine("  Atmospheric telemetry : " + target.getEnvironmentDescription());
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("enter Command Center");
        return true;
    }

    /**
     * Handles attempts to navigate to locked destinations.
     */
    private void handleLockedDestination(Planet target) {
        System.out.println();
        String name = (target != null) ? target.getName() : "destination";
        TerminalUI.printWarning("Navigation locked: Route coordinates to " + name + " are not authorized in this operational phase.");
        TerminalUI.pressEnterToContinue("return to Navigation");
    }

    // ==========================================
    // Phase 3: Galaxy Map
    // ==========================================

    /**
     * Handles [2] GALAXY MAP:
     * Displays all 5 locations, their discovery/classification states,
     * visual ASCII orbital map, and symbol legend.
     */
    private void handleGalaxyMap() {
        TerminalUI.clearScreen();
        Planet current = gameState.getCurrentLocation();
        Planet mars = gameState.getPlanetByName("Mars");
        boolean marsDiscovered = (mars != null && mars.isDiscovered());
        Planet europa = gameState.getPlanetByName("Europa");
        boolean europaDiscovered = (europa != null && europa.isDiscovered());
        Planet titan = gameState.getPlanetByName("Titan");
        boolean titanDiscovered = (titan != null && titan.isDiscovered());
        Planet kepler = gameState.getPlanetByName("Kepler-186f");
        boolean keplerDiscovered = (kepler != null && kepler.isDiscovered());

        TerminalUI.printHeaderWithSubtitle(
            "STELLAR CARTOGRAPHY // GALAXY MAP",
            "SOLAR SYSTEM & BEYOND - SECTOR GRID MAP-01"
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("CURRENT SECTOR   : ") + TerminalUI.brightGreen(current.getName().toUpperCase() + " ORBIT") + TerminalUI.gray(" (SOL SYSTEM)"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("VESSEL STATUS    : ") + TerminalUI.brightYellow(gameState.getPlayer().getSpaceshipName()) + TerminalUI.gray(" [STATIONARY PARKING ORBIT]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("ORBITAL NETWORK MAP:"));
        TerminalUI.printEmptyBoxLine();

        // Orbital network representation
        TerminalUI.printCenteredBoxLine(TerminalUI.brightYellow("[ (SOL) ]"));
        TerminalUI.printCenteredBoxLine(TerminalUI.gray("|"));

        // Earth node
        Planet earth = gameState.getPlanetByName("Earth");
        boolean earthExplored = (earth != null && earth.isExplored());
        if (current.getName().equalsIgnoreCase("Earth")) {
            if (earthExplored) {
                TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("===>>  [ EARTH ]  <<=== [CURRENT] [EXPLORED]"));
            } else {
                TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("===>>  [ EARTH ]  <<=== [CURRENT LOCATION]"));
            }
        } else if (earthExplored) {
            TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("       [ EARTH ]        [EXPLORED]"));
        } else {
            TerminalUI.printCenteredBoxLine(TerminalUI.brightCyan("       [ EARTH ]        [DISCOVERED]"));
        }
        TerminalUI.printCenteredBoxLine(TerminalUI.gray("|"));
        TerminalUI.printCenteredBoxLine(TerminalUI.cyan("| (20 Fuel)"));
        TerminalUI.printCenteredBoxLine(TerminalUI.gray("v"));

        // Mars node
        boolean marsExplored = (mars != null && mars.isExplored());
        if (current.getName().equalsIgnoreCase("Mars")) {
            if (marsExplored) {
                TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("===>>  [ MARS ]   <<=== [CURRENT] [EXPLORED]"));
            } else {
                TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("===>>  [ MARS ]   <<=== [CURRENT LOCATION]"));
            }
        } else if (marsExplored) {
            TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("       [ MARS ]         [EXPLORED]"));
        } else if (marsDiscovered) {
            TerminalUI.printCenteredBoxLine(TerminalUI.brightCyan("       [ MARS ]         [DISCOVERED]"));
        } else {
            TerminalUI.printCenteredBoxLine(TerminalUI.gray("      [ ?????? ]        ") + TerminalUI.yellow("[CLASSIFIED]"));
        }
        // Trajectory link to Europa
        if (europaDiscovered) {
            TerminalUI.printCenteredBoxLine(TerminalUI.cyan("| (25 Fuel)"));
            TerminalUI.printCenteredBoxLine(TerminalUI.gray("v"));
        } else {
            TerminalUI.printCenteredBoxLine(TerminalUI.gray(": (Locked)"));
            TerminalUI.printCenteredBoxLine(TerminalUI.gray("x"));
        }

        // Europa node
        boolean europaExplored = (europa != null && europa.isExplored());
        if (current.getName().equalsIgnoreCase("Europa")) {
            if (europaExplored) {
                TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("===>> [ EUROPA ]  <<=== [CURRENT] [EXPLORED]"));
            } else {
                TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("===>> [ EUROPA ]  <<=== [CURRENT LOCATION]"));
            }
        } else if (europaExplored) {
            TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("      [ EUROPA ]        [EXPLORED]"));
        } else if (europaDiscovered) {
            TerminalUI.printCenteredBoxLine(TerminalUI.brightCyan("      [ EUROPA ]        [DISCOVERED]"));
        } else {
            TerminalUI.printCenteredBoxLine(TerminalUI.gray("      [ ?????? ]        ") + TerminalUI.gray("[CLASSIFIED]"));
        }

        // Trajectory link to Titan
        if (titanDiscovered) {
            TerminalUI.printCenteredBoxLine(TerminalUI.cyan("| (25 Fuel)"));
            TerminalUI.printCenteredBoxLine(TerminalUI.gray("v"));
        } else {
            TerminalUI.printCenteredBoxLine(TerminalUI.gray(":"));
        }

        // Titan node
        boolean titanExplored = (titan != null && titan.isExplored());
        if (current.getName().equalsIgnoreCase("Titan")) {
            if (titanExplored) {
                TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("===>>  [ TITAN ]  <<=== [CURRENT] [EXPLORED]"));
            } else {
                TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("===>>  [ TITAN ]  <<=== [CURRENT LOCATION]"));
            }
        } else if (titanExplored) {
            TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("       [ TITAN ]        [EXPLORED]"));
        } else if (titanDiscovered) {
            TerminalUI.printCenteredBoxLine(TerminalUI.brightCyan("       [ TITAN ]        [DISCOVERED]"));
        } else {
            TerminalUI.printCenteredBoxLine(TerminalUI.gray("      [ ?????? ]        ") + TerminalUI.gray("[CLASSIFIED]"));
        }

        // Trajectory link to Kepler-186f
        if (keplerDiscovered) {
            TerminalUI.printCenteredBoxLine(TerminalUI.magenta("| (30 Fuel - Extrasolar Jump)"));
            TerminalUI.printCenteredBoxLine(TerminalUI.gray("v"));
        } else {
            TerminalUI.printCenteredBoxLine(TerminalUI.gray(":"));
        }

        // Kepler node
        boolean keplerExplored = (kepler != null && kepler.isExplored());
        if (current.getName().equalsIgnoreCase("Kepler-186f")) {
            if (keplerExplored) {
                TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("==>> [KEPLER-186F]<<== [CURRENT] [EXPLORED]"));
            } else {
                TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("==>> [KEPLER-186F]<<== [CURRENT LOCATION]"));
            }
        } else if (keplerExplored) {
            TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("    [ KEPLER-186F ]     [EXPLORED]"));
        } else if (keplerDiscovered) {
            TerminalUI.printCenteredBoxLine(TerminalUI.brightCyan("    [ KEPLER-186F ]     [DISCOVERED]"));
        } else {
            TerminalUI.printCenteredBoxLine(TerminalUI.gray("      [ ?????? ]        ") + TerminalUI.brightMagenta("[UNKNOWN BEACON]"));
        }

        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("SECTOR DIRECTORY:"));

        for (Planet p : gameState.getPlanets()) {
            String name = p.getName();
            int pad = 12 - name.length();
            if (pad < 1) pad = 1;
            String padStr = " ".repeat(pad);

            if (p.getName().equalsIgnoreCase(current.getName())) {
                String tag = p.isExplored() ? "[CURRENT / EXPLORED] " : "[CURRENT]            ";
                TerminalUI.printBoxLine("  * " + TerminalUI.brightGreen(name) + padStr + ": "
                        + TerminalUI.brightGreen(tag) + TerminalUI.white(p.getDescription()));
            } else if (p.isExplored()) {
                TerminalUI.printBoxLine("  * " + TerminalUI.brightGreen(name) + padStr + ": "
                        + TerminalUI.brightGreen("[EXPLORED]           ") + TerminalUI.white(p.getDescription()));
            } else if (p.isDiscovered()) {
                TerminalUI.printBoxLine("  * " + TerminalUI.brightCyan(name) + padStr + ": "
                        + TerminalUI.brightCyan("[DISCOVERED]         ") + TerminalUI.white(p.getDescription()));
            } else {
                TerminalUI.printBoxLine("  * " + TerminalUI.gray(name) + padStr + ": "
                        + TerminalUI.gray("[CLASSIFIED]         // Trajectory coordinates restricted"));
            }
        }

        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("MAP LEGEND:"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightGreen("<<=== [CURRENT LOCATION]") + TerminalUI.gray(" : Current Spacecraft Orbital Position"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightGreen("[EXPLORED]") + TerminalUI.gray("                : Surface Surveyed (Expeditions Completed)"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[DISCOVERED]") + TerminalUI.gray("             : Charted Celestial Body (Flight Authorized)"));
        TerminalUI.printBoxLine("  " + TerminalUI.gray("[CLASSIFIED]") + TerminalUI.gray("             : Undiscovered Sector // Navigation Restricted"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("return to Command Center");
    }

    // ==========================================
    // Ship Status & Placeholders
    // ==========================================

    /**
     * Handles [3] SHIP STATUS:
     * Displays actual spacecraft attributes and resource breakdown.
     */
    private void handleShipStatus() {
        TerminalUI.clearScreen();
        Spaceship ship = gameState.getSpaceship();
        Player player = gameState.getPlayer();

        TerminalUI.printHeader("SPACECRAFT " + player.getSpaceshipName() + " // DIAGNOSTIC REPORT");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("VESSEL DESIGNATION : ") + TerminalUI.brightCyan(player.getSpaceshipName()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("COMMANDING OFFICER : ") + TerminalUI.brightYellow(player.getCommanderName().toUpperCase()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("CURRENT SECTOR     : ") + TerminalUI.brightGreen(gameState.getCurrentLocation().getName().toUpperCase() + " ORBIT"));

        String opStatus = ship.isOperational()
                ? TerminalUI.brightGreen("[OPERATIONAL / NOMINAL]")
                : TerminalUI.brightRed("[CRITICAL COMPROMISE]");
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("OPERATIONAL STATE  : ") + opStatus);
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("TELEMETRY SENSORS & VITALS BREAKDOWN:"));
        TerminalUI.printBoxLine("  * " + TerminalUI.brightWhite("FUEL RESERVES   : ") + String.format("%3d / 100  ", ship.getFuel())
                + TerminalUI.formatProgressBar(ship.getFuel(), 100, 18)
                + " " + TerminalUI.formatResourceStatus(ship.getFuel()));
        TerminalUI.printBoxLine("  * " + TerminalUI.brightWhite("LIFE SUPPORT O2 : ") + String.format("%3d / 100  ", ship.getOxygen())
                + TerminalUI.formatProgressBar(ship.getOxygen(), 100, 18)
                + " " + TerminalUI.formatResourceStatus(ship.getOxygen()));
        TerminalUI.printBoxLine("  * " + TerminalUI.brightWhite("REACTOR ENERGY  : ") + String.format("%3d / 100  ", ship.getEnergy())
                + TerminalUI.formatProgressBar(ship.getEnergy(), 100, 18)
                + " " + TerminalUI.formatResourceStatus(ship.getEnergy()));
        TerminalUI.printBoxLine("  * " + TerminalUI.brightWhite("HULL INTEGRITY  : ") + String.format("%3d / 100  ", ship.getHullIntegrity())
                + TerminalUI.formatProgressBar(ship.getHullIntegrity(), 100, 18)
                + " " + TerminalUI.formatResourceStatus(ship.getHullIntegrity()));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("DIAGNOSTIC ADVISORY:"));
        if (ship.isAnyResourceDepleted()) {
            TerminalUI.printBoxLine("  " + TerminalUI.brightRed("[!] EMERGENCY: One or more vitals are completely DEPLETED (0%)!"));
            TerminalUI.printBoxLine("  Vessel propulsion or life support has suffered catastrophic failure.");
        } else if (ship.isAnyResourceCritical()) {
            TerminalUI.printBoxLine("  " + TerminalUI.brightRed("[!] CRITICAL: One or more vitals are at CRITICAL levels (1-25%)!"));
            TerminalUI.printBoxLine("  Immediate replenishment recommended before further space maneuvers.");
        } else if (ship.isAnyResourceCaution()) {
            TerminalUI.printBoxLine("  " + TerminalUI.brightYellow("[!] CAUTION: One or more vitals are at reduced levels (26-50%)."));
            TerminalUI.printBoxLine("  Monitor burn rates closely during interplanetary transit.");
        } else {
            TerminalUI.printBoxLine("  All primary avionics and environmental reserves operating within");
            TerminalUI.printBoxLine("  established safety guidelines (Above 50%). ODYSSEY-07 is NOMINAL.");
        }
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("return to Command Center");
    }

    /**
     * Renders a standardized placeholder screen for upcoming phase features.
     */
    private void handlePlaceholder(String title, String phaseNotice, String description) {
        TerminalUI.clearScreen();
        TerminalUI.printHeader(title);
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightYellow("[!] FEATURE STATUS : ") + TerminalUI.brightWhite(phaseNotice));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("SYSTEM BRIEFING:"));

        // Format description cleanly into box lines
        String[] words = description.split(" ");
        StringBuilder currentLine = new StringBuilder("  ");
        int maxLineLen = TerminalUI.DEFAULT_WIDTH - 6;

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLineLen) {
                TerminalUI.printBoxLine(currentLine.toString());
                currentLine = new StringBuilder("  ").append(word).append(" ");
            } else {
                currentLine.append(word).append(" ");
            }
        }
        if (currentLine.length() > 2) {
            TerminalUI.printBoxLine(currentLine.toString());
        }

        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("return to Command Center");
    }

    // ==========================================
    // Phase 6: Mission Objectives & Log Systems
    // ==========================================

    /**
     * Handles [6] MISSION OBJECTIVES:
     * Interactive campaign objectives dashboard with stage breakdowns,
     * progress telemetry, and commander directives.
     */
    private void handleMissionObjectives() {
        boolean inObjectives = true;
        gameState.updateMissionObjectives();

        while (inObjectives && running) {
            displayMissionObjectivesDashboard();
            int choice = TerminalUI.readMenuOption("  OBJECTIVES COMMAND > ", 1, 4);

            if (choice == -1) {
                running = false;
                break;
            }

            switch (choice) {
                case 1 -> displayStageDetails(MissionObjective.STAGE_1);
                case 2 -> displayStageDetails(MissionObjective.STAGE_2);
                case 3 -> displayStageDetails(MissionObjective.STAGE_3);
                case 4 -> inObjectives = false;
                default -> TerminalUI.printWarning("Invalid objectives command.");
            }
        }
    }

    /**
     * Renders the primary Mission Objectives dashboard.
     */
    private void displayMissionObjectivesDashboard() {
        TerminalUI.clearScreen();
        gameState.updateMissionObjectives();

        Player player = gameState.getPlayer();
        List<MissionObjective> allObjs = gameState.getObjectives();

        TerminalUI.printHeaderWithSubtitle(
            "MISSION DIRECTIVES // CAMPAIGN PROGRESSION",
            "VESSEL " + player.getSpaceshipName() + " // COMMANDER " + player.getCommanderName().toUpperCase()
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("CAMPAIGN STAGE  : ") + TerminalUI.brightCyan(gameState.getCurrentStage().toUpperCase()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("MISSION TITLE   : ") + TerminalUI.brightYellow(getStageMissionTitle(gameState.getCurrentStage())));

        int completed = 0;
        for (MissionObjective o : allObjs) {
            if (o.isCompleted()) completed++;
        }
        int progressPct = (completed * 100) / allObjs.size();

        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("OVERALL PROGRESS: ") + TerminalUI.formatProgressBar(progressPct, 100, 20)
                + " (" + completed + " / " + allObjs.size() + " Objectives Completed)");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("OPERATIONAL DIRECTIVE // NEXT MANDATORY ACTION:"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightYellow(">> ") + TerminalUI.brightWhite(getNextDirectiveAdvice()));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();

        // 1. In Progress / Active objectives
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("ACTIVE OBJECTIVES:"));
        boolean hasActive = false;
        for (MissionObjective obj : allObjs) {
            if (obj.isInProgress()) {
                hasActive = true;
                TerminalUI.printBoxLine("  " + TerminalUI.brightYellow("[>] IN PROGRESS : ") + TerminalUI.brightWhite(obj.getTitle())
                        + (!obj.getProgressInfo().isEmpty() ? TerminalUI.gray(" [" + obj.getProgressInfo() + "]") : ""));
                TerminalUI.printBoxLine("      " + TerminalUI.gray(obj.getDescription()));
            }
        }
        if (!hasActive) {
            TerminalUI.printBoxLine("  " + TerminalUI.gray("    No active directives in current stage."));
        }

        TerminalUI.printEmptyBoxLine();

        // 2. Completed objectives
        TerminalUI.printBoxLine("  " + TerminalUI.brightGreen("COMPLETED OBJECTIVES:"));
        boolean hasCompleted = false;
        for (MissionObjective obj : allObjs) {
            if (obj.isCompleted()) {
                hasCompleted = true;
                TerminalUI.printBoxLine("  " + TerminalUI.brightGreen("[V] COMPLETED   : ") + TerminalUI.white(obj.getTitle())
                        + TerminalUI.gray(" (" + obj.getStage() + ")"));
            }
        }
        if (!hasCompleted) {
            TerminalUI.printBoxLine("  " + TerminalUI.gray("    No directives completed yet. Begin exploration!"));
        }

        TerminalUI.printEmptyBoxLine();

        // 3. Pending / Locked objectives
        TerminalUI.printBoxLine("  " + TerminalUI.gray("PENDING / LOCKED OBJECTIVES:"));
        int pendingCount = 0;
        for (MissionObjective obj : allObjs) {
            if (obj.isPending()) {
                pendingCount++;
                if (pendingCount <= 3) {
                    TerminalUI.printBoxLine("  " + TerminalUI.gray("[ ] PENDING     : " + obj.getTitle() + " (" + obj.getStage() + ")"));
                }
            }
        }
        if (pendingCount > 3) {
            TerminalUI.printBoxLine("  " + TerminalUI.gray("      ... and " + (pendingCount - 3) + " more future directives (Select [1-3] to view stages)."));
        } else if (pendingCount == 0) {
            TerminalUI.printBoxLine("  " + TerminalUI.gray("    All campaign objectives have been unlocked or completed."));
        }

        TerminalUI.printEmptyBoxLine();
        TerminalUI.printSingleBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("DIRECTIVE COMMANDS:"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[1] ") + TerminalUI.brightWhite("STAGE 1 BREAKDOWN  ") + TerminalUI.gray("- Departure Directives & Checklist"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[2] ") + TerminalUI.brightWhite("STAGE 2 BREAKDOWN  ") + TerminalUI.gray("- Deep Space Exploration Directives"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[3] ") + TerminalUI.brightWhite("STAGE 3 BREAKDOWN  ") + TerminalUI.gray("- Beyond the Unknown Directives"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[4] ") + TerminalUI.brightWhite("RETURN TO COMMAND CENTER"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();
    }

    /**
     * Displays detailed view for a single campaign stage.
     */
    private void displayStageDetails(String stageName) {
        TerminalUI.clearScreen();
        TerminalUI.printHeaderWithSubtitle(
            "CAMPAIGN DIRECTIVES BREAKDOWN",
            stageName.toUpperCase()
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("STAGE MISSION : ") + TerminalUI.brightYellow(getStageMissionTitle(stageName)));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();

        List<MissionObjective> list = gameState.getObjectives();
        for (MissionObjective obj : list) {
            if (obj.getStage().equalsIgnoreCase(stageName)) {
                String badge = switch (obj.getStatus()) {
                    case COMPLETED -> TerminalUI.brightGreen("[V] COMPLETED  ");
                    case IN_PROGRESS -> TerminalUI.brightYellow("[>] IN PROGRESS");
                    case PENDING -> TerminalUI.gray("[ ] PENDING    ");
                };

                TerminalUI.printBoxLine("  " + badge + " : " + TerminalUI.brightWhite(obj.getTitle()));
                TerminalUI.printBoxLine("      " + TerminalUI.brightCyan("Status  : ") + obj.getStatus()
                        + (!obj.getProgressInfo().isEmpty() ? " [" + obj.getProgressInfo() + "]" : ""));
                printWrappedBoxText(TerminalUI.white(obj.getDescription()));
                TerminalUI.printEmptyBoxLine();
            }
        }

        TerminalUI.printDoubleBorder();
        TerminalUI.pressEnterToContinue("return to Mission Objectives Dashboard");
    }

    private String getStageMissionTitle(String stage) {
        return switch (stage) {
            case MissionObjective.STAGE_1 -> "OPERATION SOL DEPARTURE // FIRST INTERPLANETARY FLIGHT";
            case MissionObjective.STAGE_2 -> "PROJECT DEEP VOYAGER // OUTER SYSTEM RECONNAISSANCE";
            case MissionObjective.STAGE_3 -> "BEYOND THE UNKNOWN // KEPLER-186F ANOMALY RESOLUTION";
            default -> "OPERATION DEEP SPACE EXPLORATION";
        };
    }

    private String getNextDirectiveAdvice() {
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
            if (gameState.isCampaignCompleted()) {
                return "Campaign directives resolved (" + gameState.getCompletedEnding() + "). Telemetry archived.";
            }
            if (gameState.getCurrentLocation() == null || !gameState.getCurrentLocation().getName().equalsIgnoreCase("Kepler-186f")) {
                return "Initiate deep-space burn (30 Fuel) to navigate to extrasolar destination Kepler-186f.";
            } else if (!gameState.isSignalSourceDiscovered()) {
                return "Conduct surface exploration on Kepler-186f to pinpoint the transmission source.";
            } else {
                return "Signal source cataloged! Execute [5] FINAL MISSION DIRECTIVE to determine mission climax.";
            }
        }
    }

    /**
     * Handles [7] MISSION LOG:
     * Chronological flight recorder with timestamps, category tags, pagination,
     * and reverse/chronological order toggle.
     */
    private void handleMissionLog() {
        boolean inLog = true;
        boolean newestFirst = true;
        int currentPage = 1;
        final int pageSize = 8;

        while (inLog && running) {
            MissionLog log = gameState.getMissionLog();
            List<MissionLog.LogEntry> allEntries = newestFirst ? log.getEntriesNewestFirst() : log.getEntries();
            int totalEntries = allEntries.size();
            int totalPages = Math.max(1, (int) Math.ceil((double) totalEntries / pageSize));

            if (currentPage > totalPages) currentPage = totalPages;
            if (currentPage < 1) currentPage = 1;

            TerminalUI.clearScreen();
            Player player = gameState.getPlayer();
            TerminalUI.printHeaderWithSubtitle(
                "FLIGHT RECORDER // MISSION CHRONICLE LOG",
                "VESSEL " + player.getSpaceshipName() + " // TELEMETRY AUDIT TRAIL"
            );
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("CHRONICLE STATUS : ") + TerminalUI.brightGreen("RECORDING ACTIVE"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("TOTAL ENTRIES    : ") + TerminalUI.brightCyan(String.valueOf(totalEntries)));
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("DISPLAY SORT     : ") + (newestFirst ? TerminalUI.brightYellow("NEWEST FIRST (REVERSE CHRONOLOGICAL)") : TerminalUI.brightCyan("OLDEST FIRST (CHRONOLOGICAL)")));
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("PAGINATION       : ") + TerminalUI.white("PAGE " + currentPage + " OF " + totalPages));
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printInnerBorder();
            TerminalUI.printEmptyBoxLine();

            if (totalEntries == 0) {
                TerminalUI.printCenteredBoxLine(TerminalUI.brightYellow("NO MISSION LOG ENTRIES RECORDED IN TELEMETRY FLIGHT RECORDER."));
                TerminalUI.printEmptyBoxLine();
            } else {
                int startIndex = (currentPage - 1) * pageSize;
                int endIndex = Math.min(startIndex + pageSize, totalEntries);

                for (int i = startIndex; i < endIndex; i++) {
                    MissionLog.LogEntry entry = allEntries.get(i);
                    String timeTag = TerminalUI.brightYellow("[" + entry.getTimestamp() + "]");
                    String catBadge = formatLogCategoryBadge(entry.getCategory());
                    TerminalUI.printBoxLine("  " + timeTag + " " + catBadge + " " + TerminalUI.brightWhite(entry.getDescription()));
                }
                TerminalUI.printEmptyBoxLine();
            }

            TerminalUI.printSingleBorder();
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("RECORDER CONTROLS:"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[1] ") + TerminalUI.brightWhite("PREVIOUS PAGE    ") + TerminalUI.gray("- Inspect earlier log entries"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[2] ") + TerminalUI.brightWhite("NEXT PAGE        ") + TerminalUI.gray("- Inspect subsequent log entries"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[3] ") + TerminalUI.brightWhite("TOGGLE SORT ORDER") + TerminalUI.gray("- Switch between newest-first / oldest-first"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[4] ") + TerminalUI.brightWhite("RETURN TO COMMAND CENTER"));
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printDoubleBorder();
            System.out.println();

            int choice = TerminalUI.readMenuOption("  RECORDER COMMAND > ", 1, 4);
            if (choice == -1) {
                running = false;
                break;
            }

            switch (choice) {
                case 1 -> {
                    if (currentPage > 1) {
                        currentPage--;
                    } else {
                        TerminalUI.printWarning("Already at the first page.");
                        TerminalUI.sleep(350);
                    }
                }
                case 2 -> {
                    if (currentPage < totalPages) {
                        currentPage++;
                    } else {
                        TerminalUI.printWarning("Already at the last page.");
                        TerminalUI.sleep(350);
                    }
                }
                case 3 -> {
                    newestFirst = !newestFirst;
                    currentPage = 1;
                }
                case 4 -> inLog = false;
                default -> TerminalUI.printWarning("Invalid recorder command.");
            }
        }
    }

    private String formatLogCategoryBadge(String category) {
        String padded = String.format("%-11s", category);
        return switch (category.toUpperCase()) {
            case "MISSION" -> TerminalUI.brightCyan("[" + padded + "]");
            case "NAVIGATION" -> TerminalUI.brightBlue("[" + padded + "]");
            case "DISCOVERY" -> TerminalUI.brightGreen("[" + padded + "]");
            case "RESOURCE" -> TerminalUI.green("[" + padded + "]");
            case "HAZARD" -> TerminalUI.brightRed("[" + padded + "]");
            case "ENCOUNTER" -> TerminalUI.brightYellow("[" + padded + "]");
            case "SIGNAL" -> TerminalUI.brightMagenta("[" + padded + "]");
            default -> TerminalUI.white("[" + padded + "]");
        };
    }

    // ==========================================
    // Phase 8: Campaign Completion & Multiple Endings
    // ==========================================

    /**
     * Handles the final campaign decision on Kepler-186f.
     * Determines whether Commander returns with the Alien Artifact or remains in the anomaly.
     */
    public void handleFinalMissionDecision() {
        if (gameState.isCampaignCompleted()) {
            showCompletedCampaignReviewScreen(gameState);
            return;
        }

        Spaceship ship = gameState.getSpaceship();
        Player player = gameState.getPlayer();
        Inventory inv = gameState.getInventory();

        boolean inDecision = true;
        while (inDecision && running) {
            TerminalUI.clearScreen();
            TerminalUI.printHeaderWithSubtitle(
                "FINAL MISSION DIRECTIVE // BEYOND THE UNKNOWN",
                "KEPLER-186F // ANOMALY CONVERGENCE POINT"
            );
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("COMMANDING OFFICER : ") + TerminalUI.brightYellow(player.getCommanderName().toUpperCase()));
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("VESSEL DESIGNATION : ") + TerminalUI.brightCyan(player.getSpaceshipName()));
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("CELESTIAL SECTOR   : ") + TerminalUI.brightGreen("KEPLER-186F ORBIT"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("ANOMALY DISCOVERY  : ") + TerminalUI.brightGreen("SIGNAL SOURCE CONFIRMED"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("ALIEN ARTIFACT     : ") + TerminalUI.brightYellow(inv.getQuantity(Inventory.ALIEN_ARTIFACTS) + " IN STASIS CONTAINER"));
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printInnerBorder();
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("MISSION CLIMAX BRIEFING:"));
            TerminalUI.printBoxLine("  You have reached the origin point of the deep-space transmission.");
            TerminalUI.printBoxLine("  Beneath the basalt plateaus of Kepler-186f, the alien megastructure");
            TerminalUI.printBoxLine("  stands active, casting harmonic geometries across the cavern walls.");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  High Command directives mandate securing the Alien Artifact and");
            TerminalUI.printBoxLine("  returning to Sol. Yet an open trans-dimensional gateway hums on the dais,");
            TerminalUI.printBoxLine("  offering a journey beyond the boundaries of known spacetime.");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printInnerBorder();
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("RETURN TRAJECTORY VIABILITY ASSESSMENT:"));
            String opStatus = ship.isOperational() ? TerminalUI.brightGreen("[OPERATIONAL]") : TerminalUI.brightRed("[CRITICAL CASUALTY]");
            TerminalUI.printBoxLine("  * Vessel Operational State : " + opStatus);
            TerminalUI.printBoxLine("  * Hull Integrity           : " + ship.getHullIntegrity() + "%");
            TerminalUI.printBoxLine("  * Life Support (O2)        : " + ship.getOxygen() + "%");
            TerminalUI.printBoxLine("  * Propulsion Fuel          : " + ship.getFuel() + "% (Return burn requires 20 Fuel)");
            TerminalUI.printBoxLine("  * Cargo Fuel Cells         : " + inv.getQuantity(Inventory.FUEL_CELLS) + " available");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printSingleBorder();
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("SELECT FINAL MISSION DIRECTIVE:"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightGreen("[1] INITIATE RETURN VOYAGE TO SOL-EARTH"));
            TerminalUI.printBoxLine("      " + TerminalUI.gray("Burn propulsion to return with the Alien Artifact for humanity."));
            TerminalUI.printBoxLine("      " + TerminalUI.gray("(Requires operational vessel and at least 20 Fuel)"));
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightMagenta("[2] VENTURE INTO THE UNKNOWN"));
            TerminalUI.printBoxLine("      " + TerminalUI.gray("Step into the alien gateway on Kepler-186f and transcend human space."));
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[3] SUSPEND DECISION // RETURN TO COMMAND CENTER"));
            TerminalUI.printBoxLine("      " + TerminalUI.gray("Postpone decision to review diagnostics, conduct repairs, or restock."));
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printDoubleBorder();
            System.out.println();

            int choice = TerminalUI.readMenuOption("  DIRECTIVE COMMAND > ", 1, 3);
            if (choice == -1) {
                running = false;
                break;
            }

            switch (choice) {
                case 1 -> {
                    if (confirmAndExecuteReturnEnding(ship, player, inv)) {
                        inDecision = false;
                    }
                }
                case 2 -> {
                    if (confirmAndExecuteUnknownEnding(ship, player)) {
                        inDecision = false;
                    }
                }
                case 3 -> inDecision = false;
            }
        }
    }

    private boolean confirmAndExecuteReturnEnding(Spaceship ship, Player player, Inventory inv) {
        TerminalUI.clearScreen();
        TerminalUI.printHeader("CONFIRM FLIGHT DIRECTIVE // SOL RETURN TRAJECTORY");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightYellow("[!] ATTENTION: FINAL RETURN TRAJECTORY INITIALIZATION"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Commander, initiating the return vector will conclude exploration");
        TerminalUI.printBoxLine("  in the Kepler sector and burn long-range propulsion towards Earth.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        String confirm = TerminalUI.readLine("  Confirm return trajectory burn to Earth? [Y/N] > ");
        if (!confirm.equalsIgnoreCase("Y") && !confirm.equalsIgnoreCase("YES")) {
            System.out.println();
            TerminalUI.printWarning("Return burn aborted. Standing by at Kepler-186f.");
            TerminalUI.sleep(300);
            return false;
        }

        // Automatic emergency refueling check if fuel < 20 but player has Fuel Cells
        if (ship.getFuel() < 20 && inv.getQuantity(Inventory.FUEL_CELLS) > 0) {
            int neededCells = (int) Math.ceil((20.0 - ship.getFuel()) / 20.0);
            int availableCells = inv.getQuantity(Inventory.FUEL_CELLS);
            int cellsToUse = Math.min(neededCells, availableCells);

            TerminalUI.clearScreen();
            TerminalUI.printHeader("EMERGENCY PROPULSION RESTORATION");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  Current fuel (" + ship.getFuel() + "%) is insufficient for return burn (20 Fuel).");
            TerminalUI.printBoxLine("  Cargo manifest contains " + availableCells + " Fuel Cell(s).");
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  Transfer " + cellsToUse + " Fuel Cell(s) to propulsion drive? [Y/N] > ");
            String useCells = TerminalUI.readLine("  > ");
            if (useCells.equalsIgnoreCase("Y") || useCells.equalsIgnoreCase("YES")) {
                inv.remove(Inventory.FUEL_CELLS, cellsToUse);
                ship.restoreFuel(cellsToUse * 20);
                gameState.getMissionLog().addEntry("INVENTORY", "Emergency fuel cells transferred: +" + (cellsToUse * 20) + " Fuel.");
            }
        }

        // Determine Ending A vs Ending B
        boolean hasArtifact = inv.getQuantity(Inventory.ALIEN_ARTIFACTS) >= 1;
        boolean operational = ship.isOperational();
        boolean sufficientFuel = ship.getFuel() >= 20;

        if (hasArtifact && operational && sufficientFuel) {
            ship.consumeFuel(20);
            executeEndingA(ship, player);
        } else {
            String reason;
            if (!operational) {
                reason = "Spacecraft sustained catastrophic structural casualty during transit.";
            } else if (!sufficientFuel) {
                reason = "Propulsion fuel depleted in interstellar void (Available: " + ship.getFuel() + "%, Required: 20%).";
            } else {
                reason = "Vessel lost telemetry contact during deep-space transit.";
            }
            executeEndingB(ship, player, reason);
        }
        return true;
    }

    private boolean confirmAndExecuteUnknownEnding(Spaceship ship, Player player) {
        TerminalUI.clearScreen();
        TerminalUI.printHeader("CONFIRM FLIGHT DIRECTIVE // STEP INTO THE UNKNOWN");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightMagenta("[!] ATTENTION: TRANSCENDENCE PROTOCOL INITIALIZATION"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Commander, choosing to remain and enter the alien gateway is permanent.");
        TerminalUI.printBoxLine("  You will leave vessel ODYSSEY-07 and human civilization behind,");
        TerminalUI.printBoxLine("  stepping across the galactic threshold into uncharted existence.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        String confirm = TerminalUI.readLine("  Confirm decision to step into the alien gateway? [Y/N] > ");
        if (!confirm.equalsIgnoreCase("Y") && !confirm.equalsIgnoreCase("YES")) {
            System.out.println();
            TerminalUI.printWarning("Transcendence protocol cancelled. Standing by at Kepler-186f.");
            TerminalUI.sleep(300);
            return false;
        }

        executeEndingC(ship, player);
        return true;
    }

    private void executeEndingA(Spaceship ship, Player player) {
        String endingTitle = "Ending A: Successful Return";
        gameState.setCompletedEnding(endingTitle);
        Planet earth = gameState.getPlanetByName("Earth");
        if (earth != null) {
            gameState.setCurrentLocation(earth);
        }
        gameState.getMissionLog().addEntry("MISSION", "CAMPAIGN CONCLUDED: Ending A - Successful Return. Vessel returned to Earth with Alien Artifact.");
        gameState.updateMissionObjectives();

        try {
            SaveManager.saveGame(gameState);
        } catch (Exception ignored) {}

        TerminalUI.clearScreen();
        TerminalUI.printHeader("LONG-RANGE PROPULSION BURN // SOL TRAJECTORY");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Igniting primary sub-light ion engines (-20 FUEL)... " + TerminalUI.brightGreen("[ OK ]"));
        TerminalUI.printBoxLine("  Locking navigation homing beacon on Sol-Earth... " + TerminalUI.brightGreen("[ OK ]"));
        TerminalUI.printBoxLine("  Alien Artifact containment stasis fields active... " + TerminalUI.brightGreen("[ OK ]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        TerminalUI.renderProgressAnimation("TRANSITING INTERSTELLAR SPACE TO EARTH", 500);
        TerminalUI.sleep(300);

        TerminalUI.clearScreen();
        TerminalUI.printHeaderWithSubtitle(
            "MISSION CONCLUDED // ENDING A: SUCCESSFUL RETURN",
            "SPACECRAFT " + player.getSpaceshipName() + " // COMMANDER " + player.getCommanderName().toUpperCase()
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("=================================================================="));
        TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("         VICTORY: HUMANITY'S FIRST INTERSTELLAR TRIUMPH          "));
        TerminalUI.printCenteredBoxLine(TerminalUI.brightGreen("=================================================================="));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("NARRATIVE SUMMARY:"));
        TerminalUI.printBoxLine("  After weeks of silent cruising through the dark gulf between star");
        TerminalUI.printBoxLine("  systems, ODYSSEY-07 decelerates into Earth's high orbital dock.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Planetary sensors erupt with celebratory telemetry as communication");
        TerminalUI.printBoxLine("  channels flood with praise from Mission High Command. The mysterious");
        TerminalUI.printBoxLine("  Alien Artifact is safely delivered into the custody of the Global");
        TerminalUI.printBoxLine("  Scientific Coalition.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Its deciphered quantum networks reveal blueprints for deep-space");
        TerminalUI.printBoxLine("  propulsion, energy synthesis, and the coordinates of distant civilizations.");
        TerminalUI.printBoxLine("  Humanity is propelled into an interstellar golden age.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Commander " + TerminalUI.brightYellow(player.getCommanderName().toUpperCase()) + " is forever honored as the voyager who");
        TerminalUI.printBoxLine("  ventured beyond the unknown and brought back the future of humankind.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("view final mission summary");
        renderFinalMissionSummary(endingTitle);
    }

    private void executeEndingB(Spaceship ship, Player player, String reason) {
        String endingTitle = "Ending B: Lost in Space";
        gameState.setCompletedEnding(endingTitle);
        gameState.getMissionLog().addEntry("MISSION", "CAMPAIGN CONCLUDED: Ending B - Lost in Space. " + reason);
        gameState.updateMissionObjectives();

        try {
            SaveManager.saveGame(gameState);
        } catch (Exception ignored) {}

        TerminalUI.clearScreen();
        TerminalUI.printHeader("PROPULSION WARNING // CRITICAL SYSTEM COMPROMISE");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Attempting long-range transit burn... " + TerminalUI.brightRed("[ FAILED ]"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightRed(reason));
        TerminalUI.printBoxLine("  Broadcasting automated emergency beacon... " + TerminalUI.brightYellow("[ TRANSMITTING ]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        TerminalUI.renderProgressAnimation("DRIFTING INTO INTERSTELLAR SECTOR", 500);
        TerminalUI.sleep(300);

        TerminalUI.clearScreen();
        TerminalUI.printHeaderWithSubtitle(
            "MISSION CONCLUDED // ENDING B: LOST IN SPACE",
            "SPACECRAFT " + player.getSpaceshipName() + " // COMMANDER " + player.getCommanderName().toUpperCase()
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printCenteredBoxLine(TerminalUI.brightRed("=================================================================="));
        TerminalUI.printCenteredBoxLine(TerminalUI.brightRed("               OUTCOME: LOST AMONG THE SILENT STARS              "));
        TerminalUI.printCenteredBoxLine(TerminalUI.brightRed("=================================================================="));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("NARRATIVE SUMMARY:"));
        TerminalUI.printBoxLine("  Though the investigation of Kepler-186f succeeded and the alien");
        TerminalUI.printBoxLine("  origin was uncovered, the perilous voyage took an unforgiving toll.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + reason);
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  As propulsion fires flicker out, ODYSSEY-07 slips into the silent");
        TerminalUI.printBoxLine("  expanse between the solar system and Kepler's red dwarf star.");
        TerminalUI.printBoxLine("  The Alien Artifact rests securely in its stasis chamber, untouched.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Centuries may pass before another human exploration fleet retraces your");
        TerminalUI.printBoxLine("  trajectory. Until then, your emergency beacon continues its lonely");
        TerminalUI.printBoxLine("  call into the dark, commemorating Commander " + TerminalUI.brightYellow(player.getCommanderName().toUpperCase()) + "'s bravery.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("view final mission summary");
        renderFinalMissionSummary(endingTitle);
    }

    private void executeEndingC(Spaceship ship, Player player) {
        String endingTitle = "Ending C: The Unknown";
        gameState.setCompletedEnding(endingTitle);
        gameState.getMissionLog().addEntry("MISSION", "CAMPAIGN CONCLUDED: Ending C - The Unknown. Commander entered the alien gateway on Kepler-186f.");
        gameState.updateMissionObjectives();

        try {
            SaveManager.saveGame(gameState);
        } catch (Exception ignored) {}

        TerminalUI.clearScreen();
        TerminalUI.printHeader("GATEWAY CONVERGENCE // TRANSCENDENCE PROTOCOL");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Coupling Alien Artifact to monolith dais... " + TerminalUI.brightGreen("[ HARMONIC LOCK ]"));
        TerminalUI.printBoxLine("  Sub-space horizon expanding... " + TerminalUI.brightMagenta("[ GATEWAY OPEN ]"));
        TerminalUI.printBoxLine("  Commander stepping beyond event boundary... " + TerminalUI.brightCyan("[ TRANSCENDING ]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        TerminalUI.renderProgressAnimation("TRANSCENDING KNOWN SPACETIME DIMENSIONS", 500);
        TerminalUI.sleep(300);

        TerminalUI.clearScreen();
        TerminalUI.printHeaderWithSubtitle(
            "MISSION CONCLUDED // ENDING C: THE UNKNOWN",
            "SPACECRAFT " + player.getSpaceshipName() + " // COMMANDER " + player.getCommanderName().toUpperCase()
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printCenteredBoxLine(TerminalUI.brightMagenta("=================================================================="));
        TerminalUI.printCenteredBoxLine(TerminalUI.brightMagenta("           TRANSCENDENCE: STEPPING BEYOND THE UNKNOWN             "));
        TerminalUI.printCenteredBoxLine(TerminalUI.brightMagenta("=================================================================="));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("NARRATIVE SUMMARY:"));
        TerminalUI.printBoxLine("  Transmitting a final farewell telemetry stream to Earth Command,");
        TerminalUI.printBoxLine("  Commander " + TerminalUI.brightYellow(player.getCommanderName().toUpperCase()) + " inserts the Alien Artifact into the spire.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  The megastructure's core pulses with impossible light. The basalt");
        TerminalUI.printBoxLine("  walls dissolve into a tapestry of nebulae, quantum strings, and");
        TerminalUI.printBoxLine("  timelines of civilizations spanning billions of years.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Choosing curiosity over the safety of home, the Commander steps");
        TerminalUI.printBoxLine("  across the threshold. Physical form gives way to pure consciousness,");
        TerminalUI.printBoxLine("  joining the cosmic entity that first whispered across the stars.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Back on the surface of Kepler-186f, vessel ODYSSEY-07 rests in eternal");
        TerminalUI.printBoxLine("  silent orbit—a beacon marking where humanity first touched the infinite.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("view final mission summary");
        renderFinalMissionSummary(endingTitle);
    }

    private void renderFinalMissionSummary(String endingTitle) {
        TerminalUI.clearScreen();
        Player player = gameState.getPlayer();
        Spaceship ship = gameState.getSpaceship();
        Inventory inv = gameState.getInventory();
        List<MissionObjective> objs = gameState.getObjectives();

        TerminalUI.printHeaderWithSubtitle(
            "ORBIT: BEYOND THE UNKNOWN // FINAL MISSION REPORT",
            "CAMPAIGN EVALUATION // OFFICIAL DEBRIEFING"
        );
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("COMMANDING OFFICER : ") + TerminalUI.brightYellow(player.getCommanderName().toUpperCase()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("VESSEL DESIGNATION : ") + TerminalUI.brightCyan(player.getSpaceshipName()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("CAMPAIGN OUTCOME   : ") + TerminalUI.brightGreen(endingTitle.toUpperCase()));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("TOTAL COMPLETION   : ") + TerminalUI.brightGreen("100% [ALL DIRECTIVES RESOLVED]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("FINAL SPACECRAFT VITALS:"));
        TerminalUI.printBoxLine("  * Fuel Reserves    : " + ship.getFuel() + "%");
        TerminalUI.printBoxLine("  * Life Support O2  : " + ship.getOxygen() + "%");
        TerminalUI.printBoxLine("  * Reactor Energy   : " + ship.getEnergy() + "%");
        TerminalUI.printBoxLine("  * Hull Integrity   : " + ship.getHullIntegrity() + "%");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("RECOVERED DISCOVERIES & CARGO MANIFEST:"));
        for (String item : Inventory.VALID_ITEMS) {
            int qty = inv.getQuantity(item);
            TerminalUI.printBoxLine("  * " + String.format("%-18s", item) + " : " + (qty > 0 ? TerminalUI.brightGreen(qty + " units") : TerminalUI.gray("0 units")));
        }
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("CHARTED CELESTIAL BODIES:"));
        for (Planet p : gameState.getPlanets()) {
            String badge = p.isExplored()
                    ? TerminalUI.brightGreen("[SURFACE EXPLORED]")
                    : (p.isDiscovered() ? TerminalUI.brightCyan("[ORBIT CHARTED]   ") : TerminalUI.gray("[UNEXPLORED]     "));
            TerminalUI.printBoxLine("  * " + String.format("%-14s", p.getName()) + " : " + badge + " " + p.getDescription());
        }
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("COMPLETED CAMPAIGN DIRECTIVES:"));
        for (MissionObjective o : objs) {
            String mark = o.isCompleted() ? TerminalUI.brightGreen("[V] COMPLETED") : TerminalUI.brightYellow("[>] IN PROGRESS");
            TerminalUI.printBoxLine("  * " + mark + " : " + o.getTitle() + " (" + o.getStage() + ")");
        }
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue("return to Main Deck");
    }

    private void showCompletedCampaignReviewScreen(GameState state) {
        boolean reviewing = true;
        while (reviewing && running) {
            TerminalUI.clearScreen();
            TerminalUI.printHeaderWithSubtitle(
                "MISSION DEBRIEFING // CAMPAIGN COMPLETED",
                "VESSEL " + state.getPlayer().getSpaceshipName() + " // COMMANDER " + state.getPlayer().getCommanderName().toUpperCase()
            );
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("CAMPAIGN STATUS : ") + TerminalUI.brightGreen("[CAMPAIGN CONCLUDED]"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("ACHIEVED ENDING : ") + TerminalUI.brightYellow(state.getCompletedEnding()));
            TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("FINAL PROGRESS  : ") + TerminalUI.brightCyan("100% // ALL DIRECTIVES RESOLVED"));
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printInnerBorder();
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("DEBRIEFING OPTIONS:"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[1] ") + TerminalUI.brightWhite("VIEW FINAL MISSION SUMMARY") + TerminalUI.gray(" - Review ship vitals & expedition stats"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[2] ") + TerminalUI.brightWhite("REVIEW FLIGHT MISSION LOG ") + TerminalUI.gray(" - Read complete flight recorder chronicle"));
            TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("[3] ") + TerminalUI.brightWhite("RETURN TO MAIN MENU       ") + TerminalUI.gray(" - Back to main title deck"));
            TerminalUI.printEmptyBoxLine();
            TerminalUI.printDoubleBorder();
            System.out.println();

            int choice = TerminalUI.readMenuOption("  DEBRIEFING COMMAND > ", 1, 3);
            if (choice == -1) {
                running = false;
                break;
            }

            switch (choice) {
                case 1 -> renderFinalMissionSummary(state.getCompletedEnding());
                case 2 -> handleMissionLog();
                case 3 -> reviewing = false;
            }
        }
    }

    /**
     * Handles [3] HOW TO PLAY option.
     */
    private void handleHowToPlay() {
        TerminalUI.clearScreen();
        TerminalUI.printHeader("ORBIT: SPACECRAFT FLIGHT MANUAL & BRIEFING");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("PRIMARY DIRECTIVE:"));
        TerminalUI.printBoxLine("  As Commander of ODYSSEY-07, your mission is to investigate an");
        TerminalUI.printBoxLine("  unidentified deep-space transmission origin across uncharted sectors.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("SPACECRAFT VITALS:"));
        TerminalUI.printBoxLine("  * " + TerminalUI.brightWhite("FUEL") + "     : Consumed when traveling between planets and moons.");
        TerminalUI.printBoxLine("  * " + TerminalUI.brightWhite("OXYGEN") + "   : Critical life support. Steadily depletes over turns.");
        TerminalUI.printBoxLine("  * " + TerminalUI.brightWhite("ENERGY") + "   : Powers shields, deep sensors, and exploratory probes.");
        TerminalUI.printBoxLine("  * " + TerminalUI.brightWhite("HULL") + "     : Structural integrity. Protects against hazards & debris.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("FLIGHT OPERATIONS:"));
        TerminalUI.printBoxLine("  * " + TerminalUI.white("Turn-Based Exploration: Each decision advances mission time."));
        TerminalUI.printBoxLine("  * " + TerminalUI.white("Interplanetary Travel : Navigate between Earth, Mars, and beyond."));
        TerminalUI.printBoxLine("  * " + TerminalUI.white("Unforeseen Hazards    : Encounter derelict stations, storms & signals."));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("TERMINAL CONTROLS:"));
        TerminalUI.printBoxLine("  * Enter the numerical command corresponding to menu choices.");
        TerminalUI.printBoxLine("  * Press [ENTER] to execute commands. Invalid inputs are rejected safely.");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue();
    }

    /**
     * Handles [4] CREDITS option.
     */
    private void handleCredits() {
        TerminalUI.clearScreen();
        TerminalUI.printHeader("MISSION LOG // DEVELOPMENT CREDITS");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("PROJECT          : ") + TerminalUI.brightCyan("ORBIT: Beyond the Unknown"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("VERSION          : ") + TerminalUI.brightGreen("0.8.0 (Phase 8: Campaign Completion & Multiple Endings)"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("ARCHITECTURE     : ") + TerminalUI.brightWhite("Core Java Terminal UI (TUI)"));
        TerminalUI.printBoxLine("  " + TerminalUI.brightWhite("DEPENDENCIES     : ") + TerminalUI.brightWhite("Zero External Dependencies"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.brightCyan("DEVELOPMENT & DESIGN:"));
        TerminalUI.printBoxLine("  * Core Architecture     : Object-Oriented Java 17+ Engine");
        TerminalUI.printBoxLine("  * Terminal Rendering    : Custom ANSI & Monospaced ASCII Framework");
        TerminalUI.printBoxLine("  * Sci-Fi Conceptual Art : ORBIT Flight Control Systems");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.gray("(C) 2026 ORBIT Development Team. All rights reserved."));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();

        TerminalUI.pressEnterToContinue();
    }

    /**
     * Handles [5] EXIT option with clean shutdown.
     */
    private void handleExit() {
        TerminalUI.clearScreen();
        TerminalUI.printHeader("SPACECRAFT ODYSSEY-07 // SYSTEM POWER DOWN");
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  " + TerminalUI.yellow("INITIATING SHUTDOWN PROTOCOL..."));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printBoxLine("  Disconnecting secure terminal uplink... " + TerminalUI.brightGreen("[ OK ]"));
        TerminalUI.printBoxLine("  Flushing volatile telemetry registers... " + TerminalUI.brightGreen("[ OK ]"));
        TerminalUI.printBoxLine("  Switching to auxiliary battery standby... " + TerminalUI.brightGreen("[ OK ]"));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printInnerBorder();
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printCenteredBoxLine(TerminalUI.brightCyan("TERMINAL OFFLINE. SAFE TRAVELS, COMMANDER."));
        TerminalUI.printEmptyBoxLine();
        TerminalUI.printDoubleBorder();
        System.out.println();

        TerminalUI.sleep(300);
        this.running = false;
    }
}
