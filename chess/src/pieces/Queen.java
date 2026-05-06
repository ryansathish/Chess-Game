package pieces;

import utils.Position;
import utils.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Queen chess piece.
 * <p>
 * The Queen combines the movement of the Rook and Bishop: it can move
 * any number of squares along a rank, file, or diagonal. It cannot
 * jump over other pieces.
 * </p>
 */
public class Queen extends Piece {

    /**
     * Constructs a Queen with the specified color and starting position.
     *
     * @param color    the color of this queen (WHITE or BLACK)
     * @param position the initial position of this queen
     */
    public Queen(Color color, Position position) {
        super(color, position);
    }

    /**
     * Returns the display symbol for this queen.
     * White queen: "wQ", Black queen: "bQ".
     *
     * @return the two-character display symbol
     */
    @Override
    public String getSymbol() {
        return color == Color.WHITE ? "wQ" : "bQ";
    }

    /**
     * Computes all possible moves for this queen given the current board state.
     * <p>
     * Slides in all 8 directions (horizontal, vertical, diagonal), stopping
     * at blocking or capturable pieces.
     * </p>
     *
     * @param board the 8x8 array of pieces representing the board
     * @return a list of valid destination positions
     */
    @Override
    public List<Position> possibleMoves(Piece[][] board) {
        List<Position> moves = new ArrayList<>();
        // All 8 directions: rook + bishop combined
        int[][] directions = {
            {-1,  0}, {1,  0}, {0, -1}, {0, 1},  // rook-like
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}   // bishop-like
        };

        for (int[] dir : directions) {
            int r = position.getRow() + dir[0];
            int c = position.getCol() + dir[1];
            while (Utils.inBounds(r, c)) {
                if (board[r][c] == null) {
                    moves.add(new Position(r, c));
                } else {
                    if (board[r][c].getColor() != this.color) {
                        moves.add(new Position(r, c)); // capture
                    }
                    break; // blocked
                }
                r += dir[0];
                c += dir[1];
            }
        }
        return moves;
    }
}
