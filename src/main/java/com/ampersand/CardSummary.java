package com.ampersand;

import java.time.LocalDateTime;
import java.util.List;

public record CardSummary(
    String id,
    String title,
    String description,
    String boardId,
    String column,
    int position,
    List<String> tags,
    LocalDateTime created,
    LocalDateTime modified,
    LocalDateTime dueDate
) {
    public static CardSummary from(String id, Card card) {
        return new CardSummary(
            id,
            card.getTitle(),
            card.getDescription(),
            card.getBoardId(),
            card.getColumn(),
            card.getPosition(),
            card.getTags(),
            card.getCreated(),
            card.getModified(),
            card.getDueDate()
        );
    }
}
