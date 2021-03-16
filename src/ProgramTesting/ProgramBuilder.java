package ProgramTesting;

import Logging.BuilderPaths;
import Logging.GeneralPaths;
import Logging.Verdicts;


import java.io.File;
import java.util.logging.Level;

public class ProgramBuilder implements Buildable {

    public static final String mainFileName = "main.cpp";
    public static final String sanitizerFlags = "-fsanitize=address,undefined";
    public static final String cppVersion = "-std=c++17";


    public ProgramBuilder() {

    }

    @Override
    public boolean build() {
        String executableFileFullPath = String.join("/", Checker.executableDirectory, GeneralPaths.executableName);
        ProcessBuilder executableBuilder = new ProcessBuilder(
                "g++", cppVersion, sanitizerFlags,
                mainFileName, "-o", executableFileFullPath)
                .directory(new File(Checker.srcDirectory));

        String buildLogFileFullPath = String.join("/", Checker.logDirectory, BuilderPaths.buildLogFileName);
        executableBuilder.redirectErrorStream(true);
        executableBuilder.redirectOutput(ProcessBuilder.Redirect.to(new File(buildLogFileFullPath)));

        try {
            Checker.checkerLogger.log(Level.INFO, "Compiling source code from " +
                    Checker.srcDirectory);
            Process compileProcess = executableBuilder.start();
            int compileRes = compileProcess.waitFor();
            if (compileRes == Checker.OK) {
                Checker.checkerLogger.log(Level.INFO, "Compilation Completed");
                return true;
            } else {
                Checker.checkerLogger.log(Level.INFO, "Compilation Failed");
                Checker.writeVerdict(Verdicts.CE);
            }
        } catch (Exception e) {
            Checker.checkerLogger.log(Level.WARNING, "Exception was caught during compilation");
            Checker.writeVerdict(Verdicts.CHECKER_ERROR);
        }
        return false;
    }
}
