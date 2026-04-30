public class NoteParser {
    public static Note parse(String input) {
        String title = null;
        for (String line : input.split("\n")) {
            if (line.startsWith("title:")) {
                title = line.substring("title:".length()).trim();
                break;
            }
        }
        return new Note(title, "", null, null, null, null);
    }
}
