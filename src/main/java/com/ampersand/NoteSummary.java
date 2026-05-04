package com.ampersand;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Flat DTO for note list responses — same shape as Note, but with the id
 * (filename) included so the frontend can address each note.
 */
public record NoteSummary(
    String id,
    String title,
    String content,
    String author,
    LocalDateTime created,
    LocalDateTime modified,
    List<String> tags,
    String folder
) {
    public static NoteSummary from(String id, Note note) {
        return new NoteSummary(
            id,
            note.getTitle(),
            note.getContent(),
            note.getAuthor(),
            note.getCreated(),
            note.getModified(),
            note.getTags(),
            note.getFolder()
        );
    }
}
