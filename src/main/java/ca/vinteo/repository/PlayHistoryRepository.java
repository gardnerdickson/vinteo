package ca.vinteo.repository;

import ca.vinteo.ui.EventMediator;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PlayHistoryRepository extends SqliteRepository {

    private static final String INSERT = "INSERT INTO play_history (path, directory, name, date_time) values (?, ?, ?, ?)";
    private static final String FIND_BY_DIRECTORY = "SELECT * FROM play_history WHERE directory = ?";

    public PlayHistoryRepository(String sqliteFileLocation, EventMediator eventMediator) {
        super(sqliteFileLocation);
        eventMediator.setPlayHistoryRepository(this);
    }

    public void logItem(Item item) throws RepositoryException {
        File file = new File(item.getPath());
        try (PreparedStatement statement = newConnection().prepareStatement(INSERT)) {
            statement.setString(1, item.getPath());
            statement.setString(2, file.getParent());
            statement.setString(3, item.getName());
            statement.setString(4, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to log item to play history.", e);
        }
    }

    public Optional<PlayHistoryItem> getMostRecentlyPlayedFileFromDirectory(String directory) throws RepositoryException {
        try (PreparedStatement statement = newConnection().prepareStatement(FIND_BY_DIRECTORY)) {
            statement.setString(1, directory);
            ResultSet resultSet = statement.executeQuery();
            List<PlayHistoryItem> items = new ArrayList<>(PlayHistoryItem.createListFromResultSet(resultSet));
            items.sort(Comparator.comparing(PlayHistoryItem::getDateTime));
            return items.isEmpty() ? Optional.empty() : Optional.of(items.get(items.size() - 1));
        } catch (SQLException e) {
            throw new RepositoryException("Failed to retrieve play history items.", e);
        }
    }


}
