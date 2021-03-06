package git.doomshade.professions.utils;

import org.bukkit.ChatColor;

import java.util.regex.Pattern;

/**
 * Class representing an item attribute (used on my server, not part of the api!)
 *
 * @author Doomshade
 */
public class ItemAttribute {
    private final String attribute;
    private final String toString;
    private final Pattern pattern;
    private int value;

    /**
     * Calls {@link #ItemAttribute(String, int, AttributeType, Pattern)} with a {@link Pattern} based on {@link AttributeType} argument.
     *
     * @param attribute the name of attribute
     * @param value     the value of attribute
     * @param type      the type of attribute
     * @see AttributeType
     */
    public ItemAttribute(String attribute, int value, AttributeType type) {
        this(attribute, value, type, type == AttributeType.SKILLAPI ? Pattern.compile("[\\D]+: [0-9]+") : Pattern.compile("[+][0-9]+ [\\D]+"));
    }


    /**
     * If you do not know how {@link Pattern} works, call {@link #ItemAttribute(String, int, AttributeType)} instead.
     *
     * @param attribute the name of attribute
     * @param value     the value of attribute
     * @param type      the type of attribute
     * @param pattern   the pattern to look for
     * @see AttributeType
     */
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

    /**
     * @return the attribute value
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets the attribute value
     *
     * @param value the value to set
     */
    public void setValue(int value) {
        this.value = Math.max(value, 0);
    }

    /**
     * @return the attribute pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * @return the attribute name
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * @return the {@link String} representation of the attribute
     */
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

    /**
     * The attribute types of plugins (used on my server only)
     */
    public enum AttributeType {
        SKILLAPI, LOREATTRIBUTES
    }

}
