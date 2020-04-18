package ca.vinteo.repository;

import com.google.common.collect.ImmutableList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseChangelogItem {

    private Integer id;
    private String scriptName;
    private LocalDateTime dateTimeExecuted;

    public DatabaseChangelogItem(Integer id, String scriptName, LocalDateTime dateTimeExecuted) {
        this.id = id;
        this.scriptName = scriptName;
        this.dateTimeExecuted = dateTimeExecuted;
    }

    static DatabaseChangelogItem createFromResultSet(ResultSet resultSet) throws SQLException {
        return new DatabaseChangelogItem(
                resultSet.getInt("database_changelog_id"),
                resultSet.getString("script_name"),
                LocalDateTime.parse(resultSet.getString("date_time_executed"), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    static ImmutableList<DatabaseChangelogItem> createListFromResultSet(ResultSet resultSet) throws SQLException {
        ImmutableList.Builder<DatabaseChangelogItem> records = new ImmutableList.Builder<>();
        while (resultSet.next()) {
            records.add(createFromResultSet(resultSet));
        }
        return records.build();
    }

    public Integer getId() {
        return id;
    }

    public String getScriptName() {
        return scriptName;
    }

    public LocalDateTime getDateTimeExecuted() {
        return dateTimeExecuted;
    }

    @Override
    public String toString() {
        return "DatabaseChangelogItem{" +
                "id=" + id +
                ", scriptPath='" + scriptName + '\'' +
                ", dateTimeExecuted=" + dateTimeExecuted +
                '}';
    }
}
