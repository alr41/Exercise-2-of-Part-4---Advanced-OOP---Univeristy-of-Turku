package fi.utu.tech.ooj.exercise4.exercise2;

import fi.utu.tech.ooj.exercise4.exercise1.Zipper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayList;

// Part A) definition and implementation of Book
public record Book(Path path, String name, int lines) implements Comparable<Book> {
    public Book(Path path) throws IOException {
      this(
            path,
            Files.readAllLines(path).isEmpty() ? "Unknown Title" : Files.readAllLines(path).get(0),
            (int) Files.lines(path).count()
        );
    }

    // Part B) natural order for the books
    @Override
    public int compareTo(Book other) {
        return this.name.compareToIgnoreCase(other.name);
    }

    // Part D) count unique words
    public int getUniqueWordCount() throws IOException {
        String content = Files.readString(this.path).toLowerCase();
        String[] words = content.split("\\W+");
        Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
        uniqueWords.remove("");
        return uniqueWords.size();
    }

    @Override
    public String toString() {
        return String.format("%s (%d lines)", name.length() > 60 ? name.substring(0, 57) + "..." : name, lines);
    }
}

class TestZipper2 extends Zipper {
    List<Book> books = new ArrayList<>();

    TestZipper2(String zipFile) throws IOException {
        super(zipFile);
    }

    @Override
    public void run() throws IOException {
        super.run();

      // Part B)
      System.out.println("\n--- B) Natural Order (by Name Ascending) ---");
      Collections.sort(books); // Uses the natural order from compareTo
      books.forEach(System.out::println);
      
        // Part C) sort by line count
        Collections.sort(books, Comparator.comparingInt(Book::lines));

        System.out.println("Books sorted by line count:");
        for (Book book : books) {
            System.out.printf("%s (%d lines)%n", book.name(), book.lines());
        }

        // Part D)
        books.sort((b1, b2) -> {
            try {
                return Integer.compare(b2.getUniqueWordCount(), b1.getUniqueWordCount());
            } catch (IOException e) {
                System.err.println("Failed to count unique words: " + e.getMessage());
                return 0;
            }
        });
      
        System.out.println("\nBooks sorted by unique word count (descending):");
        for (Book book : books) {
            try {
                System.out.printf("%s — %d unique words%n", book.name(), book.getUniqueWordCount());
            } catch (IOException e) {
                System.out.println(book.name() + " — error reading file");
            }
        }
        // Part E) sort by name, then line count
        Comparator<Book> combinedComparator = Comparator
            .naturalOrder()
            .thenComparingInt(Book::lines);

        Collections.sort(books, combinedComparator);

        System.out.println("\nBooks sorted by name (then line count):");
        books.forEach(System.out::println);

        System.out.printf("""
                        
                        Handled %d Books.
                        Now we could sort it out a bit.
                        """, books.size());
    }

    @Override
    protected Handler createHandler(Path file) {
        return new Handler(file) {
            @Override
            public void handle() {
                try {
                    books.add(new Book(file));
                } catch (IOException e) {
                    System.err.println("Error reading file " + file + ": " + e.getMessage());
                }
            }
        };
    }
}

