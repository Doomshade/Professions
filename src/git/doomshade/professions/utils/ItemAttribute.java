package git.doomshade.professions.utils;

import org.bukkit.ChatColor;

import java.util.regex.Pattern;

public class ItemAttribute {
    private final String attribute;
    private final String toString;
    private final Pattern pattern;
    private int value;

    public ItemAttribute(String attribute, int value, AttributeType type, Pattern pattern) {
        this.attribute = attribute;
        this.value = value;
        this.pattern = pattern;
        switch (type) {
            case LOREATTRIBUTES:
                this.toString = ChatColor.getLastColors(attribute) + "+" + value + " " + attribute;
                break;
            case SKILLAPI:
                this.toString = attribute + ": " + value;
                break;
            default:
                this.toString = "";
                break;
        }

        //
        // DEBUG
        System.out.println("(ItemAttribute) Attribute: " + attribute + "\nValue: " + value + "\nType: " + type.toString());
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        if (value < 0) {
            this.value = 0;
        } else {
            this.value = value;
        }
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getAttribute() {
        return attribute;
    }

    @Override
    public final String toString() {
        //
        // DEBUG
        System.out.println("(ItemAttribute) " + toString);
        if (value == 0) {
            //
            // DEBUG
            System.out.println("(ItemAttribute) Value == 0 -> return empty");
            return "";
        }
        return toString;
    }

    public enum AttributeType {
        SKILLAPI, LOREATTRIBUTES
    }

}
