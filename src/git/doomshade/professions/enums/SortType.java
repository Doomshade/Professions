package git.doomshade.professions.enums;

import git.doomshade.professions.api.item.ItemType;
import org.bukkit.ChatColor;

import java.util.Comparator;

/**
 * The sort type of item types in a profession GUI
 *
 * @author Doomshade
 * @version 1.0
 */
public enum SortType {
    NAME("name", Comparator.comparing(ItemType::getName)),
    EXPERIENCE("exp", Comparator.comparing(o -> -o.getExp())),
    LEVEL_REQ("level-req", Comparator.comparing(ItemType::getLevelReq));

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
     *
     * @return {@code null} if the input is not an id of sort type, otherwise the sort type class
     *
     * @throws IllegalArgumentException if the input is invalid
     */
    public static SortType getSortType(String input) throws IllegalArgumentException {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be empty!");
        }
        String copy = ChatColor.translateAlternateColorCodes('&', input);
        copy = ChatColor.stripColor(copy);

        for (SortType st : values()) {
            if (st.s.equalsIgnoreCase(copy)) {
                return st;
            }
        }
        throw new IllegalArgumentException("No sort type " + input + " found!");
    }

    @Override
    public String toString() {
        return s;
    }

    /**
     * @return the comparator for an item type
     */
    public Comparator<ItemType<?>> getComparator() {
        return comparator;
    }
}
