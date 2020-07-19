package git.doomshade.professions.profession.professions.alchemy;

import git.doomshade.professions.profession.utils.Effect;
import org.bukkit.entity.Player;

public interface CustomPotionEffect extends Effect<String> {

    void apply(String potionEffect, Player player, boolean negated);
}
