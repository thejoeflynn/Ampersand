package com.ampersand;

import java.time.LocalDateTime;
import java.util.List;

public class Note {

    private String title;
    private String content;
    private String author;
    private LocalDateTime created;
    private LocalDateTime modified;
    private List<String> tags;
    private String folder;

    /** Backward-compatible 6-arg constructor; folder defaults to null. */
    public Note(String title, String content, String author, LocalDateTime created, LocalDateTime modified, List<String> tags) {
        this(title, content, author, created, modified, tags, null);
    }

    public Note(String title, String content, String author, LocalDateTime created, LocalDateTime modified, List<String> tags, String folder) {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        this.title = title;
        this.content = content;
        this.author = author;
        this.created = created;
        this.modified = modified;
        this.tags = tags;
        this.folder = folder;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getFolder() {
        return folder;
    }
}
