package com.ampersand;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Cards {

    public static Card readCard(Path cardsDir, String id) throws IOException {
        Path file = cardsDir.resolve(id + ".md");
        return CardParser.parse(Files.readString(file));
    }

    public static void writeCard(Path cardsDir, String id, Card card) throws IOException {
        if (!Files.exists(cardsDir)) Files.createDirectories(cardsDir);
        Path file = cardsDir.resolve(id + ".md");
        Files.writeString(file, CardSerializer.serialize(card));
    }

    public static void deleteCard(Path cardsDir, String id) throws IOException {
        Files.delete(cardsDir.resolve(id + ".md"));
    }

    public static List<CardSummary> listAll(Path cardsDir) throws IOException {
        List<CardSummary> results = new ArrayList<>();
        if (!Files.exists(cardsDir)) return results;

        List<Path> files;
        try (Stream<Path> paths = Files.list(cardsDir)) {
            files = paths.filter(p -> p.getFileName().toString().endsWith(".md")).sorted().toList();
        }

        for (Path file : files) {
            String name = file.getFileName().toString();
            String id = name.substring(0, name.length() - 3);
            Card card = CardParser.parse(Files.readString(file));
            results.add(CardSummary.from(id, card));
        }
        results.sort(Comparator.comparingInt(CardSummary::position));
        return results;
    }

    public static List<CardSummary> listByBoard(Path cardsDir, String boardId) throws IOException {
        List<CardSummary> all = listAll(cardsDir);
        List<CardSummary> filtered = new ArrayList<>();
        for (CardSummary c : all) {
            if (boardId.equals(c.boardId())) filtered.add(c);
        }
        return filtered;
    }
}
