package ca.vinteo.repository;

import com.google.common.collect.ImmutableList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Directory {
    private final int id;
    private final String path;

    public Directory(int id, String path) {
        this.id = id;
        this.path = path;
    }

    static Directory createFromResultSet(ResultSet resultSet) throws SQLException {
        return new Directory(
                resultSet.getInt("directory_id"),
                resultSet.getString("path")
        );
    }

    static ImmutableList<Directory> createListFromResultSet(ResultSet resultSet) throws SQLException {
        ArrayList<Directory> records = new ArrayList<>();
        while (resultSet.next()) {
            records.add(createFromResultSet(resultSet));
        }
        return ImmutableList.copyOf(records);
    }

    public int getId() {
        return id;
    }

    public String getPath() {
        return path;
    }
}
