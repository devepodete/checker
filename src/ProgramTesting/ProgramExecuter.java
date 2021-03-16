package ProgramTesting;

import Logging.GeneralPaths;
import Logging.RunnerPaths;
import Logging.Verdicts;

import java.io.File;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ProgramExecuter implements Runnable {

    public ProgramExecuter() {}

    @Override
    public void run() {

        if (!Checker.fileManager.directoryExists(Checker.currentProblemTestsDirectory)) {
            Checker.writeVerdict("No tests");
            logCurTestVerdict(0, Verdicts.CHECKER_ERROR);
            return;
        }

        File testsPath = new File(Checker.currentProblemTestsDirectory);
        String[] testsFoldersNames = testsPath.list();
        if (testsFoldersNames == null || testsFoldersNames.length == 0) {
            Checker.writeVerdict("No tests");
            logCurTestVerdict(0, Verdicts.CHECKER_ERROR);
            return;
        }

        Arrays.sort(testsFoldersNames, Comparator.comparing(Integer::valueOf));

        int currentTest = 1;
        for (String testFolderName : testsFoldersNames) {
            String inputFullName = String.join("/",
                    Checker.currentProblemTestsDirectory, testFolderName, RunnerPaths.inputName);
            String correctOutputFullName = String.join("/",
                    Checker.currentProblemTestsDirectory, testFolderName, RunnerPaths.outputName);

            ProcessBuilder runBuilder = new ProcessBuilder(
                    RunnerPaths.terminalName,
                    "-c",
                    "cat " + inputFullName + "| \\time -f \"%e\" -o " + Checker.runTimeFullPath +
                            " ./" + GeneralPaths.executableName)
                    .directory(new File(Checker.executableDirectory));

            runBuilder.redirectError(new File(Checker.runErrorFileFullPath))
                    .redirectOutput(new File(Checker.runOutputFileFullPath));

            try {
                Process runProcess = runBuilder.start();
                boolean hasTL = !runProcess.waitFor((long) Checker.currentProblemTimeLimit, TimeUnit.SECONDS);
                runProcess.destroy();

                if (hasTL) {
                    logTimelimitError(currentTest);
                    logCurTestVerdict(currentTest, Verdicts.TL);
                    return;
                }
                if (runProcess.exitValue() != Checker.OK) {
                    logRuntimeError(currentTest);
                    logCurTestVerdict(currentTest, Verdicts.RE);
                    return;
                }

                AnswerComparator answerComparator = new AnswerComparator();
                int compareAnswersRes = answerComparator.compare(correctOutputFullName,
                        Checker.runOutputFileFullPath);
                if (compareAnswersRes != 0) {
                    logWAError(currentTest);
                    logCurTestVerdict(currentTest, Verdicts.WA);
                    return;
                }
                logCurTestVerdict(currentTest, Verdicts.OK);

            } catch (Exception e) {
                logRuntimeError(currentTest);
                logException(e);
                return;
            }
        }

        Checker.writeVerdict(Verdicts.OK);
    }

    public static void logWAError(int test) {
        logVerdict("WA", "WA at test", test, Level.INFO);
    }

    public static void logRuntimeError(int test) {
        logVerdict("RE", "RE at test", test, Level.INFO);
    }

    public static void logTimelimitError(int test) {
        logVerdict("TL", "TL at test", test, Level.INFO);
    }

    public static void logMemorylimitError(int test) {
        logVerdict("ML", "ML at test", test, Level.INFO);
    }

    public static void logVerdict(final String verdict, final String logMsg,
                                  final int test, final Level logLevel) {
        Checker.checkerLogger.log(logLevel, logMsg + " " + test);
        Checker.writeVerdict(verdict);
    }

    public static void logException(Throwable t) {
        Checker.checkerLogger.log(Level.WARNING, "Exception in checker occurred", t);
    }

    public static void logCurTestVerdict (final int test, final String verdict) {
        Checker.fileManager.writeToFile(Checker.runLogsFileFullPath,
                test + ". " + verdict + System.lineSeparator(),
                StandardOpenOption.WRITE, StandardOpenOption.APPEND);
    }
}
