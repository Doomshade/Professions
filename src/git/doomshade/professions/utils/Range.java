package git.doomshade.professions.utils;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class containing of two {@link Integer}s.
 *
 * @author Doomshade
 */
public class Range {
    private static final Pattern RANGE_PATTERN = Pattern.compile("([0-9]+)-([0-9]+)");
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

    @Nullable
    public static Range fromString(String s) {
        Matcher m = RANGE_PATTERN.matcher(s);
        if (m.find()) {
            return new Range(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
        } else {
            try {
                return new Range(Integer.parseInt(s.trim()));
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    @Override
    public String toString() {
        return min == max ? String.valueOf(min) : String.format("%d-%d", min, max);
    }
}
