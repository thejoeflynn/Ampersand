import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NoteTest {

    @Test
    void notesTitleAndContentStorage() {
        Note note = new Note ("Title", "Content");
        assertEquals("Title", note.getTitle());
        assertEquals("Content", note.getContent());
    }

    @Test
    void notesRejectEmptyTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
        new Note("", "Some content");
        });
    }

    @Test
    void noteStoresAuthor(){
        Note note = new Note("Title", "Content", "Rip Van Winkle");
        assertEquals("Rip Van Winkle", note.getAuthor());
    }

    
}