package Logging;

import ProgramTesting.Checker;

import java.io.FileInputStream;
import java.util.logging.*;


public class CheckerLogger {
    Logger LOGGER;
    FileHandler fileHandler;

    public CheckerLogger() {
        try {
            LOGGER = Logger.getLogger(CheckerLogger.class.getName());
            fileHandler = new FileHandler(Checker.checkerLogsFullPath);
            LOGGER.addHandler(fileHandler);
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fileHandler.setFormatter(simpleFormatter);
        } catch (Exception e) {
            System.err.println("Failed to create logger");
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
