package ca.vinteo.util;

import ca.vinteo.ui.EventMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public final class VlcLauncher {

    private static final Logger logger = LoggerFactory.getLogger(VlcLauncher.class);

    private final String vlcExec;

    public VlcLauncher(String vlcExec, EventMediator eventMediator) {
        this.vlcExec = vlcExec;
        eventMediator.setVlcLauncher(this);
    }

    public void launch(Path filename) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        String[] command = new String[]{vlcExec, "--fullscreen", "--sub-track", "999", filename.toString()};
        logger.info("Opening '{}' with VLC. Executing command: {}", filename.toString(), command);
        runtime.exec(command);
    }
}
