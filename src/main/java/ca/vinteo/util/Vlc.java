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
import java.util.function.Function;

public final class Vlc {

    private enum OS {WINDOWS, LINUX}

    private static final Logger logger = LoggerFactory.getLogger(Vlc.class);
    private static final String COMMAND_TEMPLATE = "vlc_exec_template.txt";
    private static final String TEMP_FILENAME = "vlc_exec.bat";

    private final String vlcExec;
    private final String tempDirectory;
    private final Long connectionCheckIntervalMs;
    private final OS currentOs;

    private Long vlcProcessId = -1L;

    public Vlc(String vlcExec, String tempDirectory, Long connectionCheckIntervalMs, EventMediator eventMediator) {
        this.vlcExec = vlcExec;
        this.tempDirectory = tempDirectory;
        this.connectionCheckIntervalMs = connectionCheckIntervalMs;
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
                    Thread.sleep(this.connectionCheckIntervalMs);
                    if (isVlcProcessRunning()) {
                        logger.info("VLC is still running");
                        // Keep the connection alive if the file playing is on a network
                        filename.toFile().exists();
                    } else {
                        logger.info("VLC is no longer running");
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
        String command;
        Function<String, Boolean> matcher;
        if (currentOs == OS.LINUX && vlcProcessId != -1L) {
            command = "ps -p " + vlcProcessId;
            matcher = (line) -> line.contains(" " + vlcProcessId + " ");
        } else { // Windows
            command = "cmd /c tasklist /FI \"IMAGENAME eq vlc.exe\"";
            matcher = (line) -> line.contains("vlc.exe ");
        }
        Process checkVlc = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(checkVlc.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (matcher.apply(line)) {
                return true;
            }
        }
        vlcProcessId = -1L;
        return false;
    }

}
