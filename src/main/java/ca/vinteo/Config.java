package ca.vinteo;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Config {

    private final Properties properties;

    private final ImmutableSet<String> extensions;

    public Config(Path configFilePath) throws IOException {
        properties = new Properties();
        properties.load(Files.newInputStream(configFilePath));

        {
            Optional<String> extensionsLine = Optional.ofNullable(properties.getProperty("extensions"));
            String[] values = extensionsLine.map(line -> line.split(",")).orElseThrow(() -> new NoSuchElementException("'extensions' property not found"));
            this.extensions = ImmutableSet.copyOf(values);
        }

    }

    public ImmutableSet<String> getExtensions() {
        return extensions;
    }

}
