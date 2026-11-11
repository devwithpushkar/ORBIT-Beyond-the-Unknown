# ORBIT: Beyond the Unknown

> **Cinematic Sci-Fi Spacecraft Simulation & Planetary Exploration**  
> *Engineered in Java 21 / libGDX 1.14.2 (LWJGL3)*

---

## 1. Official Windows Desktop Release (v1.0.0)

ORBIT is distributed as a completely self-contained, standalone Windows x64 application. It includes a bundled lightweight Java runtime and all required game assets—**no external Java installation, development environment, or build tools are required**.

### Release Package Information
* **Package File**: `release/ORBIT-Windows-x64-v1.0.0.zip`
* **Unpacked Directory**: `build/distribution/ORBIT/`
* **Package Format**: Self-contained Windows x64 Application Image (ZIP archive, ~44.4 MB)
* **Executable**: `ORBIT.exe` (Native 64-bit Windows launcher)
* **Bundled Java Runtime**: OpenJDK 25 LTS (Microsoft build 25.0.3+9-LTS) modular runtime tailored via `jlink` (`runtime/`)
* **Target OS**: Windows 10 / Windows 11 (64-bit)
* **Display Output**: 1280x800 @ 60 FPS (OpenGL ES 2.0 / LWJGL3 hardware acceleration)

### How to Run the Packaged Game

1. **Extract Archive**: Extract `ORBIT-Windows-x64-v1.0.0.zip` to any folder on your computer.
2. **Launch Game**:
   * **Direct Executable**: Double-click `ORBIT.exe`.
   * **Batch Launcher**: Double-click `Launch-ORBIT.bat`.
   * **Console / Debug Launcher**: Run `Launch-Debug.bat` (outputs avionics telemetry and logs to a terminal window).
3. **Command-Line Arguments** (supported via `Launch-Debug.bat` or `ORBIT.exe`):
   * `--verify`: Executes the automated 11-step verification harness across all screens and campaign endings.
   * `--endinga`: Launches the Ending A (Successful Return) cinematic preview.
   * `--endingb`: Launches the Ending B (Lost in Space) cinematic preview.
   * `--endingc`: Launches the Ending C (The Unknown) cinematic preview.
   * `--nav`, `--cargo`, `--explore`, `--control`, `--log`, `--bridge`: Direct cinematic screen previews.

### Save Game Storage
* **Location**: `./saves/orbit_save.dat` (relative to the `ORBIT.exe` directory).
* **Format**: Deterministic human-readable INI telemetry format.
* **Continuity**: Preserves Commander designation, vessel vitals (Fuel, O2, Energy, Hull Integrity), planetary discovery/exploration status, cargo supplies, campaign stage, and the complete chronological flight recorder.

---

## 2. Game Overview & Features

In **ORBIT: Beyond the Unknown**, you take command of the deep-space reconnaissance vessel **ODYSSEY-07** to investigate an unidentified extraterrestrial transmission originating from the Kepler-186 system.

### Key Features
* **Cinematic 3D Environments**:
  * Rotating planetary spheres with multi-layer cloud dynamics and atmospheric rim shaders.
  * Multi-depth starfields with parallax drift.
  * Real-time 3D spacecraft maneuvering and engine exhaust particle glow.
  * Specialized 3D environments: Tactical Bridge, Planetary Navigation Cartography, Industrial Cargo Bay, Surface Reconnaissance, and the Kepler Alien Monolith.
* **Diegetic Sci-Fi Interface**:
  * Responsive Scene2D holographic flight decks with custom vector typography.
  * Live 4-resource telemetry instruments (Fuel, Oxygen, Energy, Hull Integrity) with critical threshold monitoring.
