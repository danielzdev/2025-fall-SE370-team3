package csusm.cougarplanner.commandPattern;

public class CommandManager {
    public void execute(Command cmd) {
        try {
            cmd.execute();
        } catch (Exception ex) {
            throw new RuntimeException("Command execution failed", ex);
        }
    }
}
