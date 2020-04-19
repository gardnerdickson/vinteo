package ca.vinteo.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseChangelogRepository extends SqliteRepository {

    private static final String GET_ALL = "SELECT * FROM database_changelog";
    private static final String CREATE_CHANGELOG = "INSERT INTO database_changelog (script_name, date_time_executed) VALUES (?, ?)";
    private static final String CHECK_TABLE_EXISTS = "SELECT count(*) AS table_exists FROM sqlite_master WHERE type = 'table' AND name = ?";
    private static final String SCRIPT_LOCATION = "/database_updates";
    private static final String CHANGELOG_TABLE_NAME = "database_changelog";

    private static final Logger logger = LoggerFactory.getLogger(DatabaseChangelogRepository.class);


    public DatabaseChangelogRepository(String sqliteFileLocation) {
        super(sqliteFileLocation);
    }

    public void checkAndExecuteUpdates(boolean firstTimeSetup) throws RepositoryException {
        try (Connection connection = newConnection()) {
            // If this is the first time the application has been run, create the changelog table
            if (firstTimeSetup) {
                logger.info("Performing first time setup on database.");
                executeScript(SCRIPT_LOCATION + "/update_1.sql", connection);
                insertChangelog(SCRIPT_LOCATION + "/update_1.sql", connection);
            // If this is an existing installation but there is no changelog table, execute update_1 to create the changelog table,
            // then add update_1 and update_2 to the changelog table.
            } else if (!checkIfTableExists(CHANGELOG_TABLE_NAME, connection)) {
                logger.info("Adding changelog table to existing installation.");
                executeScript(SCRIPT_LOCATION + "/update_1.sql", connection);
                insertChangelog(SCRIPT_LOCATION + "/update_1.sql", connection);
                insertChangelog(SCRIPT_LOCATION + "/update_2.sql", connection);
            }

            // Now we can get all of the scripts in the "database_updates" directory, compare them with what is already in the changelog table, and
            // execute the scripts that are not already in the changelog table
            List<DatabaseChangelogItem> changelogItems;
            PreparedStatement statement = connection.prepareStatement(GET_ALL);
            ResultSet resultSet = statement.executeQuery();
            changelogItems = new ArrayList<>(DatabaseChangelogItem.createListFromResultSet(resultSet));
            Set<String> scriptNames = changelogItems.stream().map(DatabaseChangelogItem::getScriptName).collect(Collectors.toSet());

            int fileNum = 3;
            boolean noMoreUpdateScripts = false;
            do {
                try {
                    String resourceName  = SCRIPT_LOCATION + "/update_" + fileNum + ".sql";
                    if (!scriptNames.contains(resourceName)) {
                        executeScript(resourceName, connection);
                        insertChangelog(resourceName, connection);
                        logger.info("Executed database update: " + resourceName);
                    }
                } catch (FileNotFoundException e) {
                    noMoreUpdateScripts = true;
                }
                fileNum++;
            } while (!noMoreUpdateScripts);
        } catch (SQLException | FileNotFoundException e) {
            throw new RepositoryException("Failed to execute database updates.", e);
        }
    }

    private void executeScript(String script, Connection connection) throws SQLException, RepositoryException, FileNotFoundException {
        InputStream scriptStream = getClass().getResourceAsStream(script);
        if (scriptStream == null) {
            throw new FileNotFoundException("Resource " + script + " not found.");
        }
        executeSqlScript(scriptStream, connection);
    }

    private boolean checkIfTableExists(String tableName, Connection connection) throws SQLException {
        PreparedStatement checkTableStatement = connection.prepareStatement(CHECK_TABLE_EXISTS);
        checkTableStatement.setString(1, tableName);
        return checkTableStatement.executeQuery().getInt("table_exists") == 1;
    }

    private void insertChangelog(String scriptName, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(CREATE_CHANGELOG);
        statement.setString(1, scriptName);
        statement.setString(2, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        statement.executeUpdate();
    }

    public static void main(String[] args) throws Exception {
        DatabaseChangelogRepository repo = new DatabaseChangelogRepository("C:\\Users\\Gardner\\Documents\\Projects\\Vinteo\\storage_test.db");
        repo.checkAndExecuteUpdates(false);
    }
}
