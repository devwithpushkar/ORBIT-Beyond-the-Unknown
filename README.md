# ORBIT: Beyond the Unknown

> **A Terminal-Based Space Exploration & Survival Adventure**  
> *Engineered in Pure Core Java (Java 17+)*

---

## 1. Project Overview

**ORBIT: Beyond the Unknown** is a text-driven space exploration and survival game where the player takes command of the spacecraft **ODYSSEY-07** to investigate an unidentified deep-space transmission originating from an uncharted region of the galaxy.

Unlike conventional console applications, ORBIT is built around an interactive terminal interface designed to evoke the aesthetic of an authentic spacecraft operating system, featuring:
* Monospaced ASCII spacecraft art and borders
* Multi-color ANSI terminal telemetry (cyan, blue, green, yellow, red, white, magenta)
* Animated diagnostics, loading bars, thruster burns, and subsystem boot-up sequences
* Robust input handling and resilient menu navigation
* Real-time spacecraft telemetry, vitals tracking, and interplanetary travel

---

## 2. Technology Constraints & Architecture

* **Language**: Java 17 or newer (Tested on OpenJDK 25 LTS)
* **Dependencies**: Core Java standard library only (zero external libraries)
* **Build System**: Pure `javac` / `java` (no Maven/Gradle required)
* **Platform Support**: Windows Terminal, PowerShell, CMD, Linux terminals, and macOS Terminal
* **Interface**: 100% Terminal-based UI (TUI) with automatic ANSI detection and fallback

---

## 3. Project Directory Structure

```text
Orbit/
├── src/
│   └── orbit/
│       ├── Main.java              # Application entry point & top-level exception handling
│       ├── Game.java              # Lifecycle controller, Navigation, Galaxy Map & Command Center
│       ├── models/
│       │   ├── Player.java        # Commander identity & vessel designation model
│       │   ├── Spaceship.java     # Vitals (Fuel, O2, Energy, Hull) with 25% thresholding & clamping
│       │   ├── Planet.java        # Planetary destination with discovery/exploration states
│       │   ├── Inventory.java     # Cargo hold management for 5 resource items
│       │   ├── ExplorationEvent.java # 12 planetary surface reconnaissance events across 4 categories
│       │   ├── MissionObjective.java # 12 campaign objectives across 3 narrative stages
│       │   ├── MissionLog.java    # Chronological flight recorder with audit trail and timestamps
│       │   └── GameState.java     # Active session state: player, ship, planets, location, inventory & objectives
│       └── ui/
│           └── TerminalUI.java    # Central ANSI/ASCII rendering, progress bars & input engine
├── assets/                        # Future ASCII art and static game assets (.gitkeep)
├── saves/                         # Local mission telemetry and save files (.gitkeep)
├── project.md                     # Comprehensive master game specification
├── README.md                      # Project documentation and quickstart guide
└── .gitignore                     # Git ignore rules for build artifacts and local saves
```

---

## 4. Current Status: Phase 6 (Mission Objectives, Campaign Progression & Mission Log)

### Phase 1: Project Foundation (Completed)
* [x] **Terminal UI Engine (`TerminalUI.java`)**: ANSI color palette, visible length centering, ASCII panels, progress animations, spinners, safe Scanner input handling.
* [x] **Atmospheric Startup Sequence**: Cold-boot firmware diagnostics, ORBIT ASCII art title, subsystem power-on diagnostics.
* [x] **Main Menu Loop**: Responsive options ([1] New Game, [2] Continue Game, [3] How to Play, [4] Credits, [5] Exit).

### Phase 2: Core Game Models & Command Center (Completed)
* [x] **Player Model (`Player.java`)**: Commander identity and default vessel (`ODYSSEY-07`).
* [x] **Spaceship Model (`Spaceship.java`)**: Core vitals (Fuel, O2, Energy, Hull), resource consumption, restoration, damage, and operational checks.
* [x] **Planet Model (`Planet.java`)**: Planetary telemetry, descriptions, and discovery/exploration status.
* [x] **Game State (`GameState.java`)**: Active session manager tracking Player, Spaceship, current location, and 5 celestial bodies.
* [x] **Command Center Dashboard (`Game.java`)**: Interactive command operations hub with real-time vitals and diagnostic telemetry.

