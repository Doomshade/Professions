package git.doomshade.professions.profession.types.crafting.alchemy;

import org.bukkit.entity.Player;

public interface CustomPotionEffect {

    void apply(String potionEffect, Player player, boolean negated);
}
