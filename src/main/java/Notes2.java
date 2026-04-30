import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Notes2 {
    public static Note readNote(Path notesDir, String noteId) throws IOException {
        Path noteFile = notesDir.resolve(noteId + ".md");
        String content = Files.readString(noteFile);
        return NoteParser.parse(content);
    }
}
