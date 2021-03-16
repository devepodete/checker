package ProgramTesting;

import Commands.Command;
import FileManagement.FileManager;
import Logging.CheckerLogger;
import Logging.RunnerPaths;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.*;

public class Checker {

    public static final int OK = 0;

    public static final String rootDirectory = "/home/zero/Projects/IdeaProjects/Checker/testArea";

    public static final String checkerConfigFileName = "logger.config";
    public static final String checkerLogsFileName = "checkerLogs.log";
    public static final String checkerConfigFullPath = String.join("/", rootDirectory, checkerConfigFileName);
    public static final String checkerLogsFullPath = String.join("/", rootDirectory, checkerLogsFileName);

    public static final String testsFolderName = "tests";
    public static final String srcFolderName = "src";
    public static final String buildFolderName = "build";
    public static final String logsFolderName = "logs";


    static int serverPort;
    static Socket socket = null;
    static ObjectInputStream commandSourceInputStream;

    // working directories
    static String currentContest, currentProblem, currentSubmit;
    static String currentProblemDirectory, currentProblemTestsDirectory, currentSubmitDirectory;
    static String srcDirectory, buildDirectory, logDirectory;
    static String runTimeFullPath, runOutputFileFullPath,
            runErrorFileFullPath, runLogsFileFullPath, runVerdictFullPath;

    public static String executableDirectory;


    // time limits
    static String currentProblemTimeLimitFileFullName;
    static float currentProblemTimeLimit;
    final static float defaultTimeLimitSeconds = 10.0f;

    // memory limits
    static String currentProblemMemoryLimitFileFullName;
    static float currentProblemMemoryLimit;
    final static float defaultMemoryLimitMegabytes = 256.0f;

    static FileManager fileManager = new FileManager();
    static CheckerLogger checkerLogger = new CheckerLogger();

    public static void main(String[] args) throws IOException {
        initChecker(args);
        connectToMain();

        checkerLogger.log(Level.INFO, "Checker initialized");

        boolean run = true;
        while (run) {
            try {
                Command cmd = (Command) commandSourceInputStream.readObject();

                switch (cmd.getCommandType()) {
                    case CHECK:
                        if (!verifyCheckerCheckCommand(cmd.getCommandArguments())) {
                            checkerLogger.log(Level.WARNING,
                                    "Got invalid arguments for check command: " +
                                            cmd.getCommandArguments().toString());
                        } else {
                            boolean parseOK = parseDirectories(cmd.getCommandArguments());
                            if (!parseOK) {
                                continue;
                            }

                            boolean createOK = createDirectories();
                            if (!createOK) {
                                continue;
                            }

                            checkCurrentSubmit();
                        }
                        break;
                    case EXIT:
                        run = false;
                        break;
                }

            } catch (ClassNotFoundException e) {
                checkerLogger.log(Level.WARNING, "Failed to cast received object to " +
                        Command.class.getName() + " class");
            } catch (Exception e) {
                checkerLogger.log(Level.SEVERE, "Got some unknown exception", e);
            }
        }

        terminate();
        checkerLogger.log(Level.INFO, "Checker destroyed");
    }

    public static boolean verifyCheckerCheckCommand(List<String> commandArguments) {
        return commandArguments.size() == 3;
    }

    public static void initChecker(String[] args) {
        if (args.length != 1) {

            String toThrow = "Failed to initialize Checker with command line arguments " +
                    Arrays.toString(args);
            throw new RuntimeException(toThrow);
        }

        serverPort = Integer.parseInt(args[0]);
    }

    public static void connectToMain() throws IOException {
        socket = new Socket("localhost", serverPort);
        commandSourceInputStream = new ObjectInputStream(socket.getInputStream());
    }

    public static void terminate() {
        try {
            commandSourceInputStream.close();
            socket.close();
        } catch (Exception ignored) {}
    }

