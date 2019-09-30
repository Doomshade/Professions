package git.doomshade.professions.utils;

public class ExpFormula {
    private final int x, y, z;

    public ExpFormula(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int calculate(int level) {
        return x * level * level + y * level + z;
    }
}
