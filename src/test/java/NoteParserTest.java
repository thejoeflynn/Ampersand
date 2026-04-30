import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
