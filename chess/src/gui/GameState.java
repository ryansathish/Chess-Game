package gui;

import board.Board;
import pieces.*;
import utils.Position;

import java.io.*;

/**
 * Captures a complete snapshot of the game state at a single point in time.
 * Used for save/load (serialization to disk) and undo (kept in memory stack).
 *
 * <p>The snapshot stores:
 * <ul>
 *   <li>The board's 8x8 grid (piece type, color, position for each cell)</li>
 *   <li>Whose turn it is</li>
 *   <li>The piece that moved, its from/to, and any captured piece (for undo UI)</li>
 * </ul>
 * </p>
 */
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Serializable piece record: type, color, row, col. */
    public static class PieceRecord implements Serializable {
        public String type;   // "Pawn","Rook","Knight","Bishop","Queen","King"
        public String color;  // "WHITE" or "BLACK"
        public int row, col;

        public PieceRecord(Piece p) {
            this.type  = p.getClass().getSimpleName();
            this.color = p.getColor().name();
            this.row   = p.getPosition().getRow();
            this.col   = p.getPosition().getCol();
        }
    }

    /** The board grid as piece records (null for empty squares). */
    public PieceRecord[][] grid = new PieceRecord[8][8];

    /** Whose turn it is. */
    public String currentTurn;

    /** Move info for history-panel undo label removal. */
    public String movedPieceSymbol;
    public int fromRow, fromCol;
    public int toRow, toCol;
    public PieceRecord captured;   // null if no capture

    /**
     * Takes a snapshot from the current board state.
     *
     * @param board   the board to snapshot
     * @param turn    the current turn
     * @param piece   the piece that just moved (null for initial state)
     * @param from    source position (null for initial state)
     * @param to      destination position (null for initial state)
     * @param cap     captured piece (null if none)
     */
    public GameState(Board board, Piece.Color turn, Piece piece,
                     Position from, Position to, Piece cap) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getGrid()[r][c];
                grid[r][c] = (p != null) ? new PieceRecord(p) : null;
            }
        }
        currentTurn = turn.name();
        if (piece != null) {
            movedPieceSymbol = piece.getSymbol();
            fromRow = from.getRow(); fromCol = from.getCol();
            toRow   = to.getRow();   toCol   = to.getCol();
        }
        captured = (cap != null) ? new PieceRecord(cap) : null;
    }

    /**
     * Reconstructs a Board from this snapshot.
     *
     * @return a new Board reflecting the saved state
     */
    public Board restoreBoard() {
        Board board = new Board();
        // Clear the board first
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                board.getGrid()[r][c] = null;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                PieceRecord rec = grid[r][c];
                if (rec == null) continue;
                Piece.Color col = Piece.Color.valueOf(rec.color);
                Position pos = new Position(rec.row, rec.col);
                Piece p = createPiece(rec.type, col, pos);
                board.getGrid()[r][c] = p;
            }
        }
        return board;
    }

    /**
     * Returns the restored current turn.
     *
     * @return the Piece.Color for whose turn it is
     */
    public Piece.Color restoreTurn() {
        return Piece.Color.valueOf(currentTurn);
    }

    /**
     * Factory method to create a Piece from its type name.
     */
    private Piece createPiece(String type, Piece.Color color, Position pos) {
        switch (type) {
            case "Pawn":   return new Pawn(color, pos);
            case "Rook":   return new Rook(color, pos);
            case "Knight": return new Knight(color, pos);
            case "Bishop": return new Bishop(color, pos);
            case "Queen":  return new Queen(color, pos);
            case "King":   return new King(color, pos);
            default:       return new Queen(color, pos);
        }
    }

    /**
     * Serializes this state to a file.
     *
     * @param file the file to write to
     * @throws IOException if writing fails
     */
    public void saveToFile(File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        }
    }

    /**
     * Deserializes a GameState from a file.
     *
     * @param file the file to read from
     * @return the loaded GameState
     * @throws IOException            if reading fails
     * @throws ClassNotFoundException if the class is not found
     */
    public static GameState loadFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (GameState) ois.readObject();
        }
    }
}
