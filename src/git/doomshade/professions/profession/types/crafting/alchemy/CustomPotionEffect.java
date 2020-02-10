package git.doomshade.professions.profession.types.crafting.alchemy;

import git.doomshade.professions.profession.types.utils.Effect;
import org.bukkit.entity.Player;

public interface CustomPotionEffect extends Effect<String> {

    void apply(String potionEffect, Player player, boolean negated);
}
