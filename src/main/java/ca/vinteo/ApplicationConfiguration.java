package ca.vinteo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;

public final class ApplicationConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);

    private static final String VLC_DEFAULT = "vlc";
    private static final String VLC_COMMAND_PROPERTY = "command.vlc";
    private static final String USER_SETTINGS_PROPERTY = "usersettings.file";
    private static final String SQL_FILE_PROPERTY = "sqlite.file";
    private static final String TEMP_DIRECTORY_PROPERTY = "temp.dir";
    private static final String CONNECTION_CHECK_INTERVAL_MS = "connectionCheck.interval.ms";
    private static final String DEFAULT_CONFIG_FILE_NAME = "config.properties";

    private final String vlcCommand;
    private final String userSettingsFile;
    private final String sqliteFile;
    private final String tempDirectory;
    private final Long connectionCheckIntervalMs;

    ApplicationConfiguration() throws IOException {
        this(null);
    }

    ApplicationConfiguration(Path configFilePath) throws IOException {
        Properties properties = new Properties();
        if (configFilePath != null) {
            logger.info("Configuration path provided. Loading configuration from '{}'", configFilePath);
            properties.load(Files.newInputStream(configFilePath));
        } else {
            Path currentDirectory = Paths.get("", DEFAULT_CONFIG_FILE_NAME).toAbsolutePath();
            logger.info("No configuration path provided. Loading configuration from '{}'", currentDirectory);
            properties.load(Files.newInputStream(currentDirectory));
        }

        {
            logger.debug("Looking up property: {}", VLC_COMMAND_PROPERTY);
            Optional<String> property = Optional.ofNullable(properties.getProperty(VLC_COMMAND_PROPERTY));
            vlcCommand = property.orElseGet(() -> {
                logger.info("'{}' property not found. Using default.", VLC_COMMAND_PROPERTY);
                return VLC_DEFAULT;
            });
        }

        {
            logger.debug("Looking up property: {}", USER_SETTINGS_PROPERTY);
            Optional<String> property = Optional.ofNullable(properties.getProperty(USER_SETTINGS_PROPERTY));
            userSettingsFile = property.orElseThrow(() -> new NoSuchElementException("'" + USER_SETTINGS_PROPERTY + "' property not found."));
        }

        {
            logger.debug("Looking up property: {}", SQL_FILE_PROPERTY);
            Optional<String> property = Optional.ofNullable(properties.getProperty(SQL_FILE_PROPERTY));
            sqliteFile = property.orElseThrow(() -> new NoSuchElementException("'" + SQL_FILE_PROPERTY + "' property not found."));
        }

        {
            logger.debug("Looking up property: {}", TEMP_DIRECTORY_PROPERTY);
            Optional<String> property = Optional.ofNullable(properties.getProperty(TEMP_DIRECTORY_PROPERTY));
            tempDirectory = property.orElseThrow(() -> new NoSuchElementException("'" + TEMP_DIRECTORY_PROPERTY + "' property not found."));
        }

        {
            logger.debug("Looking up property: {}", CONNECTION_CHECK_INTERVAL_MS);
            Optional<String> property = Optional.ofNullable(properties.getProperty(CONNECTION_CHECK_INTERVAL_MS));
            connectionCheckIntervalMs = Long.parseLong(property.orElseThrow(() -> new NoSuchElementException("'" + CONNECTION_CHECK_INTERVAL_MS + "' property not found.")));
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

    public String getTempDirectory() {
        return tempDirectory;
    }

    public Long getConnectionCheckIntervalMs() {
        return connectionCheckIntervalMs;
    }
}
