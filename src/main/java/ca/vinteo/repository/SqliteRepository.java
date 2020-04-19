package ca.vinteo.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.stream.Collectors;

public abstract class SqliteRepository {

    private static final String CONNECTION_PREFIX = "jdbc:sqlite:";
    private static final String COMMENT_REGEX = "--(.)*";

    String connectionString;

    public SqliteRepository(String sqliteFileLocation) {
        this.connectionString = CONNECTION_PREFIX  + sqliteFileLocation;
    }

    public Connection newConnection() throws SQLException {
        return DriverManager.getConnection(connectionString);
    }

    public static void executeSqlScript(InputStream inputStream, Connection connection) throws SQLException, RepositoryException {
        String scriptContents;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            scriptContents = bufferedReader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RepositoryException("Failed to read script");
        }
        String setupScriptWithCommentsRemoved = scriptContents.replaceAll(COMMENT_REGEX, "");
        Scanner statementScanner = new Scanner(setupScriptWithCommentsRemoved).useDelimiter(";");
        while (statementScanner.hasNext()) {
            String statement = statementScanner.next().trim();
            if (statement.length() > 0) {
                connection.createStatement().execute(statement);
            }
        }
    }

}
