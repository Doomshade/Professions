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

package git.doomshade.professions.utils;

import org.bukkit.ChatColor;

import java.util.regex.Pattern;

/**
 * Class representing an item attribute (used on my server, not part of the api!)
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class ItemAttribute {
    private final String attribute;
    private final String toString;
    private final Pattern pattern;
    private int value;

    /**
     * Calls {@link #ItemAttribute(String, int, AttributeType, Pattern)} with a {@link Pattern} based on {@link
     * AttributeType} argument.
     *
     * @param attribute the name of attribute
     * @param value     the value of attribute
     * @param type      the type of attribute
     *
     * @see AttributeType
     */
    public ItemAttribute(String attribute, int value, AttributeType type) {
        this(attribute, value, type, type == AttributeType.SKILLAPI ?
                Pattern.compile("[\\D]+: [0-9]+") :
                Pattern.compile("[+][0-9]+ [\\D]+"));
    }


    /**
     * If you do not know how {@link Pattern} works, call {@link #ItemAttribute(String, int, AttributeType)} instead.
     *
     * @param attribute the name of attribute
     * @param value     the value of attribute
     * @param type      the type of attribute
     * @param pattern   the pattern to look for
     *
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
                throw new IllegalArgumentException(type + " is not a valid attribute type!");
        }

        //
        // DEBUG
        System.out.println(
                "(ItemAttribute) Attribute: " + attribute + "\nValue: " + value + "\nType: " + type);
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
        SKILLAPI,
        LOREATTRIBUTES
    }

}
