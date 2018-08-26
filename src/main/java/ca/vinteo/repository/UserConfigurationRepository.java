package ca.vinteo.repository;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class UserConfigurationRepository extends FileRepository {

    private final Gson parser;

    public UserConfigurationRepository(Path fileLocation) {
        super(fileLocation);
        parser = new GsonBuilder().setPrettyPrinting().create();
    }

    public UserConfiguration load() throws IOException {
        try (JsonReader reader = new JsonReader(Files.newReader(fileLocation.toFile(), StandardCharsets.UTF_8))) {
            return parser.fromJson(reader, UserConfiguration.class);
        }
    }

    public void save(UserConfiguration userConfiguration) throws IOException {
        try (FileWriter writer = new FileWriter(fileLocation.toFile())) {
            parser.toJson(userConfiguration, UserConfiguration.class, writer);
        }
    }

}
