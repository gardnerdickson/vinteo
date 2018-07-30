package ca.vinteo.util;

import ca.vinteo.ui.EventMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DesktopUtil {

    private static final Logger logger = LoggerFactory.getLogger(DesktopUtil.class);

    public DesktopUtil(EventMediator eventMediator) {
        eventMediator.setDesktopUtil(this);
    }

    public void openFolder(Path filename) {
        Path directory = Files.isRegularFile(filename) ? filename.getParent() : filename;
        new Thread(() -> {
            try {
                logger.info("Opening directory in file explorer: '{}'", directory);
                Desktop.getDesktop().open(directory.toFile());
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to open directory: " + directory, e);
            }
        }).start();
    }


}
