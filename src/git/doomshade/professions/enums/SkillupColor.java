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
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.ChatColor;

/**
 * Enum for item type level restrictions, the color and chance to gain XP
 *
 * @author Doomshade
 * @version 1.0
 */
public enum SkillupColor {
    RED(ChatColor.RED),
    YELLOW(ChatColor.YELLOW),
    GREEN(ChatColor.GREEN),
    GRAY(ChatColor.GRAY);

    private ChatColor chatColor;
    private int changeAfter;
    private double chance;

    SkillupColor(ChatColor chatColor) {
        this(chatColor, 0, 0);
    }

    SkillupColor(ChatColor chatColor, int changeAfter, double chance) {
        this.chatColor = chatColor;
        this.changeAfter = changeAfter;
        this.chance = chance;
    }

    /**
     * @param itemType the item type lvl
     * @param upd      the user's profession lvl
     *
     * @return the skillup color based on item type lvl and user's current profession lvl
     */
    public static SkillupColor getSkillupColor(ItemType<?> itemType, UserProfessionData upd) {
        SkillupColor color = SkillupColor.RED;

        // calc the difference between the profession lvl and item type lvl
        // subtract the change after attribute from the change after attribute
        // if the lvl is still >=0, change the skillup color and repeat until
        // we hit the lowest skillup color
        for (int lvl = upd.getLevel() - itemType.getLevelReq(), curr, pos = 0;
             lvl >= 0 && pos != values().length;
             curr = color.changeAfter, lvl -= curr, color = SkillupColor.values()[pos++]) {
        }

        return color;
    }

    public void setSkillupColor(ChatColor chatColor, int changeAfter, double chance) {
        this.chatColor = chatColor;
        this.changeAfter = changeAfter;
        this.chance = chance;
    }

    public double getChance() {
        return chance;
    }

    public ChatColor getColor() {
        return chatColor;
    }
}