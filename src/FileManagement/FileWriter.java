package FileManagement;

import java.nio.file.StandardOpenOption;
import java.util.Optional;

public interface FileWriter {
    void writeToFile(final String filename, final String s, StandardOpenOption... options);
}
