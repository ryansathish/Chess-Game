package pieces;

import utils.Position;
import utils.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a King chess piece.
 * <p>
 * The King moves exactly one square in any direction (horizontally,
 * vertically, or diagonally). The King may not move into check.
 * Castling logic is handled at the Board/Game level.
 * </p>
 */
public class King extends Piece {

    /**
     * Constructs a King with the specified color and starting position.
     *
     * @param color    the color of this king (WHITE or BLACK)
     * @param position the initial position of this king
     */
    public King(Color color, Position position) {
        super(color, position);
    }

    /**
     * Returns the display symbol for this king.
     * White king: "wK", Black king: "bK".
     *
     * @return the two-character display symbol
     */
    @Override
    public String getSymbol() {
        return color == Color.WHITE ? "wK" : "bK";
    }

    /**
     * Computes all possible destination squares for this king given the board.
     * <p>
     * Considers all 8 adjacent squares, filtering out out-of-bounds squares
     * and squares occupied by friendly pieces. Does not filter squares that
     * would leave the king in check (handled by the Board).
     * </p>
     *
     * @param board the 8x8 array of pieces representing the board
     * @return a list of candidate destination positions
     */
    @Override
    public List<Position> possibleMoves(Piece[][] board) {
        List<Position> moves = new ArrayList<>();
        int row = position.getRow();
        int col = position.getCol();

        int[][] directions = {
            {-1, -1}, {-1, 0}, {-1, 1},
            { 0, -1},           { 0, 1},
            { 1, -1}, { 1, 0}, { 1, 1}
        };

        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            if (Utils.inBounds(r, c)) {
                if (board[r][c] == null || board[r][c].getColor() != this.color) {
                    moves.add(new Position(r, c));
                }
            }
        }
        return moves;
    }
}
