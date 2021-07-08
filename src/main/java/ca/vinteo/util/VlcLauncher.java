package ca.vinteo.util;

import ca.vinteo.ui.EventMediator;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class VlcLauncher {

    private static final Logger logger = LoggerFactory.getLogger(VlcLauncher.class);
    private static final String COMMAND_TEMPLATE = "cmd_template.txt";
    private static final String TEMP_FILENAME = "vlc_exec.bat";

    private final String vlcExec;
    private final String tempDirectory;

    public VlcLauncher(String vlcExec, String tempDirectory, EventMediator eventMediator) {
        this.vlcExec = vlcExec;
        this.tempDirectory = tempDirectory;
        eventMediator.setVlcLauncher(this);
    }

    public void launch(Path filename) throws IOException {
        String os = System.getProperty("os.name");
        if (os.startsWith("Windows")) {
            URL templateUrl = Resources.getResource(COMMAND_TEMPLATE);
            String template = Resources.toString(templateUrl, StandardCharsets.UTF_8);
            String command = template.replace("{VLC}", vlcExec).replace("{FILENAME}", filename.toString());
            logger.info("Generated command is '{}'", command);
            File execFile = new File(Paths.get(tempDirectory, TEMP_FILENAME).toString());
            execFile.delete();
            Files.asCharSink(execFile, StandardCharsets.UTF_8).write(command);
            logger.info("Executing command.");
            Runtime.getRuntime().exec(execFile.getAbsoluteFile().toString());
        } else if (os.startsWith("Linux")) {
            Runtime runtime = Runtime.getRuntime();
            String[] command = new String[]{vlcExec, "--fullscreen", "--sub-track", "999", filename.toString()};
            logger.info("Opening '{}' with VLC. Executing command: {}", filename.toString(), command);
            runtime.exec(command);
        } else {
            throw new UnsupportedOperationException("Unrecognized platform: " + os);
        }
    }
}
