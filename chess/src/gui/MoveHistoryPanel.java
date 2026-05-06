package gui;

import pieces.Piece;
import utils.Position;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A side panel that displays the move history, captured pieces,
 * and provides an Undo button to revert the last move.
 *
 * <p>This panel fulfills <b>Extra GUI Feature 3</b>: Game History Panel with Undo Button.</p>
 */
public class MoveHistoryPanel extends JPanel {

    /** Text area displaying the move history. */
    private JTextArea historyArea;

    /** Labels showing captured pieces for each player. */
    private JLabel whiteCapturedLabel;
    private JLabel blackCapturedLabel;

    /** The Undo button. */
    private JButton undoButton;

    /** The status label showing whose turn it is. */
    private JLabel statusLabel;

    /** Listener for undo requests. */
    private UndoListener undoListener;

    /** Counter for full move numbers. */
    private int moveNumber = 1;
    private boolean whiteJustMoved = false;

    /** Captured pieces lists. */
    private List<String> whiteCaptured = new ArrayList<>();
    private List<String> blackCaptured = new ArrayList<>();

    /**
     * Constructs the MoveHistoryPanel with all sub-components initialized.
     */
    public MoveHistoryPanel() {
        setLayout(new BorderLayout(5, 5));
        setPreferredSize(new Dimension(220, 600));
        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));

        // Status label at the top
        statusLabel = new JLabel("White's Turn", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(240, 240, 240));
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(6, 4, 6, 4)));
        add(statusLabel, BorderLayout.NORTH);

        // Center: move history + captured pieces
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        // Move history text area
        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyArea.setBackground(new Color(252, 252, 252));
        historyArea.setMargin(new Insets(4, 6, 4, 4));
        JScrollPane scrollPane = new JScrollPane(historyArea);
        scrollPane.setBorder(new TitledBorder("Move History"));
        scrollPane.setPreferredSize(new Dimension(210, 350));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Captured pieces panel
        JPanel capturePanel = new JPanel(new GridLayout(2, 1, 2, 2));
        capturePanel.setBorder(new TitledBorder("Captured Pieces"));

        whiteCapturedLabel = new JLabel("<html><b>White captured:</b> </html>");
        whiteCapturedLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        whiteCapturedLabel.setVerticalAlignment(SwingConstants.TOP);

        blackCapturedLabel = new JLabel("<html><b>Black captured:</b> </html>");
        blackCapturedLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        blackCapturedLabel.setVerticalAlignment(SwingConstants.TOP);

        capturePanel.add(whiteCapturedLabel);
        capturePanel.add(blackCapturedLabel);
        centerPanel.add(capturePanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom: Undo button
        undoButton = new JButton("↩ Undo Last Move");
        undoButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        undoButton.setBackground(new Color(90, 120, 170));
        undoButton.setForeground(Color.WHITE);
        undoButton.setFocusPainted(false);
        undoButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 90, 140)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        undoButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        undoButton.addActionListener(e -> {
            if (undoListener != null) undoListener.onUndo();
        });
        add(undoButton, BorderLayout.SOUTH);
    }

    /**
     * Sets the undo listener.
     *
     * @param listener the listener to notify on undo
     */
    public void setUndoListener(UndoListener listener) {
        this.undoListener = listener;
    }

    /**
     * Records a move in the history panel.
     *
     * @param piece    the piece that moved
     * @param from     the source position
     * @param to       the destination position
     * @param captured the captured piece, or null
     * @param turn     the color that just moved
     */
    public void recordMove(Piece piece, Position from, Position to, Piece captured, Piece.Color turn) {
        String pieceStr = piece.getSymbol();
        String captureStr = (captured != null) ? "x" + captured.getSymbol() : "";
        String moveStr = pieceStr + ": " + from + " → " + to + captureStr;

        if (turn == Piece.Color.WHITE) {
            historyArea.append(moveNumber + ". " + moveStr + "\n");
            whiteJustMoved = true;
        } else {
            if (whiteJustMoved) {
                // Replace last line to show both moves on one line
                // Just append black's move on a new line indented
                historyArea.append("   " + moveStr + "\n");
                moveNumber++;
            } else {
                historyArea.append(moveNumber + ". ... " + moveStr + "\n");
                moveNumber++;
            }
            whiteJustMoved = false;
        }

        // Auto-scroll to bottom
        historyArea.setCaretPosition(historyArea.getDocument().getLength());

        // Update captured pieces
        if (captured != null) {
            String sym = getPieceUnicode(captured) + " ";
            if (turn == Piece.Color.WHITE) {
                whiteCaptured.add(sym);
            } else {
                blackCaptured.add(sym);
            }
            updateCapturedLabels();
        }

        // Update status
        Piece.Color next = (turn == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
        statusLabel.setText((next == Piece.Color.WHITE ? "White" : "Black") + "'s Turn");
        statusLabel.setBackground(next == Piece.Color.WHITE
                ? new Color(245, 245, 245) : new Color(60, 60, 60));
        statusLabel.setForeground(next == Piece.Color.WHITE ? Color.BLACK : Color.WHITE);
    }

    /**
     * Removes the last recorded move entry (called during undo).
     */
    public void undoLastMove() {
        String text = historyArea.getText();
        if (text.isEmpty()) return;

        // Remove last line
        int lastNewline = text.lastIndexOf('\n', text.length() - 2);
        if (lastNewline >= 0) {
            historyArea.setText(text.substring(0, lastNewline + 1));
        } else {
            historyArea.setText("");
        }

        // Adjust captured pieces
        if (!whiteCaptured.isEmpty() || !blackCaptured.isEmpty()) {
            // We'd need the info from undo to know which to remove;
            // for simplicity, the caller (ChessGUI) manages this.
        }
    }

    /**
     * Removes the last captured piece entry from the specified color's list.
     *
     * @param capturer the color that captured the piece
     */
    public void removeLastCapture(Piece.Color capturer) {
        List<String> list = (capturer == Piece.Color.WHITE) ? whiteCaptured : blackCaptured;
        if (!list.isEmpty()) {
            list.remove(list.size() - 1);
            updateCapturedLabels();
        }
    }

    /**
     * Updates the captured piece display labels.
     */
    private void updateCapturedLabels() {
        StringBuilder wb = new StringBuilder("<html><b>White captured:</b> ");
        for (String s : whiteCaptured) wb.append(s);
        wb.append("</html>");
        whiteCapturedLabel.setText(wb.toString());

        StringBuilder bb = new StringBuilder("<html><b>Black captured:</b> ");
        for (String s : blackCaptured) bb.append(s);
        bb.append("</html>");
        blackCapturedLabel.setText(bb.toString());
    }

    /**
     * Resets all history and captured pieces for a new game.
     */
    public void reset() {
        historyArea.setText("");
        whiteCaptured.clear();
        blackCaptured.clear();
        moveNumber = 1;
        whiteJustMoved = false;
        updateCapturedLabels();
        statusLabel.setText("White's Turn");
        statusLabel.setBackground(new Color(245, 245, 245));
        statusLabel.setForeground(Color.BLACK);
    }

    /**
     * Returns the Unicode symbol for a piece (for display).
     *
     * @param piece the piece
     * @return the Unicode string
     */
    private String getPieceUnicode(Piece piece) {
        boolean white = piece.getColor() == Piece.Color.WHITE;
        if (piece instanceof pieces.King)   return white ? "♔" : "♚";
        if (piece instanceof pieces.Queen)  return white ? "♕" : "♛";
        if (piece instanceof pieces.Rook)   return white ? "♖" : "♜";
        if (piece instanceof pieces.Bishop) return white ? "♗" : "♝";
        if (piece instanceof pieces.Knight) return white ? "♘" : "♞";
        if (piece instanceof pieces.Pawn)   return white ? "♙" : "♟";
        return piece.getSymbol();
    }

    /**
     * Functional interface for undo requests.
     */
    public interface UndoListener {
        /** Called when the player requests to undo the last move. */
        void onUndo();
    }
}
