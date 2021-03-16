import Commands.Command;
import Commands.CommandReader;
import Commands.CommandType;
import Logging.BuilderPaths;
import Logging.RunnerPaths;
import ProgramTesting.Checker;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;


public class Main {
    static ServerSocket serverSocket;
    static Socket checkerSocket;
    static Process checkerProcess;

    static ObjectOutputStream writeToCheckerStream;

    final static long MAX_CHECKER_EXIT_WAIT_TIME_SECONDS = 5;
    
    public static void main(String[] args) throws IOException, InterruptedException {
        //System.out.println("Building checker... ");
        //buildChecker();
        //System.out.println("OK");
        System.out.println("Creating server socket... ");
        createServerSocket();
        System.out.println("Server local port: " + serverSocket.getLocalPort());
        System.out.println("Running checker process... ");
        runCheckerProcess();
        System.out.println("Connecting to checker... ");
        connectToChecker();

        System.out.println("-----------------");
        System.out.println("Initialization completed");
        System.out.println("-----------------");

        CommandReader commandReader = new CommandReader(System.in);
        do {
            System.out.print("> ");
            System.out.flush();
            commandReader.read();
            Command curCommand = commandReader.getCommand();
            System.out.println("Got command: " + curCommand.getCommandType() + " " + curCommand.getCommandArguments().toString());
            if (curCommand.getCommandType() != CommandType.INVALID_COMMAND) {
                writeToCheckerStream.writeObject(curCommand);
                System.out.println("Command sent to checker");
            } else {
                System.err.println("Invalid command");
            }

            if (curCommand.getCommandType() == CommandType.EXIT) {
                break;
            }
        } while (true);


        System.out.println("Terminating Main...");
        terminate();
        System.out.println("Main terminated successfully");
    }


    public static void createServerSocket() throws IOException {
        serverSocket = new ServerSocket(0);
    }

    public static void buildChecker() throws IOException, InterruptedException {
        ProcessBuilder checkerBuildProcessBuilder = new ProcessBuilder(
                BuilderPaths.javacPath,
                "-d",
                BuilderPaths.CheckerClassPath
        );

        Process p = checkerBuildProcessBuilder.start();
        p.waitFor();
    }

    public static void runCheckerProcess() throws IOException {
        ProcessBuilder checkerRunProcessBuilder = new ProcessBuilder(
                RunnerPaths.terminalName,
                "-c",
                RunnerPaths.javaPath + " -classpath " + BuilderPaths.CheckerClassPath + " " + BuilderPaths.CheckerClassName + " " +
                String.valueOf(serverSocket.getLocalPort()))
                .directory(new File(BuilderPaths.buildClassPath));

        checkerProcess = checkerRunProcessBuilder.start();
    }

    public static void connectToChecker() throws IOException {
        System.out.println("Awaiting checker to connect... ");
        checkerSocket = serverSocket.accept();
        writeToCheckerStream = new ObjectOutputStream(checkerSocket.getOutputStream());
    }

    public static void terminate() throws InterruptedException, IOException {
        System.out.println("Waiting for checker process to terminate... ");
        boolean exitedNormally = checkerProcess.waitFor(MAX_CHECKER_EXIT_WAIT_TIME_SECONDS, TimeUnit.SECONDS);
        if (exitedNormally) {
            System.out.println("Checker finished job normally");
        } else {
            System.out.println("Checker was killed");
        }
        
        System.out.println("Closing objectOutputStream... ");
        writeToCheckerStream.close();

        System.out.println("Closing checkerSocket... ");
        checkerSocket.close();

        System.out.println("Closing serverSocket... ");
        serverSocket.close();
    }

}
