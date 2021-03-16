package Commands;

import java.io.InputStream;
import java.util.Scanner;

public class CommandReader implements AbstractCommandReader {
    Scanner scanner;
    CommandParser commandParser;

    public CommandReader(InputStream inputStream) {
        scanner = new Scanner(inputStream);
        commandParser = new CommandParser();

    }

    public Command getCommand() {
        return commandParser.getCommand();
    }

    @Override
    public boolean hasNext() {
        return scanner.hasNext();
    }


    @Override
    public void read() {
        commandParser.parse(scanner.nextLine());
    }
}
