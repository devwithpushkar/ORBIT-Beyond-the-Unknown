package orbit.ui.components;

import orbit.ui.OrbitTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Custom-styled sci-fi button component featuring smooth hover transitions,
 * glowing accent borders, tactical bracket iconography, and crisp typography.
 */
public class SciFiButton extends JButton {

    public enum Variant {
        PRIMARY,   // Electric cyan border and highlights
        SECONDARY, // Subdued slate border for routine operations
        DANGER     // Alert red highlights for irreversible actions
    }

    private final Variant variant;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public SciFiButton(String text) {
        this(text, Variant.SECONDARY);
    }

    public SciFiButton(String text, Variant variant) {
        super(text);
        this.variant = (variant != null) ? variant : Variant.SECONDARY;

        setFont(OrbitTheme.FONT_MONO_BOLD);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(10, 20, 10, 20));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled() && SwingUtilities.isLeftMouseButton(e)) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width = Math.max(size.width + 32, 160);
        size.height = Math.max(size.height + 12, 42);
        return size;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        OrbitTheme.enableAntiAliasing(g2);

        int width = getWidth();
        int height = getHeight();

        // 1. Determine surface color based on state
        Color bgColor;
        Color borderColor;
        Color textColor;

        if (!isEnabled()) {
            bgColor = OrbitTheme.BG_VOID;
            borderColor = OrbitTheme.BORDER_MUTED;
            textColor = OrbitTheme.TEXT_DISABLED;
        } else if (isPressed) {
            bgColor = OrbitTheme.BG_CARD_PRESSED;
            borderColor = resolveAccentColor();
            textColor = OrbitTheme.TEXT_PRIMARY;
        } else if (isHovered) {
            bgColor = OrbitTheme.BG_CARD_HOVER;
            borderColor = resolveAccentColor();
            textColor = (variant == Variant.PRIMARY) ? OrbitTheme.CYAN_ELECTRIC : OrbitTheme.TEXT_PRIMARY;
        } else {
            bgColor = (variant == Variant.PRIMARY) ? OrbitTheme.BG_CARD : OrbitTheme.BG_PANEL;
            borderColor = (variant == Variant.PRIMARY) ? OrbitTheme.BORDER_BRIGHT : OrbitTheme.BORDER_SUBTLE;
            textColor = (variant == Variant.PRIMARY) ? OrbitTheme.CYAN_ELECTRIC : OrbitTheme.TEXT_SECONDARY;
        }

        // 2. Draw background pill/box
        int arc = 6;
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, width - 1, height - 1, arc, arc);

        // 3. Draw border
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(isHovered && isEnabled() ? 1.5f : 1.0f));
        g2.drawRoundRect(0, 0, width - 1, height - 1, arc, arc);

        // 4. Draw tactical corner tick marks on hover
        if (isHovered && isEnabled()) {
            g2.setColor(resolveAccentColor());
            int tick = 5;
            // Top-left
            g2.drawLine(2, 2, 2 + tick, 2);
            g2.drawLine(2, 2, 2, 2 + tick);
            // Bottom-right
            g2.drawLine(width - 3, height - 3, width - 3 - tick, height - 3);
            g2.drawLine(width - 3, height - 3, width - 3, height - 3 - tick);
        }

        // 5. Draw text
        g2.setColor(textColor);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textX = (width - fm.stringWidth(getText())) / 2;
        int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }

    private Color resolveAccentColor() {
        return switch (variant) {
            case PRIMARY -> OrbitTheme.CYAN_ELECTRIC;
            case DANGER -> OrbitTheme.STATUS_ALERT;
            case SECONDARY -> OrbitTheme.BLUE_ACCENT;
        };
    }
}

