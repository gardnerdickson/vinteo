package ca.vinteo.repository;

public abstract class DataRepository {

    protected String connectionString;

    public DataRepository(String connectionString) {
        this.connectionString = connectionString;
    }
}
