package csusm.cougarplanner.commandPattern;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Invoker for the Command design pattern.
 * <p>
 * Holds two stacks — one for undo, one for redo — and runs commands against
 * them to provide the familiar Ctrl+Z / Ctrl+Y behavior for task actions.
 * A freshly executed command pushes onto {@code undoStack} and invalidates
 * any pending redos (because the user has branched the history). Undo moves
 * a command from undoStack to redoStack; redo moves it back.
 * <p>
 * Commands that return {@code false} from {@link Command#isUndoable()} are
 * executed but never recorded, so they don't pollute the history.
 */
public class CommandManager {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command cmd) {
        try {
            cmd.execute();
            if (cmd.isUndoable()) {
                undoStack.push(cmd);
                // A new action invalidates the redo history — the user has
                // branched off from the previous redo timeline.
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
