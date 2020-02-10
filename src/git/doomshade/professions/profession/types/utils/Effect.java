package git.doomshade.professions.profession.types.utils;

import org.bukkit.entity.Player;

public interface Effect<T> {
    void apply(T effect, Player player, boolean inverted);
}
