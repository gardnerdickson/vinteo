package ca.vinteo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;

public final class ApplicationConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);

    private static final String VLC_DEFAULT = "vlc";

    private final String vlcCommand;
    private final String userSettingsFile;
    private final String sqliteFile;

    ApplicationConfiguration(Path configFilePath) throws IOException {
        Properties properties = new Properties();
        properties.load(Files.newInputStream(configFilePath));

        {
            Optional<String> property = Optional.ofNullable(properties.getProperty("command.vlc"));
            vlcCommand = property.orElseGet(() -> {
                logger.info("'command.vlc property not found. Using default.'");
                return VLC_DEFAULT;
            });
        }

        {
            Optional<String> property = Optional.ofNullable(properties.getProperty("usersettings.file"));
            userSettingsFile = property.orElseThrow(() -> new NoSuchElementException("'usersettings.file' property not found."));
        }

        {
            Optional<String> property = Optional.ofNullable(properties.getProperty("sqlite.file"));
            sqliteFile = property.orElseThrow(() -> new NoSuchElementException("'sqlite.file' property not found."));
        }

    }

    public String getUserSettingsFile() {
        return userSettingsFile;
    }

    public String getVlcCommand() {
        return vlcCommand;
    }

    public String getSqliteFile() {
        return sqliteFile;
    }

}
