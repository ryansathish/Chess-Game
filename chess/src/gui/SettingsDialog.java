package gui;

import javax.swing.*;
import java.awt.*;

/**
 * A settings dialog that lets players customize the board appearance.
 *
 * <p>This fulfills <b>Extra GUI Feature 2</b>: Settings Window for Customization.
 * Players can choose board color themes and board size, with live preview.</p>
 */
public class SettingsDialog extends JDialog {

    /** The board panel to apply settings to. */
    private final ChessBoardPanel boardPanel;

    /** The main frame (used for layout updates). */
    private final JFrame parentFrame;

    /** Combo boxes for settings selections. */
    private JComboBox<String> themeCombo;
    private JComboBox<String> sizeCombo;

    /** Board theme color pairs [light, dark]. */
    private static final Color[][] THEMES = {
        { new Color(240, 217, 181), new Color(181, 136, 99) },  // Classic Wood
        { new Color(235, 235, 210), new Color(119, 149, 86) },  // Green Classic
        { new Color(220, 220, 220), new Color(100, 100, 100) }, // Modern Gray
        { new Color(210, 230, 255), new Color(70, 100, 160) },  // Blue Ocean
        { new Color(255, 230, 200), new Color(160, 80, 80) },   // Red Walnut
        { new Color(255, 255, 220), new Color(180, 160, 60) },  // Golden
    };

    private static final String[] THEME_NAMES = {
        "Classic Wood", "Green Classic", "Modern Gray",
        "Blue Ocean", "Red Walnut", "Golden"
    };

    private static final int[] SIZES = { 60, 70, 80, 90, 100 };
    private static final String[] SIZE_NAMES = { "Small (60px)", "Medium-Small (70px)",
        "Medium (80px)", "Large (90px)", "Extra Large (100px)" };

    /**
     * Constructs the settings dialog.
     *
     * @param parent     the parent JFrame
     * @param boardPanel the board panel to apply settings to
     */
    public SettingsDialog(JFrame parent, ChessBoardPanel boardPanel) {
        super(parent, "Settings", true);
        this.boardPanel = boardPanel;
        this.parentFrame = parent;

        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        JLabel title = new JLabel("Board Settings");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        content.add(title, gbc);
        gbc.gridwidth = 1;

        // Theme selector
        gbc.gridx = 0; gbc.gridy = 1;
        content.add(new JLabel("Board Theme:"), gbc);
        themeCombo = new JComboBox<>(THEME_NAMES);
        themeCombo.setPreferredSize(new Dimension(180, 28));
        gbc.gridx = 1;
        content.add(themeCombo, gbc);

        // Size selector
        gbc.gridx = 0; gbc.gridy = 2;
        content.add(new JLabel("Board Size:"), gbc);
        sizeCombo = new JComboBox<>(SIZE_NAMES);
        sizeCombo.setSelectedIndex(2); // default: 80px
        sizeCombo.setPreferredSize(new Dimension(180, 28));
        gbc.gridx = 1;
        content.add(sizeCombo, gbc);

        add(content, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton applyBtn = new JButton("Apply");
        JButton cancelBtn = new JButton("Cancel");

        applyBtn.setBackground(new Color(70, 130, 180));
        applyBtn.setForeground(Color.WHITE);
        applyBtn.setFocusPainted(false);

        applyBtn.addActionListener(e -> applySettings());
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(cancelBtn);
        btnPanel.add(applyBtn);
        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Applies the selected settings to the board panel.
     */
    private void applySettings() {
        int themeIdx = themeCombo.getSelectedIndex();
        boardPanel.setBoardColors(THEMES[themeIdx][0], THEMES[themeIdx][1]);

        int sizeIdx = sizeCombo.getSelectedIndex();
        boardPanel.setSquareSize(SIZES[sizeIdx]);

        parentFrame.pack();
        dispose();
    }
}
