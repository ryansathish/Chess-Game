package utils;

/**
 * Utility class providing static helper methods for the chess game.
 * <p>
 * Contains methods for parsing chess notation, validating input format,
 * and converting between board coordinates and algebraic notation.
 * </p>
 */
public class Utils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Utils() {}

    /**
     * Parses a standard chess algebraic position string (e.g., "E2") into a
     * {@link Position} object.
     *
     * @param notation a two-character string with a letter (A–H) and digit (1–8)
     * @return the corresponding {@link Position}, or {@code null} if input is invalid
     */
    public static Position parsePosition(String notation) {
        if (notation == null || notation.length() != 2) return null;
        char fileChar = Character.toUpperCase(notation.charAt(0));
        char rankChar = notation.charAt(1);
        if (fileChar < 'A' || fileChar > 'H') return null;
        if (rankChar < '1' || rankChar > '8') return null;
        int col = fileChar - 'A';
        int row = 8 - (rankChar - '0');
        return new Position(row, col);
    }

    /**
     * Validates whether a move string is in the correct format: "XX YY"
     * where XX and YY are valid board squares (e.g., "E2 E4").
     *
     * @param move the move string entered by the player
     * @return {@code true} if the format is valid
     */
    public static boolean isValidMoveFormat(String move) {
        if (move == null) return false;
        String trimmed = move.trim().toUpperCase();

        // Handle castling notation
        if (trimmed.equals("O-O") || trimmed.equals("O-O-O")) return true;

        // Handle standard move: "E2 E4" or promotion "E7 E8=Q"
        String[] parts = trimmed.split("\\s+");
        if (parts.length != 2) return false;

        String from = parts[0];
        String to = parts[1];

        // Strip promotion suffix from destination (e.g., "E8=Q" -> "E8")
        if (to.contains("=")) {
            String[] promo = to.split("=");
            if (promo.length != 2 || promo[1].length() != 1) return false;
            char promoPiece = promo[1].charAt(0);
            if ("QRBN".indexOf(promoPiece) == -1) return false;
            to = promo[0];
        }

        return parsePosition(from) != null && parsePosition(to) != null;
    }

    /**
     * Extracts the promotion piece character from a move string, if present.
     * For example, "E7 E8=Q" returns 'Q'.
     *
     * @param move the move string entered by the player
     * @return the promotion character, or {@code '\0'} if no promotion is specified
     */
    public static char getPromotionPiece(String move) {
        if (move == null) return '\0';
        String upper = move.trim().toUpperCase();
        if (upper.contains("=")) {
            int idx = upper.indexOf('=');
            if (idx + 1 < upper.length()) {
                return upper.charAt(idx + 1);
            }
        }
        return '\0';
    }

    /**
     * Checks whether a given row and column are within the valid board bounds (0–7).
     *
     * @param row the row index to check
     * @param col the column index to check
     * @return {@code true} if both indices are within [0, 7]
     */
    public static boolean inBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
