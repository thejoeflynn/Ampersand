import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Notes2 {
    public static Note readNote(Path notesDir, String noteId) throws IOException {
        Path noteFile = notesDir.resolve(noteId + ".md");
        String content = Files.readString(noteFile);
        return NoteParser.parse(content);
    }

    public static void writeNote(Path notesDir, String noteId, Note note) throws IOException {
        Path noteFile = notesDir.resolve(noteId + ".md");
        String content = NoteSerializer.serialize(note);
        Files.writeString(noteFile, content);
    }

    public static void updateNote(Path notesDir, String noteId, String newContent) throws IOException {
        Note existing = readNote(notesDir, noteId);
        Note updated = new Note(
            existing.getTitle(),
            newContent,
            existing.getAuthor(),
            existing.getCreated(),
            LocalDateTime.now(),
            existing.getTags()
        );
        writeNote(notesDir, noteId, updated);
    }

    public static void deleteNote(Path notesDir, String noteId) throws IOException {
        Path noteFile = notesDir.resolve(noteId + ".md");
        Files.delete(noteFile);
    }

    public static List<Note> searchByTitle(Path notesDir, String query) throws IOException {
        List<Note> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        List<Path> files;
        try (Stream<Path> paths = Files.list(notesDir)) {
            files = paths.filter(p -> p.getFileName().toString().endsWith(".md")).toList();
        }

        for (Path file : files) {
            Note note = NoteParser.parse(Files.readString(file));
            if (note.getTitle() != null && note.getTitle().toLowerCase().contains(lowerQuery)) {
                results.add(note);
            }
        }

        return results;
    }

    public static List<Note> search(Path notesDir, String query) throws IOException {
        List<Note> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        List<Path> files;
        try (Stream<Path> paths = Files.list(notesDir)) {
            files = paths.filter(p -> p.getFileName().toString().endsWith(".md")).toList();
        }

        for (Path file : files) {
            Note note = NoteParser.parse(Files.readString(file));
            boolean titleMatch = note.getTitle() != null && note.getTitle().toLowerCase().contains(lowerQuery);
            boolean contentMatch = note.getContent() != null && note.getContent().toLowerCase().contains(lowerQuery);
            boolean tagMatch = note.getTags() != null && note.getTags().stream()
                    .anyMatch(tag -> tag.toLowerCase().contains(lowerQuery));
            if (titleMatch || contentMatch || tagMatch) {
                results.add(note);
            }
        }

        return results;
    }
}