### Phase 3: Navigation System & Galaxy Map (Completed)
* [x] **Scripted Mars Authorization Directive**: Earth starts as only discovered world; opening Navigation unlocks Mars via priority High-Command transmission.
* [x] **Interplanetary Navigation System (`Game.java`)**: Flight trajectory calculations, Earth ↔ Mars transit (20 fuel cost), departure and orbital insertion animations.
* [x] **Stellar Cartography & Galaxy Map (`Game.java`)**: Dynamic ASCII orbital map grid, sector catalog, and legend.
* [x] **Resource Thresholds (`Spaceship.java`, `TerminalUI.java`)**: Standardized 0% (Depleted), 1–25% (Critical), 26–50% (Caution), >50% (Normal).

### Phase 4: Cargo & Inventory Management System (Completed)
* [x] **Inventory Model (`src/orbit/models/Inventory.java`)**: Safe tracking of 5 supply categories (Metal, Energy Cells, Oxygen Canisters, Fuel Cells, Alien Artifacts), bounds checking, and restoration rules.
* [x] **GameState Integration (`GameState.java`)**: Starter supply loadout (2 Metal, 2 Energy Cells, 2 Oxygen Canisters, 2 Fuel Cells, 0 Artifacts) preserved across sessions.
* [x] **Interactive Cargo Hold Screen (`Game.java`)**: Manifest display, item consumption with validation and confirmation, technical specification details.

### Phase 5: Planetary Exploration & Random Event System (Completed)
* [x] **Exploration Interface (`Game.java`)**: Added `[5] EXPLORE PLANET` to Command Center; restricted to Earth and Mars.
* [x] **Expedition Mechanics**: 10 Energy, 5 Oxygen consumption, pre-launch confirmation, resource depletion protection, repeated surveys supported.
* [x] **Random Event System (`src/orbit/models/ExplorationEvent.java`)**: 12 predefined atmospheric events (Discovery, Resource, Hazard, Encounter) with safe vitals modification.
* [x] **Galaxy Map Integration**: Explored world indicators on ASCII map grid and planetary catalog.

### Phase 6: Mission Objectives, Campaign Progression & Mission Log (Completed)
* [x] **Mission Objective Model (`src/orbit/models/MissionObjective.java`)**:
  * Structured objective tracking with IDs, titles, descriptions, stages, statuses (`PENDING`, `IN_PROGRESS`, `COMPLETED`), and progress info.
  * 12 official campaign objectives from `project.md` across 3 stages:
    * **Stage 1 — Departure**: *Launch from Earth*, *Travel to Mars*, *Complete Initial Exploration*, *Collect Basic Resources*.
    * **Stage 2 — Deep Space Exploration**: *Explore Europa*, *Visit Titan*, *Collect Required Materials*, *Discover Unidentified Transmission*.
    * **Stage 3 — Beyond the Unknown**: *Travel to Kepler-186f*, *Investigate the Signal*, *Discover Signal Source*, *Make Final Mission Decision*.
* [x] **Campaign Progression & Dynamic Stage Advancement (`GameState.java`)**:
  * Evaluates gameplay actions automatically without manual player intervention.
  * Advances from **Stage 1** to **Stage 2** immediately upon completing all 4 Stage 1 requirements.
  * Automatically transitions Stage 2 objectives from `PENDING` to `IN_PROGRESS` on milestone completion.
  * Unimplemented destinations (Europa, Titan, Kepler-186f) remain properly locked / pending.
  * Dynamic overall mission progress percentage calculated: `(completed / 12) * 100%`.
* [x] **Chronological Flight Recorder (`src/orbit/models/MissionLog.java`)**:
  * Session-relative sequential timestamps (`LOG-001`, `LOG-002`, ...).
  * Color-coded category tags (`MISSION`, `NAVIGATION`, `DISCOVERY`, `RESOURCE`, `HAZARD`, `ENCOUNTER`, `SIGNAL`).
  * Deduplication guards against consecutive repeated logs and screen reopening.
  * Supports both chronological (oldest-first) and reverse chronological (newest-first) views.
* [x] **Command Center Integration & Terminal Dashboards (`Game.java`)**:
  * **Mission Objectives Dashboard (`[6]`)**: Displays stage title, overall progress bar, tactical directives (next mandatory action), active/completed/pending sections, and detailed stage drill-downs.
  * **Mission Log Screen (`[7]`)**: Paginated audit trail (8 entries per page), sort order toggling, and clean empty-state messaging.
  * **Gameplay Event Hooks**: Automatically logs commissioning, Mars discovery, orbital burn departures, arrival insertions, surface excursions, resource recoveries, hazards, signal detection, and stage advancement.
  * **Active Session Continuity**: Returning to Main Menu and selecting Continue Game preserves all objectives and flight logs.

