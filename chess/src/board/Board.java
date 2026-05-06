package board;

import pieces.*;
import utils.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the chess board and manages all board-level operations.
 * <p>
 * The board is an 8x8 grid where each cell holds a {@link Piece} reference
 * or {@code null} if the square is empty. Row 0 corresponds to rank 8 (black's
 * back rank) and row 7 corresponds to rank 1 (white's back rank).
 * </p>
 */
public class Board {

    /** The 8x8 matrix of chess pieces. Null indicates an empty square. */
    private Piece[][] grid;

    /** List of pieces captured throughout the game. */
    private List<Piece> capturedPieces;

    /**
     * Constructs a new Board and initializes it with pieces in their
     * standard starting positions.
     */
    public Board() {
        grid = new Piece[8][8];
        capturedPieces = new ArrayList<>();
        initializeBoard();
    }

    /**
     * Places all chess pieces on the board in their standard starting positions.
     * Black pieces occupy ranks 8 and 7 (rows 0–1); white pieces occupy ranks
     * 2 and 1 (rows 6–7).
     */
    public void initializeBoard() {
        // Black back rank (row 0 = rank 8)
        grid[0][0] = new Rook(Piece.Color.BLACK,   new Position(0, 0));
        grid[0][1] = new Knight(Piece.Color.BLACK, new Position(0, 1));
        grid[0][2] = new Bishop(Piece.Color.BLACK, new Position(0, 2));
        grid[0][3] = new Queen(Piece.Color.BLACK,  new Position(0, 3));
        grid[0][4] = new King(Piece.Color.BLACK,   new Position(0, 4));
        grid[0][5] = new Bishop(Piece.Color.BLACK, new Position(0, 5));
        grid[0][6] = new Knight(Piece.Color.BLACK, new Position(0, 6));
        grid[0][7] = new Rook(Piece.Color.BLACK,   new Position(0, 7));

        // Black pawns (row 1 = rank 7)
        for (int col = 0; col < 8; col++) {
            grid[1][col] = new Pawn(Piece.Color.BLACK, new Position(1, col));
        }

        // Empty rows 2–5
        for (int row = 2; row <= 5; row++) {
            for (int col = 0; col < 8; col++) {
                grid[row][col] = null;
            }
        }

        // White pawns (row 6 = rank 2)
        for (int col = 0; col < 8; col++) {
            grid[6][col] = new Pawn(Piece.Color.WHITE, new Position(6, col));
        }

        // White back rank (row 7 = rank 1)
        grid[7][0] = new Rook(Piece.Color.WHITE,   new Position(7, 0));
        grid[7][1] = new Knight(Piece.Color.WHITE, new Position(7, 1));
        grid[7][2] = new Bishop(Piece.Color.WHITE, new Position(7, 2));
        grid[7][3] = new Queen(Piece.Color.WHITE,  new Position(7, 3));
        grid[7][4] = new King(Piece.Color.WHITE,   new Position(7, 4));
        grid[7][5] = new Bishop(Piece.Color.WHITE, new Position(7, 5));
        grid[7][6] = new Knight(Piece.Color.WHITE, new Position(7, 6));
        grid[7][7] = new Rook(Piece.Color.WHITE,   new Position(7, 7));
    }

    /**
     * Returns the piece at the specified position, or {@code null} if empty.
     *
     * @param position the board position to query
     * @return the {@link Piece} at that position, or {@code null}
     */
    public Piece getPiece(Position position) {
        return grid[position.getRow()][position.getCol()];
    }

    /**
     * Moves the piece from the {@code from} position to the {@code to} position.
     * If a piece occupies the destination square, it is captured and added to
     * the captured pieces list. The piece's internal position is updated.
     *
     * @param from the source position
     * @param to   the destination position
     * @return the captured piece, or {@code null} if no capture occurred
     */
    public Piece movePiece(Position from, Position to) {
        Piece moving  = grid[from.getRow()][from.getCol()];
        Piece captured = grid[to.getRow()][to.getCol()];

        if (captured != null) {
            capturedPieces.add(captured);
        }

        grid[to.getRow()][to.getCol()]     = moving;
        grid[from.getRow()][from.getCol()] = null;

        if (moving != null) {
            moving.setPosition(to);
        }

        return captured;
    }

    /**
     * Checks whether the king of the given color is currently in check.
     * <p>
     * A king is in check if any opponent piece has a possible move that
     * lands on the king's square.
     * </p>
     *
     * @param color the color of the king to check
     * @return {@code true} if that king is in check
     */
    public boolean isCheck(Piece.Color color) {
        Position kingPos = findKing(color);
        if (kingPos == null) return false;

        Piece.Color opponent = (color == Piece.Color.WHITE)
                               ? Piece.Color.BLACK : Piece.Color.WHITE;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null && p.getColor() == opponent) {
                    for (Position move : p.possibleMoves(grid)) {
                        if (move.equals(kingPos)) return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the given color is in checkmate.
     * <p>
     * A player is in checkmate if they are in check and have no legal move
     * that removes the check.
     * </p>
     *
     * @param color the color to check for checkmate
     * @return {@code true} if that color is in checkmate
     */
    public boolean isCheckmate(Piece.Color color) {
        if (!isCheck(color)) return false;
        return !hasAnyLegalMove(color);
    }

    /**
     * Checks whether the given color is in stalemate.
     * <p>
     * A player is in stalemate if they are NOT in check but have no legal moves.
     * </p>
     *
     * @param color the color to check for stalemate
     * @return {@code true} if that color is in stalemate
     */
    public boolean isStalemate(Piece.Color color) {
        if (isCheck(color)) return false;
        return !hasAnyLegalMove(color);
    }

    /**
     * Determines whether the given color has at least one legal move available.
     * A move is legal if it does not leave the moving player's king in check.
     *
     * @param color the color to test
     * @return {@code true} if at least one legal move exists
     */
    private boolean hasAnyLegalMove(Piece.Color color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null && p.getColor() == color) {
                    for (Position dest : p.possibleMoves(grid)) {
                        if (isLegalMove(new Position(r, c), dest, color)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Tests whether moving a piece from {@code from} to {@code to} leaves the
     * moving player's king in check (making it an illegal move).
     *
     * @param from  the source position
     * @param to    the destination position
     * @param color the color of the moving player
     * @return {@code true} if the move is legal (does not expose own king)
     */
    public boolean isLegalMove(Position from, Position to, Piece.Color color) {
        // Simulate the move on a copy of the grid
        Piece[][] copy = copyGrid();
        Piece moving   = copy[from.getRow()][from.getCol()];
        copy[to.getRow()][to.getCol()]     = moving;
        copy[from.getRow()][from.getCol()] = null;
        if (moving != null) moving.setPosition(to);

        boolean inCheck = isCheckOnGrid(copy, color);

        // Restore position (the copy is discarded, but piece object was mutated)
        if (moving != null) moving.setPosition(from);

        return !inCheck;
    }

    /**
     * Checks whether the king of the given color is in check on a given grid state.
     *
     * @param testGrid the board grid to evaluate
     * @param color    the color of the king to check
     * @return {@code true} if the king is attacked
     */
    private boolean isCheckOnGrid(Piece[][] testGrid, Piece.Color color) {
        // Locate king
        Position kingPos = null;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = testGrid[r][c];
                if (p instanceof King && p.getColor() == color) {
                    kingPos = new Position(r, c);
                    break;
                }
            }
            if (kingPos != null) break;
        }
        if (kingPos == null) return false;

        Piece.Color opponent = (color == Piece.Color.WHITE)
                               ? Piece.Color.BLACK : Piece.Color.WHITE;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = testGrid[r][c];
                if (p != null && p.getColor() == opponent) {
                    for (Position m : p.possibleMoves(testGrid)) {
                        if (m.equals(kingPos)) return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Finds the position of the king belonging to the specified color.
     *
     * @param color the color of the king to locate
     * @return the king's {@link Position}, or {@code null} if not found
     */
    private Position findKing(Piece.Color color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p instanceof King && p.getColor() == color) {
                    return new Position(r, c);
                }
            }
        }
        return null;
    }

    /**
     * Creates a shallow copy of the current grid array.
     * Piece objects themselves are shared (not deep-copied).
     *
     * @return a 2D array copy of the current grid
     */
    private Piece[][] copyGrid() {
        Piece[][] copy = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, 8);
        }
        return copy;
    }

    /**
     * Returns the list of all pieces captured so far in the game.
     *
     * @return a list of captured {@link Piece} objects
     */
    public List<Piece> getCapturedPieces() {
        return capturedPieces;
    }

    /**
     * Returns the raw 8x8 grid array.
     *
     * @return the board grid
     */
    public Piece[][] getGrid() {
        return grid;
    }

    /**
     * Prints the current state of the chessboard to the console.
     * <p>
     * Displays file labels (A–H) across the top, rank numbers (8–1)
     * down the left side, and each square either as its piece symbol
     * or as "##" for an empty dark square / "  " for an empty light square.
     * </p>
     */
    public void display() {
        System.out.println();
        System.out.println("    A   B   C   D   E   F   G   H");
        System.out.println("  +---+---+---+---+---+---+---+---+");

        for (int row = 0; row < 8; row++) {
            int rank = 8 - row;
            System.out.print(rank + " |");
            for (int col = 0; col < 8; col++) {
                Piece p = grid[row][col];
                if (p != null) {
                    System.out.print(" " + p.getSymbol() + "|");
                } else {
                    // Alternate empty square shading
                    boolean dark = (row + col) % 2 == 1;
                    System.out.print(dark ? "## |" : "   |");
                }
            }
            System.out.println(" " + rank);
            System.out.println("  +---+---+---+---+---+---+---+---+");
        }
        System.out.println("    A   B   C   D   E   F   G   H");
        System.out.println();
    }
}
