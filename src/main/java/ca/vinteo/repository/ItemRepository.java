package ca.vinteo.repository;

import ca.vinteo.ui.EventMediator;
import com.google.common.collect.ImmutableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class ItemRepository extends SqliteRepository {

    private static final String INSERT = "INSERT INTO item (path, name) values (?, ?)";
    private static final String FIND_ALL = "SELECT * FROM item";
    private static final String FIND_WHERE_NAME_LIKE = "SELECT * FROM item WHERE lower(name) like ?";
    private static final String TRUNCATE = "DELETE FROM item";

    public ItemRepository(String connectionString, EventMediator eventMediator) {
        super(connectionString);
        eventMediator.setItemRepository(this);
    }

    public void clearItems() throws RepositoryException {
        try (Statement statement = newConnection().createStatement()) {
            statement.execute(TRUNCATE);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to clear items table.", e);
        }
    }

    public void addItems(List<Item> items) throws RepositoryException {
        try (PreparedStatement statement = newConnection().prepareStatement(INSERT)) {
            int batchSize = 0;
            for (Item item : items) {
                statement.setString(1, item.getPath());
                statement.setString(2, item.getName());
                statement.addBatch();

                batchSize++;
                if (batchSize % 100 == 0 || batchSize == items.size()) {
                    statement.executeBatch();
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to add items", e);
        }
    }

    public ImmutableList<Item> findAllItems() throws RepositoryException {
        try (Statement statement = newConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(FIND_ALL);
            return Item.createListFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to retrieve items", e);
        }
    }

    public ImmutableList<Item> findLike(String query) throws RepositoryException {
        try (PreparedStatement statement = newConnection().prepareStatement(FIND_WHERE_NAME_LIKE)) {
            statement.setString(1, "%" + query + "%");
            ResultSet resultSet = statement.executeQuery();
            return Item.createListFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to retrieve items", e);
        }
    }

}
