package me.kcra.datapackfixer;

import me.kcra.datapackfixer.tasks.RepackingTask;
import me.kcra.datapackfixer.tasks.UnzippingTask;
import org.apache.commons.cli.*;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatapackFixer {
    public static final Logger LOGGER = Logger.getLogger("DatapackFixer");
    public static final Path WORK_FOLDER;
    private static final File REPACKED_FILE;
    private static final HelpFormatter HELP_FORMATTER = new HelpFormatter();
    private static final Options OPTS = new Options()
            .addRequiredOption("f", "file", true, "Path to the datapack")
            .addOption("r", "repack", false, "Repacks the fixed datapack file.");

    static {
        final long time = System.currentTimeMillis();
        WORK_FOLDER = Path.of(System.getProperty("user.dir"), "work-" + time);
        REPACKED_FILE = Path.of(System.getProperty("user.dir"), "fixed-" + time + ".zip").toFile();
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = new DefaultParser().parse(OPTS, args);

            if (cmd.hasOption("f")) {
                //noinspection ResultOfMethodCallIgnored
                WORK_FOLDER.toFile().mkdirs();
                final File datapackFile = Path.of(cmd.getOptionValue("f")).toFile();
                try {
                    UnzippingTask.of(datapackFile).start();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Could not unzip file!", e);
                    System.exit(-1);
                }
                if (cmd.hasOption("r")) {
                    try {
                        RepackingTask.of(REPACKED_FILE).start();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Could not repack file!", e);
                        System.exit(-1);
                    }
                }
            } else {
                HELP_FORMATTER.printHelp("DatapackFixer", OPTS);
            }
        } catch (ParseException e) {
            HELP_FORMATTER.printHelp("DatapackFixer", OPTS);
        }
    }
}
