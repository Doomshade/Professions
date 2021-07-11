package git.doomshade.professions.profession.utils;

import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            com.sucy.skill.manager.AttributeManager.Attribute sapiAttribute = com.sucy.skill.SkillAPI.getAttributeManager().getAttribute(attribute);
            if (sapiAttribute != null) {
                com.sucy.skill.SkillAPI.getPlayerData(player).addBonusAttributes(attribute, amount);
            }
        }
    }
}
