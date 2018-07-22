package ca.vinteo.repository;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class LibraryConfigService {

    private final DirectoryRepository directoryRepository;

    public LibraryConfigService(DirectoryRepository directoryRepository) {
        this.directoryRepository = directoryRepository;
    }

    public void addDirectory(String path) throws RepositoryException {
        directoryRepository.addDirectory(path);
    }

    public ImmutableList<Directory> findAllDirectories() throws RepositoryException {
        return directoryRepository.findAllDirectories();
    }

}
