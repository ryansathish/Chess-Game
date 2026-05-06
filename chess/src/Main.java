import game.Game;

/**
 * Entry point for the Console Chess application.
 * <p>
 * Creates a new {@link Game} instance and starts the game loop.
 * </p>
 */
public class Main {

    /**
     * Main method — launches the chess game.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Game game = new Game();
        game.play();
    }
}
