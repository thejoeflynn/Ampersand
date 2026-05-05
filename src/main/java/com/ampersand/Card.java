package com.ampersand;

import java.time.LocalDateTime;
import java.util.List;

public class Card {

    private String title;
    private String description;
    private String boardId;
    private String column;
    private int position;
    private List<String> tags;
    private LocalDateTime created;
    private LocalDateTime modified;
    private LocalDateTime dueDate;

    public Card(String title, String description, String boardId, String column, int position,
                List<String> tags, LocalDateTime created, LocalDateTime modified, LocalDateTime dueDate) {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        this.title = title;
        this.description = description;
        this.boardId = boardId;
        this.column = column;
        this.position = position;
        this.tags = tags;
        this.created = created;
        this.modified = modified;
        this.dueDate = dueDate;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getBoardId() { return boardId; }
    public String getColumn() { return column; }
    public int getPosition() { return position; }
    public List<String> getTags() { return tags; }
    public LocalDateTime getCreated() { return created; }
    public LocalDateTime getModified() { return modified; }
    public LocalDateTime getDueDate() { return dueDate; }
}
