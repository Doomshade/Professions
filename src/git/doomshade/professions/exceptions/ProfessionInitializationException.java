package git.doomshade.professions.exceptions;

import java.util.List;

public class ProfessionInitializationException extends Exception {


    public static final int NO_ID = -1;

    public ProfessionInitializationException(Class<?> clazz, List<String> list) {
        this(clazz, list, NO_ID);
    }

    public ProfessionInitializationException(Class<?> clazz, List<String> list, int id) {
        super("Could not fully deserialize " + clazz.getSimpleName() + (id != NO_ID ? " with id " + id : "") + " as some of the keys are missing! - " + list + ".");
    }
}
