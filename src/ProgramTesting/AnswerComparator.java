package ProgramTesting;

import Logging.RunnerPaths;

import java.util.Comparator;
import java.util.logging.Level;

public class AnswerComparator implements Comparator<String> {


    @Override
    public int compare(String file1, String file2) {
        ProcessBuilder diffBuilder = new ProcessBuilder(
                RunnerPaths.terminalName,
                "-c",
                "cmp " + file1 + " " + file2);
        //System.out.println("Comparing files " + fullRunOutputFileName + " " + correctOutputFullName);
        try {
            Process diffProcess = diffBuilder.start();
            return diffProcess.waitFor();
        } catch (Exception e) {
            Checker.checkerLogger.log(Level.WARNING, "Error while comparing answers", e);
        }

        return 1;
    }
}
