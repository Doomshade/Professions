package git.doomshade.professions.utils;

/**
 * Formula for calculating exp based on level. This is copied from {@code Eniripsa96}'s code.
 *
 * @see com.sucy.skill.data.ExpFormula
 */
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
