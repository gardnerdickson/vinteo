package ca.vinteo.repository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class InitializationRepository extends DataRepository {

    private static final String SETUP_SCRIPT_RESOURCE = "setup.sql";

    public InitializationRepository(String connectionString) {
        super(connectionString);
    }

    public void executeSetup() throws RepositoryException {
        String commentRegex = "--(.)*";
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            Path setupScript = Paths.get(ClassLoader.getSystemResource(SETUP_SCRIPT_RESOURCE).toURI());
            String setupScriptContentsWithCommentsRemoved = new String(Files.readAllBytes(setupScript), StandardCharsets.UTF_8).replaceAll(commentRegex, "");
            Scanner statementScanner = new Scanner(setupScriptContentsWithCommentsRemoved).useDelimiter(";");
            while (statementScanner.hasNext()) {
                String statement = statementScanner.next().trim();
                if (statement.length() > 0) {
                    connection.createStatement().execute(statement);
                }
            }
        } catch (URISyntaxException | IOException | SQLException e) {
            throw new RepositoryException(e);
        }
    }

}
