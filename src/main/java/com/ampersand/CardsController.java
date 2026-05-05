package com.ampersand;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardsController {

    private static final Path CARDS_DIR = Path.of(System.getProperty("user.home"), ".notes", "cards");

    @GetMapping
    public List<CardSummary> list(@RequestParam(required = false) String board) throws IOException {
        if (board != null && !board.isEmpty()) return Cards.listByBoard(CARDS_DIR, board);
        return Cards.listAll(CARDS_DIR);
    }

    @GetMapping("/{id}")
    public Card readOne(@PathVariable String id) throws IOException {
        return Cards.readCard(CARDS_DIR, id);
    }

    @PostMapping
    public Card create(@RequestBody CardRequest req) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        int position = req.position() != null ? req.position() : 0;
        String column = req.column() != null ? req.column() : "To Do";
        Card card = new Card(
            req.title(),
            req.description(),
            req.boardId(),
            column,
            position,
            req.tags(),
            now,
            now,
            req.dueDate()
        );
        Cards.writeCard(CARDS_DIR, req.id(), card);
        return card;
    }

    @PutMapping("/{id}")
    public Card update(@PathVariable String id, @RequestBody CardRequest req) throws IOException {
        Card existing = Cards.readCard(CARDS_DIR, id);
        Card updated = new Card(
            req.title() != null ? req.title() : existing.getTitle(),
            req.description() != null ? req.description() : existing.getDescription(),
            req.boardId() != null ? req.boardId() : existing.getBoardId(),
            req.column() != null ? req.column() : existing.getColumn(),
            req.position() != null ? req.position() : existing.getPosition(),
            req.tags() != null ? req.tags() : existing.getTags(),
            existing.getCreated(),
            LocalDateTime.now(),
            req.dueDate() != null ? req.dueDate() : existing.getDueDate()
        );
        Cards.writeCard(CARDS_DIR, id, updated);
        return updated;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) throws IOException {
        Cards.deleteCard(CARDS_DIR, id);
    }
}
