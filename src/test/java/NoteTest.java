import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoteTest {

    @Test
    void notesTitleAndContentStorage() {
        Note note = new Note ("Title", "Content");
        assertEquals("Title", note.getTitle());
        assertEquals("Content", note.getContent());
    }

}