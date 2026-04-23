package csusm.cougarplanner.commandPattern;

public interface Command {
    void execute() throws Exception;

    void undo() throws Exception;

    // In-row field edits (title/description) are excluded from the undo stack
    // to avoid character-by-character history. Commands that shouldn't be
    // recorded override this to return false.
    default boolean isUndoable() { return true; }
}
