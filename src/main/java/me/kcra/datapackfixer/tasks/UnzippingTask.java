package me.kcra.datapackfixer.tasks;

import lombok.RequiredArgsConstructor;
import me.kcra.datapackfixer.DatapackFixer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@RequiredArgsConstructor(staticName = "of")
public class UnzippingTask {
    private boolean started = false;
    private final List<CompletableFuture<Void>> tasks = new ArrayList<>();
    private final File file;

    public void start() throws Exception {
        if (started) {
            throw new UnsupportedOperationException("You cannot start this task again!");
        }
        started = true;
        try (final ZipFile zipFile = new ZipFile(file)) {
            final Enumeration<? extends ZipEntry> iter = zipFile.entries();

            while (iter.hasMoreElements()) {
                final ZipEntry entry = iter.nextElement();
                final File entryFile = Path.of(DatapackFixer.WORK_FOLDER.toString(), entry.getName()).toFile();
                tasks.add(CompletableFuture.runAsync(() -> {
                    //noinspection ResultOfMethodCallIgnored
                    entryFile.mkdirs();
                    try {
                        Files.copy(zipFile.getInputStream(entry), entryFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ignored) {
                        DatapackFixer.LOGGER.log(Level.WARNING, "Could not extract file " + entry.getName() + ", skipping.");
                    }
                }, DatapackFixer.THREAD_POOL));
            }
            CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).join();
            DatapackFixer.LOGGER.log(Level.INFO, "Processed " + tasks.size() + " files total, datapack extracted.");
            tasks.clear();
        }
    }
}
