package csusm.cougarplanner.commandPattern;

/**
 * Common interface for any user action that should be reversible (and so
 * driven through {@link CommandManager}). Each implementation wraps a single
 * task action — create, delete, toggle complete, etc. — with the bit of code
 * needed to roll it back.
 *
 * Most commands snapshot whatever state they need at execute() time so that
 * undo() doesn't have to go re-read it from disk or the cache.
 */
public interface Command {

    /**
     * Performs the action. Called once when the user triggers it, and again
     * by {@link CommandManager#redo()}.
     */
    void execute() throws Exception;

    /**
     * Reverts whatever execute() did. Implementations should be tolerant of
     * being called when there's nothing to undo (e.g. a delete that didn't
     * find its target) rather than throwing.
     */
    void undo() throws Exception;

    // In-row field edits (title/description) are excluded from the undo stack
    // to avoid character-by-character history. Commands that shouldn't be
    // recorded override this to return false.
    default boolean isUndoable() { return true; }
}
