package gui;

import ai.ChessAI;
import board.Board;
import pieces.*;
import utils.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 * The main chessboard panel that renders the 8x8 grid and handles
 * mouse input for click-to-move and drag-and-drop piece movement.
 *
 * <p>Supports two interaction modes:
 * <ul>
 *   <li><b>Click-to-move:</b> Click a piece to select it, then click a destination.</li>
 *   <li><b>Drag-and-drop:</b> Press and drag a piece to a destination square.</li>
 * </ul>
 * </p>
 */
public class ChessBoardPanel extends JPanel {

    /** Size in pixels of each board square. */
    private int squareSize = 80;

    /** The logical chess board model. */
    private Board board;

    /** The currently selected square (for click-to-move), or null. */
    private Position selectedPos;

    /** The piece being dragged, or null if no drag in progress. */
    private Piece draggedPiece;

    /** The board position from which dragging started. */
    private Position dragFrom;

    /** Current mouse coordinates during a drag operation. */
    private int dragX, dragY;

    /** Listener to notify when a move is made. */
    private MoveListener moveListener;

    /** Hook called just before a move is executed (for undo snapshot). */
    private Runnable preMoveHook;

    /** Optional AI engine; null when playing human vs human. */
    private ChessAI chessAI;

    /** Whether the AI is currently computing its move (blocks input). */
    private boolean aiThinking = false;

    /** Light square color. */
    private Color lightColor = new Color(240, 217, 181);

    /** Dark square color. */
    private Color darkColor = new Color(181, 136, 99);

    /** Highlight color for the selected square. */
    private Color selectColor = new Color(130, 151, 105, 200);

    /** Highlight color for possible move dots. */
    private Color moveHighlight = new Color(0, 0, 0, 70);

    /** Whose turn it currently is. */
    private Piece.Color currentTurn = Piece.Color.WHITE;

    /** Possible moves for the currently selected piece. */
    private List<Position> possibleMoves = new ArrayList<>();

    /**
     * Constructs the ChessBoardPanel with a fresh board.
     */
    public ChessBoardPanel() {
        board = new Board();
        setPreferredSize(new Dimension(squareSize * 8 + 40, squareSize * 8 + 40));
        setupMouseListeners();
    }

    /**
     * Sets the callback listener for move events.
     *
     * @param listener the MoveListener to notify
     */
    public void setMoveListener(MoveListener listener) {
        this.moveListener = listener;
    }

    /**
     * Sets a hook that is called just before any move is executed.
     * Used by ChessGUI to take an undo snapshot.
     *
     * @param hook the Runnable to call before each move
     */
    public void setPreMoveHook(Runnable hook) {
        this.preMoveHook = hook;
    }

    /**
     * Sets the AI engine. Pass {@code null} to disable AI (human vs human).
     * When set, after each human move the AI automatically plays its response.
     *
     * @param ai the {@link ChessAI} to use, or null for human vs human
     */
    public void setChessAI(ChessAI ai) {
        this.chessAI = ai;
    }

    /**
     * Sets the board colors for customization.
     *
     * @param light the light square color
     * @param dark  the dark square color
     */
    public void setBoardColors(Color light, Color dark) {
        this.lightColor = light;
        this.darkColor = dark;
        repaint();
    }

    /**
     * Sets the square size for board resizing.
     *
     * @param size the new size in pixels per square
     */
    public void setSquareSize(int size) {
        this.squareSize = size;
        setPreferredSize(new Dimension(squareSize * 8 + 40, squareSize * 8 + 40));
        revalidate();
        repaint();
    }

    /**
     * Returns the underlying Board model.
     *
     * @return the board
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Returns whose turn it currently is.
     *
     * @return the current turn color
     */
    public Piece.Color getCurrentTurn() {
        return currentTurn;
    }

    /**
     * Sets the current turn (used for undo operations).
     *
     * @param color the color to set as current turn
     */
    public void setCurrentTurn(Piece.Color color) {
        this.currentTurn = color;
    }

