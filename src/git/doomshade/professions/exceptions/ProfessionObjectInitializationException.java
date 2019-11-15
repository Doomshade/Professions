package git.doomshade.professions.exceptions;

import java.util.List;

public class ProfessionObjectInitializationException extends Exception {
    private static final int NO_ID = -1;

    public ProfessionObjectInitializationException(Class<?> clazz, List<String> list) {
        this(clazz, list, NO_ID);
    }

    public ProfessionObjectInitializationException(Class<?> clazz, List<String> list, int id) {
        this(clazz, list, id, "");
    }

    public ProfessionObjectInitializationException(Class<?> clazz, List<String> list, int id, String additionalMessage) {
        super("Could not fully deserialize of object " + clazz.getSimpleName() + (id != NO_ID ? " with id " + id : "") + " as some of the keys are missing! - " + list + ". " + additionalMessage);
    }
}
