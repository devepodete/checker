import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;


public class Main {

    public static final int OK = 0;

    public static final String terminalName = "zsh";

    public static final String rootDirectory = "/home/zero/Projects/IdeaProjects/Checker/testArea";

    public static final String testsFolderName = "tests";
    public static final String srcFolderName = "src";
    public static final String buildFolderName = "build";
    public static final String logsFolderName = "logs";

    // log and output files
    public static final String buildLogFileName = "build.log";
    public static final String runOutputFileName = "runOut.txt";
    public static final String runErrorFileName = "runError.log";
    public static final String runTimeFileName = "runTime.log";
    public static final String verdictFileName = "verdict.log";
    public static final String runLogsFileName = "run.log";


    public static final String mainFileName = "main.cpp";
    public static final String sanitizerFlags = "-fsanitize=address,undefined";
    public static final String inputName = "in.txt";
    public static final String outputName = "out.txt";
    public static final String timeLimitName = "timeLimit.txt";
    public static final String memoryLimitName = "memoryLimit.txt";
    public static final String executableName = "a.out";

    public static void main(String[] args) throws IOException, InterruptedException {
        final String currentContest = "AStar";
        final String currentProblem = "ProblemA";
        final String currentSubmit = "sevaSubmit";
        //final int timeLimit = ;

        final String currentProblemDirectory = String.join("/", rootDirectory,
                currentContest, currentProblem);
        final String currentProblemTimeLimitFileFullName = String.join("/",
                currentProblemDirectory, timeLimitName);
        final String currentProblemMemoryLimitFileFullName = String.join("/",
                currentProblemDirectory, memoryLimitName);
        final String currentProblemTestsDirectory = String.join("/",
                currentProblemDirectory, testsFolderName);

        final String currentSubmitDirectory = String.join("/", currentProblemDirectory,
                currentSubmit);

        final String srcDirectory = String.join("/", currentSubmitDirectory, srcFolderName);
        final String buildDirectory = String.join("/", currentSubmitDirectory, buildFolderName);
        final String logDirectory = String.join("/", currentSubmitDirectory, logsFolderName);


        final String runTimeFullPath = String.join("/", logDirectory, runTimeFileName);
        final String runOutputFileFullPath = String.join("/", logDirectory, runOutputFileName);
        final String runErrorFileFullPath = String.join("/", logDirectory, runErrorFileName);
        final String runLogsFileFullPath = String.join("/", logDirectory, runLogsFileName);
        final String runVerdictFullPath = String.join("/", logDirectory, verdictFileName);

        final String executableDirectory = buildDirectory;


        // create *build* and *log* directories
        System.out.print("Creating build directory " + buildDirectory + "... ");
        Optional<Boolean> createBuildRes = createDirectory(buildDirectory);
        if (createBuildRes.isEmpty()) {
            System.err.println("Failed to create build directory. Exiting.");
        }
        System.out.println("OK");

        System.out.print("Creating log directory " + logDirectory + "... ");
        Optional<Boolean> createLogRes = createDirectory(logDirectory);
        if (createLogRes.isEmpty()) {
            System.err.println("Failed to create log directory. Exiting.");
        }
        System.out.println("OK");

        String executableFileFullPath = String.join("/", executableDirectory, executableName);
        ProcessBuilder executableBuilder = new ProcessBuilder(
                "g++", sanitizerFlags,
                mainFileName, "-o", executableFileFullPath)
                .directory(new File(srcDirectory));

        String buildLogFileFullPath = String.join("/", logDirectory, buildLogFileName);
        executableBuilder.redirectErrorStream(true);
        executableBuilder.redirectOutput(ProcessBuilder.Redirect.to(new File(buildLogFileFullPath)));

        System.out.println("Compiling...");
        System.out.println("compiling in directory: " + srcDirectory);
        System.out.println("executable full path: " + executableFileFullPath);
        Process compileProcess = executableBuilder.start();
        int compileRes = compileProcess.waitFor();


        if (compileRes == OK) {
            System.out.println("Compiling OK");

            System.out.println("Testing...");
            Optional<Boolean> testsResult = testExecutable(executableDirectory, executableName,
                    currentProblemTestsDirectory, runTimeFullPath, runOutputFileFullPath,
                    runErrorFileFullPath, runLogsFileFullPath, runVerdictFullPath);
            if (testsResult.isEmpty()) {
                System.err.println("Testing completed with system errors");
            } else {
                System.out.println("Testing completed without system errors");
            }

        } else {
            System.out.println("FAIL: " + compileRes);
        }

    }

