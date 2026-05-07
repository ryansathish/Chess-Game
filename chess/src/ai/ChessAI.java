package ai;

import board.Board;
import pieces.*;
import utils.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A chess AI engine that uses the Minimax algorithm with alpha-beta pruning
 * to select moves for the computer player.
 *
 * <p>The AI evaluates positions using a material + positional scoring function
 * and searches to a configurable depth. At the default depth of 3, it plays
 * at a beginner-to-intermediate level and responds in under a second on most hardware.</p>
 *
 * <p>Piece-square tables are derived from standard chess engine heuristics and
 * reward:</p>
 * <ul>
 *   <li>Control of the center for pawns and knights</li>
 *   <li>Active bishops on open diagonals</li>
 *   <li>Rooks on open files (7th rank bonus)</li>
 *   <li>King safety (staying castled, avoiding the center)</li>
 * </ul>
 */
public class ChessAI {

    /** Search depth (ply). 3 = reasonable strength without perceptible lag. */
    private static final int DEFAULT_DEPTH = 3;

    /** The color this AI plays as. */
    private final Piece.Color aiColor;

    /** The search depth to use. */
    private final int depth;

    // ── Piece values (centipawns) ────────────────────────────────────────────
    private static final int PAWN_VAL   = 100;
    private static final int KNIGHT_VAL = 320;
    private static final int BISHOP_VAL = 330;
    private static final int ROOK_VAL   = 500;
    private static final int QUEEN_VAL  = 900;
    private static final int KING_VAL   = 20000;

    // ── Piece-square tables (from White's perspective; row 0 = rank 8) ───────
    // Scores are indexed [row][col]. For Black pieces, we mirror vertically (row 7-row).

    private static final int[][] PAWN_TABLE = {
        {  0,  0,  0,  0,  0,  0,  0,  0 },
        { 50, 50, 50, 50, 50, 50, 50, 50 },
        { 10, 10, 20, 30, 30, 20, 10, 10 },
        {  5,  5, 10, 25, 25, 10,  5,  5 },
        {  0,  0,  0, 20, 20,  0,  0,  0 },
        {  5, -5,-10,  0,  0,-10, -5,  5 },
        {  5, 10, 10,-20,-20, 10, 10,  5 },
        {  0,  0,  0,  0,  0,  0,  0,  0 }
    };

    private static final int[][] KNIGHT_TABLE = {
        {-50,-40,-30,-30,-30,-30,-40,-50 },
        {-40,-20,  0,  0,  0,  0,-20,-40 },
        {-30,  0, 10, 15, 15, 10,  0,-30 },
        {-30,  5, 15, 20, 20, 15,  5,-30 },
        {-30,  0, 15, 20, 20, 15,  0,-30 },
        {-30,  5, 10, 15, 15, 10,  5,-30 },
        {-40,-20,  0,  5,  5,  0,-20,-40 },
        {-50,-40,-30,-30,-30,-30,-40,-50 }
    };

    private static final int[][] BISHOP_TABLE = {
        {-20,-10,-10,-10,-10,-10,-10,-20 },
        {-10,  0,  0,  0,  0,  0,  0,-10 },
        {-10,  0,  5, 10, 10,  5,  0,-10 },
        {-10,  5,  5, 10, 10,  5,  5,-10 },
        {-10,  0, 10, 10, 10, 10,  0,-10 },
        {-10, 10, 10, 10, 10, 10, 10,-10 },
        {-10,  5,  0,  0,  0,  0,  5,-10 },
        {-20,-10,-10,-10,-10,-10,-10,-20 }
    };

    private static final int[][] ROOK_TABLE = {
        {  0,  0,  0,  0,  0,  0,  0,  0 },
        {  5, 10, 10, 10, 10, 10, 10,  5 },
        { -5,  0,  0,  0,  0,  0,  0, -5 },
        { -5,  0,  0,  0,  0,  0,  0, -5 },
        { -5,  0,  0,  0,  0,  0,  0, -5 },
        { -5,  0,  0,  0,  0,  0,  0, -5 },
        { -5,  0,  0,  0,  0,  0,  0, -5 },
        {  0,  0,  0,  5,  5,  0,  0,  0 }
    };

    private static final int[][] QUEEN_TABLE = {
        {-20,-10,-10, -5, -5,-10,-10,-20 },
        {-10,  0,  0,  0,  0,  0,  0,-10 },
        {-10,  0,  5,  5,  5,  5,  0,-10 },
        { -5,  0,  5,  5,  5,  5,  0, -5 },
        {  0,  0,  5,  5,  5,  5,  0, -5 },
        {-10,  5,  5,  5,  5,  5,  0,-10 },
        {-10,  0,  5,  0,  0,  0,  0,-10 },
        {-20,-10,-10, -5, -5,-10,-10,-20 }
    };

