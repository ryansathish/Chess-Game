import gui.ChessGUI;
import javax.swing.SwingUtilities;

/**
 * Entry point for the Phase 2 Chess GUI application.
 */
public class Main {
    /**
     * Launches the chess GUI on the Swing event dispatch thread.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new ChessGUI();
        });
    }
}
