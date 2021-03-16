package Commands;

import java.util.Arrays;
import java.util.List;

public class CommandParser implements StringParser {
    private Command command = new Command();

    public CommandParser(){}

    @Override
    public void parse(String commandLine) {
        List<String> strings = Arrays.asList(commandLine.split(" "));
        if (strings.isEmpty()) {
            command.setCommandType(CommandType.INVALID_COMMAND);
            return;
        }

        parseCommand(strings.get(0));
        command.setCommandArguments(strings.subList(1, strings.size()));
    }

    public void parseCommand(String cmd) {
        if (cmd.equals("check")) {
            command.setCommandType(CommandType.CHECK);
        } else if (cmd.equals("exit")) {
            command.setCommandType(CommandType.EXIT);
        } else {
            command.setCommandType(CommandType.INVALID_COMMAND);
        }
    }

    public Command getCommand() {
        return command;
    }

    public CommandType getCommandType() {
        return command.getCommandType();
    }

    public List<String> getCommandArguments() {
        return command.getCommandArguments();
    }
}
