package orbit;

import orbit.ui.TerminalUI;

/**
 * Main application entry point for ORBIT: Beyond the Unknown.
 * Initializes the runtime environment, starts the game lifecycle,
 * and handles top-level unexpected exceptions gracefully.
 */
public class Main {

    public static void main(String[] args) {
        // Register shutdown hook for clean terminal reset if interrupted (e.g., Ctrl+C)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print(TerminalUI.RESET);
            System.out.flush();
        }));

        try {
            Game game = new Game();
            game.start();
        } catch (Exception e) {
            System.err.println();
            TerminalUI.printError("A critical unexpected system error occurred: " + e.getMessage());
            System.err.println("  Trace details: " + e.getClass().getName());
            System.exit(1);
        }
    }
}
