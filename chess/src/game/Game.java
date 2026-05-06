package game;

import board.Board;
import pieces.*;
import player.Player;
import utils.Position;
import utils.Utils;

/**
 * Controls the overall flow of a chess game.
 * <p>
 * Manages the two players, the board, and the main game loop. Alternates
 * turns, validates and executes moves, checks for check/checkmate/stalemate,
 * and declares a winner or draw when the game ends.
 * </p>
 */
public class Game {

    /** The chess board for this game. */
    private Board board;

    /** The player controlling white pieces. */
    private Player whitePlayer;

    /** The player controlling black pieces. */
    private Player blackPlayer;

    /** The color of the player whose turn it currently is. */
    private Piece.Color currentTurn;

    /** Whether the game is still active. */
    private boolean running;

    /**
     * Constructs a new Game, creating the board and both players,
     * and sets White as the first player to move.
     */
    public Game() {
        board       = new Board();
        whitePlayer = new Player(Piece.Color.WHITE);
        blackPlayer = new Player(Piece.Color.BLACK);
        currentTurn = Piece.Color.WHITE;
        running     = false;
    }

    /**
     * Initializes the game state.
     * Resets the board to starting positions and sets White to move first.
     */
    public void start() {
        board = new Board();
        currentTurn = Piece.Color.WHITE;
        running = true;
        System.out.println("===========================================");
        System.out.println("       Welcome to Console Chess!");
        System.out.println("===========================================");
        System.out.println("  Move format : E2 E4");
        System.out.println("  Castling    : O-O  (kingside)");
        System.out.println("               O-O-O (queenside)");
        System.out.println("  Promotion   : E7 E8=Q");
        System.out.println("  Quit        : quit");
        System.out.println("===========================================");
    }

    /**
     * Ends the game, sets the running flag to false, and prints a summary.
     *
     * @param message the end-game message to display (winner or draw)
     */
    public void end(String message) {
        running = false;
        board.display();
        System.out.println("===========================================");
        System.out.println("  GAME OVER: " + message);
        System.out.println("===========================================");

        // Show captured pieces
        if (!board.getCapturedPieces().isEmpty()) {
            System.out.print("  Captured pieces: ");
            for (Piece p : board.getCapturedPieces()) {
                System.out.print(p.getSymbol() + " ");
            }
            System.out.println();
        }
    }

    /**
     * Runs the main game loop.
     * <p>
     * Repeatedly displays the board, prompts the current player for a move,
     * validates and executes it, then checks for check, checkmate, and stalemate.
     * Alternates turns until the game ends.
     * </p>
     */
    public void play() {
        start();

        while (running) {
            board.display();

            Player current = (currentTurn == Piece.Color.WHITE) ? whitePlayer : blackPlayer;
            Piece.Color opponent = (currentTurn == Piece.Color.WHITE)
                                   ? Piece.Color.BLACK : Piece.Color.WHITE;

            // Announce check if applicable
            if (board.isCheck(currentTurn)) {
                System.out.println("  *** " + current.getName() + " is in CHECK! ***");
            }

            // Get move from player
            System.out.print("  ");
            String moveInput = current.makeMove(board);

            // Allow quitting
            if (moveInput.equalsIgnoreCase("quit")) {
                end("Player quit. No winner declared.");
                break;
            }

            // Attempt to execute the move
            boolean success = executeMove(current, moveInput);

            if (!success) {
                continue; // Re-prompt the same player
            }

            // After successful move, check end conditions for opponent
            if (board.isCheckmate(opponent)) {
                end(current.getName() + " wins by checkmate!");
            } else if (board.isStalemate(opponent)) {
                end("Draw by stalemate!");
            } else {
                // Switch turns
                currentTurn = opponent;
            }
        }
    }

    /**
     * Attempts to execute the move entered by the player.
     * <p>
     * Validates that:
     * <ul>
     *   <li>There is a piece at the source square belonging to the current player.</li>
     *   <li>The destination is in the piece's list of possible moves.</li>
     *   <li>The move does not leave the current player's king in check.</li>
     * </ul>
     * If the move is invalid, an error message is printed and {@code false} is returned.
     * Handles castling notation ("O-O", "O-O-O") as a special case.
     * </p>
     *
     * @param player    the player attempting the move
     * @param moveInput the raw move string entered by the player
     * @return {@code true} if the move was successfully executed
     */
    private boolean executeMove(Player player, String moveInput) {
        String upper = moveInput.trim().toUpperCase();

        // Handle castling
        if (upper.equals("O-O") || upper.equals("O-O-O")) {
            return executeCastle(player, upper.equals("O-O"));
        }

        Position from = player.parseFrom(moveInput);
        Position to   = player.parseTo(moveInput);

        if (from == null || to == null) {
            System.out.println("  Error: Could not parse positions.");
            return false;
        }

        Piece piece = board.getPiece(from);

        // Check there's a piece here
        if (piece == null) {
            System.out.println("  No piece at " + from + ".");
            return false;
        }

        // Check it belongs to the current player
        if (piece.getColor() != player.getColor()) {
            System.out.println("  That piece does not belong to you.");
            return false;
        }

        // Check destination is a valid move for that piece
        boolean validDest = false;
        for (Position p : piece.possibleMoves(board.getGrid())) {
            if (p.equals(to)) {
                validDest = true;
                break;
            }
        }

        if (!validDest) {
            System.out.println("  Invalid move for " + piece.getSymbol() + ".");
            return false;
        }

        // Check the move doesn't leave own king in check
        if (!board.isLegalMove(from, to, player.getColor())) {
            System.out.println("  That move leaves your king in check.");
            return false;
        }

        // Execute the move
        Piece captured = board.movePiece(from, to);
        if (captured != null) {
            System.out.println("  " + player.getName() + " captured " + captured.getSymbol() + "!");
        }

        // Handle pawn promotion
        char promoChar = Utils.getPromotionPiece(moveInput);
        handlePromotion(to, promoChar, player.getColor());

        System.out.println("  Moved " + piece.getSymbol() + " from " + from + " to " + to + ".");
        return true;
    }

