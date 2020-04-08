package git.doomshade.professions.enums;

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

    public static SkillupColor getSkillupColor(int itemTypeLvl, int userProfessionLvl) {
        int difference = userProfessionLvl - itemTypeLvl;
        int curr = 0;
        SkillupColor color = SkillupColor.RED;
        int pos = 0;
        for (int i = difference; i >= 0 && pos != values().length; ) {
            color = SkillupColor.values()[pos++];
            curr += color.changeAfter;
            i -= curr;
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