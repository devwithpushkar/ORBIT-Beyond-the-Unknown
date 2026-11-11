package orbit.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import orbit.controllers.OrbitController;
import orbit.render.OrbitGame;

/**
 * DesktopLauncher boots the hardware-accelerated LWJGL3 libGDX desktop client for ORBIT.
 */
public class DesktopLauncher {

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            if ("verify".equalsIgnoreCase(args[0]) || "--verify".equalsIgnoreCase(args[0])) {
                System.setProperty("orbit.verify", "true");
            } else if ("endinga".equalsIgnoreCase(args[0]) || "--endinga".equalsIgnoreCase(args[0])) {
                System.setProperty("orbit.launchEndingA", "true");
            } else if ("endingb".equalsIgnoreCase(args[0]) || "--endingb".equalsIgnoreCase(args[0])) {
                System.setProperty("orbit.launchEndingB", "true");
            } else if ("endingc".equalsIgnoreCase(args[0]) || "--endingc".equalsIgnoreCase(args[0])) {
                System.setProperty("orbit.launchEndingC", "true");
            } else if ("control".equalsIgnoreCase(args[0]) || "missioncontrol".equalsIgnoreCase(args[0])) {
                System.setProperty("orbit.launchMissionControl", "true");
            } else if ("log".equalsIgnoreCase(args[0]) || "missionlog".equalsIgnoreCase(args[0])) {
                System.setProperty("orbit.launchMissionLog", "true");
            } else if ("explore".equalsIgnoreCase(args[0]) || "exploration".equalsIgnoreCase(args[0])) {
                System.setProperty("orbit.launchExploration", "true");
            } else if ("cargo".equalsIgnoreCase(args[0])) {
                System.setProperty("orbit.launchCargo", "true");
            } else if ("bridge".equalsIgnoreCase(args[0])) {
                System.setProperty("orbit.launchBridge", "true");
            } else if ("nav".equalsIgnoreCase(args[0]) || "navigation".equalsIgnoreCase(args[0])) {
                System.setProperty("orbit.launchNav", "true");
            }
        }

        System.out.println("[ORBIT-LAUNCHER] Initializing LWJGL3 Application Configuration (1280x800 @ 60 FPS)...");
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("ORBIT: Beyond the Unknown // Cinematic Spacecraft Simulation");
        config.setWindowedMode(1280, 800);
        config.setWindowSizeLimits(1024, 680, -1, -1);
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0); // Fast hardware backbuffer without MSAA stall

        System.out.println("[ORBIT-LAUNCHER] Starting Lwjgl3Application window and game loop...");
        OrbitController controller = new OrbitController();
        new Lwjgl3Application(new OrbitGame(controller), config);
        System.out.println("[ORBIT-LAUNCHER] Application loop exited cleanly.");
    }
}

