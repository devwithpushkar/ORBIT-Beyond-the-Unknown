package orbit.ui.components;

import orbit.ui.OrbitTheme;

import javax.swing.*;
import java.awt.*;

/**
 * Tactical telemetry gauge representing spacecraft vital resources
 * (Fuel, Oxygen, Energy, Hull Integrity). Renders status labels, live percentages,
 * and segmented glowing health/resource tracks.
 */
public class ResourceGauge extends JComponent {

    private final String label;
    private final String unit;
    private int value;
    private final int maxValue;

    public ResourceGauge(String label, int initialValue) {
        this(label, initialValue, 100, "%");
    }

    public ResourceGauge(String label, int initialValue, int maxValue, String unit) {
        this.label = label;
        this.value = Math.max(0, Math.min(maxValue, initialValue));
        this.maxValue = Math.max(1, maxValue);
        this.unit = (unit != null) ? unit : "";
        setOpaque(false);
    }

    public void setValue(int value) {
        this.value = Math.max(0, Math.min(maxValue, value));
        repaint();
    }

    public int getValue() {
        return value;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(240, 52);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(160, 48);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        OrbitTheme.enableAntiAliasing(g2);

        int width = getWidth();
        int height = getHeight();

        // 1. Label and percentage readout
        g2.setFont(OrbitTheme.FONT_TINY_MONO);
        g2.setColor(OrbitTheme.TEXT_SECONDARY);
        g2.drawString(label.toUpperCase(), 0, 14);

        int percentage = (int) Math.round(((double) value / maxValue) * 100);
        String valueStr = value + unit;
        Color statusColor = OrbitTheme.getResourceStatusColor(percentage);

        g2.setFont(OrbitTheme.FONT_MONO_BOLD);
        g2.setColor(statusColor);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(valueStr, width - fm.stringWidth(valueStr), 14);

        // 2. Bar track
        int barY = 22;
        int barHeight = 14;
        int barWidth = width;

        g2.setColor(OrbitTheme.BG_VOID);
        g2.fillRect(0, barY, barWidth, barHeight);

        g2.setColor(OrbitTheme.BORDER_MUTED);
        g2.drawRect(0, barY, barWidth - 1, barHeight - 1);

        // 3. Fill bar
        int fillWidth = (int) Math.round(((double) value / maxValue) * (barWidth - 4));
        if (fillWidth > 0) {
            // Fill background
            g2.setColor(statusColor);
            g2.fillRect(2, barY + 2, fillWidth, barHeight - 4);

            // Subtle top highlight sheen
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fillRect(2, barY + 2, fillWidth, 2);

            // Segmented tick lines across the bar
            g2.setColor(OrbitTheme.BG_VOID);
            int segmentStep = 12;
            for (int x = 2 + segmentStep; x < fillWidth; x += segmentStep) {
                g2.drawLine(x, barY + 2, x, barY + barHeight - 3);
            }
        }

        g2.dispose();
    }
}