    private static final int[][] KING_MID_TABLE = {
        {-30,-40,-40,-50,-50,-40,-40,-30 },
        {-30,-40,-40,-50,-50,-40,-40,-30 },
        {-30,-40,-40,-50,-50,-40,-40,-30 },
        {-30,-40,-40,-50,-50,-40,-40,-30 },
        {-20,-30,-30,-40,-40,-30,-30,-20 },
        {-10,-20,-20,-20,-20,-20,-20,-10 },
        { 20, 20,  0,  0,  0,  0, 20, 20 },
        { 20, 30, 10,  0,  0, 10, 30, 20 }
    };

    /**
     * Constructs a ChessAI for the given color using the default search depth.
     *
     * @param aiColor the color this AI will play as
     */
    public ChessAI(Piece.Color aiColor) {
        this(aiColor, DEFAULT_DEPTH);
    }

    /**
     * Constructs a ChessAI for the given color at the specified depth.
     *
     * @param aiColor the color this AI will play as
     * @param depth   the minimax search depth in plies
     */
    public ChessAI(Piece.Color aiColor, int depth) {
        this.aiColor = aiColor;
        this.depth   = depth;
    }

    /**
     * Returns the color this AI plays as.
     *
     * @return the AI's color
     */
    public Piece.Color getColor() {
        return aiColor;
    }

    // ── Move representation ─────────────────────────────────────────────────

    /**
     * A simple container for a from-to move pair.
     */
    public static class Move {
        /** Source position. */
        public final Position from;
        /** Destination position. */
        public final Position to;

        /**
         * Constructs a move.
         *
         * @param from source position
         * @param to   destination position
         */
        public Move(Position from, Position to) {
            this.from = from;
            this.to   = to;
        }
    }

    // ── Public API ──────────────────────────────────────────────────────────

    /**
     * Selects the best move for the AI using minimax with alpha-beta pruning.
     *
     * @param board the current board state
     * @return the best {@link Move} found, or {@code null} if no legal moves exist
     */
    public Move getBestMove(Board board) {
        List<Move> moves = getAllLegalMoves(board, aiColor);
        if (moves.isEmpty()) return null;

        // Shuffle for variety among equal-scored moves
        Collections.shuffle(moves);

        Move bestMove  = null;
        int  bestScore = Integer.MIN_VALUE;
        boolean maximising = (aiColor == Piece.Color.WHITE);

        for (Move move : moves) {
            Board simulated = simulateMove(board, move);
            int score = minimax(simulated, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, !maximising);
            if (bestMove == null || score > bestScore) {
                bestScore = score;
                bestMove  = move;
            }
        }
        return bestMove;
    }

    // ── Minimax ─────────────────────────────────────────────────────────────

