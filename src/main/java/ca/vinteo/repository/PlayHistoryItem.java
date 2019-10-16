package ca.vinteo.repository;

import com.google.common.collect.ImmutableList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class PlayHistoryItem {

    private Integer id;
    private String path;
    private String directory;
    private String name;
    private LocalDateTime dateTime;

    public PlayHistoryItem(Integer id, String path, String directory, String name, LocalDateTime dateTime) {
        this.id = id;
        this.path = path;
        this.directory = directory;
        this.name = name;
        this.dateTime = dateTime;
    }

    static PlayHistoryItem createFromResultSet(ResultSet resultSet) throws SQLException {
        return new PlayHistoryItem(
                resultSet.getInt("play_history_id"),
                resultSet.getString("path"),
                resultSet.getString("directory"),
                resultSet.getString("name"),
                LocalDateTime.parse(resultSet.getString("date_time"), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    static ImmutableList<PlayHistoryItem> createListFromResultSet(ResultSet resultSet) throws SQLException {
        ArrayList<PlayHistoryItem> records = new ArrayList<>();
        while (resultSet.next()) {
            records.add(createFromResultSet(resultSet));
        }
        return ImmutableList.copyOf(records);
    }

    public Integer getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getDirectory() {
        return directory;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return "HistoryItem{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", directory='" + directory + '\'' +
                ", name='" + name + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }
}
