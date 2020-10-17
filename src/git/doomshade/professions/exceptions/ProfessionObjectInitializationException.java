package git.doomshade.professions.exceptions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;

public class ProfessionObjectInitializationException extends Exception {
    private static final int NO_ID = -1;
    private final Collection<String> keys;

    /**
     * Calls {@link #ProfessionObjectInitializationException(Class, Collection, int)} with an ID of -1 (Magical number)
     *
     * @param clazz the item type class in which the error occurred
     * @param keys  the keys of missing keys
     */
    public ProfessionObjectInitializationException(Class<?> clazz, Collection<String> keys) {
        this(clazz, keys, NO_ID, ExceptionReason.MISSING_KEYS);
    }

    public ProfessionObjectInitializationException(Class<?> clazz, Collection<String> keys, ExceptionReason reason) {
        this(clazz, keys, NO_ID, reason);
    }

    public ProfessionObjectInitializationException(Class<?> clazz, Collection<String> keys, int id, ExceptionReason reason) {
        this(clazz, keys, id, "", reason);
    }

    /**
     * Calls {@link #ProfessionObjectInitializationException(Class, Collection, int, String)} with an ID of -1 (Magical number) and empty additional message
     *
     * @param clazz             the item type class in which the error occurred
     * @param keys              the keys of missing keys
     * @param additionalMessage the additional message to add at the end of exception
     */
    public ProfessionObjectInitializationException(Class<?> clazz, Collection<String> keys, String additionalMessage) {
        this(clazz, keys, NO_ID, additionalMessage);
    }

    /**
     * Calls {@link #ProfessionObjectInitializationException(Class, Collection, int, String)} with an empty additional message
     *
     * @param clazz the item type class in which the error occurred
     * @param keys  the keys of missing keys
     * @param id    the ID of {@code ItemType}
     */
    public ProfessionObjectInitializationException(Class<?> clazz, Collection<String> keys, int id) {
        this(clazz, keys, id, "");
    }

    /**
     * The main constructor of this exception. Consider calling {@link git.doomshade.professions.Professions#log(String, Level)} instead of {@link #printStackTrace()} for the exception message if you do not need to print the stack trace.
     *
     * @param clazz             the item type class in which the error occurred
     * @param keys              the missing keys
     * @param id                the ID of {@code ItemType}
     * @param additionalMessage the additional message to add at the end of exception
     */
    public ProfessionObjectInitializationException(Class<?> clazz, Collection<String> keys, int id, String additionalMessage) {
        this(clazz, keys, id, additionalMessage, ExceptionReason.MISSING_KEYS);
    }

    public ProfessionObjectInitializationException(Class<?> clazz, Collection<String> keys, int id, String additionalMessage, ExceptionReason reason) {
        super("Could not fully deserialize object of " + clazz.getSimpleName() + (id != NO_ID ? " with id " + id : "") + " " + reason.s + " - " + keys + ". " + additionalMessage);
        this.keys = keys;
    }

    public enum ExceptionReason {
        MISSING_KEYS("as some of the keys are missing!"), KEY_ERROR("as a key has been assigned wrong value!");

        final String s;

        ExceptionReason(String s) {
            this.s = s;
        }
    }

    public ProfessionObjectInitializationException(String message) {
        super(message);
        keys = new HashSet<>();
    }

    public Collection<String> getKeys() {
        return Collections.unmodifiableCollection(keys);
    }

    public void addKey(String key) {
        keys.add(key);
    }

    public void addKeys(Collection<String> keys) {
        this.keys.addAll(keys);
    }

    public void add(ProfessionObjectInitializationException ex) {
        addKeys(ex.getKeys());
    }
}
