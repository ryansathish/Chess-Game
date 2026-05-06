package pieces;

import utils.Position;
import utils.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Knight chess piece.
 * <p>
 * The Knight moves in an "L" shape: two squares in one direction
 * and one square perpendicular. The Knight is the only piece that
 * can jump over other pieces.
 * </p>
 */
public class Knight extends Piece {

    /**
     * Constructs a Knight with the specified color and starting position.
     *
     * @param color    the color of this knight (WHITE or BLACK)
     * @param position the initial position of this knight
     */
    public Knight(Color color, Position position) {
        super(color, position);
    }

    /**
     * Returns the display symbol for this knight.
     * White knight: "wN", Black knight: "bN".
     *
     * @return the two-character display symbol
     */
    @Override
    public String getSymbol() {
        return color == Color.WHITE ? "wN" : "bN";
    }

    /**
     * Computes all possible moves for this knight given the current board state.
     * <p>
     * Considers all 8 possible L-shaped jumps; filters out out-of-bounds
     * and squares occupied by friendly pieces.
     * </p>
     *
     * @param board the 8x8 array of pieces representing the board
     * @return a list of valid destination positions
     */
    @Override
    public List<Position> possibleMoves(Piece[][] board) {
        List<Position> moves = new ArrayList<>();
        int row = position.getRow();
        int col = position.getCol();

        int[][] jumps = {
            {-2, -1}, {-2, 1},
            {-1, -2}, {-1, 2},
            { 1, -2}, { 1, 2},
            { 2, -1}, { 2, 1}
        };

        for (int[] jump : jumps) {
            int r = row + jump[0];
            int c = col + jump[1];
            if (Utils.inBounds(r, c)) {
                if (board[r][c] == null || board[r][c].getColor() != this.color) {
                    moves.add(new Position(r, c));
                }
            }
        }
        return moves;
    }
}