    /**
     * Resets the board to the starting position and clears state.
     */
    public void resetBoard() {
        board = new Board();
        currentTurn = Piece.Color.WHITE;
        selectedPos = null;
        draggedPiece = null;
        possibleMoves.clear();
        repaint();
    }

    /**
     * Replaces the board with a new one (used for load game).
     *
     * @param newBoard the board to load
     * @param turn     the turn to resume with
     */
    public void loadBoard(Board newBoard, Piece.Color turn) {
        this.board = newBoard;
        this.currentTurn = turn;
        selectedPos = null;
        draggedPiece = null;
        possibleMoves.clear();
        repaint();
    }

    // -----------------------------------------------------------------------
    //  Mouse interaction
    // -----------------------------------------------------------------------

    /**
     * Sets up mouse listeners for click-to-move and drag-and-drop.
     */
    private void setupMouseListeners() {
        MouseAdapter adapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                Position pos = pixelToPosition(e.getX(), e.getY());
                if (pos == null) return;

                Piece piece = board.getPiece(pos);

                // Start drag if pressing on own piece
                if (piece != null && piece.getColor() == currentTurn) {
                    draggedPiece = piece;
                    dragFrom = pos;
                    dragX = e.getX();
                    dragY = e.getY();
                    // Also set selected for visual feedback
                    selectedPos = pos;
                    possibleMoves = piece.possibleMoves(board.getGrid());
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedPiece != null) {
                    dragX = e.getX();
                    dragY = e.getY();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Position to = pixelToPosition(e.getX(), e.getY());

                if (draggedPiece != null) {
                    if (to != null && !to.equals(dragFrom)) {
                        // Drag-and-drop move
                        attemptMove(dragFrom, to);
                    }
                    // Clear drag state regardless
                    draggedPiece = null;
                    dragFrom = null;
                    selectedPos = null;
                    possibleMoves.clear();
                    repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Ignore clicks that were part of a drag operation
                if (draggedPiece != null) return;

                Position clicked = pixelToPosition(e.getX(), e.getY());
                if (clicked == null) return;

                if (selectedPos == null) {
                    // Select a piece
                    Piece piece = board.getPiece(clicked);
                    if (piece != null && piece.getColor() == currentTurn) {
                        selectedPos = clicked;
                        possibleMoves = piece.possibleMoves(board.getGrid());
                        repaint();
                    }
                } else {
                    // Try to move selected piece to clicked square
                    if (clicked.equals(selectedPos)) {
                        // Deselect
                        selectedPos = null;
                        possibleMoves.clear();
                    } else {
                        Piece clickedPiece = board.getPiece(clicked);
                        if (clickedPiece != null && clickedPiece.getColor() == currentTurn) {
                            // Switch selection to new piece
                            selectedPos = clicked;
                            possibleMoves = clickedPiece.possibleMoves(board.getGrid());
                        } else {
                            attemptMove(selectedPos, clicked);
                            selectedPos = null;
                            possibleMoves.clear();
                        }
                    }
                    repaint();
                }
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    /**
     * Attempts to move a piece from {@code from} to {@code to}.
     * Fires the move listener on success, then triggers the AI if configured.
     *
     * @param from the source position
     * @param to   the destination position
     */
    private void attemptMove(Position from, Position to) {
        if (aiThinking) return; // Block input while AI computes
        Piece piece = board.getPiece(from);
        if (piece == null || piece.getColor() != currentTurn) return;

        // Check destination is in possible moves
        boolean validDest = false;
        for (Position p : piece.possibleMoves(board.getGrid())) {
            if (p.equals(to)) { validDest = true; break; }
        }
        if (!validDest) return;

        // Check not leaving own king in check
        if (!board.isLegalMove(from, to, currentTurn)) return;

        // Snapshot for undo before modifying state
        if (preMoveHook != null) preMoveHook.run();

        // Execute move
        Piece captured = board.movePiece(from, to);

        // Handle pawn promotion
        handlePromotion(to, currentTurn);

        // Notify listener
        if (moveListener != null) {
            moveListener.onMove(piece, from, to, captured, currentTurn);
        }

        // Check endgame
        Piece.Color opponent = (currentTurn == Piece.Color.WHITE)
                ? Piece.Color.BLACK : Piece.Color.WHITE;

        if (board.isCheckmate(opponent)) {
            repaint();
            String winner = (currentTurn == Piece.Color.WHITE) ? "White" : "Black";
            JOptionPane.showMessageDialog(this, winner + " wins by Checkmate!",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
            if (moveListener != null) moveListener.onGameOver(currentTurn);
            return;
        } else if (board.isStalemate(opponent)) {
            repaint();
            JOptionPane.showMessageDialog(this, "Stalemate! The game is a draw.",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
            if (moveListener != null) moveListener.onGameOver(null);
            return;
        }

        currentTurn = opponent;
        repaint();

        // Trigger AI if it's the AI's turn
        if (chessAI != null && chessAI.getColor() == currentTurn && gameActive()) {
            scheduleAIMove();
        }
    }

    /**
     * Returns true if the game is still active (no game-over condition).
     *
     * @return true if game is ongoing
     */
    private boolean gameActive() {
        return !board.isCheckmate(currentTurn) && !board.isStalemate(currentTurn);
    }

    /**
     * Schedules an AI move on a background thread to avoid blocking the EDT.
     * Shows a "thinking" cursor while computing.
     */
    private void scheduleAIMove() {
        aiThinking = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<ChessAI.Move, Void> worker = new SwingWorker<>() {
            @Override
            protected ChessAI.Move doInBackground() {
                return chessAI.getBestMove(board);
            }

            @Override
            protected void done() {
                try {
                    ChessAI.Move aiMove = get();
                    if (aiMove != null) {
                        executeAIMove(aiMove);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    aiThinking = false;
                    setCursor(Cursor.getDefaultCursor());
                    repaint();
                }
            }
        };
        worker.execute();
    }

    /**
     * Executes an AI move on the board, updating state and notifying listeners.
     *
     * @param move the AI's chosen move
     */
    private void executeAIMove(ChessAI.Move move) {
        Piece piece = board.getPiece(move.from);
        if (piece == null) return;

        if (preMoveHook != null) preMoveHook.run();

        Piece captured = board.movePiece(move.from, move.to);

        // Auto-promote AI pawns to Queen
        Piece moved = board.getGrid()[move.to.getRow()][move.to.getCol()];
        if (moved instanceof Pawn) {
            int backRank = (moved.getColor() == Piece.Color.WHITE) ? 0 : 7;
            if (move.to.getRow() == backRank) {
                board.getGrid()[move.to.getRow()][move.to.getCol()] =
                        new Queen(moved.getColor(), move.to);
            }
        }

        if (moveListener != null) {
            moveListener.onMove(piece, move.from, move.to, captured, currentTurn);
        }

        Piece.Color opponent = (currentTurn == Piece.Color.WHITE)
                ? Piece.Color.BLACK : Piece.Color.WHITE;

        if (board.isCheckmate(opponent)) {
            repaint();
            String winner = (currentTurn == Piece.Color.WHITE) ? "White" : "Black";
            JOptionPane.showMessageDialog(this, winner + " wins by Checkmate!",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
            if (moveListener != null) moveListener.onGameOver(currentTurn);
            return;
        } else if (board.isStalemate(opponent)) {
            repaint();
            JOptionPane.showMessageDialog(this, "Stalemate! The game is a draw.",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
            if (moveListener != null) moveListener.onGameOver(null);
            return;
        }

        currentTurn = opponent;
        repaint();
    }

    /**
     * Handles pawn promotion by showing a dialog for piece choice.
     *
     * @param pos   the position of the pawn that may promote
     * @param color the color of the promoting player
     */
    private void handlePromotion(Position pos, Piece.Color color) {
        Piece p = board.getPiece(pos);
        if (!(p instanceof Pawn)) return;
        int backRank = (color == Piece.Color.WHITE) ? 0 : 7;
        if (pos.getRow() != backRank) return;

        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(this,
                "Choose promotion piece:",
                "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        Piece promoted;
        switch (choice) {
            case 1: promoted = new Rook(color, pos);   break;
            case 2: promoted = new Bishop(color, pos); break;
            case 3: promoted = new Knight(color, pos); break;
            default: promoted = new Queen(color, pos); break;
        }
        board.getGrid()[pos.getRow()][pos.getCol()] = promoted;
    }

    /**
     * Converts pixel coordinates to a board Position.
     *
     * @param x the x pixel coordinate
     * @param y the y pixel coordinate
     * @return the Position, or null if outside the board
     */
    private Position pixelToPosition(int x, int y) {
        int offsetX = 30; // left label area
        int offsetY = 10; // top label area
        int col = (x - offsetX) / squareSize;
        int row = (y - offsetY) / squareSize;
        if (row < 0 || row >= 8 || col < 0 || col >= 8) return null;
        return new Position(row, col);
    }

    /**
     * Returns the top-left pixel coordinate of a board square.
     *
     * @param row the row index
     * @param col the column index
     * @return a Point with the pixel coordinates
     */
    private Point positionToPixel(int row, int col) {
        return new Point(30 + col * squareSize, 10 + row * squareSize);
    }

    // -----------------------------------------------------------------------
    //  Painting
    // -----------------------------------------------------------------------

    /**
     * Paints the board, coordinates, pieces, highlights, and the dragged piece.
     *
     * @param g the Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBoard(g2);
        drawCoordinates(g2);
        drawHighlights(g2);
        drawPieces(g2);
        drawDraggedPiece(g2);
    }

    /**
     * Draws the alternating light and dark squares.
     *
     * @param g2 the Graphics2D context
     */
    private void drawBoard(Graphics2D g2) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boolean light = (row + col) % 2 == 0;
                g2.setColor(light ? lightColor : darkColor);
                Point p = positionToPixel(row, col);
                g2.fillRect(p.x, p.y, squareSize, squareSize);
            }
        }
    }

    /**
     * Draws file (A-H) and rank (1-8) coordinate labels.
     *
     * @param g2 the Graphics2D context
     */
    private void drawCoordinates(Graphics2D g2) {
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));

        // File labels (A–H) at the top
        for (int col = 0; col < 8; col++) {
            char file = (char) ('A' + col);
            int x = 30 + col * squareSize + squareSize / 2 - 4;
            g2.drawString(String.valueOf(file), x, 8);
        }

        // Rank labels (8–1) on the left
        for (int row = 0; row < 8; row++) {
            int rank = 8 - row;
            int y = 10 + row * squareSize + squareSize / 2 + 5;
            g2.drawString(String.valueOf(rank), 14, y);
        }
    }

    /**
     * Draws selection highlight and possible-move indicators.
     *
     * @param g2 the Graphics2D context
     */
    private void drawHighlights(Graphics2D g2) {
        // Selected square
        if (selectedPos != null) {
            Point p = positionToPixel(selectedPos.getRow(), selectedPos.getCol());
            g2.setColor(selectColor);
            g2.fillRect(p.x, p.y, squareSize, squareSize);
        }

        // Possible move dots
        for (Position move : possibleMoves) {
            Point p = positionToPixel(move.getRow(), move.getCol());
            Piece target = board.getPiece(move);
            if (target != null) {
                // Ring for capture squares
                g2.setColor(new Color(200, 50, 50, 120));
                g2.setStroke(new BasicStroke(4));
                g2.drawOval(p.x + 4, p.y + 4, squareSize - 8, squareSize - 8);
                g2.setStroke(new BasicStroke(1));
            } else {
                // Dot for empty squares
                g2.setColor(moveHighlight);
                int dotSize = squareSize / 3;
                g2.fillOval(p.x + (squareSize - dotSize) / 2,
                        p.y + (squareSize - dotSize) / 2,
                        dotSize, dotSize);
            }
        }
    }

    /**
     * Draws all pieces on the board, skipping the one being dragged.
     *
     * @param g2 the Graphics2D context
     */
    private void drawPieces(Graphics2D g2) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getGrid()[row][col];
                if (piece == null) continue;
                // Skip dragged piece (it's drawn separately at cursor)
                if (draggedPiece != null && draggedPiece == piece) continue;
                Point p = positionToPixel(row, col);
                drawPiece(g2, piece, p.x, p.y, squareSize);
            }
        }
    }

    /**
     * Draws the piece currently being dragged at the cursor position.
     *
     * @param g2 the Graphics2D context
     */
    private void drawDraggedPiece(Graphics2D g2) {
        if (draggedPiece == null) return;
        int x = dragX - squareSize / 2;
        int y = dragY - squareSize / 2;
        drawPiece(g2, draggedPiece, x, y, squareSize);
    }

    /**
     * Renders a single chess piece as a Unicode chess symbol with a styled background.
     *
     * @param g2    the Graphics2D context
     * @param piece the piece to draw
     * @param x     the top-left x coordinate
     * @param y     the top-left y coordinate
     * @param size  the square size in pixels
     */
    private void drawPiece(Graphics2D g2, Piece piece, int x, int y, int size) {
        String symbol = getPieceUnicode(piece);
        boolean isWhite = piece.getColor() == Piece.Color.WHITE;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.setFont(new Font("Serif", Font.PLAIN, (int)(size * 0.72)));
        FontMetrics fm = g2.getFontMetrics();
        int sw = fm.stringWidth(symbol);
        int sx = x + (size - sw) / 2;
        int sy = y + (size + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(symbol, sx + 1, sy + 1);

        // Piece fill color
        g2.setColor(isWhite ? Color.WHITE : new Color(30, 30, 30));
        g2.drawString(symbol, sx, sy);

        // Outline for contrast
        g2.setColor(isWhite ? new Color(80, 80, 80) : new Color(220, 220, 220));
        g2.setFont(new Font("Serif", Font.PLAIN, (int)(size * 0.72)));
        // Draw text slightly stroked by painting multiple offsets
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g2.drawString(symbol, sx + dx, sy + dy);
                }
            }
        }
        g2.setColor(isWhite ? Color.WHITE : new Color(30, 30, 30));
        g2.drawString(symbol, sx, sy);
    }

    /**
     * Returns the Unicode chess symbol for a given piece.
     *
     * @param piece the chess piece
     * @return the Unicode character string
     */
    private String getPieceUnicode(Piece piece) {
        boolean white = piece.getColor() == Piece.Color.WHITE;
        if (piece instanceof King)   return white ? "\u2654" : "\u265A";
        if (piece instanceof Queen)  return white ? "\u2655" : "\u265B";
        if (piece instanceof Rook)   return white ? "\u2656" : "\u265C";
        if (piece instanceof Bishop) return white ? "\u2657" : "\u265D";
        if (piece instanceof Knight) return white ? "\u2658" : "\u265E";
        if (piece instanceof Pawn)   return white ? "\u2659" : "\u265F";
        return "?";
    }

    /**
     * Functional interface for move and game-over events.
     */
    public interface MoveListener {
        /**
         * Called when a successful move is made.
         *
         * @param piece    the piece that moved
         * @param from     the source position
         * @param to       the destination position
         * @param captured the captured piece, or null
         * @param turn     the color that just moved
         */
        void onMove(Piece piece, Position from, Position to, Piece captured, Piece.Color turn);

        /**
         * Called when the game ends.
         *
         * @param winner the winning color, or null for a draw
         */
        void onGameOver(Piece.Color winner);
    }
}
