import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Future Proof Notes Manager - Version Zero
 * A personal notes manager using text files with YAML headers.
 * Interactive shell version.
 */
public class NotesShell {

    private static final Path NOTES_DIR = Path.of(System.getProperty("user.home"), ".notes");

    /**
     * Initialize the notes application.
     */
    private static void setup() {
        System.out.println("Future Proof Notes Manager v0.0");
        System.out.println("========================================");

        // Check if notes directory exists
        if (!Files.exists(NOTES_DIR)) {
            System.out.println("Notes directory not found at " + NOTES_DIR);
            System.out.println("Run 'notes init' to create it.");
        } else {
            System.out.println("Notes directory: " + NOTES_DIR);
        }

        System.out.println();
    }

    /**
     * Display help information.
     */
    private static void showHelp() {
        String helpText = """

                Available commands:
                  help    - Display this help information
                  quit    - Exit the application
                """;
        System.out.println(helpText);
    }

    /**
     * Main command loop for processing user input.
     */
    private static void commandLoop() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("notes> ");

                // Check for EOF
                if (!scanner.hasNextLine()) {
                    System.out.println();
                    break;
                }

                String command = scanner.nextLine().trim().toLowerCase();

                // Handle empty input
                if (command.isEmpty()) {
                    continue;
                }

                // Process commands
                switch (command) {
                    case "quit":
                        return;
                    case "help":
                        showHelp();
                        break;
                    default:
                        System.out.println("Unknown command: '" + command + "'");
                        System.out.println("Type 'help' for available commands.");
                }
            }
        } catch (Exception e) {
            System.out.println("\nUse 'quit' to exit.");
        }
    }

    /**
     * Clean up and exit the application.
     */
    private static void finish() {
        System.out.println("\nGoodbye!");
    }

    /**
     * Main entry point for the notes application.
     */
    public static void main(String[] args) {
        // Setup
        setup();

        // Command loop
        commandLoop();

        // Finish
        finish();
    }
}
