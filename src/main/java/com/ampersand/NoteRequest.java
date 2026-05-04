package com.ampersand;

import java.util.List;

public record NoteRequest(
    String id,
    String title,
    String content,
    String author,
    List<String> tags
) {}
