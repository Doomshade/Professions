package git.doomshade.professions.utils;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class containing of two {@link Integer}s.
 *
 * @author Doomshade
 */
public class Range {
    public static final Pattern RANGE_PATTERN = Pattern.compile("([\\d]+)(-([\\d]+))?");
    private final int min, max;
    private final Random random = new Random();

    /**
     * Calls {@link #Range(int, int)} with the same argument.
     *
     * @param a the range
     */
    public Range(int a) {
        this(a, a);
    }

    /**
     * Note that it doesn't need to be in order of {@code min, max}, the constructor calls {@link Math#min(int, int)} and {@link Math#max(int, int)}
     *
     * @param a the first {@link Integer} of range
     * @param b the second {@link Integer} of range
     */
    public Range(int a, int b) {
        this.min = Math.min(a, b);
        this.max = Math.max(a, b);
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

    public static Range fromString(String s) throws Exception {
        Matcher m = RANGE_PATTERN.matcher(s);
        if (m.find()) {
            final String group = m.group(3);
            final int min = Integer.parseInt(m.group(1));
            if (group != null)
                try {
                    return new Range(min, Integer.parseInt(group));
                } catch (NumberFormatException e) {
                    throw new Exception("Could not get range from \"" + s + "\"");
                }
            else
                return new Range(min);
        } else {
            try {
                return new Range(Integer.parseInt(s.trim()));
            } catch (NumberFormatException e) {
                throw new Exception("Could not get range from \"" + s + "\"");
            }
        }
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
        return min == max ? String.valueOf(min) : String.format("%d-%d", min, max);
    }
}
