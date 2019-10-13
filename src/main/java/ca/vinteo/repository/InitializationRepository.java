package ca.vinteo.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.stream.Collectors;

public class InitializationRepository extends SqliteRepository {

    private static final String SETUP_SCRIPT_RESOURCE = "/setup.sql";
    private static final String COMMENT_REGEX = "--(.)*";

    public InitializationRepository(String connectionString) {
        super(connectionString);
    }

    public void executeSetup() throws RepositoryException {
        String setupScriptContents;
        InputStream stream = getClass().getResourceAsStream(SETUP_SCRIPT_RESOURCE);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            setupScriptContents = reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RepositoryException("Failed to read setup script.", e);
        }
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            String setupScriptContentsWithCommentsRemoved = setupScriptContents.replaceAll(COMMENT_REGEX, "");
            Scanner statementScanner = new Scanner(setupScriptContentsWithCommentsRemoved).useDelimiter(";");
            while (statementScanner.hasNext()) {
                String statement = statementScanner.next().trim();
                if (statement.length() > 0) {
                    connection.createStatement().execute(statement);
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to execute setup script.", e);
        }
    }

}
