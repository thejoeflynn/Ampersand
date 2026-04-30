import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Notes2 {
    public static String readNote(Path notesDir, String noteId) throws IOException {
        Path noteFile = notesDir.resolve(noteId + ".md");
        return Files.readString(noteFile);
    }
}
