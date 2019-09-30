package git.doomshade.professions.enums;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;

public enum SortType {
    NAME("name"), EXPERIENCE("exp"), LEVEL_REQ("level-req");

    private final String s;

    SortType(String s) {
        this.s = s;
    }

    @Nullable
    public static SortType getSortType(String input) {
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be empty!");
        }
        String copy = ChatColor.translateAlternateColorCodes('&', input);
        copy = ChatColor.stripColor(copy);

        for (SortType st : values()) {
            if (st.s.equalsIgnoreCase(copy)) {
                return st;
            }
        }
        return null;
    }
}
