package orbit.ui.components;

import orbit.ui.OrbitTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Custom dark-mode sci-fi dialog for system notifications, settings, and credits,
 * eliminating default operating system grey modal dialogs.
 */
public class SciFiDialog extends JDialog {

    private int selectedOptionIndex = -1;
    public static boolean AUTO_DISMISS_TEST_MODE = false;

    public SciFiDialog(Window owner, String title, String heading, String message, String... buttonLabels) {
        this(owner, title, heading, message, false, buttonLabels);
    }

    public SciFiDialog(Window owner, String title, String heading, String message, boolean isConfirm, String... buttonLabels) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(OrbitTheme.BG_PANEL);
        root.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrbitTheme.CYAN_ELECTRIC, 1),
                new EmptyBorder(20, 24, 20, 24)
        ));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel headingLabel = new JLabel(heading.toUpperCase());
        headingLabel.setFont(OrbitTheme.FONT_HEADER_MONO);
        headingLabel.setForeground(OrbitTheme.CYAN_ELECTRIC);
        headerPanel.add(headingLabel, BorderLayout.WEST);

        root.add(headerPanel, BorderLayout.NORTH);

        // Body message
        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(OrbitTheme.FONT_BODY);
        messageArea.setForeground(OrbitTheme.TEXT_PRIMARY);
        messageArea.setBackground(OrbitTheme.BG_PANEL);
        messageArea.setEditable(false);
        messageArea.setFocusable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(new EmptyBorder(8, 0, 8, 0));
        root.add(messageArea, BorderLayout.CENTER);

        // Actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        String[] labels = (buttonLabels != null && buttonLabels.length > 0) ? buttonLabels : new String[]{"DISMISS"};
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            String label = labels[i];
            SciFiButton.Variant variant = (i == 0 && isConfirm) ? SciFiButton.Variant.PRIMARY : SciFiButton.Variant.SECONDARY;
            SciFiButton btn = new SciFiButton(label, variant);
            btn.addActionListener(e -> {
                selectedOptionIndex = index;
                dispose();
            });
            buttonPanel.add(btn);
        }
        root.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setSize(Math.max(480, getWidth()), Math.max(200, getHeight()));
        setLocationRelativeTo(owner);
    }

    public static void showMessage(Window owner, String title, String heading, String message) {
        SciFiDialog dialog = new SciFiDialog(owner, title, heading, message, "ACKNOWLEDGE");
        if (AUTO_DISMISS_TEST_MODE) {
            dialog.dispose();
            return;
        }
        dialog.setVisible(true);
    }

    public static boolean showConfirm(Window owner, String title, String heading, String message) {
        return showConfirm(owner, title, heading, message, "CONFIRM", "CANCEL");
    }

    public static boolean showConfirm(Window owner, String title, String heading, String message, String confirmLabel, String cancelLabel) {
        SciFiDialog dialog = new SciFiDialog(owner, title, heading, message, true, confirmLabel, cancelLabel);
        if (AUTO_DISMISS_TEST_MODE) {
            dialog.selectedOptionIndex = 0;
            dialog.dispose();
            return true;
        }
        dialog.setVisible(true);
        return dialog.selectedOptionIndex == 0;
    }

    public static int showOptionDialog(Window owner, String title, String heading, String message, String... buttonLabels) {
        SciFiDialog dialog = new SciFiDialog(owner, title, heading, message, true, buttonLabels);
        if (AUTO_DISMISS_TEST_MODE) {
            dialog.selectedOptionIndex = 0;
            dialog.dispose();
            return 0;
        }
        dialog.setVisible(true);
        return dialog.selectedOptionIndex;
    }
}

