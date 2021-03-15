package Commands;

import java.io.Serializable;
import java.util.List;

public class Command implements Serializable {
    private CommandType commandType;
    private List<String> commandArguments;

    public Command() {}

    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public List<String> getCommandArguments() {
        return commandArguments;
    }

    public void setCommandArguments(List<String> commandArguments) {
        this.commandArguments = commandArguments;
    }
}
