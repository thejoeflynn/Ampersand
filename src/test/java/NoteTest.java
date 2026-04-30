import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NoteTest {

    @Test
    void noteTitleAndContentStorage() {
        Note note = new Note ("Title", "Content", "Rip Van Winkle", LocalDateTime.now(), LocalDateTime.now());
        assertEquals("Title", note.getTitle());
        assertEquals("Content", note.getContent());
    }

    @Test
    void noteRejectEmptyTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
        new Note("", "Some content", "Rip Van Winkle", LocalDateTime.now(), LocalDateTime.now());
        });
    }

    @Test
    void noteStoresAuthor(){
        Note note = new Note("Title", "Content", "Rip Van Winkle", LocalDateTime.now(), LocalDateTime.now());
        assertEquals("Rip Van Winkle", note.getAuthor());
    }

    @Test
    void noteStoresCreatedTimestamp(){
        LocalDateTime now = LocalDateTime.now();
        Note note = new Note("Title", "Content", "Joe", now, now);
        assertEquals(now, note.getCreated());
    }

    @Test
    void noteStoresModifiedTimestamp(){
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime modified = LocalDateTime.of(2026, 4, 30, 0, 0);
        Note note = new Note("Title", "Content", "Joe", created, modified);
        assertEquals(modified, note.getModified());
    }
}