    /**
     * Runs the minimax algorithm with alpha-beta pruning.
     *
     * @param board        the board state to evaluate
     * @param depth        remaining depth to search
     * @param alpha        best score the maximising player can guarantee
     * @param beta         best score the minimising player can guarantee
     * @param isMaximising true if it is the maximising player's turn
     * @return the evaluated score from the maximising player's perspective
     */
    private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximising) {
        if (depth == 0) {
            return evaluateBoard(board);
        }

        Piece.Color current  = isMaximising ? Piece.Color.WHITE : Piece.Color.BLACK;
        List<Move>  moves    = getAllLegalMoves(board, current);

        if (moves.isEmpty()) {
            if (board.isCheck(current)) {
                // Checkmate — heavily penalise (or reward)
                return isMaximising ? -100000 - depth : 100000 + depth;
            }
            return 0; // Stalemate
        }

        if (isMaximising) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moves) {
                Board sim  = simulateMove(board, move);
                int   eval = minimax(sim, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha   = Math.max(alpha, eval);
                if (beta <= alpha) break; // β cut-off
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : moves) {
                Board sim  = simulateMove(board, move);
                int   eval = minimax(sim, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta    = Math.min(beta, eval);
                if (beta <= alpha) break; // α cut-off
            }
            return minEval;
        }
    }

    // ── Move generation ─────────────────────────────────────────────────────

    /**
     * Returns all legal moves for the given color on the given board.
     *
     * @param board the board state
     * @param color the color whose moves to generate
     * @return a list of all legal {@link Move} objects
     */
    private List<Move> getAllLegalMoves(Board board, Piece.Color color) {
        List<Move> moves = new ArrayList<>();
        Piece[][] grid = board.getGrid();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p == null || p.getColor() != color) continue;

                Position from = new Position(r, c);
                for (Position to : p.possibleMoves(grid)) {
                    if (board.isLegalMove(from, to, color)) {
                        moves.add(new Move(from, to));
                    }
                }
            }
        }
        return moves;
    }

    // ── Board simulation ────────────────────────────────────────────────────

    /**
     * Returns a new Board that reflects the state after executing the given move.
     * The original board is not modified.
     *
     * @param board the current board
     * @param move  the move to apply
     * @return a new Board with the move applied
     */
    private Board simulateMove(Board board, Move move) {
        // Deep-copy the grid into a fresh Board
        Board copy  = new Board();
        Piece[][] srcGrid  = board.getGrid();
        Piece[][] destGrid = copy.getGrid();

        // Clear the auto-initialized board
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                destGrid[r][c] = null;

        // Clone each piece
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = srcGrid[r][c];
                if (p != null) {
                    destGrid[r][c] = clonePiece(p);
                }
            }
        }

        // Apply move on copy
        copy.movePiece(move.from, move.to);

        // Handle pawn promotion to Queen (simplification for AI)
        Piece moved = destGrid[move.to.getRow()][move.to.getCol()];
        if (moved instanceof Pawn) {
            int backRank = (moved.getColor() == Piece.Color.WHITE) ? 0 : 7;
            if (move.to.getRow() == backRank) {
                destGrid[move.to.getRow()][move.to.getCol()] =
                        new Queen(moved.getColor(), move.to);
            }
        }

        return copy;
    }

    /**
     * Creates a copy of a piece with the same type, color, and position.
     *
     * @param p the piece to clone
     * @return a new piece of the same type
     */
    private Piece clonePiece(Piece p) {
        Position pos = new Position(p.getPosition().getRow(), p.getPosition().getCol());
        if (p instanceof Pawn)   return new Pawn(p.getColor(), pos);
        if (p instanceof Rook)   return new Rook(p.getColor(), pos);
        if (p instanceof Knight) return new Knight(p.getColor(), pos);
        if (p instanceof Bishop) return new Bishop(p.getColor(), pos);
        if (p instanceof Queen)  return new Queen(p.getColor(), pos);
        if (p instanceof King)   return new King(p.getColor(), pos);
        return new Queen(p.getColor(), pos); // fallback
    }

    // ── Evaluation ─────────────────────────────────────────────────────────

    /**
     * Evaluates the board position from White's perspective.
     * Positive scores favour White; negative scores favour Black.
     * The AI negates the score if it plays Black.
     *
     * @param board the board to evaluate
     * @return the evaluation score in centipawns
     */
    private int evaluateBoard(Board board) {
        int score = 0;
        Piece[][] grid = board.getGrid();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p == null) continue;

                int pieceValue    = getPieceValue(p);
                int positional    = getPositionalBonus(p, r, c);
                int total         = pieceValue + positional;

                score += (p.getColor() == Piece.Color.WHITE) ? total : -total;
            }
        }

        // From the AI's perspective: negate if AI plays Black
        return (aiColor == Piece.Color.WHITE) ? score : -score;
    }

    /**
     * Returns the material value for a piece.
     *
     * @param p the piece
     * @return centipawn value
     */
    private int getPieceValue(Piece p) {
        if (p instanceof Pawn)   return PAWN_VAL;
        if (p instanceof Knight) return KNIGHT_VAL;
        if (p instanceof Bishop) return BISHOP_VAL;
        if (p instanceof Rook)   return ROOK_VAL;
        if (p instanceof Queen)  return QUEEN_VAL;
        if (p instanceof King)   return KING_VAL;
        return 0;
    }

    /**
     * Returns the positional bonus from the appropriate piece-square table.
     * White pieces use the table directly; Black pieces use the vertically mirrored table.
     *
     * @param p   the piece
     * @param row the piece's row on the board
     * @param col the piece's column on the board
     * @return the positional bonus in centipawns
     */
    private int getPositionalBonus(Piece p, int row, int col) {
        int tableRow = (p.getColor() == Piece.Color.WHITE) ? row : (7 - row);
        int[][] table = getTable(p);
        if (table == null) return 0;
        return table[tableRow][col];
    }

    /**
     * Returns the piece-square table for the given piece type.
     *
     * @param p the piece
     * @return the appropriate 8x8 table, or null if not applicable
     */
    private int[][] getTable(Piece p) {
        if (p instanceof Pawn)   return PAWN_TABLE;
        if (p instanceof Knight) return KNIGHT_TABLE;
        if (p instanceof Bishop) return BISHOP_TABLE;
        if (p instanceof Rook)   return ROOK_TABLE;
        if (p instanceof Queen)  return QUEEN_TABLE;
        if (p instanceof King)   return KING_MID_TABLE;
        return null;
    }
}
