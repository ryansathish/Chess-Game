package player;

import pieces.Piece;
import utils.Position;
import utils.Utils;
import board.Board;

import java.util.Scanner;

/**
 * Represents a chess player (either human controlling white or black pieces).
 * <p>
 * The Player is responsible for prompting the user for input, parsing
 * the entered move notation, and validating the format before passing
 * the move to the Game for execution.
 * </p>
 */
public class Player {

    /** The color assigned to this player (WHITE or BLACK). */
    private Piece.Color color;

    /** Scanner used to read console input from this player. */
    private Scanner scanner;

    /**
     * Constructs a Player with the given color.
     * Uses {@link System#in} as the input source.
     *
     * @param color the color of this player's pieces (WHITE or BLACK)
     */
    public Player(Piece.Color color) {
        this.color   = color;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Returns the color of this player.
     *
     * @return the player's color (WHITE or BLACK)
     */
    public Piece.Color getColor() {
        return color;
    }

    /**
     * Returns a display-friendly name for this player based on their color.
     *
     * @return "White" or "Black"
     */
    public String getName() {
        return color == Piece.Color.WHITE ? "White" : "Black";
    }

    /**
     * Prompts this player to enter a move and reads input from the console.
     * <p>
     * Keeps prompting until the player enters a string that passes basic
     * format validation (see {@link Utils#isValidMoveFormat(String)}).
     * </p>
     *
     * @param board the current board state (displayed before prompting)
     * @return the raw move string entered by the player (e.g., "E2 E4")
     */
    public String makeMove(Board board) {
        String input;
        while (true) {
            System.out.print(getName() + "'s move (e.g. E2 E4): ");
            input = scanner.nextLine().trim();

            if (Utils.isValidMoveFormat(input)) {
                return input;
            } else {
                System.out.println("  Invalid format. Use 'E2 E4', 'O-O', or 'O-O-O'.");
            }
        }
    }

    /**
     * Parses the source position from a valid move string.
     *
     * @param move a valid move string (e.g., "E2 E4")
     * @return the source {@link Position}, or {@code null} for castling moves
     */
    public Position parseFrom(String move) {
        String upper = move.trim().toUpperCase();
        if (upper.equals("O-O") || upper.equals("O-O-O")) return null;
        return Utils.parsePosition(upper.split("\\s+")[0]);
    }

    /**
     * Parses the destination position from a valid move string.
     * Strips any promotion suffix (e.g., "E8=Q" becomes "E8").
     *
     * @param move a valid move string (e.g., "E7 E8=Q")
     * @return the destination {@link Position}, or {@code null} for castling moves
     */
    public Position parseTo(String move) {
        String upper = move.trim().toUpperCase();
        if (upper.equals("O-O") || upper.equals("O-O-O")) return null;
        String dest = upper.split("\\s+")[1];
        if (dest.contains("=")) {
            dest = dest.split("=")[0];
        }
        return Utils.parsePosition(dest);
    }
}
