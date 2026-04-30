import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

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
}
