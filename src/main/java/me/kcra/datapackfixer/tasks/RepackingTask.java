package me.kcra.datapackfixer.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import me.kcra.datapackfixer.DatapackFixer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Log
@RequiredArgsConstructor(staticName = "of")
public class RepackingTask implements BasicTask {
    private boolean started = false;
    private final File zipFile;

    @Override
    public void start() throws Exception {
        if (started) {
            throw new UnsupportedOperationException("You cannot start this task again!");
        }
        started = true;
        try (final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Files.walkFileTree(DatapackFixer.WORK_FOLDER, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    try {
                        outputStream.putNextEntry(new ZipEntry(DatapackFixer.WORK_FOLDER.relativize(file).toString()));
                        final byte[] bytes = Files.readAllBytes(file);
                        outputStream.write(bytes, 0, bytes.length);
                        outputStream.closeEntry();
                    } catch (IOException ignored) {
                        log.log(Level.WARNING, "Could not repack file " + file + ", skipping.");
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        DatapackFixer.LOGGER.log(Level.INFO, "Repacked datapack.");
    }
}
