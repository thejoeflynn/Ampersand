import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Future Proof Notes Manager - Version Zero (CLI)
 * A personal notes manager using text files with YAML headers.
 * Command-line interface version.
 */
public class Notes0 {

    private static final Path NOTES_DIR = Path.of(System.getProperty("user.home"), ".notes");

    /**
     * Initialize the notes application.
     */
    private static Path setup() {
        // Define the notes directory in HOME
        // Check if notes directory exists (silent check for CLI version)
        // It will be shown if needed by specific commands
        return NOTES_DIR;
    }

    /**
     * Display help information.
     */
    private static void showHelp() {
        String helpText = String.format("""
                Future Proof Notes Manager v0.0

                Usage: java Notes0 [command]

                Available commands:
                  help    - Display this help information

                Notes directory: %s
                """, NOTES_DIR);
        System.out.println(helpText.trim());
    }

    /**
     * Clean up and exit the application.
     */
    private static void finish(int exitCode) {
        System.exit(exitCode);
    }

    /**
     * Main entry point for the notes CLI application.
     */
    public static void main(String[] args) {
        // Setup
        Path notesDir = setup();

        // Parse command-line arguments
        if (args.length < 1) {
            // No command provided
            System.err.println("Error: No command provided.");
            System.err.println("Usage: java Notes0 [command]");
            System.err.println("Try 'java Notes0 help' for more information.");
            finish(1);
        }

        String command = args[0].toLowerCase();

        // Process command
        switch (command) {
            case "help":
                showHelp();
                finish(0);
                break;
            default:
                System.err.println("Error: Unknown command '" + command + "'");
                System.err.println("Try 'java Notes0 help' for more information.");
                finish(1);
        }
    }
}
