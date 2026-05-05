package com.ampersand;

import java.time.LocalDateTime;
import java.util.List;

public record CardRequest(
    String id,
    String title,
    String description,
    String boardId,
    String column,
    Integer position,
    List<String> tags,
    LocalDateTime dueDate
) {}