    public static boolean parseDirectories(List<String> args) {
        currentContest = args.get(0);
        currentProblem = args.get(1);
        currentSubmit = args.get(2);
        List<String> mainDirectories = List.of(currentContest, currentProblem, currentSubmit);
        if (! fileManager.directoriesExists(mainDirectories)) {
            checkerLogger.log(Level.WARNING, "Some of directories " + mainDirectories.toString() +
                    " does not exist. Checking is not possible.");
            return false;
        }

        currentProblemDirectory = String.join("/", rootDirectory,
                currentContest, currentProblem);
        currentProblemTestsDirectory = String.join("/",
                currentProblemDirectory, testsFolderName);
        currentSubmitDirectory = String.join("/",
                currentProblemDirectory, currentSubmit);

        srcDirectory = String.join("/", currentSubmitDirectory, srcFolderName);
        if (! fileManager.directoryExists(srcDirectory)) {
            checkerLogger.log(Level.WARNING, "Source directory " + srcDirectory +
                    " does not exist. Checking is not possible.");
            return false;
        }
        buildDirectory = String.join("/", currentSubmitDirectory, buildFolderName);
        logDirectory = String.join("/", currentSubmitDirectory, logsFolderName);


        runTimeFullPath = String.join("/", logDirectory, RunnerPaths.runTimeFileName);
        runOutputFileFullPath = String.join("/", logDirectory, RunnerPaths.runOutputFileName);
        runErrorFileFullPath = String.join("/", logDirectory, RunnerPaths.runErrorFileName);
        runLogsFileFullPath = String.join("/", logDirectory, RunnerPaths.runLogsFileName);
        runVerdictFullPath = String.join("/", logDirectory, RunnerPaths.verdictFileName);
        executableDirectory = buildDirectory;


        // time limit
        currentProblemTimeLimitFileFullName = String.join("/",
                currentProblemDirectory, RunnerPaths.timeLimitName);
        parseTimeLimit();

        //memory limit
        currentProblemMemoryLimitFileFullName = String.join("/",
                currentProblemDirectory, RunnerPaths.memoryLimitName);
        parseMemoryLimit();

        return true;
    }

    public static boolean createDirectories() {
        Optional<Boolean> buildDirectoryCreated = fileManager.createDirectory(buildDirectory);
        if (buildDirectoryCreated.isEmpty()) {
            checkerLogger.log(Level.WARNING, "Build directory " + buildDirectory +
                    " can not be created. Checking is not possible.");
            return false;
        }

        Optional<Boolean> logDirectoryCreated = fileManager.createDirectory(logDirectory);
        if (logDirectoryCreated.isEmpty()) {
            checkerLogger.log(Level.WARNING, "Build directory " + logDirectory +
                    " can not be created. Checking is not possible.");
            return false;
        }

        return true;
    }

    public static void parseTimeLimit() {
        Optional<Float> parseTLRes = fileManager.parseFloatValueFromFile(
                currentProblemTimeLimitFileFullName);
        if (parseTLRes.isEmpty()) {
            checkerLogger.log(Level.INFO, "Unable to read TL for problem '"
                    + currentProblem + "' from contest '" + currentContest + ". Default value set" +
                    "to " + defaultTimeLimitSeconds + " seconds");
            currentProblemTimeLimit = defaultTimeLimitSeconds;
        } else {
            currentProblemTimeLimit = parseTLRes.get();
        }
    }

    public static void parseMemoryLimit() {
        Optional<Float> parseTLRes = fileManager.parseFloatValueFromFile(
                currentProblemTimeLimitFileFullName);
        if (parseTLRes.isEmpty()) {
            checkerLogger.log(Level.INFO, "Unable to read ML for problem '"
                    + currentProblem + "' from contest '" + currentContest + ". Default value set" +
                    "to " + defaultMemoryLimitMegabytes + " MB");
            currentProblemMemoryLimit = defaultMemoryLimitMegabytes;
        } else {
            currentProblemMemoryLimit = parseTLRes.get();
        }
    }

    public static void checkCurrentSubmit() {
        ProgramBuilder programBuilder = new ProgramBuilder();
        boolean buildOK = programBuilder.build();
        if (!buildOK) {
            return;
        }

        ProgramExecuter programExecuter = new ProgramExecuter();
        programExecuter.run();
    }

    public static void writeVerdict(final String msg) {
        fileManager.writeToFile(runVerdictFullPath, msg,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
