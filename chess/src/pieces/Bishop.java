package pieces;

import utils.Position;
import utils.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Bishop chess piece.
 * <p>
 * The Bishop moves any number of squares diagonally and cannot
 * jump over other pieces. Each Bishop is confined to squares of
 * one color for the entire game.
 * </p>
 */
public class Bishop extends Piece {

    /**
     * Constructs a Bishop with the specified color and starting position.
     *
     * @param color    the color of this bishop (WHITE or BLACK)
     * @param position the initial position of this bishop
     */
    public Bishop(Color color, Position position) {
        super(color, position);
    }

    /**
     * Returns the display symbol for this bishop.
     * White bishop: "wB", Black bishop: "bB".
     *
     * @return the two-character display symbol
     */
    @Override
    public String getSymbol() {
        return color == Color.WHITE ? "wB" : "bB";
    }

    /**
     * Computes all possible moves for this bishop given the current board state.
     * <p>
     * Slides diagonally in all four directions, stopping at blocking or
     * capturable pieces.
     * </p>
     *
     * @param board the 8x8 array of pieces representing the board
     * @return a list of valid destination positions
     */
    @Override
    public List<Position> possibleMoves(Piece[][] board) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

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
