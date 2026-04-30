import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Notes2Test {

    @Test
    void readNote_returnsParsedNote(@TempDir Path tempDir) throws Exception {
        Path noteFile = tempDir.resolve("my-note.md");
        Files.writeString(noteFile, """
                ---
                title: Test Note
                author: Joe
                ---
                Hello, world!
                """);

        Note note = Notes2.readNote(tempDir, "my-note");

        assertEquals("Test Note", note.getTitle());
        assertEquals("Joe", note.getAuthor());
    }
}
