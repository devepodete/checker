package FileManagement;

import java.util.Optional;

public interface FloatParser {
    Optional<Float> parseFloatValueFromFile(final String filename);
}
