package com.ampersand;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CardParser {
    public static Card parse(String input) {
        String title = null;
        String description;
        String boardId = null;
        String column = null;
        int position = 0;
        List<String> tags = null;
        LocalDateTime created = null;
        LocalDateTime modified = null;
        LocalDateTime dueDate = null;
        StringBuilder body = new StringBuilder();

        boolean seenOpener = false;
        boolean inBody = false;

        for (String line : input.split("\n")) {
            if (line.equals("---")) {
                if (!seenOpener) seenOpener = true;
                else inBody = true;
                continue;
            }
            if (inBody) {
                body.append(line).append("\n");
                continue;
            }
            if (line.startsWith("title:")) {
                title = line.substring("title:".length()).trim();
            } else if (line.startsWith("boardId:")) {
                boardId = line.substring("boardId:".length()).trim();
            } else if (line.startsWith("column:")) {
                column = line.substring("column:".length()).trim();
            } else if (line.startsWith("position:")) {
                String value = line.substring("position:".length()).trim();
                try { position = Integer.parseInt(value); } catch (NumberFormatException ignored) {}
            } else if (line.startsWith("created:")) {
                String value = line.substring("created:".length()).trim();
                if (value.endsWith("Z")) value = value.substring(0, value.length() - 1);
                created = LocalDateTime.parse(value);
            } else if (line.startsWith("modified:")) {
                String value = line.substring("modified:".length()).trim();
                if (value.endsWith("Z")) value = value.substring(0, value.length() - 1);
                modified = LocalDateTime.parse(value);
            } else if (line.startsWith("dueDate:")) {
                String value = line.substring("dueDate:".length()).trim();
                if (value.endsWith("Z")) value = value.substring(0, value.length() - 1);
                if (!value.isEmpty()) dueDate = LocalDateTime.parse(value);
            } else if (line.startsWith("tags:")) {
                String value = line.substring("tags:".length()).trim();
                if (value.startsWith("[") && value.endsWith("]")) {
                    value = value.substring(1, value.length() - 1);
                }
                tags = new ArrayList<>();
                if (!value.isEmpty()) {
                    for (String part : value.split(",")) {
                        tags.add(part.trim());
                    }
                }
            }
        }

        description = body.toString();
        return new Card(title, description, boardId, column, position, tags, created, modified, dueDate);
    }
}
