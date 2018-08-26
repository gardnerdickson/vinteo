package ca.vinteo.repository;

import java.util.List;
import java.util.Set;

public class UserConfiguration {

    private List<String> directories;
    private Set<String> fileExtensions;

    public List<String> getDirectories() {
        return directories;
    }

    public void setDirectories(List<String> directories) {
        this.directories = directories;
    }

    public Set<String> getFileExtensions() {
        return fileExtensions;
    }

    public void setFileExtensions(Set<String> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }
}
