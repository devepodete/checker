import Commands.Command;
import FileManagement.FileManager;
import Logging.CheckerLogger;
import Logging.RunnerPaths;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.*;

public class Checker {

    public static final int OK = 0;

    public static final String terminalName = "zsh";

    public static final String rootDirectory = "/home/zero/Projects/IdeaProjects/Checker/testArea";

    public static final String checkerLogsFileName = "checkerLogs.log";
    public static final String checkerLogsFullPath = String.join("/", rootDirectory, checkerLogsFileName);

    public static final String testsFolderName = "tests";
    public static final String srcFolderName = "src";
    public static final String buildFolderName = "build";
    public static final String logsFolderName = "logs";


    static int serverPort;
    static Socket socket = null;
    static ObjectInputStream commandSourceInputStream;

    static String currentContest, currentProblem, currentSubmit;
    static String currentProblemDirectory;

    static String currentProblemTimeLimitFileFullName;
    static float currentProblemTimeLimit;
    final static float defaultTimeLimitSeconds = 10.0f;

    static String currentProblemMemoryLimitFileFullName;
    static float currentProblemMemoryLimit;
    final static float defaultMemoryLimitMegabytes = 256.0f;

    static FileManager fileManager = new FileManager();
    static CheckerLogger checkerLogger = new CheckerLogger(checkerLogsFullPath);

    public static void main(String[] args) throws IOException {
        initChecker(args);
        connectToMain();

        CheckerLogger checkerLogger = new CheckerLogger(checkerLogsFullPath);

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
                            parseDirectories(cmd.getCommandArguments());

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

        final String currentContest = "AStar";
        final String currentProblem = "ProblemA";
        final String currentSubmit = "sevaSubmit";
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

    public static void terminate() throws IOException {
        commandSourceInputStream.close();
        socket.close();
    }

    public static void parseDirectories(List<String> args) {
        currentContest = args.get(0);
        currentProblem = args.get(1);
        currentSubmit = args.get(2);


        currentProblemDirectory = String.join("/", rootDirectory,
                currentContest, currentProblem);

        currentProblemTimeLimitFileFullName = String.join("/",
                currentProblemDirectory, RunnerPaths.timeLimitName);
        parseTimeLimit();

        currentProblemMemoryLimitFileFullName = String.join("/",
                currentProblemDirectory, RunnerPaths.memoryLimitName);
        parseMemoryLimit();



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
}