* **Authoritative Campaign Mechanics**:
  * **Stage 1 (Departure)**: Launch from Earth, decode high-command trajectory briefing, and execute the 20-fuel burn to Mars.
  * **Stage 2 (Deep Space Exploration)**: Journey to Europa and Titan, gather rare minerals, and detect the carrier signal.
  * **Stage 3 (Beyond the Unknown)**: Jump to extrasolar destination Kepler-186f and retrieve the Alien Artifact.
* **Three Narrative Endings**:
  * **Ending A — Successful Return**: Earth orbit homecoming, surviving crew, interstellar renaissance.
  * **Ending B — Lost in Space**: Adrift in the void, emergency distress beacon pulsing in darkness.
  * **Ending C — The Unknown**: Planetary spire transcendence into extraterrestrial consciousness.

---

## 3. Technology Stack & Architecture

* **Engine**: libGDX 1.14.2 (LWJGL3 backend)
* **Graphics**: Hardware-accelerated OpenGL 3.3 / OpenGL ES 2.0 with custom GLSL ES 1.00 shaders
* **Audio**: Tactical avionics sound synthesis and multi-channel UI feedback via OpenAL
* **UI**: libGDX Scene2D with custom `CinematicTheme` components (`HoloButton`, `ShipResourceGauge`, `TelemetryReadout`)
* **Persistence**: Non-blocking deterministic text serialization via `SaveManager`

---

## 4. Building and Running from Source (Developer Workflow)

If you wish to compile or modify the application from source:

### Prerequisites
* JDK 21 or JDK 25 installed (`javac -version`).
* Git.

### Gradle Commands
From the project root:

```powershell
# Run the Desktop LWJGL3 game in development mode:
.\gradlew.bat runDesktop

# Compile production classes:
.\gradlew.bat classes

# Create unpacked application distribution (build/install/orbit):
.\gradlew.bat installDist

# Create distribution ZIP (build/distributions/orbit-1.0.0.zip):
.\gradlew.bat distZip
```

### Creating the Self-Contained Windows Release via `jpackage`
```powershell
# 1. Build application distribution
.\gradlew.bat installDist

# 2. Generate minimal JRE using jlink
jlink --add-modules java.base,java.desktop,java.logging,java.management,jdk.unsupported,java.naming,java.xml `
      --strip-debug --no-man-pages --no-header-files --output build/runtime

# 3. Package native Windows application image
jpackage --type app-image --name ORBIT --app-version 1.0.0 `
         --runtime-image build/runtime --input build/install/orbit/lib `
         --main-jar orbit-1.0.0.jar --main-class orbit.desktop.DesktopLauncher `
         --dest build/distribution

# 4. Copy assets & launcher scripts
Copy-Item -Recurse -Force assets build\distribution\ORBIT\assets
New-Item -ItemType Directory -Force build\distribution\ORBIT\saves
```

---

## 5. Verification & Quality Assurance

The standalone release has undergone end-to-end verification via the automated `Phase89Verifier` test suite:
1. **Clean Production Build**: Verified.
2. **Bundled Runtime Launch**: Verified (OpenJDK 25 LTS executing `ORBIT.exe` without system Java).
3. **Main Menu Initialization**: Verified (3D planetary scene, starfield, Scene2D interface).
4. **Expedition Commissioning**: Verified (Commander registration, starter loadout).
5. **Planetary Navigation & Mars Unlock**: Verified (Trajectory decryption briefing, 20 fuel transit burn, arrival insertion).
6. **Cargo Hold Deck**: Verified (5 inventory categories, consumption vitals restoration).
7. **Exploration Operations**: Verified (10 Energy + 5 Oxygen expedition cost, surface event handling).
8. **Mission Control & Mission Log**: Verified (Objective progress tracking, chronological audit trail).
9. **All Three Cinematic Endings**: Verified (Authoritative eligibility rules, 3D scenes, debriefing dialogs).
10. **Save / Load Persistence**: Verified (`saves/orbit_save.dat` integrity).

---

## 6. License & Credits

(C) 2026 ORBIT Development Team. Engineered for space exploration enthusiasts.
