package pieces;

import utils.Position;
import utils.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Pawn chess piece.
 * <p>
 * Pawns move forward one square, or two squares from their starting row.
 * They capture diagonally forward one square. En passant and promotion
 * logic is handled at the board/game level.
 * </p>
 * <ul>
 *   <li>White pawns move from higher row indices toward row 0 (rank 8).</li>
 *   <li>Black pawns move from lower row indices toward row 7 (rank 1).</li>
 * </ul>
 */
public class Pawn extends Piece {

    /**
     * Constructs a Pawn with the specified color and starting position.
     *
     * @param color    the color of this pawn (WHITE or BLACK)
     * @param position the initial position of this pawn
     */
    public Pawn(Color color, Position position) {
        super(color, position);
    }

    /**
     * Returns the display symbol for this pawn.
     * White pawn: "wp", Black pawn: "bp".
     *
     * @return the two-character display symbol
     */
    @Override
    public String getSymbol() {
        return color == Color.WHITE ? "wp" : "bp";
    }

    /**
     * Computes all possible moves for this pawn given the current board state.
     * <p>
     * Includes one-square advance, two-square advance from starting rank,
     * and diagonal captures. Does not include en passant.
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

        // White moves up (decreasing row), Black moves down (increasing row)
        int direction = (color == Color.WHITE) ? -1 : 1;
        int startRow   = (color == Color.WHITE) ? 6 : 1;

        // One square forward
        int newRow = row + direction;
        if (Utils.inBounds(newRow, col) && board[newRow][col] == null) {
            moves.add(new Position(newRow, col));

            // Two squares forward from starting position
            if (row == startRow && board[row + 2 * direction][col] == null) {
                moves.add(new Position(row + 2 * direction, col));
            }
        }

        // Diagonal captures
        for (int dc : new int[]{-1, 1}) {
            int captureCol = col + dc;
            if (Utils.inBounds(newRow, captureCol)) {
                Piece target = board[newRow][captureCol];
                if (target != null && target.getColor() != this.color) {
                    moves.add(new Position(newRow, captureCol));
                }
            }
        }

        return moves;
    }
}
