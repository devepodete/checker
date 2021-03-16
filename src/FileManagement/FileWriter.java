package FileManagement;

import java.nio.file.StandardOpenOption;
import java.util.Optional;

public interface FileWriter {
    Optional<Boolean> writeToFile(final String filename, final String s, StandardOpenOption... options);
}
