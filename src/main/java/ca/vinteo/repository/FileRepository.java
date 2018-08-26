package ca.vinteo.repository;

import java.nio.file.Path;

public abstract class FileRepository {

    protected final Path fileLocation;

    public FileRepository(Path fileLocation) {
        this.fileLocation = fileLocation;
    }

}
