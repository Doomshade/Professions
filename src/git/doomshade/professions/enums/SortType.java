package git.doomshade.professions.enums;

import git.doomshade.professions.profession.types.ItemType;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * The sort type of item types in a profession GUI
 *
 * @author Doomshade
 * @version 1.0
 */
public enum SortType {
    NAME("name", Comparator.comparing(ItemType::getName)), EXPERIENCE("exp", Comparator.comparing(o -> -o.getExp())), LEVEL_REQ("level-req", Comparator.comparing(ItemType::getLevelReq));

    /**
     * the sort type ID in text
     */
    private final String s;

    /**
     * the comparator of this sort
     */
    private final Comparator<ItemType<?>> comparator;

    SortType(String s, Comparator<ItemType<?>> comparator) {
        this.comparator = comparator;
        this.s = s;
    }

    /**
     * @param input the string input that should be a sort type
     * @return {@code null} if the input is not an id of sort type, otherwise the sort type class
     */
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

    public void sort(List<ItemType<?>> list) {
        list.sort(comparator);
    }

    public Comparator<ItemType<?>> getComparator() {
        return comparator;
    }
}
