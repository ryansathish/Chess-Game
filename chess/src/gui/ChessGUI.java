package gui;

import ai.ChessAI;
import board.Board;
import pieces.Piece;
import utils.Position;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The main application window for the Phase 2 Chess GUI.
 *
 * <p>Integrates the chessboard panel, move history panel, menu bar,
 * and coordinates between all components. Implements:</p>
 * <ul>
 *   <li><b>Feature 1 – Menu Bar:</b> New Game, Save Game, Load Game, Settings.</li>
 *   <li><b>Feature 2 – Settings:</b> Board color themes and size.</li>
 *   <li><b>Feature 3 – Move History + Undo:</b> Full move log with undo support.</li>
 * </ul>
 */
public class ChessGUI extends JFrame {

    /** The board rendering panel. */
    private ChessBoardPanel boardPanel;

    /** The move history and undo side panel. */
    private MoveHistoryPanel historyPanel;

    /** Stack of game states for undo functionality. */
    private Deque<GameState> undoStack = new ArrayDeque<>();

    /** Whether the game is currently active (not over). */
    private boolean gameActive = true;

    /** The AI engine; null = human vs human mode. */
    private ChessAI chessAI = null;

    /** Menu item for toggling AI mode (stored so we can update its label). */
    private JMenuItem aiToggleItem;

