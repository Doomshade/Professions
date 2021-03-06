package git.doomshade.professions.exceptions;

import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.io.ProfessionLogger;

import java.util.Collection;
import java.util.logging.Level;

public class ProfessionInitializationException extends InitializationException {

    private static final int NO_ID = -1;

    public ProfessionInitializationException(String message) {
        super(message);
    }

    /**
     * Calls {@link #ProfessionInitializationException(Class, Collection, int)} with an ID of -1 (Magical number).
     *
     * @param clazz the item type class in which the error occurred
     * @param keys  the keys of missing keys
     */
    public ProfessionInitializationException(Class<?> clazz, Collection<String> keys) {
        this(clazz, keys, NO_ID);
    }

    /**
     * Calls {@link #ProfessionInitializationException(Class, Collection, int, String)} with an ID of -1 (Magical number) and empty additional message
     *
     * @param clazz             the item type class in which the error occurred
     * @param keys              the keys of missing keys
     * @param additionalMessage the additional message to add at the end of exception
     */
    public ProfessionInitializationException(Class<?> clazz, Collection<String> keys, String additionalMessage) {
        this(clazz, keys, NO_ID, additionalMessage);
    }


    /**
     * Calls {@link #ProfessionInitializationException(Class, Collection, int, String)} with an empty additional message
     *
     * @param clazz the item type class in which the error occurred
     * @param keys  the keys of missing keys
     * @param id    the ID of {@code ItemType}
     */
    public ProfessionInitializationException(Class<?> clazz, Collection<String> keys, int id) {
        this(clazz, keys, id, "");
    }


    /**
     * The main constructor of this exception. Consider calling {@link ProfessionLogger#log(String, Level)} instead of {@link #printStackTrace()} for the exception message if you do not need to print the stack trace.
     *
     * @param clazz             the item type class in which the error occurred
     * @param keys              the missing keys
     * @param id                the ID of {@link ItemType}
     * @param additionalMessage the additional message to add at the end of exception
     */
    public ProfessionInitializationException(Class<?> clazz, Collection<String> keys, int id, String additionalMessage) {
        super("Could not fully deserialize " + clazz.getSimpleName().replace("itemtype", "") + (id != NO_ID ? " with id " + id : "") + " as some of the keys are missing! - " + keys + ". " + additionalMessage);
    }
}
