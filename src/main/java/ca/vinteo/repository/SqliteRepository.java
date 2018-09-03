package ca.vinteo.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class SqliteRepository {

    private static final String CONNECTION_PREFIX = "jdbc:sqlite:";

    String connectionString;

    public SqliteRepository(String sqliteFileLocation) {
        this.connectionString = CONNECTION_PREFIX  + sqliteFileLocation;
    }

    public Connection newConnection() throws SQLException {
        return DriverManager.getConnection(connectionString);
    }

}
