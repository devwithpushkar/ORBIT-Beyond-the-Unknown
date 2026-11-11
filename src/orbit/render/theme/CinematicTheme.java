package orbit.render.theme;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;

/**
 * CinematicTheme defines the visual design tokens, color palette, typography hierarchy,
 * spacing metrics, and animation easing standards for ORBIT's cinematic frontend.
 *
 * Design Pillars:
 * - Grounded, diegetic spacecraft interfaces (tactile instruments, recessed panels, physical bezels).
 * - Warm cinematic amber highlights balanced with cool calibrated avionics cyan.
 * - Deep multi-layered space void backplanes.
 * - Restrained, high-legibility telemetry typography.
 */
public final class CinematicTheme {

    private CinematicTheme() {}

    // --- 1. Chassis & Space Void Palette ---
    public static final Color VOID_BLACK       = new Color(0.024f, 0.035f, 0.055f, 1.0f);   // Deep cosmic background (#06090e)
    public static final Color CHASSIS_PLATE    = new Color(0.047f, 0.067f, 0.098f, 0.94f);  // Physical avionics frame (#0c1119)
    public static final Color PANEL_GLASS      = new Color(0.063f, 0.094f, 0.145f, 0.84f);  // Semi-translucent glass (#101825)
    public static final Color BEZEL_EDGE       = new Color(0.125f, 0.180f, 0.255f, 0.70f);  // Metallic chassis edge line
    public static final Color BEZEL_HIGHLIGHT  = new Color(0.220f, 0.320f, 0.440f, 0.85f);  // Top specular reflection line

    // --- 2. Cinematic Warmth & Telemetry Instrument Accents ---
    public static final Color AMBER_WARM       = new Color(0.965f, 0.655f, 0.145f, 1.0f);   // Warm flight vector amber (#f6a725)
    public static final Color AMBER_DIM        = new Color(0.550f, 0.360f, 0.080f, 0.75f);  // Inactive warm amber
    public static final Color SOLAR_GOLD       = new Color(0.940f, 0.760f, 0.260f, 1.0f);   // Fusion energy / core (#f0c242)
    public static final Color CYAN_AVIONICS    = new Color(0.180f, 0.850f, 1.000f, 1.0f);   // Cool HUD projection cyan (#2ed9ff)
    public static final Color CYAN_DIM         = new Color(0.080f, 0.420f, 0.520f, 0.65f);  // Telemetry grid cyan
    public static final Color STEEL_BLUE       = new Color(0.450f, 0.600f, 0.760f, 1.0f);   // Armor / Hull frame (#7399c2)
    public static final Color OXYGEN_AQUA      = new Color(0.150f, 0.900f, 0.850f, 1.0f);   // Life support / O2 (#26e6d9)

    // --- 3. Telemetry Status Indicators ---
    public static final Color STATUS_NOMINAL   = new Color(0.180f, 0.840f, 0.450f, 1.0f);   // All systems nominal (#2ed673)
    public static final Color STATUS_CAUTION   = new Color(0.980f, 0.720f, 0.120f, 1.0f);   // Caution threshold (#fab81f)
    public static final Color STATUS_CRITICAL  = new Color(0.940f, 0.220f, 0.270f, 1.0f);   // Emergency alert breach (#f03845)

    // --- 4. High-Contrast Typography Tones ---
    public static final Color TEXT_PRIMARY     = new Color(0.940f, 0.960f, 0.980f, 1.0f);   // Primary instrument readout off-white
    public static final Color TEXT_SECONDARY   = new Color(0.720f, 0.780f, 0.860f, 0.90f);  // Secondary telemetry label
    public static final Color TEXT_MUTED       = new Color(0.440f, 0.520f, 0.620f, 0.75f);  // Micro-telemetry stencil
    public static final Color TEXT_DIM         = new Color(0.280f, 0.340f, 0.420f, 0.55f);  // Inactive annotation

    // --- 5. Animation Timing & Easing Standards ---
    public static final float DURATION_INSTANT       = 0.08f;  // Click / press response
    public static final float DURATION_HOVER         = 0.14f;  // Button hover & pip illumination
    public static final float DURATION_INTERACT      = 0.22f;  // Dialog show / hide
    public static final float DURATION_PANEL_SLIDE   = 0.38f;  // Instrument drawer toggle
    public static final float DURATION_SCREEN_FADE   = 0.75f;  // Cinematic screen transitions
    public static final float DURATION_STROBE        = 0.80f;  // Caution / alert warning strobe period

    public static final Interpolation EASING_DECEL   = Interpolation.pow2Out; // Decelerating entry
    public static final Interpolation EASING_HOVER   = Interpolation.sine;    // Smooth hover lerp
    public static final Interpolation EASING_SMOOTH  = Interpolation.fade;    // Fade transitions

    // --- 6. Component Sizing & Layout Metrics ---
    public static final float CORNER_BEVEL_RADIUS    = 6.0f;
    public static final float BORDER_WIDTH_THIN      = 1.0f;
    public static final float BORDER_WIDTH_HEAVY     = 2.0f;
    public static final float FLIGHT_SWITCH_HEIGHT   = 52.0f;
    public static final float FLIGHT_SWITCH_WIDTH    = 380.0f;
    public static final float GAUGE_HEIGHT_SLIM      = 22.0f;
    public static final float GAUGE_HEIGHT_STANDARD  = 34.0f;
}

