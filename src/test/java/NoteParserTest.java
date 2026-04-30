import org.junit.jupiter.api.Test;
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
}
