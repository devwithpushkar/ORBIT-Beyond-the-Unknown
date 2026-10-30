package orbit.ui;

import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * TerminalUI is the central terminal rendering utility for ORBIT.
 * It provides ANSI-colored text, bordered panels, progress bars,
 * animated typewriter text, spinners, and safe user input handling.
 *
 * If ANSI escape sequences are not supported by the environment,
 * TerminalUI gracefully falls back to clean, plain text formatting.
 */
public class TerminalUI {

    // --- Standard ANSI Escape Codes ---
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";

    // Text Colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // High Intensity / Bright Text Colors
    public static final String BRIGHT_BLACK = "\u001B[90m"; // Gray
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_MAGENTA = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    // Layout Constants
    public static final int DEFAULT_WIDTH = 74;

    private static final Pattern ANSI_PATTERN = Pattern.compile("\\u001B\\[[;?0-9]*[a-zA-Z]");
    private static boolean ansiEnabled = detectAnsiSupport();
    private static Scanner scanner = new Scanner(System.in);

    /**
     * Determines if the current runtime terminal environment supports ANSI escape sequences.
     */
    private static boolean detectAnsiSupport() {
        // Respect NO_COLOR standard (https://no-color.org)
        if (System.getenv("NO_COLOR") != null) {
            return false;
        }

        String term = System.getenv("TERM");
        if (term != null && term.equalsIgnoreCase("dumb")) {
            return false;
        }

        // Windows 10 build 10586+ / Windows 11 Windows Terminal natively support ANSI.
        // Unix, Linux, macOS terminal emulators generally support ANSI.
        return true;
    }

    /**
     * Explicitly enable or disable ANSI color rendering.
     */
    public static void setAnsiEnabled(boolean enabled) {
        ansiEnabled = enabled;
    }

    public static boolean isAnsiEnabled() {
        return ansiEnabled;
    }

    /**
     * Configure a custom Scanner (useful for testing or redirection).
     */
    public static void setScanner(Scanner customScanner) {
        if (customScanner != null) {
            scanner = customScanner;
        }
    }

    public static Scanner getScanner() {
        return scanner;
    }

    // --- Color Utility Helpers ---

    public static String cyan(String text) {
        return colorize(CYAN, text);
    }

    public static String brightCyan(String text) {
        return colorize(BRIGHT_CYAN, text);
    }

    public static String blue(String text) {
        return colorize(BLUE, text);
    }

    public static String brightBlue(String text) {
        return colorize(BRIGHT_BLUE, text);
    }

    public static String green(String text) {
        return colorize(GREEN, text);
    }

    public static String brightGreen(String text) {
        return colorize(BRIGHT_GREEN, text);
    }

    public static String yellow(String text) {
        return colorize(YELLOW, text);
    }

    public static String brightYellow(String text) {
        return colorize(BRIGHT_YELLOW, text);
    }

    public static String red(String text) {
        return colorize(RED, text);
    }

    public static String brightRed(String text) {
        return colorize(BRIGHT_RED, text);
    }

    public static String white(String text) {
        return colorize(WHITE, text);
    }

    public static String brightWhite(String text) {
        return colorize(BRIGHT_WHITE, text);
    }

    public static String magenta(String text) {
        return colorize(MAGENTA, text);
    }

    public static String brightMagenta(String text) {
        return colorize(BRIGHT_MAGENTA, text);
    }

    public static String gray(String text) {
        return colorize(BRIGHT_BLACK, text);
    }

    public static String bold(String text) {
        return colorize(BOLD, text);
    }

    public static String dim(String text) {
        return colorize(DIM, text);
    }

    private static String colorize(String ansiCode, String text) {
        if (!ansiEnabled || text == null) {
            return text;
        }
        return ansiCode + text + RESET;
    }

    /**
     * Strips all ANSI escape codes from the provided text.
     */
    public static String stripAnsi(String text) {
        if (text == null) {
            return "";
        }
        return ANSI_PATTERN.matcher(text).replaceAll("");
    }

    /**
     * Returns the printable/visible character length of the text,
     * excluding any ANSI escape codes.
     */
    public static int visibleLength(String text) {
        return stripAnsi(text).length();
    }

    // --- Screen Manipulation ---