    /**
     * Handles pawn promotion after a pawn reaches the opposite back rank.
     * <p>
     * If the piece at {@code pos} is a pawn on the back rank, replaces it
     * with the promoted piece. Defaults to Queen if no valid promotion piece
     * character is provided.
     * </p>
     *
     * @param pos       the destination position of the pawn
     * @param promoChar the character representing the desired piece ('Q','R','B','N')
     * @param color     the color of the promoting player
     */
    private void handlePromotion(Position pos, char promoChar, Piece.Color color) {
        Piece p = board.getPiece(pos);
        if (!(p instanceof Pawn)) return;

        int backRank = (color == Piece.Color.WHITE) ? 0 : 7;
        if (pos.getRow() != backRank) return;

        Piece promoted;
        switch (promoChar) {
            case 'R': promoted = new Rook(color, pos);   break;
            case 'B': promoted = new Bishop(color, pos); break;
            case 'N': promoted = new Knight(color, pos); break;
            default:  promoted = new Queen(color, pos);  break; // default to Queen
        }

        board.getGrid()[pos.getRow()][pos.getCol()] = promoted;
        System.out.println("  Pawn promoted to " + promoted.getSymbol() + "!");
    }

    /**
     * Executes a castling move (kingside or queenside) for the given player.
     * <p>
     * Validates that:
     * <ul>
     *   <li>The king and rook are in their original positions and have not moved.</li>
     *   <li>Squares between them are empty.</li>
     *   <li>The king does not pass through or land on a square under attack.</li>
     * </ul>
     * </p>
     *
     * @param player    the player attempting to castle
     * @param kingside  {@code true} for kingside (O-O), {@code false} for queenside (O-O-O)
     * @return {@code true} if castling was successfully executed
     */
    private boolean executeCastle(Player player, boolean kingside) {
        Piece.Color color = player.getColor();
        int row = (color == Piece.Color.WHITE) ? 7 : 0;
        int kingCol = 4;
        int rookCol = kingside ? 7 : 0;
        int kingDest = kingside ? 6 : 2;
        int rookDest = kingside ? 5 : 3;

        Piece king = board.getGrid()[row][kingCol];
        Piece rook = board.getGrid()[row][rookCol];

        if (!(king instanceof King) || !(rook instanceof Rook)) {
            System.out.println("  Castling not available: pieces have moved or been captured.");
            return false;
        }

        if (king.getColor() != color || rook.getColor() != color) {
            System.out.println("  Castling not available.");
            return false;
        }

        // Check squares between king and rook are empty
        int minCol = Math.min(kingCol, rookCol) + 1;
        int maxCol = Math.max(kingCol, rookCol) - 1;
        for (int c = minCol; c <= maxCol; c++) {
            if (board.getGrid()[row][c] != null) {
                System.out.println("  Castling not available: pieces in the way.");
                return false;
            }
        }

        // Check king is not in check, passing through check, or landing in check
        if (board.isCheck(color)) {
            System.out.println("  Cannot castle while in check.");
            return false;
        }

        int step = kingside ? 1 : -1;
        for (int c = kingCol + step; c != kingDest + step; c += step) {
            Position testPos = new Position(row, c);
            if (!board.isLegalMove(new Position(row, kingCol), testPos, color)) {
                System.out.println("  Cannot castle through check.");
                return false;
            }
        }

        // Execute castling
        board.getGrid()[row][kingDest] = king;
        board.getGrid()[row][rookDest] = rook;
        board.getGrid()[row][kingCol]  = null;
        board.getGrid()[row][rookCol]  = null;
        king.setPosition(new Position(row, kingDest));
        rook.setPosition(new Position(row, rookDest));

        System.out.println("  " + player.getName() + " castled " + (kingside ? "kingside" : "queenside") + "!");
        return true;
    }
}
