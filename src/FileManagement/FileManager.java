package FileManagement;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Scanner;

public class FileManager implements FileWriter, DirectoryCreator, FloatParser, FileCleaner{
    @Override
    public Optional<Boolean> writeToFile(final String filename, final String s, StandardOpenOption... options) {
        try {
            File f = new File(filename);
            try {
                boolean createdExtraDirectories = f.getParentFile().mkdirs();
                f.createNewFile();
            } catch (Exception e) {
                System.err.println("Exception was caught while creating file '" + filename);
                e.printStackTrace();
                return Optional.empty();
            }

            Files.write(Paths.get(filename), s.getBytes(), options);
            return Optional.of(true);
        } catch (Exception e) {
            System.err.println("Exception was caught while writing to file '" + filename + "' string '" + s +"'");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * tries to create directory
     *
     * @param directoryFullPath full path of directory that will be created
     * @return Optional(true) if succeeded; Optional(false) if directory already existed;
     * Optional.empty() if exception was caught
     */
    @Override
    public Optional<Boolean> createDirectory(String directoryFullPath) {
        try {
            File newDirectory = new File(directoryFullPath);
            if (!newDirectory.exists()) {
                newDirectory.mkdirs();
                return Optional.of(true);
            } else {
                return Optional.of(false);
            }
        } catch (Exception e) {
            System.err.println("Failed to create directory \'" + directoryFullPath + "\'");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<Float> parseFloatValueFromFile(String filename) {
        try {
            Scanner s = new Scanner(new File(filename))
            return Optional.of(s.nextFloat());
        } catch (Exception e) {
            System.err.println("Exception was caught while parsing float from file " + filename);
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<Boolean> clearFile(final String filename) {
        try {
            Files.newBufferedWriter(Path.of(filename), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)
                    .close();
            return Optional.of(true);
        } catch (Exception e) {
            System.err.println("Exception was caught while trying to clear file " + filename);
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
