import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

    @Test
    void writeNote_writesNoteToDisk(@TempDir Path tempDir) throws Exception {
        Note note = new Note("Test Note", "Hello, world!", "Joe", null, null, null);

        Notes2.writeNote(tempDir, "test-note", note);

        String written = Files.readString(tempDir.resolve("test-note.md"));
        assertTrue(written.contains("title: Test Note"));
        assertTrue(written.contains("author: Joe"));
        assertTrue(written.contains("Hello, world!"));
    }

    @Test
    void updateNotes_replacesBodyContent(@TempDir Path tempDir) throws Exception {
        //Existing note
        Files.writeString(tempDir.resolve("my-note.md"),"""
                ---
                title: My Note
                ---
                Original Paragraph
                ---      
                """);
                
        //Update the note
        Notes2.updateNote(tempDir, "my-note", "New better sleeker generally cooler paragraph");

        //verify update and file originality
        String written = Files.readString(tempDir.resolve("my-note.md"));
        assertTrue(written.contains("title: My Note"));
        assertTrue(written.contains("New better sleeker generally cooler paragraph"));
        assertFalse(written.contains("Original Paragraph"));
    }

    @Test
    void updateNote_bumpsModifiedTimestamp(@TempDir Path tempDir) throws Exception {
        LocalDateTime oldModified = LocalDateTime.of(2020, 1, 1, 0, 0);
        Note original = new Note("My Note", "Old body", null, null, oldModified, null);
        Notes2.writeNote(tempDir, "my-note", original);

        Notes2.updateNote(tempDir, "my-note", "New body");

        Note updated = Notes2.readNote(tempDir, "my-note");
        assertTrue(updated.getModified().isAfter(oldModified));
    }

    @Test
    void deleteNote_removesFileFromDisk(@TempDir Path tempDir) throws Exception {
        Path noteFile = tempDir.resolve("doomed-note.md");
        Files.writeString(noteFile, """
                ---
                title: Doomed
                ---
                Body
                """);
        assertTrue(Files.exists(noteFile));

        Notes2.deleteNote(tempDir, "doomed-note");

        assertFalse(Files.exists(noteFile));
    }
}
