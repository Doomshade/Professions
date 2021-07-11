package git.doomshade.professions.exceptions;

import git.doomshade.professions.io.ProfessionLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;

public class ProfessionObjectInitializationException extends InitializationException {
    public static final int NO_ID = -1;
    private Collection<String> keys;
    private Class<?> clazz;
    private String additionalMessage = "";
    private ExceptionReason reason;
    private int id = -1;

    // separations in ids
    // should be the same size as (reasons - 1)


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
     * The main constructor of this exception. Consider calling {@link ProfessionLogger#log(String, Level)} instead of {@link #printStackTrace()} for the exception message if you do not need to print the stack trace.
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
        super();
        this.reason = reason;
        this.keys = keys;
        this.id = id;
        this.clazz = clazz;
        this.additionalMessage = additionalMessage;

        /*
        if (id != -1)
            this.ids.add(id);

        this.reasons.add(reason);
        this.separations.add(0);*/
    }

    public void setAdditionalMessage(String additionalMessage) {
        this.additionalMessage = additionalMessage;
    }

    @Override
    public String getMessage() {
        if (clazz == null) {
            return super.getMessage();
        }
        String s = String.format("Could not fully deserialize object of %s.<br>Reason(s):<br>",
                clazz.getSimpleName());

        s = s.concat(String.format("- %s (%s)%s %s", reason, keys, id == -1 ? "" : " for id: " + id, additionalMessage));

        /*
         s_ids,
                        reasons.stream().map(x -> x.s).collect(Collectors.joining(", ")),
                        keys,
                        additionalMessage
         */

       /* int lastId = 0;
        for (int i = 0; i < reasons.size(); i++) {
            int separator = separations.get(i);
            if (separator == 0) {
                separator = this.ids.size();
            }
            final List<Integer> sublist = this.ids.subList(lastId, separator);
            lastId = separator;
            String forIds = sublist.isEmpty() ? "." : " for ids: " + sublist + ".";
            s = s.concat(String.format("%s (%s)%s %s", reasons.get(i).s, keys, forIds, additionalMessage));

            if (i + 1 != reasons.size()) {
                s = s.concat("\n");
            }
        }*/

        return s;
    }


    public ProfessionObjectInitializationException(String message) {
        super(message);
        keys = new HashSet<>();
    }

    /*
    public void addId(int id) {
        this.ids.add(id);
    }

    public void addIds(Collection<Integer> ids) {
        this.ids.addAll(ids);
    }

    public void separate() {
        this.separations.add(this.ids.size());
    }*/

    public Collection<String> getKeys() {
        return Collections.unmodifiableCollection(keys);
    }

    public void addKey(String key) {
        keys.add(key);
    }

    public void addKeys(Collection<String> keys) {
        this.keys.addAll(keys);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public int getId() {
        return id;
    }

    public void setReason(ExceptionReason reason) {
        this.reason = reason;
    }

    public ExceptionReason getReason() {
        return reason;
    }

    /*
    public ProfessionObjectInitializationException setNext(ProfessionObjectInitializationException ex) {

        ex.previous = this;
        this.next = ex;

        return ex;

        addKeys(ex.getKeys());
        addIds(ex.ids);
        separate();
    }*/


    public enum ExceptionReason {
        MISSING_KEYS("some of the keys are missing"), KEY_ERROR("keys have been assigned wrong value");

        final String s;

        ExceptionReason(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }
    }
}
