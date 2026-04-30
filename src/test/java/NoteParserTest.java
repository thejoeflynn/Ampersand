import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoteParserTest {

    @Test
    void parsesTitleFromYamlFrontmatter() {
        String input = """
                ---
                title: My Note
                ---
                Hello, world!
                """;
        Note note = NoteParser.parse(input);
        assertEquals("My Note", note.getTitle());
    }

    @Test
    void parsesAuthorFromYamlFrontmatter() {
        String input = """
                ---
                title: My Note
                author: Joe
                ---
                Hello, world!
                """;
        Note note = NoteParser.parse(input);
        assertEquals("Joe", note.getAuthor());
    }

    @Test
    void parsesCreatedFromYamlFrontmatter() {
        String input = """
                ---
                title: My Note
                created: 2026-01-01T00:00:00
                ---
                Hello, world!
                """;
        Note note = NoteParser.parse(input);
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), note.getCreated());
    }

    @Test
    void parsesModifiedFromYamlFrontmatter() {
        String input = """
                ---
                title: My Note
                modified: 2026-04-30T12:00:00
                ---
                Hello, world!
                """;
        Note note = NoteParser.parse(input);
        assertEquals(LocalDateTime.of(2026,4, 30, 12, 0), note.getModified());
    }

    @Test
    void parsesTagsFromYamlFrontmatter() {
        String input = """
                ---
                title: My Note
                tags: [zipcode, important, notes]
                ---
                Hello, world!
                """;
        Note note = NoteParser.parse(input);
        assertEquals(List.of("zipcode", "important", "notes"), note.getTags());
    }

    @Test
    void parsesBodyContent(){
        String input = """
                ---
                title: My Note
                ---
                Hello, world!
                This is the body paragraph yeah.
                """;
        Note note = NoteParser.parse(input);
        assertTrue(note.getContent().contains("Hello, world!"));
        assertTrue(note.getContent().contains("This is the body paragraph yeah."));
    }

}
