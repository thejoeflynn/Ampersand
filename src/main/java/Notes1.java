import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Future Proof Notes Manager - Version One (CLI)
 * A personal notes manager using text files with YAML headers.
 * Command-line interface version with 'list' command.
 *
 * SETUP REMINDER:
 * Before running the 'list' command, copy the test notes to your notes directory:
 *     cp -r test-notes/* ~/.notes/
 * or create the directory structure:
 *     mkdir -p ~/.notes/notes
 *     cp test-notes/*.md ~/.notes/notes/
 */

public class Notes1 {

    private static final Path NOTES_DIR = Path.of(System.getProperty("user.home"), ".notes");

    private static Path setup() {
        return NOTES_DIR;
    }

    private static Map<String, String> parseYamlHeader(Path filePath) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("file", filePath.getFileName().toString());

        try {
            List<String> lines = Files.readAllLines(filePath);

            if (lines.isEmpty() || !lines.get(0).trim().equals("---")) {
                metadata.put("title", filePath.getFileName().toString());
                return metadata;
            }

            int yamlEnd = -1;
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).trim().equals("---")) {
                    yamlEnd = i;
                    break;
                }
            }

            if (yamlEnd == -1) {
                metadata.put("title", filePath.getFileName().toString());
                return metadata;
            }

            for (int i = 1; i < yamlEnd; i++) {
                String line = lines.get(i).trim();
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    metadata.put(key, value);
                }
            }

        } catch (IOException e) {
            metadata.put("error", e.getMessage());
        }

        return metadata;
    }

    private static boolean listNotes(Path notesDir) {
        // Check if notes directory exists
        if (!Files.exists(notesDir)) {
            System.err.println("Error: Notes directory does not exist: " + notesDir);
            System.err.println("Create it with: mkdir -p ~/.notes/notes");
            System.err.println("Then copy test notes: cp test-notes/*.md ~/.notes/notes/");
            return false;
        }

        Path notesSubdir = notesDir.resolve("notes");
        Path searchDir = Files.exists(notesSubdir) ? notesSubdir : notesDir;

        List<Path> noteFiles;
        try (Stream<Path> paths = Files.walk(searchDir, 1)) {
            noteFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.endsWith(".md") || name.endsWith(".note") || name.endsWith(".txt");
                    })
                    .sorted()
                    .toList();
        } catch (IOException e) {
            System.err.println("Error reading notes directory: " + e.getMessage());
            return false;
        }

        if (noteFiles.isEmpty()) {
            System.out.println("No notes found in " + notesDir);
            System.err.println("Copy test notes with: cp test-notes/*.md ~/.notes/");
            return true;
        }

        System.out.println("Notes in " + notesDir + ":");
        System.out.println("=".repeat(60));

        for (Path noteFile : noteFiles) {
            Map<String, String> metadata = parseYamlHeader(noteFile);
            String title = metadata.getOrDefault("title", noteFile.getFileName().toString());
            String created = metadata.getOrDefault("created", "N/A");
            String tags = metadata.getOrDefault("tags", "");

            System.out.println("\n" + noteFile.getFileName());
            System.out.println("  Title: " + title);
            if (!created.equals("N/A")) {
                System.out.println("  Created: " + created);
            }
            if (!tags.isEmpty()) {
                System.out.println("  Tags: " + tags);
            }
        }

        System.out.println("\n" + noteFiles.size() + " note(s) found.");
        return true;
    }

    private static boolean readNote(Path notesDir, String noteId) {
        Path notesSubdir = notesDir.resolve("notes");
        Path searchDir = Files.exists(notesSubdir) ? notesSubdir : notesDir;

        try {
            Note note = Notes2.readNote(searchDir, noteId);
            System.out.println("Title:    " + note.getTitle());
            if (note.getAuthor() != null) System.out.println("Author:   " + note.getAuthor());
            if (note.getCreated() != null) System.out.println("Created:  " + note.getCreated());
            if (note.getModified() != null) System.out.println("Modified: " + note.getModified());
            if (note.getTags() != null) System.out.println("Tags:     " + note.getTags());
            System.out.println();
            System.out.println(note.getContent());
            return true;
        } catch (IOException e) {
            System.err.println("Error reading note '" + noteId + "': " + e.getMessage());
            return false;
        }
    }

    private static boolean newNote(Path notesDir, String noteId) {
        Path notesSubdir = notesDir.resolve("notes");
        Path searchDir = Files.exists(notesSubdir) ? notesSubdir : notesDir;

        try {
            if (!Files.exists(searchDir)) {
                Files.createDirectories(searchDir);
            }
            LocalDateTime now = LocalDateTime.now();
            Note note = new Note(noteId, "", null, now, now, null);
            Notes2.writeNote(searchDir, noteId, note);
            System.out.println("Created note: " + noteId);
            return true;
        } catch (IOException e) {
            System.err.println("Error creating note: " + e.getMessage());
            return false;
        }
    }

    private static boolean updateNote(Path notesDir, String noteId, String newContent) {
        Path notesSubdir = notesDir.resolve("notes");
        Path searchDir = Files.exists(notesSubdir) ? notesSubdir : notesDir;

        try {
            Notes2.updateNote(searchDir, noteId, newContent);
            System.out.println("Updated note: " + noteId);
            return true;
        } catch (IOException e) {
            System.err.println("Error updating note: " + e.getMessage());
            return false;
        }
    }

    private static boolean deleteNote(Path notesDir, String noteId) {
        Path notesSubdir = notesDir.resolve("notes");
        Path searchDir = Files.exists(notesSubdir) ? notesSubdir : notesDir;

        System.out.print("Delete note '" + noteId + "'? (y/N): ");
        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine().trim().toLowerCase();
        if (!answer.equals("y") && !answer.equals("yes")) {
            System.out.println("Cancelled.");
            return true;
        }

        try {
            Notes2.deleteNote(searchDir, noteId);
            System.out.println("Deleted note: " + noteId);
            return true;
        } catch (IOException e) {
            System.err.println("Error deleting note: " + e.getMessage());
            return false;
        }
    }

    private static boolean editNote(Path notesDir, String noteId) {
        Path notesSubdir = notesDir.resolve("notes");
        Path searchDir = Files.exists(notesSubdir) ? notesSubdir : notesDir;

        try {
            Note existing = Notes2.readNote(searchDir, noteId);
            Path tempFile = Files.createTempFile("ampersand-edit-", ".md");
            Files.writeString(tempFile, existing.getContent());

            String editor = System.getenv("EDITOR");
            if (editor == null || editor.isEmpty()) editor = "vi";

            ProcessBuilder pb = new ProcessBuilder(editor, tempFile.toString());
            pb.inheritIO();
            int exitCode = pb.start().waitFor();

            if (exitCode != 0) {
                Files.deleteIfExists(tempFile);
                System.err.println("Editor exited due to error.");
                return false;
            }

            String newContent = Files.readString(tempFile);
            Files.deleteIfExists(tempFile);

            Notes2.updateNote(searchDir, noteId, newContent);
            System.out.println("Updated note: " + noteId);
            return true;
        } catch (IOException | InterruptedException e) {
            System.err.println("Error editing note: " + e.getMessage());
            return false;
        }
    }

    private static boolean searchNotes(Path notesDir, String query) {
        Path notesSubdir = notesDir.resolve("notes");
        Path searchDir = Files.exists(notesSubdir) ? notesSubdir : notesDir;

        try {
            List<Note> results = Notes2.search(searchDir, query);
            if (results.isEmpty()) {
                System.out.println("No notes match '" + query + "'.");
                return true;
            }
            System.out.println("Found " + results.size() + " note(s) matching '" + query + "':");
            for (Note note : results) {
                System.out.println("  - " + note.getTitle());
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error searching: " + e.getMessage());
            return false;
        }
    }

    private static void showHelp() {
        String helpText = String.format("""
                Future Proof Notes Manager v0.1

                Usage: java Notes1 [command]

                Available commands:
                  help        - Display this help information
                  list        - List all notes in the notes directory
                  read <id>   - Read and display a note by id
                  new <id>    - Create a new note with the given id
                  update <id> <content...> - Replace a note's body content
                  delete <id> - Delete a note by id
                  search <query> - Search notes by title (case-insensitive)
                  edit <id>   - Open a note in your $EDITOR for interactive editing

                Notes directory: %s

                Setup:
                  To test the 'list' command, copy sample notes:
                    mkdir -p ~/.notes/notes
                    cp test-notes/*.md ~/.notes/notes/
                """, NOTES_DIR);
        System.out.println(helpText.trim());
    }

    private static void finish(int exitCode) {
        System.exit(exitCode);
    }

    public static void main(String[] args) {
        // Setup
        Path notesDir = setup();

        // Parse command-line arguments
        if (args.length < 1) {
            // No command provided
            System.err.println("Error: No command provided.");
            System.err.println("Usage: java Notes1 [command]");
            System.err.println("Try 'java Notes1 help' for more information.");
            finish(1);
        }

        String command = args[0].toLowerCase();

        // Process command
        switch (command) {
            case "help":
                showHelp();
                finish(0);
                break;
            case "list":
                boolean success = listNotes(notesDir);
                finish(success ? 0 : 1);
                break;
            case "read":
                if (args.length < 2) {
                    System.err.println("Usage: java Notes1 read <id>");
                    finish(1);
                }
                boolean readSuccess = readNote(notesDir, args[1]);
                finish(readSuccess ? 0 : 1);
                break;
            case "new":
                if (args.length < 2) {
                    System.err.println("Usage: java Notes1 new <id>");
                    finish(1);
                }
                boolean newSuccess = newNote(notesDir, args[1]);
                finish(newSuccess ? 0 : 1);
                break;
            case "update":
                if (args.length < 3) {
                    System.err.println("Usage: java Notes1 update <id> <content...>");
                    finish(1);
                }
                String newContent = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                boolean updateSuccess = updateNote(notesDir, args[1], newContent);
                finish(updateSuccess ? 0 : 1);
                break;
            case "delete":
                if (args.length < 2) {
                    System.err.println("Usage: java Notes1 delete <id>");
                    finish(1);
                }
                boolean deleteSuccess = deleteNote(notesDir, args[1]);
                finish(deleteSuccess ? 0 : 1);
                break;
            case "search":
                if (args.length < 2) {
                    System.err.println("Usage: java Notes1 search <query>");
                    finish(1);
                }
                boolean searchSuccess = searchNotes(notesDir, args[1]);
                finish(searchSuccess ? 0 : 1);
                break;
            case "edit":
                if (args.length < 2) {
                    System.err.println("Usage: java Notes1 edit <id>");
                    finish(1);
                }
                boolean editSuccess = editNote(notesDir, args[1]);
                finish(editSuccess ? 0 : 1);
                break;
            default:
                System.err.println("Error: Unknown command '" + command + "'");
                System.err.println("Try 'java Notes1 help' for more information.");
                finish(1);
        }
    }
}
