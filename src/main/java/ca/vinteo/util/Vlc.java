package ca.vinteo.util;

import ca.vinteo.ui.EventMediator;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Vlc {

    private enum OS {WINDOWS, LINUX}

    private static final Logger logger = LoggerFactory.getLogger(Vlc.class);
    private static final String COMMAND_TEMPLATE = "cmd_template.txt";
    private static final String TEMP_FILENAME = "vlc_exec.bat";
    private static final Long PROCESS_CHECK_INTERVAL_MS = 5000L;

    private final String vlcExec;
    private final String tempDirectory;
    private final OS currentOs;

    private Long vlcProcessId = -1L;

    public Vlc(String vlcExec, String tempDirectory, EventMediator eventMediator) {
        this.vlcExec = vlcExec;
        this.tempDirectory = tempDirectory;
        String os = System.getProperty("os.name");
        if (os.startsWith("Windows")) {
            this.currentOs = OS.WINDOWS;
        } else if (os.startsWith("Linux")) {
            this.currentOs = OS.LINUX;
        } else {
            throw new UnsupportedOperationException("Unrecognized platform: " + os);
        }
        eventMediator.setVlcLauncher(this);
    }

    public void launch(Path filename) throws IOException {
        if (currentOs == OS.WINDOWS) {
            URL templateUrl = Resources.getResource(COMMAND_TEMPLATE);
            String template = Resources.toString(templateUrl, StandardCharsets.UTF_8);
            String command = template.replace("{VLC}", vlcExec).replace("{FILENAME}", filename.toString());
            logger.info("Generated command is '{}'", command);
            File execFile = new File(Paths.get(tempDirectory, TEMP_FILENAME).toString());
            execFile.delete();
            Files.asCharSink(execFile, StandardCharsets.UTF_8).write(command);
            logger.info("Executing command.");
            Runtime.getRuntime().exec(execFile.getAbsoluteFile().toString());
        } else {
            Runtime runtime = Runtime.getRuntime();
            String[] command = new String[]{vlcExec, "--fullscreen", "--sub-track", "999", filename.toString()};
            logger.info("Opening '{}' with VLC. Executing command: {}", filename.toString(), command);
            Process vlcProcess = runtime.exec(command);
            try {
                Field pidField = vlcProcess.getClass().getDeclaredField("pid");
                pidField.setAccessible(true);
                vlcProcessId = pidField.getLong(vlcProcess);
            } catch (Exception e) {
                vlcProcessId = -1L;
            }
            System.out.println("VLC PROCESS ID: " + vlcProcessId);
        }

        new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(PROCESS_CHECK_INTERVAL_MS);
                    logger.debug("Checking if VLC is still running.");
                    if (isVlcProcessRunning()) {
                        // Keep the connection alive if the file playing is on a network
                        filename.toFile().exists();
                    } else {
                        return;
                    }
                }
            } catch (InterruptedException | IOException e) {
                logger.error("Failed to check status of VLC process.", e);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private boolean isVlcProcessRunning() throws IOException {
        if (vlcProcessId != -1L) {
            String command;
            if (currentOs == OS.WINDOWS) {
                command = "cmd /c tasklist /FI \"PID eq " + vlcProcessId + "\"";
            } else {
                command = "ps -p " + vlcProcessId;
            }
            Process checkVlc = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(checkVlc.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(" " + vlcProcessId + " ")) {
                    return true;
                }
            }
        }
        vlcProcessId = -1L;
        return false;
    }

}
