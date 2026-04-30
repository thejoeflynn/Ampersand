import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NoteTest {

    @Test
    void noteTitleAndContentStorage() {
        Note note = new Note ("Title", "Content", "Rip Van Winkle", LocalDateTime.now(), LocalDateTime.now(), List.of());
        assertEquals("Title", note.getTitle());
        assertEquals("Content", note.getContent());
    }

    @Test
    void noteRejectEmptyTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
        new Note("", "Some content", "Rip Van Winkle", LocalDateTime.now(), LocalDateTime.now(), List.of());
        });
    }

    @Test
    void noteStoresAuthor(){
        Note note = new Note("Title", "Content", "Rip Van Winkle", LocalDateTime.now(), LocalDateTime.now(), List.of());
        assertEquals("Rip Van Winkle", note.getAuthor());
    }

    @Test
    void noteStoresCreatedTimestamp(){
        LocalDateTime now = LocalDateTime.now();
        Note note = new Note("Title", "Content", "Joe", now, now, List.of());
        assertEquals(now, note.getCreated());
    }

    @Test
    void noteStoresModifiedTimestamp(){
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime modified = LocalDateTime.of(2026, 4, 30, 0, 0);
        Note note = new Note("Title", "Content", "Joe", created, modified, List.of());
        assertEquals(modified, note.getModified());
    }

    @Test
    void noteStoresTags(){
        List<String> tags = List.of("important", "zipcode", "babproductions", "capeathletic");
        Note note = new Note("Title", "Content", "Joe",
            LocalDateTime.now(), LocalDateTime.now(), tags);
        assertEquals(tags, note.getTags());
    }
}