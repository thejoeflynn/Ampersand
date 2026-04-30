import java.time.LocalDateTime;

public class Note {

    private String title;
    private String content;
    private String author;
    private LocalDateTime created;
    private LocalDateTime modified;

    public Note(String title, String content, String author, LocalDateTime created, LocalDateTime modified) {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        this.title = title;
        this.content = content;
        this.author = author;
        this.created = created;
        this.modified = modified;
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
}
