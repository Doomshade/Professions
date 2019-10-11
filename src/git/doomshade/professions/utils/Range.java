package git.doomshade.professions.utils;

import java.util.Random;

public class Range {
    private final int min, max;
    private final Random random = new Random();

    public Range(int a){
        this(a, a);
    }

    public Range(int a, int b) {
        this.min = Math.min(a, b);
        this.max = Math.max(a, b);
    }

    public int getRandom() {
        return random.nextInt(1 + Math.abs(max - min)) + min - 1;
    }

    public int getMin(){
        return min;
    }

    public int getMax() {
        return max;
    }
}
