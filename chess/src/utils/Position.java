package utils;

/**
 * Represents a position on the chessboard using row and column indices.
 * <p>
 * Rows are numbered 0–7 (corresponding to ranks 8–1 from top to bottom),
 * and columns are numbered 0–7 (corresponding to files A–H).
 * </p>
 */
public class Position {

    /** The row index (0 = rank 8, 7 = rank 1). */
    private int row;

    /** The column index (0 = file A, 7 = file H). */
    private int col;

    /**
     * Constructs a Position with the given row and column.
     *
     * @param row the row index (0–7)
     * @param col the column index (0–7)
     */
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Returns the row index of this position.
     *
     * @return the row (0–7)
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the column index of this position.
     *
     * @return the column (0–7)
     */
    public int getCol() {
        return col;
    }

    /**
     * Checks whether this position is within the valid 8x8 board bounds.
     *
     * @return {@code true} if both row and column are in range [0, 7]
     */
    public boolean isValid() {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    /**
     * Checks equality between this position and another object.
     * Two positions are equal if they share the same row and column.
     *
     * @param obj the object to compare
     * @return {@code true} if both positions have the same row and column
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return this.row == other.row && this.col == other.col;
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     *
     * @return the computed hash code
     */
    @Override
    public int hashCode() {
        return 31 * row + col;
    }

    /**
     * Returns the chess notation string for this position (e.g., "E2", "A8").
     *
     * @return the algebraic notation of the position
     */
    @Override
    public String toString() {
        char file = (char) ('A' + col);
        int rank = 8 - row;
        return "" + file + rank;
    }
}
