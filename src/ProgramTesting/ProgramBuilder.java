package ProgramTesting;

import java.io.File;

public class ProgramBuilder implements Buildable {

    public static final String mainFileName = "main.cpp";
    public static final String sanitizerFlags = "-fsanitize=address,undefined";
    public static final String cppVersion = "-std=c++17";



    public ProgramBuilder() {

    }

    @Override
    public void build() {
        String executableFileFullPath = String.join("/", executableDirectory, executableName);
        ProcessBuilder executableBuilder = new ProcessBuilder(
                "g++", cppVersion, sanitizerFlags,
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
    }
}