    /**
     * Constructs and displays the main Chess GUI window.
     */
    public ChessGUI() {
        super("Chess – Phase 3  [Human vs Human]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        // Board panel
        boardPanel = new ChessBoardPanel();

        // History panel
        historyPanel = new MoveHistoryPanel();

        // Wire up move listener
        boardPanel.setMoveListener(new ChessBoardPanel.MoveListener() {
            @Override
            public void onMove(Piece piece, Position from, Position to,
                               Piece captured, Piece.Color turn) {
                // Save state BEFORE this move for undo
                // (we saved it just before the move — see preMove hook below)
                historyPanel.recordMove(piece, from, to, captured, turn);
            }

            @Override
            public void onGameOver(Piece.Color winner) {
                gameActive = false;
            }
        });

        // We need to hook into the board to save pre-move state.
        // We accomplish this by overriding via a wrapper approach:
        // ChessBoardPanel calls preMove before executing; we register here.
        boardPanel.setPreMoveHook(() -> {
            // Snapshot before every move for undo
            GameState state = new GameState(
                    boardPanel.getBoard(),
                    boardPanel.getCurrentTurn(),
                    null, null, null, null);
            undoStack.push(state);
        });

        // Wire undo
        historyPanel.setUndoListener(() -> performUndo());

        // Layout
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
        add(historyPanel, BorderLayout.EAST);

        // Menu bar
        setJMenuBar(buildMenuBar());

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    /**
     * Builds the application menu bar with Game and View menus.
     *
     * @return the constructed JMenuBar
     */
    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // ── Game menu ──────────────────────────────────────────────────────
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setMnemonic('G');

        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        newGameItem.addActionListener(e -> newGame());

        // AI Mode submenu
        JMenu aiMenu = new JMenu("AI Opponent");
        ButtonGroup aiGroup = new ButtonGroup();

        JRadioButtonMenuItem humanItem = new JRadioButtonMenuItem("Human vs Human", true);
        JRadioButtonMenuItem easyItem  = new JRadioButtonMenuItem("Easy (depth 2)");
        JRadioButtonMenuItem medItem   = new JRadioButtonMenuItem("Medium (depth 3)");
        JRadioButtonMenuItem hardItem  = new JRadioButtonMenuItem("Hard (depth 4)");

        aiGroup.add(humanItem); aiGroup.add(easyItem);
        aiGroup.add(medItem);   aiGroup.add(hardItem);

        humanItem.addActionListener(e -> setAIMode(null,   0));
        easyItem .addActionListener(e -> setAIMode(Piece.Color.BLACK, 2));
        medItem  .addActionListener(e -> setAIMode(Piece.Color.BLACK, 3));
        hardItem .addActionListener(e -> setAIMode(Piece.Color.BLACK, 4));

        aiMenu.add(humanItem); aiMenu.add(easyItem);
        aiMenu.add(medItem);   aiMenu.add(hardItem);

        JMenuItem saveItem = new JMenuItem("Save Game");
        saveItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        saveItem.addActionListener(e -> saveGame());

        JMenuItem loadItem = new JMenuItem("Load Game");
        saveItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        loadItem.addActionListener(e -> loadGame());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(aiMenu);
        gameMenu.addSeparator();
        gameMenu.add(saveItem);
        gameMenu.add(loadItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        menuBar.add(gameMenu);

        // ── View menu ──────────────────────────────────────────────────────
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');

        JMenuItem settingsItem = new JMenuItem("Settings…");
        settingsItem.setAccelerator(KeyStroke.getKeyStroke("ctrl COMMA"));
        settingsItem.addActionListener(e -> openSettings());

        viewMenu.add(settingsItem);
        menuBar.add(viewMenu);

        // ── Help menu ──────────────────────────────────────────────────────
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Chess – Phase 3\n\nFully integrated GUI chess game with AI opponent.\n\n"
                + "How to play:\n"
                + "  • Click a piece then click its destination, OR\n"
                + "  • Drag a piece to its destination.\n\n"
                + "AI Opponent:\n"
                + "  • Game → AI Opponent → Easy / Medium / Hard\n"
                + "  • You play White; AI plays Black.\n"
                + "  • AI uses Minimax with Alpha-Beta Pruning.\n\n"
                + "Extra features:\n"
                + "  • Move history with undo (right panel)\n"
                + "  • Board customization (View → Settings)\n"
                + "  • Save & Load game (Game menu)",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        return menuBar;
    }

    /**
     * Activates or deactivates the AI opponent.
     *
     * @param color the color for the AI to play, or null for human vs human
     * @param depth the minimax search depth
     */
    private void setAIMode(Piece.Color color, int depth) {
        if (color == null) {
            chessAI = null;
            boardPanel.setChessAI(null);
            setTitle("Chess – Phase 3  [Human vs Human]");
        } else {
            chessAI = new ChessAI(color, depth);
            boardPanel.setChessAI(chessAI);
            String label = (depth == 2) ? "Easy" : (depth == 3) ? "Medium" : "Hard";
            setTitle("Chess – Phase 3  [You: White  vs  AI: Black  (" + label + ")]");
        }
        newGame();
    }

    /**
     * Starts a new game, resetting the board and history.
     */
    private void newGame() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Start a new game? Current progress will be lost.",
                "New Game", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        boardPanel.resetBoard();
        boardPanel.setChessAI(chessAI); // re-apply AI after reset
        historyPanel.reset();
        undoStack.clear();
        gameActive = true;
    }

    /**
     * Opens a file chooser to save the current game state to disk.
     */
    private void saveGame() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Game");
        chooser.setFileFilter(new FileNameExtensionFilter("Chess Save (*.chess)", "chess"));
        chooser.setSelectedFile(new File("mygame.chess"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().endsWith(".chess")) {
            file = new File(file.getAbsolutePath() + ".chess");
        }

        try {
            GameState state = new GameState(
                    boardPanel.getBoard(),
                    boardPanel.getCurrentTurn(),
                    null, null, null, null);
            state.saveToFile(file);
            JOptionPane.showMessageDialog(this,
                    "Game saved to:\n" + file.getAbsolutePath(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save game:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens a file chooser to load a previously saved game from disk.
     */
    private void loadGame() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load Game");
        chooser.setFileFilter(new FileNameExtensionFilter("Chess Save (*.chess)", "chess"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try {
            GameState state = GameState.loadFromFile(file);
            Board restoredBoard = state.restoreBoard();
            Piece.Color turn = state.restoreTurn();

            boardPanel.loadBoard(restoredBoard, turn);
            historyPanel.reset();
            undoStack.clear();
            gameActive = true;

            JOptionPane.showMessageDialog(this,
                    "Game loaded successfully!",
                    "Loaded", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load game:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens the settings dialog for board customization.
     */
    private void openSettings() {
        SettingsDialog dialog = new SettingsDialog(this, boardPanel);
        dialog.setVisible(true);
    }

    /**
     * Performs an undo operation, reverting the board to the previous state.
     */
    private void performUndo() {
        if (undoStack.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nothing to undo!", "Undo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        GameState prev = undoStack.pop();
        Board restoredBoard = prev.restoreBoard();
        Piece.Color turn = prev.restoreTurn();

        boardPanel.loadBoard(restoredBoard, turn);
        historyPanel.undoLastMove();

        // If a capture was undone, remove from captured list
        if (prev.captured != null) {
            // The turn stored is before the move, so the capturer was the opposite
            Piece.Color capturer = (turn == Piece.Color.WHITE)
                    ? Piece.Color.BLACK : Piece.Color.WHITE;
            historyPanel.removeLastCapture(capturer);
        }

        gameActive = true; // Allow play to resume after undoing a game-over
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new ChessGUI();
        });
    }
}
