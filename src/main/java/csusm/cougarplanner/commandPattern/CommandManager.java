package csusm.cougarplanner.commandPattern;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandManager {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command cmd) {
        try {
            cmd.execute();
            if (cmd.isUndoable()) {
                undoStack.push(cmd);
                redoStack.clear();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Command execution failed", ex);
        }
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        Command cmd = undoStack.pop();
        try {
            cmd.undo();
            redoStack.push(cmd);
        } catch (Exception ex) {
            throw new RuntimeException("Command undo failed", ex);
        }
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        Command cmd = redoStack.pop();
        try {
            cmd.execute();
            undoStack.push(cmd);
        } catch (Exception ex) {
            throw new RuntimeException("Command redo failed", ex);
        }
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
}
