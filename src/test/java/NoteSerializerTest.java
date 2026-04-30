import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoteSerializerTest {

    @Test
    void serializesTitleToYamlFrontmatter() {
        Note note = new Note("My Note", "Body content", null, null, null, null);
        String output = NoteSerializer.serialize(note);
        assertTrue(output.contains("title: My Note"));
    }

    @Test
    void serializesAuthorToYamlFrontmatter() {
        Note note = new Note("My Note", "Body", "Joe", null, null, null);
        String output = NoteSerializer.serialize(note);
        assertTrue(output.contains("author: Joe"));
    }

    @Test
    void serializesCreatedToYamlFrontmatter() {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 0, 0);
        Note note = new Note("My Note", "Body", null, created, null, null);
        String output = NoteSerializer.serialize(note);
        assertTrue(output.contains("created: 2026-01-01T00:00"));
    }

    @Test
    void serializesModifiedToYamlFrontmatter() {
        LocalDateTime modified = LocalDateTime.of(2026, 4, 30, 12, 0);
        Note note = new Note("My Note", "Body", null, null, modified, null);
        String output = NoteSerializer.serialize(note);
        assertTrue(output.contains("modified: 2026-04-30T12:00"));
    }

    @Test
    void serializesTagsToYamlFrontmatter() {
        Note note = new Note("My Note", "Body", null, null, null, List.of("java", "bootcamp"));
        String output = NoteSerializer.serialize(note);
        assertTrue(output.contains("tags: [java, bootcamp]"));
    }
}
