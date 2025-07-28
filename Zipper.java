package fi.utu.tech.ooj.exercise4.exercise1;

import fi.utu.tech.ooj.exercise4.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipInputStream;

// WORKAROUND: if the zip file is not found, copy books.zip from the resources directory
// to the project's root and follow the two instructions below,
// also marked with WORKAROUND comments.

/**

A class that models unzipping (extracting a compressed zip package).
<p>
The idea is that while an object of the class exists, there is also a temporary directory
created by the object on the disk. When the object is closed, the directory is also deleted.
<p>
How to use it? Create an object. Creation assumes that the zip file must exist.
The class's 'run' method activates the unzipping. Finally, close the object ('close').
<p>
Hint: closing is easy with Java's try-with-resources feature.
*/
abstract public class Zipper implements AutoCloseable {
    // zip-file for unzipping
    private final String zipFile;

    // java class, from which package the zip file is looked for
    private final Class<?> resolver = Main.class;

    // path of temp directory
    protected final Path tempDirectory;

    /**
     * Records the given zip file and
     * creates a temporary directory 'tempDirectory'.
     *
     * @param zipFile Zip file path (precondition: must exist and be non-null).
     * @throws IOException If the zip file is not found or the temporary directory cannot be created.
     */
    public Zipper(String zipFile) throws IOException {
        // WORKAROUND: if the zip file is not found, comment out the next two lines.
        if (resolver.getResource(zipFile) == null)
            throw new FileNotFoundException(zipFile);

        this.zipFile = zipFile;

        tempDirectory = Files.createTempDirectory("dtek0066");
        System.out.println("Created a temp directory " + tempDirectory);
    }

    /**
     * Deletes the temporary directory 'tempDirectory' when the object is closed.
     *
     * @throws IOException In case of any I/O errors.
     */
    @Override
    public void close() throws IOException {
        try (final var stream = Files.walk(tempDirectory)) {
            stream
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        System.out.println("The removed temp directory was " + tempDirectory);
    }

    /**
     * Unzip the file 'zipFile' to the temporary directory 'tempDirectory'.
     *
     * @throws IOException In case of any I/O errors.
     */
    private void unzip() throws IOException {
        final var destinationDir = tempDirectory.toFile();

        // WORKAROUND: If the zip file is not found, change to the following
        // try (final var inputStream = new FileInputStream(zipFile);
        try (final var inputStream = resolver.getResourceAsStream(zipFile);
             final var stream = new ZipInputStream(inputStream)) {
            var zipEntry = stream.getNextEntry();
            while (zipEntry != null) {
                final var newFile = new File(destinationDir, zipEntry.getName());
                final var destDirPath = destinationDir.getCanonicalPath();
                final var destFilePath = newFile.getCanonicalPath();
                if (!destFilePath.startsWith(destDirPath + File.separator)) {
                    throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
                }
                System.out.println("Puretaan " + newFile);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory: " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    final var parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory: " + parent);
                    }

                    // write file content
                    try (final var fos = new FileOutputStream(newFile)) {
                        stream.transferTo(fos);
                    }
                }
                zipEntry = stream.getNextEntry();
            }
            stream.closeEntry();
        }
    }

    /**
     * Executes unzipping and creates a handler for every created file.
     *
     * @throws IOException In case of any I/O errors.
     */
    public void run() throws IOException {
        unzip();

        for (final var handler : createHandlers())
            handler.handle();
    }

    protected List<Handler> createHandlers() throws IOException {
        try (final var stream = Files.list(tempDirectory)) {
            return stream.map(this::createHandler).toList();
        }
    }

    /**
     * Creation of the Handler.
     *
     * @param file The file to be handerl (precondition: must exist and be non-null)
     * @return Handler
     */
    protected abstract Handler createHandler(Path file);

    /**
     * A handler for a single file, responsible for processing
     * an individual file.
     */
    protected abstract static class Handler {
        public final Path file;

        /**
         * Initializes the handler.
         *
         * @param file The file to be handled (precondition: must exist and be non-null)
         */
        public Handler(Path file) {
            this.file = file;
        }

        /**
         * Processes the file.
         *
         * @throws IOException In case of any I/O errors.
         */
        abstract public void handle() throws IOException;
    }
}