    /**
     * tries to create directory
     *
     * @param directoryFullPath full path of directory that will be created
     * @return Optional(true) if succeeded; Optional(false) if directory already existed;
     * Optional.empty() if exception was caught
     */
    public static Optional<Boolean> createDirectory(String directoryFullPath) {
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

    public static Optional<Boolean> testExecutable(String curExecutableDirectory, String curExecutableName,
                                         String currentProblemTestsDirectory,
                                         String fullRunTimeFileName,
                                         String fullRunOutputFileName, String fullRunErrorFileName,
                                         String fullRunLogsFileName, String fullRunVerdictFileName) {

        String fullExecutableName = curExecutableDirectory + curExecutableName;

        File testsPath = new File(currentProblemTestsDirectory);
        String[] testsFoldersNames = testsPath.list();
        assert testsFoldersNames != null;
        Arrays.sort(testsFoldersNames);

        int currentTest = 1;

        //System.out.println("curExecutableDirectory: " + curExecutableDirectory);
        //System.out.println("runOutputFileName: " + runOutputFileName);

        for (String testFolderName : testsFoldersNames) {
            System.out.println("Test " + currentTest + " in folder " + testFolderName + "...");

            String inputFullName = String.join("/", currentProblemTestsDirectory, testFolderName, inputName);
            String correctOutputFullName = String.join("/", currentProblemTestsDirectory, testFolderName, outputName);



            ProcessBuilder runBuilder = new ProcessBuilder(
                    terminalName,
                    "-c",
                    "cat " + inputFullName +  "| \\time -f \"%e\" -o " + fullRunTimeFileName + " ./" + executableName)
                    .directory(new File(curExecutableDirectory));

            //System.out.println("curExecutableDirectory: " + curExecutableDirectory);

            runBuilder.redirectError(new File(fullRunErrorFileName))
                    .redirectOutput(new File(fullRunOutputFileName));

            try {
                Process runProcess = runBuilder.start();

                int runResult = runProcess.waitFor();
                if (runResult == OK) {
                    // no runtime errors

                    ProcessBuilder diffBuilder = new ProcessBuilder(
                            terminalName,
                            "-c",
                            "diff " + fullRunOutputFileName + " " + correctOutputFullName)
                            .directory(new File(curExecutableDirectory));
                    //System.out.println("Comparing files " + fullRunOutputFileName + " " + correctOutputFullName);
                    Process diffProcess = diffBuilder.start();
                    int diffResult = diffProcess.waitFor();
                    Optional<Boolean> writeResult, writeVerdictResult;

                    if (diffResult == OK) {
                        System.out.println("OK on test " + currentTest);
                        //clearFile(fullRunLogsFileName);
                        // correct answer
                        writeResult = writeToFile(fullRunLogsFileName,
                                currentTest + ". OK" + System.lineSeparator(),
                                StandardOpenOption.WRITE, StandardOpenOption.APPEND);

                    } else {
                        System.out.println("WA on test " + currentTest);
                        // wrong answer
                        writeResult = writeToFile(fullRunLogsFileName,
                                currentTest + ". WA" + System.lineSeparator(),
                                StandardOpenOption.WRITE, StandardOpenOption.APPEND);
                        writeVerdictResult = writeToFile(fullRunVerdictFileName,
                                "WA",
                                StandardOpenOption.WRITE);

                        if (writeVerdictResult.isEmpty()) {
                            return Optional.empty();
                        } else {
                            return Optional.of(false);
                        }
                    }

                    if (writeResult.isEmpty()) {
                        return Optional.empty();
                    }

                } else {
                    // runtime error
                    Optional<Boolean> writeResult = writeToFile(fullRunLogsFileName,
                            currentTest + ". RE" + System.lineSeparator(),
                            StandardOpenOption.WRITE, StandardOpenOption.APPEND);
                    if (writeResult.isEmpty()) {
                        return Optional.empty();
                    }

                    Optional<Boolean> writeVerdictResult = writeToFile(fullRunVerdictFileName,
                            "RE",
                            StandardOpenOption.WRITE);
                    if (writeVerdictResult.isEmpty()) {
                        return Optional.of(false);
                    }

                    return Optional.of(false);
                }
            } catch (Exception e) {
                System.err.println("Exception was caught while running executable in " + curExecutableDirectory);
                e.printStackTrace();
                return Optional.empty();
            }

            currentTest++;
        }

        Optional<Boolean> writeResult = writeToFile(fullRunVerdictFileName,
                "OK",
                StandardOpenOption.WRITE);
        if (writeResult.isEmpty()) {
            return Optional.of(false);
        }

        return Optional.of(true);
    }


    public static Optional<Boolean> writeToFile(final String filename, final String s, StandardOpenOption... options) {
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

    public static Optional<Boolean> clearFile(final String filename) {
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

    public static Optional<Float> parseFloatValueFromFile(final String filename) {
        try {
            Scanner s = new Scanner(new File(filename));
            return Optional.of(s.nextFloat());
        } catch (Exception e) {
            System.err.println("Exception was caught while parsing float from file " + filename);
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
