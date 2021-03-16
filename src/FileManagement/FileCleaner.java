package FileManagement;

import java.util.Optional;

public interface FileCleaner {
    Optional<Boolean> clearFile(final String filename);
}
