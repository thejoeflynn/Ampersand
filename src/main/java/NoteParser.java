import java.time.LocalDateTime;

public class NoteParser {
    public static Note parse(String input) {
        String title = null;
        String author = null;
        LocalDateTime created = null;
        for (String line : input.split("\n")) {
            if (line.startsWith("title:")) {
                title = line.substring("title:".length()).trim();
            } else if (line.startsWith("author:")) {
                author = line.substring("author:".length()).trim();
            } else if (line.startsWith("created:")) {
                created = LocalDateTime.parse(line.substring("created:".length()).trim());
            }
        }
        return new Note(title, "", author, created, null, null);
    }
}
