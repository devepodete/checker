package Commands;

public interface AbstractCommandReader {
    boolean hasNext();
    void read();
    Command getCommand();
}
