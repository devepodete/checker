package FileManagement;

import java.util.Optional;

public interface DirectoryCreator {
    Optional<Boolean> createDirectory(String directoryFullPath);
}
