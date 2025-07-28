package fi.utu.tech.ooj.exercise4.exercise1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Test zipper.
 * <p>
 * Extracts zip, iterates through the files and prints information from each file.
 * <p>
 * From each file there will be printed:
 * - name
 * - amount of lines
 * - amount of words
 */
class TestZipper extends Zipper {
    TestZipper(String zipFile) throws IOException {
        super(zipFile);
    }

    @Override
    protected Handler createHandler(Path file) {
        return new Handler(file) {
            @Override
            public void handle() throws IOException {
                var regex = Pattern.compile("\\W");
                var contents = Files.readString(file);
                var lines = Files.readAllLines(file);
                var firstLine = lines.isEmpty() ? "unknown" : lines.getFirst();
                var words = regex.splitAsStream(contents).filter(s -> !s.isBlank()).map(String::toLowerCase).toList();

                System.out.printf("""
                                
                                Originally was fetched from %s.
                                The founded file is %s.
                                The file contains %d lines.
                                The file contains %d words.
                                Possible title of the work: %s
                                
                                """,
                        tempDirectory,
                        file.getFileName(),
                        lines.size(),
                        words.size(),
                        firstLine
                );
            }
        };
    }
}
