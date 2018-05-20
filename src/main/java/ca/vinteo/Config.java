package ca.vinteo;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;

public final class Config {

    private final ImmutableSet<String> extensions;
    private final ImmutableSet<String> directories;

    Config(Path configFilePath) throws IOException {
        Properties properties = new Properties();
        properties.load(Files.newInputStream(configFilePath));

        {
            Optional<String> extensionsLine = Optional.ofNullable(properties.getProperty("extensions"));
            String[] values = extensionsLine.map(line -> line.split(",")).orElseThrow(() -> new NoSuchElementException("'extensions' property not found"));
            extensions = ImmutableSet.copyOf(values);
        }

        {
            int index = 0;
            Set<String> directoryProperties = new HashSet<>();
            String propertyValue;
            while ((propertyValue = properties.getProperty("directories." + index)) != null) {
                directoryProperties.add(propertyValue);
                index++;
            }
            if (directoryProperties.isEmpty()) {
                throw new NoSuchFileException("There were no 'directory.X' properties found.");
            }
            directories = ImmutableSet.copyOf(directoryProperties);
        }

    }

    public ImmutableSet<String> getExtensions() {
        return extensions;
    }

    public ImmutableSet<String> getDirectories() {
        return directories;
    }

}
