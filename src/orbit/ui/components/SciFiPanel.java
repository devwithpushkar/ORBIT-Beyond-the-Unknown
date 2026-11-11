package orbit.ui.components;

import orbit.ui.OrbitTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Tactical container panel featuring sci-fi HUD styling, optional header banner,
 * subtle cyan accent notches, and container padding.
 */
public class SciFiPanel extends JPanel {

    private String title;
    private String subtitle;
    private Color accentColor = OrbitTheme.CYAN_ELECTRIC;

    public SciFiPanel() {
        this(null, null);
    }

    public SciFiPanel(String title) {
        this(title, null);
    }

    public SciFiPanel(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
        setOpaque(false);
        setLayout(new BorderLayout());

        int topPad = (title != null) ? 38 : 16;
        setBorder(new EmptyBorder(topPad, 16, 16, 16));
    }

    public void setTitle(String title) {
        this.title = title;
        int topPad = (title != null) ? 38 : 16;
        setBorder(new EmptyBorder(topPad, 16, 16, 16));
        repaint();
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        repaint();
    }

    public void setAccentColor(Color accentColor) {
        this.accentColor = accentColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        OrbitTheme.enableAntiAliasing(g2);

        int width = getWidth();
        int height = getHeight();

        // 1. Fill panel surface
        g2.setColor(OrbitTheme.BG_PANEL);
        g2.fillRect(0, 0, width, height);

        // 2. Draw subtle border
        g2.setColor(OrbitTheme.BORDER_SUBTLE);
        g2.drawRect(0, 0, width - 1, height - 1);

        // 3. Draw header bar if title is specified
        if (title != null && !title.isEmpty()) {
            int headerHeight = 30;

            // Header background
            g2.setColor(OrbitTheme.BG_CARD);
            g2.fillRect(1, 1, width - 2, headerHeight);

            // Left accent bar
            g2.setColor(accentColor);
            g2.fillRect(1, 1, 4, headerHeight);

            // Title text
            g2.setFont(OrbitTheme.FONT_HEADER_MONO);
            g2.setColor(OrbitTheme.TEXT_PRIMARY);
            FontMetrics fm = g2.getFontMetrics();
            int titleY = (headerHeight - fm.getHeight()) / 2 + fm.getAscent() + 1;
            g2.drawString(title.toUpperCase(), 14, titleY);

            // Subtitle text if present
            if (subtitle != null && !subtitle.isEmpty()) {
                g2.setFont(OrbitTheme.FONT_TINY_MONO);
                g2.setColor(OrbitTheme.TEXT_MUTED);
                FontMetrics sfm = g2.getFontMetrics();
                int subX = width - sfm.stringWidth(subtitle) - 14;
                g2.drawString(subtitle.toUpperCase(), subX, titleY);
            }

            // Divider under header
            g2.setColor(OrbitTheme.BORDER_MUTED);
            g2.drawLine(1, headerHeight + 1, width - 2, headerHeight + 1);
        }

        // 4. Subtle corner accent ticks
        g2.setColor(OrbitTheme.BORDER_SUBTLE.brighter());
        int tick = 6;
        g2.drawLine(0, 0, tick, 0);
        g2.drawLine(0, 0, 0, tick);
        g2.drawLine(width - 1, 0, width - 1 - tick, 0);
        g2.drawLine(width - 1, 0, width - 1, tick);
        g2.drawLine(0, height - 1, tick, height - 1);
        g2.drawLine(0, height - 1, 0, height - 1 - tick);
        g2.drawLine(width - 1, height - 1, width - 1 - tick, height - 1);
        g2.drawLine(width - 1, height - 1, width - 1, height - 1 - tick);

        g2.dispose();
    }
}

