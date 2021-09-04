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

package git.doomshade.professions.enums;

import git.doomshade.professions.api.item.ItemType;
import org.bukkit.ChatColor;

import java.util.Comparator;

/**
 * The sort type of item types in a profession GUI
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
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
