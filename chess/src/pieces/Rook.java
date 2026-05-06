package pieces;

import utils.Position;
import utils.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Rook chess piece.
 * <p>
 * The Rook moves any number of squares horizontally or vertically.
 * It cannot jump over other pieces.
 * </p>
 */
public class Rook extends Piece {

    /**
     * Constructs a Rook with the specified color and starting position.
     *
     * @param color    the color of this rook (WHITE or BLACK)
     * @param position the initial position of this rook
     */
    public Rook(Color color, Position position) {
        super(color, position);
    }

    /**
     * Returns the display symbol for this rook.
     * White rook: "wR", Black rook: "bR".
     *
     * @return the two-character display symbol
     */
    @Override
    public String getSymbol() {
        return color == Color.WHITE ? "wR" : "bR";
    }

    /**
     * Computes all possible moves for this rook given the current board state.
     * <p>
     * Slides along ranks and files, stopping at blocking or capturable pieces.
     * </p>
     *
     * @param board the 8x8 array of pieces representing the board
     * @return a list of valid destination positions
     */
    @Override
    public List<Position> possibleMoves(Piece[][] board) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

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
