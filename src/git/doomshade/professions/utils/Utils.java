package git.doomshade.professions.utils;

import com.avaje.ebean.validation.NotNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A set of method utilities
 *
 * @author Doomshade
 */
public final class Utils {

    public static final String YML_EXTENSION = ".yml";

    public static String getReceiveXp(int xp) {
        return String.format(" and received %d XP", xp);
    }

    public static Location getLookingAt(Player player) {
        return player.getTargetBlock((Set<Material>) null, 5).getLocation();
    }


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
     */
    public static <A> A findInIterable(Iterable<A> iterable, Predicate<A> condition) throws SearchNotFoundException {
        for (A a : iterable) {
            if (condition.test(a)) {
                return a;
            }
        }
        throw new SearchNotFoundException();
    }

    /**
     * Casts an object to desired class.
     * Note that this method has no way of telling whether the object is instanceof the class it is being cast to,
     * thus a {@link ClassCastException} will be thrown if object cannot be casted to the class
     *
     * @param obj the object to class
     * @param <T> the class to cast to
     * @return the casted class
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) throws ClassCastException {
        return (T) obj;
    }

    /**
     * @param map    the {@link ConfigurationSerializable#serialize()} map
     * @param values the {@link Enum} values ({@code Enum.values()})
     * @return a {@link Set} of {@link String}s
     */
    @NotNull
    public static Set<String> getMissingKeys(Map<String, Object> map, FileEnum[] values, FileEnum... filtered) {
        return getMissingKeysEnum(map, values, filtered).stream().map(Object::toString).collect(Collectors.toSet());
    }

    public static Set<FileEnum> getMissingKeysEnum(Map<String, Object> map, FileEnum[] values, FileEnum... filtered) {
        Set<FileEnum> list = new HashSet<>();

        _loop:
        for (FileEnum value : values) {
            final String key = value.toString();
            if (!map.containsKey(key)) {
                for (FileEnum filter : filtered) {
                    if (value == filter) {
                        continue _loop;
                    }
                }
                list.add(value);
            }
        }

        return list;
    }

    public static List<String> translateLore(List<String> lore) {
        if (lore == null) return new ArrayList<>();
        List<String> newLore = new ArrayList<>(lore);
        for (int i = 0; i < newLore.size(); i++) {
            final String s = newLore.get(i);
            newLore.set(i, translateName(s));
        }
        return newLore;
    }

    public static String translateName(String name) {
        return name.isEmpty() ? name : ChatColor.translateAlternateColorCodes('&', name);
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
