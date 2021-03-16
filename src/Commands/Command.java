package Commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Command implements Serializable {
    private CommandType commandType;
    private ArrayList<String> commandArguments;

    public Command() {}

    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public ArrayList<String> getCommandArguments() {
        return commandArguments;
    }

    public void setCommandArguments(ArrayList<String> commandArguments) {
        this.commandArguments = new ArrayList<>(commandArguments);
    }
}
