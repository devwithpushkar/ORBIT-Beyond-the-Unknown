package orbit.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Design system and visual styling constants for the ORBIT desktop application.
 * Defines the unified sci-fi aesthetic: deep navy/charcoal surfaces, electric cyan accents,
 * crisp monospaced telemetry readouts, and muted status indicators.
 */
public final class OrbitTheme {

    private OrbitTheme() {}

    // --- Color Palette: Deep Space Surfaces ---
    public static final Color BG_VOID         = new Color(0x06, 0x09, 0x13); // Deepest viewport void
    public static final Color BG_PANEL        = new Color(0x0C, 0x13, 0x22); // Core panel surface
    public static final Color BG_CARD         = new Color(0x12, 0x1C, 0x30); // Raised card surface
    public static final Color BG_CARD_HOVER   = new Color(0x1A, 0x27, 0x42); // Interactive hover state
    public static final Color BG_CARD_PRESSED = new Color(0x0E, 0x17, 0x28); // Clicked state
    public static final Color BG_INPUT        = new Color(0x09, 0x0E, 0x1A); // Text input background

    // --- Color Palette: Accents & Energy ---
    public static final Color CYAN_ELECTRIC   = new Color(0x00, 0xE5, 0xFF); // Primary focal accent
    public static final Color CYAN_DIM        = new Color(0x02, 0x84, 0xC7); // Secondary cyan
    public static final Color BLUE_ACCENT     = new Color(0x3B, 0x82, 0xF6); // Electric blue
    public static final Color BLUE_DIM        = new Color(0x1D, 0x4E, 0xD8); // Deep electric blue

    // --- Color Palette: Status & Avionics ---
    public static final Color STATUS_NOMINAL  = new Color(0x10, 0xB9, 0x81); // Muted operational emerald
    public static final Color STATUS_WARNING  = new Color(0xF5, 0x9E, 0x0B); // Amber caution
    public static final Color STATUS_ALERT    = new Color(0xEF, 0x44, 0x44); // Critical alert red

    // --- Color Palette: Borders & Dividers ---
    public static final Color BORDER_SUBTLE   = new Color(0x1E, 0x2E, 0x48); // Standard panel border
    public static final Color BORDER_BRIGHT   = new Color(0x00, 0xBA, 0xD6); // Active/focused border
    public static final Color BORDER_MUTED    = new Color(0x15, 0x21, 0x36); // Inner divider line

    // --- Color Palette: Typography ---
    public static final Color TEXT_PRIMARY    = new Color(0xF8, 0xFA, 0xFC); // Pure crisp white
    public static final Color TEXT_SECONDARY  = new Color(0x94, 0xA3, 0xB8); // Interface slate
    public static final Color TEXT_MUTED      = new Color(0x64, 0x74, 0x8B); // Low-contrast telemetry
    public static final Color TEXT_CYAN       = new Color(0x38, 0xBD, 0xF8); // Cyan highlights
    public static final Color TEXT_DISABLED   = new Color(0x47, 0x55, 0x69); // Inactive state

    // --- Typography Hierarchy ---
    public static final Font FONT_TITLE       = resolveFont("Consolas", Font.BOLD, 38, Font.MONOSPACED);
    public static final Font FONT_SUBTITLE    = resolveFont("Segoe UI", Font.BOLD, 13, Font.SANS_SERIF);
    public static final Font FONT_HEADER      = resolveFont("Segoe UI", Font.BOLD, 16, Font.SANS_SERIF);
    public static final Font FONT_HEADER_MONO = resolveFont("Consolas", Font.BOLD, 16, Font.MONOSPACED);
    public static final Font FONT_BODY        = resolveFont("Segoe UI", Font.PLAIN, 13, Font.SANS_SERIF);
    public static final Font FONT_BODY_BOLD   = resolveFont("Segoe UI", Font.BOLD, 13, Font.SANS_SERIF);
    public static final Font FONT_MONO        = resolveFont("Consolas", Font.PLAIN, 13, Font.MONOSPACED);
    public static final Font FONT_MONO_BOLD   = resolveFont("Consolas", Font.BOLD, 13, Font.MONOSPACED);
    public static final Font FONT_TELEMETRY   = resolveFont("Consolas", Font.PLAIN, 12, Font.MONOSPACED);
    public static final Font FONT_TINY_MONO   = resolveFont("Consolas", Font.BOLD, 10, Font.MONOSPACED);

    private static Font resolveFont(String family, int style, int size, String fallbackFamily) {
        Font f = new Font(family, style, size);
        if (f.getFamily().equalsIgnoreCase("Dialog") && !family.equalsIgnoreCase("Dialog")) {
            return new Font(fallbackFamily, style, size);
        }
        return f;
    }

    /**
     * Enables high-quality rendering hints for smooth shapes and crisp text rendering.
     */
    public static void enableAntiAliasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * Creates a standardized sci-fi panel border with subtle padding.
     */
    public static Border createPanelBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER_SUBTLE, 1),
                new EmptyBorder(12, 16, 12, 16)
        );
    }

    /**
     * Creates a card border with custom inset padding.
     */
    public static Border createCardBorder(int top, int left, int bottom, int right) {
        return new CompoundBorder(
                new LineBorder(BORDER_SUBTLE, 1),
                new EmptyBorder(top, left, bottom, right)
        );
    }

    /**
     * Resolves status indicator color based on a percentage value (0 to 100).
     */
    public static Color getResourceStatusColor(int percentage) {
        if (percentage > 50) return STATUS_NOMINAL;
        if (percentage >= 25) return STATUS_WARNING;
        return STATUS_ALERT;
    }
}
