package com.ampersand;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NotesController {

    private static final Path NOTES_DIR = Path.of(System.getProperty("user.home"), ".notes", "notes");

    @GetMapping
    public List<Note> listAll() throws IOException {
        return Notes2.listAll(NOTES_DIR);
    }

    @GetMapping("/{id}")
    public Note readOne(@PathVariable String id) throws IOException {
        return Notes2.readNote(NOTES_DIR, id);
    }

    @PostMapping
    public Note create(@RequestBody NoteRequest req) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        Note note = new Note(req.title(), req.content(), req.author(), now, now, req.tags());
        Notes2.writeNote(NOTES_DIR, req.id(), note);
        return note;
    }
}
