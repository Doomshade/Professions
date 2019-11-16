package git.doomshade.professions.utils;

import com.avaje.ebean.validation.NotNull;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A set of method utilities
 *
 * @author Doomshade
 */
public final class Utils {

    /**
     * @param iterable  the iterable (e.g. {@link Collection})
     * @param condition the condition based on which the method should query
     * @param <A>       the generic type
     * @return a {@link Set} of {@code <A>} with given {@code condition}
     * @throws SearchNotFoundException when nothing is found under the given condition
     */
    @NotNull
    public static <A> Set<A> findAllInIterable(Iterable<A> iterable, Predicate<A> condition) throws SearchNotFoundException {
        Set<A> set = new HashSet<>();
        for (A a : iterable) {
            if (condition.test(a)) {
                set.add(a);
            }
        }

        if (!set.isEmpty()) {
            return set;
        }
        throw new SearchNotFoundException();
    }

    /**
     * @param iterable  the iterable (e.g. {@link Collection})
     * @param condition the condition based on which the method should query
     * @param <A>       the generic type
     * @return an {@code <A>} with given {@code condition}
     * @throws SearchNotFoundException when nothing is found under the given condition
     * @see #findAllInIterable(Iterable, Predicate)
     */
    public static <A> A findInIterable(Iterable<A> iterable, Predicate<A> condition) throws SearchNotFoundException {
        return findAllInIterable(iterable, condition).iterator().next();
    }

    /**
     * @param map    the {@link ConfigurationSerializable#serialize()} map
     * @param values the {@link Enum} values ({@code Enum.values()})
     * @return a {@link Set} of {@link String}s
     */
    @NotNull
    public static Set<String> getMissingKeys(Map<String, Object> map, Enum[] values) {
        Set<String> list = new HashSet<>();

        for (Enum value : values) {
            final String key = value.toString();
            if (!map.containsKey(key)) {
                list.add(key);
            }
        }

        return list;
    }

    /**
     * Just an Exception for handling.
     *
     * @see #findAllInIterable(Iterable, Predicate)
     * @see #findInIterable(Iterable, Predicate)
     */
    public static class SearchNotFoundException extends Exception {

    }
}
