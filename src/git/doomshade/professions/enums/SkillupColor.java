package git.doomshade.professions.enums;

import org.bukkit.ChatColor;

public enum SkillupColor {
    RED(ChatColor.RED), YELLOW(ChatColor.YELLOW), GREEN(ChatColor.GREEN), GRAY(ChatColor.GRAY);

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

        //
        // DEBUG
        System.out.println("Difference: " + difference);
        int curr = 0;
        SkillupColor color = SkillupColor.RED;
        int pos = 0;
        for (int i = difference; i >= 0 && pos != values().length; ) {
            color = SkillupColor.values()[pos++];
            curr += color.changeAfter;
            //
            // DEBUG
            System.out.println("color change after: " + curr);
            i -= curr;
            //
            // DEBUG
            System.out.println("Current difference: " + i);
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
