/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package git.doomshade.professions.utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A set of method utilities
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class Utils {
    public static final String YML_EXTENSION = ".yml";
    public static final String EXAMPLE_ID = "example-id";
    public static final String EXAMPLE_NAME = "some-name";
    public static final long TICKS = 20L;
    private static final int PLAYER_TARGET_RANGE = 5;

    private Utils() {
    }

    /**
     * Sorts the map by value
     *
     * @param map the map to sort
     *
     * @return sorted map
     */
    public static <K, V> Map<K, V> sortMapByValue(Map<K, V> map, Comparator<V> comparator) {
        LinkedHashMap<K, V> sortedMap = new LinkedHashMap<>();
        if (map == null) {
            return sortedMap;
        }
        if (comparator == null) {
            sortedMap.putAll(map);
            return sortedMap;
        }
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());

        list.sort(Map.Entry.comparingByValue(comparator));
        for (Map.Entry<K, V> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static String getReceiveXp(int xp) {
        return String.format(" and received %d XP", xp);
    }

    /**
     * @param player the player
     *
     * @return the block the player is currently looking at
     */
    public static Block getLookingAt(Player player) {
        return player.getTargetBlock(null, PLAYER_TARGET_RANGE);
    }


    /**
     * @param iterable  the iterable (e.g. {@link Collection})
     * @param condition the condition based on which the method should query
     * @param <A>       the generic type
     *
     * @return a {@link Set} of {@code <A>} with given {@code condition}
     *
     * @throws SearchNotFoundException when nothing is found under the given condition
     */
    @NotNull
    public static <A> Collection<A> findAllInIterable(Iterable<A> iterable, Predicate<A> condition)
            throws SearchNotFoundException {
        Collection<A> arr = new ArrayList<>();
        for (A a : iterable) {
            if (condition.test(a)) {
                arr.add(a);
            }
        }

        if (!arr.isEmpty()) {
            return arr;
        }
        throw new SearchNotFoundException();
    }

    /**
     * @param iterable  the iterable (e.g. {@link Collection})
     * @param condition the condition based on which the method should query
     * @param <A>       the generic type
     *
     * @return an {@code <A>} with given {@code condition}
     *
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
     *
     * @param obj the object to class
     * @param <T> the class to cast to
     *
     * @return the casted class
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) throws ClassCastException {
        return (T) obj;
    }

    /**
     * Gets the missing keys of a map based on enum values. Useful for deserialization - checking if all keys are
     * present
     *
     * @param map     the {@link ConfigurationSerializable#serialize()} map
     * @param values  the {@link Enum} values ({@code Enum.values()})
     * @param ignored the ignored enum values
     *
     * @return a {@link Set} of {@link String}s
     */
    @NotNull
    public static Set<String> getMissingKeys(Map<String, Object> map, FileEnum[] values, FileEnum... ignored) {
        return getMissingKeysEnum(map, values, ignored).stream().map(Object::toString).collect(Collectors.toSet());
    }

    /**
     * Gets the missing keys of a map based on enum values. Useful for deserialization - checking if all keys are
     * present
     *
     * @param map     the {@link ConfigurationSerializable#serialize()} map
     * @param values  the {@link Enum} values ({@code Enum.values()})
     * @param ignored the ignored enum values
     *
     * @return a {@link Set} of {@link FileEnum}s
     */
    public static Set<FileEnum> getMissingKeysEnum(Map<String, Object> map, FileEnum[] values, FileEnum... ignored) {
        Set<FileEnum> list = new HashSet<>();

        _loop:
        for (FileEnum value : values) {
            final String key = value.toString();
            if (!map.containsKey(key)) {
                for (FileEnum ignore : ignored) {
                    if (value.equals(ignore)) {
                        continue _loop;
                    }
                }
                list.add(value);
            }
        }

        return list;
    }

    /**
     * Translates the colour in lore
     *
     * @param lore the lore
     *
     * @return translated lore
     */
    public static List<String> translateLore(List<String> lore) {
        if (lore == null) {
            return new ArrayList<>();
        }
        List<String> newLore = new ArrayList<>(lore);
        for (int i = 0; i < newLore.size(); i++) {
            final String s = newLore.get(i);
            newLore.set(i, translateColour(s));
        }
        return newLore;
    }

    /**
     * Replaces '{@literal &}' with in-game colour codes
     *
     * @param s the string
     *
     * @return translated string
     */
    public static String translateColour(String s) {
        return s.isEmpty() ? s : ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String locationToString(Location loc) {
        return loc.getBlock().getLocation().toVector().toString();
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
