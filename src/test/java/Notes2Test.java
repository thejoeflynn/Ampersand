import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Notes2.readNote method.
 *
 * TDD: these tests describe what readNote SHOULD do.
 * The implementation lives in Notes2.java.
 */
class Notes2Test {

    @Test
    void readNote_returnsContentForExistingNote(@TempDir Path tempDir) throws Exception {
        // Arrange: drop a fake note file into a fresh temp directory
        Path noteFile = tempDir.resolve("my-note.md");
        String fakeNoteContent = """
                ---
                title: Test Note
                ---

                Hello, world!
                """;
        Files.writeString(noteFile, fakeNoteContent);

        // Act: ask Notes2 to read the note by its ID (filename without extension)
        String result = Notes2.readNote(tempDir, "my-note");

        // Assert: the result should include both the title and the body
        assertNotNull(result, "readNote should return content, not null");
        assertTrue(result.contains("Test Note"), "result should contain the title");
        assertTrue(result.contains("Hello, world!"), "result should contain the body");
    }
}
