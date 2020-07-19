package git.doomshade.professions.profession.professions.jewelcrafting;

import com.sucy.skill.SkillAPI;
import git.doomshade.professions.Professions;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GemUtils {

    public static final Map<String, GemEffect> IDS;
    static final String ACTIVE_GEM_NBT_TAG = "activeGem";
    static final GemEffect ADD_ATTRIBUTE_EFFECT;

    static {
        ADD_ATTRIBUTE_EFFECT = new GemEffect() {

            final Pattern ATTRIBUTE_PATTERN = Pattern.compile("([\\w]+):([0-9]+)");


            @Override
            public void apply(Gem gem, Player player, boolean inverted) {

                for (String gemEffect : gem.getContext()) {
                    Matcher m = ATTRIBUTE_PATTERN.matcher(gemEffect);
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
                        // TODO uncomment !!!!!!!!!!!!!!!!!!!!
                        //git.doomshade.loreattributes.user.User.getUser(player).addCustomAttribute(laAttribute, amount);
                    } else {
                        com.sucy.skill.manager.AttributeManager.Attribute sapiAttribute = SkillAPI.getAttributeManager().getAttribute(attribute);
                        if (sapiAttribute != null) {
                            SkillAPI.getPlayerData(player).addBonusAttributes(attribute, amount);
                        }
                    }
                }

                if (inverted) {
                    final String s = "Unapplying " + gem.toString() + " from " + player.getName();
                    Professions.log(s, Level.CONFIG);
                } else {
                    final String s = "Applying " + gem.toString() + " onto " + player.getName();
                    Professions.log(s, Level.CONFIG);
                }
            }

            @Override
            public String toString() {
                return "ADD_ATTRIBUTE";
            }
        };


        IDS = new HashMap<String, GemEffect>() {
            {
                put(ADD_ATTRIBUTE_EFFECT.toString(), ADD_ATTRIBUTE_EFFECT);
            }
        };
    }


}
