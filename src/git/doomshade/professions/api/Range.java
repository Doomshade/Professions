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

package git.doomshade.professions.api;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class containing of two {@link Integer}s.
 *
 * @author Doomshade
 */
public final class Range implements ConfigurationSerializable {
    public static final Pattern RANGE_PATTERN = Pattern.compile("(\\d+)(-(\\d+))?");
    public static final String KEY_MIN = "from";
    public static final String KEY_MAX = "to";
    private final int min, max;
    private final Random random = new Random();
    private final String s;

    /**
     * Calls {@link #Range(int, int)} with the same argument.
     *
     * @param a the range
     */
    public Range(int a) {
        this(a, a);
    }

    /**
     * Note that it doesn't need to be in order of {@code min, max}, the constructor calls {@link Math#min(int, int)}
     * and {@link Math#max(int, int)}
     *
     * @param a the first {@link Integer} of range
     * @param b the second {@link Integer} of range
     */
    public Range(int a, int b) {
        this.min = Math.min(a, b);
        this.max = Math.max(a, b);
        this.s = min == max ? String.valueOf(min) : String.format("%d-%d", min, max);
    }

    /**
     * @param s the string
     *
     * @return optional of range if the range could be parsed from the string
     */
    public static Optional<Range> fromString(String s) {
        Matcher m = RANGE_PATTERN.matcher(s);
        if (m.find()) {
            final String group = m.group(3);
            final int min = Integer.parseInt(m.group(1));
            if (group != null) {
                try {
                    return Optional.of(new Range(min, Integer.parseInt(group)));
                } catch (NumberFormatException e) {
                    return Optional.empty();
                }
            } else {
                return Optional.of(new Range(min));
            }
        }
        try {
            return Optional.of(new Range(Integer.parseInt(s.trim())));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

    }

    /**
     * Attempts to deserialize the range
     *
     * @param map the serialization
     *
     * @return the deserialized range
     *
     * @throws IllegalArgumentException if the map is invalid
     */
    public static Range deserialize(Map<String, Object> map) throws IllegalArgumentException {
        try {
            int min = (int) map.get(KEY_MIN);
            int max = (int) map.get(KEY_MAX);
            return new Range(min, max);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * @return a random number between {@link #getMin()} and {@link #getMax()} inclusive.
     */
    public int getRandom() {
        return random.nextInt(1 + Math.abs(max - min)) + min - 1;
    }

    /**
     * @return the smaller number of the two
     */
    public int getMin() {
        return min;
    }

    /**
     * @return the bigger number of the two
     */
    public int getMax() {
        return max;
    }

    public boolean isInRange(int num, boolean includeRange) {
        if (includeRange) {
            return num >= min && num <= max;
        } else {
            return num > min && num < max;
        }
    }

    @Override
    public String toString() {
        return s;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return new HashMap<>() {
            {
                put(KEY_MIN, min);
                put(KEY_MAX, max);

            }
        };
    }
}
