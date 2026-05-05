package com.ampersand;

public class CardSerializer {
    public static String serialize(Card card) {
        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append("title: ").append(card.getTitle()).append("\n");
        if (card.getBoardId() != null) {
            sb.append("boardId: ").append(card.getBoardId()).append("\n");
        }
        if (card.getColumn() != null) {
            sb.append("column: ").append(card.getColumn()).append("\n");
        }
        sb.append("position: ").append(card.getPosition()).append("\n");
        if (card.getCreated() != null) {
            sb.append("created: ").append(card.getCreated()).append("\n");
        }
        if (card.getModified() != null) {
            sb.append("modified: ").append(card.getModified()).append("\n");
        }
        if (card.getDueDate() != null) {
            sb.append("dueDate: ").append(card.getDueDate()).append("\n");
        }
        if (card.getTags() != null) {
            sb.append("tags: [").append(String.join(", ", card.getTags())).append("]\n");
        }
        sb.append("---\n");
        sb.append(card.getDescription() == null ? "" : card.getDescription());
        return sb.toString();
    }
}