    /**
     * Clears the terminal screen using standard ANSI sequences with fallback.
     */
    public static void clearScreen() {
        if (ansiEnabled) {
            System.out.print("\033[H\033[2J\033[3J");
            System.out.flush();
        } else {
            // Non-ANSI fallback: print blank lines
            for (int i = 0; i < 35; i++) {
                System.out.println();
            }
        }
    }

    // --- Centering & Alignment ---

    /**
     * Centers text within the specified width, taking ANSI escape codes into account.
     */
    public static String center(String text, int width) {
        if (text == null) {
            return "";
        }
        int vLen = visibleLength(text);
        if (vLen >= width) {
            return text;
        }
        int totalPadding = width - vLen;
        int left = totalPadding / 2;
        int right = totalPadding - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    public static String center(String text) {
        return center(text, DEFAULT_WIDTH);
    }

    public static void printCentered(String text, int width) {
        System.out.println(center(text, width));
    }

    public static void printCentered(String text) {
        printCentered(text, DEFAULT_WIDTH);
    }

    // --- Borders and Panel Rendering ---

    public static void printDoubleBorder() {
        System.out.println(brightCyan("+=" + "=".repeat(DEFAULT_WIDTH - 4) + "=+"));
    }

    public static void printSingleBorder() {
        System.out.println(blue("+-" + "-".repeat(DEFAULT_WIDTH - 4) + "-+"));
    }

    public static void printInnerBorder() {
        System.out.println(gray("+-" + "-".repeat(DEFAULT_WIDTH - 4) + "-+"));
    }

    public static void printBoxLine(String text) {
        int contentWidth = DEFAULT_WIDTH - 4;
        int vLen = visibleLength(text);
        if (vLen > contentWidth) {
            System.out.println(brightCyan("| ") + text + brightCyan(" |"));
        } else {
            int padding = contentWidth - vLen;
            System.out.println(brightCyan("| ") + text + " ".repeat(padding) + brightCyan(" |"));
        }
    }

    public static void printCenteredBoxLine(String text) {
        int contentWidth = DEFAULT_WIDTH - 4;
        String centeredText = center(text, contentWidth);
        System.out.println(brightCyan("| ") + centeredText + brightCyan(" |"));
    }

    public static void printEmptyBoxLine() {
        System.out.println(brightCyan("| ") + " ".repeat(DEFAULT_WIDTH - 4) + brightCyan(" |"));
    }

    /**
     * Prints a stylized sci-fi panel header.
     */
    public static void printHeader(String title) {
        printDoubleBorder();
        printCenteredBoxLine(brightWhite(bold(title)));
        printDoubleBorder();
    }

    /**
     * Prints a dual-line panel header with a main title and subtitle.
     */
    public static void printHeaderWithSubtitle(String title, String subtitle) {
        printDoubleBorder();
        printCenteredBoxLine(brightWhite(bold(title)));
        printCenteredBoxLine(brightCyan(subtitle));
        printDoubleBorder();
    }

    // --- Status Messages ---

    public static void printSuccess(String message) {
        System.out.println("  " + brightGreen("[+] SUCCESS: ") + brightWhite(message));
    }

    public static void printWarning(String message) {
        System.out.println("  " + brightYellow("[!] WARNING: ") + yellow(message));
    }

    public static void printError(String message) {
        System.out.println("  " + brightRed("[-] ERROR:   ") + red(message));
    }

    public static void printInfo(String message) {
        System.out.println("  " + brightCyan("[i] SYSTEM:  ") + cyan(message));
    }

    public static void printSubsystemStatus(String subsystem, String status) {
        int pad = 24 - visibleLength(subsystem);
        if (pad < 1) pad = 1;
        String dots = ".".repeat(pad);
        System.out.println("  " + brightWhite(subsystem) + " " + gray(dots) + " " + brightGreen("[" + status + "]"));
    }

    // --- Progress & Animation Utilities ---

    /**
     * Formats an ASCII progress bar string.
     */
    public static String formatProgressBar(int current, int max, int barWidth) {
        int safeMax = (max <= 0) ? 1 : max;
        int clampedVal = Math.max(0, Math.min(current, safeMax));
        double ratio = (double) clampedVal / safeMax;
        int filled = (int) Math.round(ratio * barWidth);
        int empty = barWidth - filled;
        int percent = (int) Math.round(ratio * 100);

        String fillStr = "#".repeat(filled);
        String emptyStr = "-".repeat(empty);

        String color;
        if (percent == 0) {
            color = RED;
        } else if (percent <= 25) {
            color = RED;
        } else if (percent <= 50) {
            color = YELLOW;
        } else {
            color = GREEN;
        }
        String coloredFill = colorize(color, fillStr);

        return "[" + coloredFill + gray(emptyStr) + "] " + brightWhite(String.format("%3d%%", percent));
    }

    /**
     * Formats a resource status badge according to the standard thresholds:
     * Above 50%: [NORMAL] (Green)
     * 26-50%:   [CAUTION] (Yellow)
     * 1-25%:    [CRITICAL] (Red)
     * 0%:       [DEPLETED] (Red)
     */
    public static String formatResourceStatus(int value) {
        if (value == 0) {
            return brightRed("[DEPLETED]");
        } else if (value <= 25) {
            return brightRed("[CRITICAL]");
        } else if (value <= 50) {
            return brightYellow("[CAUTION]");
        } else {
            return brightGreen("[NORMAL]");
        }
    }

    /**
     * Renders a short animated progress bar sequence in-place.
     */
    public static void renderProgressAnimation(String label, int durationMs) {
        int steps = 20;
        int stepDuration = Math.max(10, durationMs / steps);
        int barWidth = 24;

        for (int i = 0; i <= steps; i++) {
            int percent = (i * 100) / steps;
            int filled = (i * barWidth) / steps;
            int empty = barWidth - filled;

            String fillStr = "#".repeat(filled);
            String emptyStr = "-".repeat(empty);

            System.out.print("\r  " + brightCyan(label) + " [" + green(fillStr) + gray(emptyStr) + "] "
                    + brightWhite(String.format("%3d%%", percent)));
            System.out.flush();
            sleep(stepDuration);
        }
        System.out.println(" " + brightGreen("[ OK ]"));
    }

    /**
     * Renders a short rotating spinner animation.
     */
    public static void renderSpinner(String message, int durationMs) {
        char[] spinner = {'|', '/', '-', '\\'};
        int interval = 70;
        int cycles = Math.max(1, durationMs / interval);

        for (int i = 0; i < cycles; i++) {
            System.out.print("\r  " + brightCyan(message) + " " + brightYellow("[" + spinner[i % spinner.length] + "]"));
            System.out.flush();
            sleep(interval);
        }
        System.out.print("\r  " + brightCyan(message) + " " + brightGreen("[ OK ]") + "       \n");
    }

    /**
     * Outputs text character by character with a configurable delay.
     */
    public static void typewrite(String text, int delayMs) {
        if (text == null) return;
        for (char c : text.toCharArray()) {
            System.out.print(c);
            System.out.flush();
            sleep(delayMs);
        }
        System.out.println();
    }

    /**
     * Safely pause the thread for specified milliseconds.
     */
    public static void sleep(int ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // --- User Input Utilities ---

    /**
     * Prompts the user and reads a trimmed line of text.
     */
    public static String readLine(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        try {
            if (scanner.hasNextLine()) {
                return scanner.nextLine().trim();
            }
        } catch (Exception e) {
            // EOF or scanner error
        }
        return "";
    }

    /**
     * Prompts the user repeatedly until a valid integer between min and max is provided.
     * Returns -1 if input stream is closed.
     */
    public static int readMenuOption(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            System.out.flush();

            String line;
            try {
                if (scanner.hasNextLine()) {
                    line = scanner.nextLine().trim();
                } else {
                    return -1; // EOF reached
                }
            } catch (Exception e) {
                return -1;
            }

            if (line.isEmpty()) {
                continue;
            }

            try {
                int choice = Integer.parseInt(line);
                if (choice >= min && choice <= max) {
                    return choice;
                } else {
                    printWarning("Input out of range. Please enter an option from [" + min + "] to [" + max + "].");
                }
            } catch (NumberFormatException e) {
                printWarning("Invalid command syntax. Please enter a numerical option [" + min + "-" + max + "].");
            }
        }
    }

    /**
     * Pauses until the user presses Enter, displaying a default return prompt.
     */
    public static void pressEnterToContinue() {
        pressEnterToContinue("return to Main Menu");
    }

    /**
     * Pauses until the user presses Enter with a specified action/destination prompt.
     */
    public static void pressEnterToContinue(String destination) {
        System.out.println();
        System.out.print("  " + brightCyan("Press [ENTER] to " + destination + "..."));
        System.out.flush();
        try {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        } catch (Exception e) {
            // Ignore
        }
        System.out.println();
    }
}
