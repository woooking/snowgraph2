package edu.pku.sei.tsr.snowgraph.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

public class FileUtils {
    private static Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static void createDirectory(File dir) {
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                logger.error("Could not createAndInit dir: {}!", dir.getName());
            }
        }
    }

    public static void createDirectory(Path dirPath) {
        createDirectory(dirPath.toFile());
    }
}
