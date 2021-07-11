package git.doomshade.professions.profession.professions.jewelcrafting;

import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.utils.EffectUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class GemUtils {

    public static final Map<String, GemEffect> IDS;
    static final String ACTIVE_GEM_NBT_TAG = "activeGem";
    static final String GEM_NBT_TAG = "gemItemType";
    public static final GemEffect ADD_ATTRIBUTE_EFFECT;

    static {
        ADD_ATTRIBUTE_EFFECT = new GemEffect() {

            @Override
            public void apply(Gem gem, Player player, boolean inverted) {

                for (String gemEffect : gem.getContext()) {
                    EffectUtils.addAttributes(player, inverted, gemEffect);
                }

                final String s;
                if (!inverted) {
                    s = String.format("Applying %s to %s", gem, player.getName());
                } else {
                    s = String.format("Unapplying %s from %s", gem, player.getName());
                }
                ProfessionLogger.log(s, Level.CONFIG);
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
