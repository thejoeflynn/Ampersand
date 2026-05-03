package com.ampersand;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class NoteParser {
    public static Note parse(String input) {
        String title = null;
        String author = null;
        LocalDateTime created = null;
        LocalDateTime modified = null;
        List<String> tags = null;
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
            } else if (line.startsWith("author:")) {
                author = line.substring("author:".length()).trim();
            } else if (line.startsWith("created:")) {
                String value = line.substring("created:".length()).trim();
                if (value.endsWith("Z")) value = value.substring(0, value.length() - 1);
                created = LocalDateTime.parse(value);
            } else if (line.startsWith("modified:")) {
                String value = line.substring("modified:".length()).trim();
                if (value.endsWith("Z")) value = value.substring(0, value.length() - 1);
                modified = LocalDateTime.parse(value);
            } else if (line.startsWith("tags:")) {
                String value = line.substring("tags:".length()).trim();
                value = value.substring(1, value.length() - 1);
                tags = new ArrayList<>();
                for (String part : value.split(",")) {
                    tags.add(part.trim());
                }
            }
        }
        return new Note(title, body.toString(), author, created, modified, tags);
    }
}
