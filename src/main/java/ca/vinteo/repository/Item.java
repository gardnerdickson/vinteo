package ca.vinteo.repository;

import com.google.common.collect.ImmutableList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Item {

    private Integer id;
    private String path;
    private String name;

    public Item(Integer id, String path, String name) {
        this.id = id;
        this.path = path;
        this.name = name;
    }

    static Item createFromResultSet(ResultSet resultSet) throws SQLException {
        return new Item(
                resultSet.getInt("item_id"),
                resultSet.getString("path"),
                resultSet.getString("name")
        );
    }

    static ImmutableList<Item> createListFromResultSet(ResultSet resultSet) throws SQLException {
        ArrayList<Item> records = new ArrayList<>();
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

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