### Phase 8: Save/Load, Campaign Completion & Multiple Endings (Completed)
* [x] **Persistent Save/Load System (`src/orbit/models/SaveManager.java`)**:
  * Core Java deterministic serialization to `saves/orbit_save.dat`.
  * Preserves commander, vessel, all 4 vitals, location, planets discovered/explored, cargo supplies, objectives, stage, decision flags, and complete mission log.
  * Overwrite protection with pre-save metadata summary confirmation.
  * Graceful handling of missing or corrupted save files.
* [x] **Full Campaign Progression (Stages 1–3)**:
  * **Stage 2 (Deep Space Exploration)**: Unlocks Europa and Titan; custom travel routes (25 Fuel each); outer planet exploration and material harvesting.
  * **Stage 3 (Beyond the Unknown)**: Unlocks extrasolar destination Kepler-186f (30 Fuel); surface reconnaissance investigates carrier frequency (1420.405 MHz); secures the Alien Artifact.
* [x] **Three Distinct Endings**:
  * **Ending A (Successful Return)**: Artifact secured, spacecraft operational and $\ge 20$ Fuel; returns home to Sol, launching humanity into an interstellar golden age.
  * **Ending B (Lost in Space)**: Artifact secured, but insufficient resources/fuel or spacecraft casualty; vessel drifts into the interstellar void with beacon active.
  * **Ending C (The Unknown)**: Commander enters the alien gateway on Kepler-186f; transcends human space-time to join the ancient creators.
  * Each ending features custom narrative sequences, debriefing summary, flight log entry, 100% mission completion, and prevention of repeated triggers.
* [x] **Seamless Session Continuity**:
  * Main Menu `[2] CONTINUE GAME` restores saved campaigns.
  * Completed campaigns offer full debriefing review (summary, flight log).
  * New games can be initialized cleanly anytime without corrupting archives.

---

## 5. How to Compile and Run

Ensure you have **Java 17** or newer installed (`java -version`, `javac -version`).

### From the Project Root (`Orbit/`)

#### Windows (PowerShell / Command Prompt):
```powershell
# 1. Compile all source files into bin directory
javac -d bin src/orbit/models/*.java src/orbit/ui/*.java src/orbit/*.java

# 2. Run the application
java -cp bin orbit.Main
```

Alternatively, direct recursive compilation:
```powershell
javac -d bin (Get-ChildItem -Recurse -Filter *.java src).FullName
java -cp bin orbit.Main
```

#### Linux / macOS (Bash / Zsh):
```bash
# 1. Compile
javac -d bin $(find src -name "*.java")

# 2. Run
java -cp bin orbit.Main
```

---

## 6. Development Roadmap

* **Phase 1: Project Foundation** (Complete) — Base architecture, Terminal UI framework, boot sequence, main menu navigation.
* **Phase 2: Core Game Models & Dashboard** (Complete) — Player, Spaceship, Planet, GameState, and Command Center dashboard.
* **Phase 3: Navigation System & Galaxy Map** (Complete) — Interplanetary travel (Earth ↔ Mars), Mars unlock briefing, ASCII galaxy map, 25% critical resource thresholding.
* **Phase 4: Cargo & Inventory Management** (Complete) — Inventory model, 5 supply categories, item consumption & vitals restoration, manifest view.
* **Phase 5: Planetary Exploration & Anomalies** (Complete) — Surface landing, 12 random events (Discovery, Resource, Hazard, Encounter), Galaxy Map explored tracking.
* **Phase 6: Mission Objectives & Mission Log** (Complete) — 12 campaign objectives, 3 stages, dynamic progression, chronological flight recorder, pagination.
* **Phase 7: Space Hazards & Encounters** (Complete) — Environmental hazard alerts across 4 vitals (Fuel, O2, Energy, Hull), interactive countermeasure deployment & cargo mitigation, operational casualty check & expedition inhibition, narrative tactical encounter choices, and zero-fictional-reward compliance.
* **Phase 8: Game Persistence & Multiple Endings** (Complete) — Core Java Save/load in `saves/`, Stage 2 & 3 campaign progression (Europa, Titan, Kepler-186f), signal source discovery & Alien Artifact, Endings A, B, and C with distinct narrative sequences, debriefing review screen.

---

## 7. License & Credits

(C) 2026 ORBIT Development Team. Built for terminal exploration enthusiasts.

