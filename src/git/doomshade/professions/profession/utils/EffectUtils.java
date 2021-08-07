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

package git.doomshade.professions.profession.utils;

import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class EffectUtils {
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("([\\w]+):([0-9]+)");

    public static void addAttributes(Player player, boolean inverted, String attr) {
        Matcher m = ATTRIBUTE_PATTERN.matcher(attr);
        if (!m.find()) {
            return;
        }
        String attribute = m.group(1);
        int amount = Integer.parseInt(m.group(2));
        if (inverted) {
            amount = -amount;
        }

        git.doomshade.loreattributes.Attribute laAttribute = git.doomshade.loreattributes.Attribute.parse(attribute);

        if (laAttribute != null) {
            // TODO uncomment !!!!!!!!!!!!!!!!!!!!!
            git.doomshade.loreattributes.user.User.getUser(player).addCustomAttribute(laAttribute, amount);
        } else {
            com.sucy.skill.manager.AttributeManager.Attribute sapiAttribute =
                    com.sucy.skill.SkillAPI.getAttributeManager().getAttribute(attribute);
            if (sapiAttribute != null) {
                com.sucy.skill.SkillAPI.getPlayerData(player).addBonusAttributes(attribute, amount);
            }
        }
    }
}
