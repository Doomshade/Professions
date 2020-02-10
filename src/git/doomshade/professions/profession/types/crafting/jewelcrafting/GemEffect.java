package git.doomshade.professions.profession.types.crafting.jewelcrafting;

import git.doomshade.professions.profession.types.utils.Effect;
import org.bukkit.entity.Player;

public interface GemEffect extends Effect<Gem> {

    void apply(Gem gem, Player player, boolean inverted);
}
