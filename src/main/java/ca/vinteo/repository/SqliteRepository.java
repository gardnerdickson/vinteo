package ca.vinteo.repository;

public abstract class SqliteRepository {

    protected String connectionString;

    public SqliteRepository(String connectionString) {
        this.connectionString = connectionString;
    }
}
