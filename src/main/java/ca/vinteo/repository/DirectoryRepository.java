package ca.vinteo.repository;

import com.google.common.collect.ImmutableList;

import java.sql.*;

public class DirectoryRepository extends SqliteRepository {

    private static final String FIND_ALL = "SELECT * FROM directory";
    private static final String INSERT = "INSERT INTO directory (path) values (?)";

    public DirectoryRepository(String connectionString) {
        super(connectionString);
    }

    public ImmutableList<Directory> findAllDirectories() throws RepositoryException {
        ImmutableList<Directory> records;
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(FIND_ALL);
            records = Directory.createListFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
        return records;
    }

    public void addDirectory(String path) throws RepositoryException {
        try (Connection connection = DriverManager.getConnection(connectionString)) {
            PreparedStatement statement = connection.prepareStatement(INSERT);
            statement.setString(1, path);
            statement.execute();
        } catch (SQLException e) {
            throw new RepositoryException(e);
        }
    }

}
