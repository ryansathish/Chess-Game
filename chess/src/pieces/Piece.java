package pieces;

import utils.Position;
import java.util.List;

/**
 * Abstract base class representing a chess piece.
 * All specific chess pieces (Pawn, Rook, Knight, Bishop, Queen, King)
 * extend this class and implement their unique movement rules.
 */
public abstract class Piece {

    /**
     * Enumeration representing the color of a chess piece.
     */
    public enum Color {
        /** White player's piece. */
        WHITE,
        /** Black player's piece. */
        BLACK
    }

    /** The color of this piece (WHITE or BLACK). */
    protected Color color;

    /** The current position of this piece on the board. */
    protected Position position;

    /**
     * Constructs a Piece with the specified color and position.
     *
     * @param color    the color of the piece (WHITE or BLACK)
     * @param position the initial position of the piece on the board
     */
    public Piece(Color color, Position position) {
        this.color = color;
        this.position = position;
    }

    /**
     * Returns the color of this piece.
     *
     * @return the color (WHITE or BLACK)
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns the current position of this piece.
     *
     * @return the current {@link Position}
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets the position of this piece to the given position.
     *
     * @param position the new position for this piece
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Returns a list of all legally reachable positions from the current position,
     * ignoring check conditions. Each subclass implements movement rules specific
     * to its piece type.
     *
     * @param board the 2D array representing the current board state
     * @return a list of possible destination {@link Position} objects
     */
    public abstract List<Position> possibleMoves(Piece[][] board);

    /**
     * Returns the two-character string representation of this piece
     * used for console display (e.g., "wp", "bK", "wN").
     *
     * @return the display symbol of the piece
     */
    public abstract String getSymbol();

    /**
     * Returns a string representation of this piece showing its symbol and position.
     *
     * @return a descriptive string for the piece
     */
    @Override
    public String toString() {
        return getSymbol() + "@" + position;
    }
}
