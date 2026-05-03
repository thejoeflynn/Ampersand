package com.ampersand;

public class NoteSerializer {
    public static String serialize(Note note) {
        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append("title: ").append(note.getTitle()).append("\n");
        if (note.getAuthor() != null) {
            sb.append("author: ").append(note.getAuthor()).append("\n");
        }
        if (note.getCreated() != null) {
            sb.append("created: ").append(note.getCreated()).append("\n");
        }
        if (note.getModified() != null) {
            sb.append("modified: ").append(note.getModified()).append("\n");
        }
        if (note.getTags() != null) {
            sb.append("tags: [").append(String.join(", ", note.getTags())).append("]\n");
        }
        sb.append("---\n");
        sb.append(note.getContent());
        return sb.toString();
    }
}
