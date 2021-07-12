package ca.vinteo.util;

import ca.vinteo.ui.EventMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.function.Function;

public final class Vlc {

    private enum OS {WINDOWS, LINUX}

    private static final Logger logger = LoggerFactory.getLogger(Vlc.class);
    private static final Long PROCESS_CHECK_INTERVAL_MS = 5000L;

    private final String vlcExec;
    private final OS currentOs;

    private Long vlcProcessId = -1L;

    public Vlc(String vlcExec, EventMediator eventMediator) {
        this.vlcExec = vlcExec;
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
        String[] command = new String[]{vlcExec, "--fullscreen", "--sub-track", "999", filename.toString()};
        logger.info("Opening '{}' with VLC. Executing command: {}", filename.toString(), command);
        Process vlcProcess = Runtime.getRuntime().exec(command);
        if (currentOs == OS.LINUX) {
            try {
                Field pidField = vlcProcess.getClass().getDeclaredField("pid");
                pidField.setAccessible(true);
                vlcProcessId = pidField.getLong(vlcProcess);
            } catch (Exception e) {
                vlcProcessId = -1L;
            }
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
