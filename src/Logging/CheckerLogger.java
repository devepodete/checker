package Logging;

import java.io.FileInputStream;
import java.util.logging.*;


public class CheckerLogger {
    Logger LOGGER;

    public CheckerLogger(String logFileName) {
        try (FileInputStream is = new FileInputStream(logFileName)) {
            LogManager.getLogManager().readConfiguration(is);
            LOGGER = Logger.getLogger(CheckerLogger.class.getName());
        } catch (Exception e) {
            System.err.println("Failed to create logger with file " + logFileName);
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void log(Level level, String msg) {
        LOGGER.log(level, msg);
    }

    public void log(Level level, String msg, Throwable thrown) {
        LOGGER.log(level, msg, thrown);
    }
}
