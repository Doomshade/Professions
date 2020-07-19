package git.doomshade.professions.profession.utils;

import org.bukkit.entity.Player;

/**
 * An effect that is applicable to player.
 *
 * @param <T>
 * @author Doomshade
 * @version 1.0
 */
public interface Effect<T> {

    /**
     * An effect to apply to a player
     *
     * @param effect   the effect to apply
     * @param player   the player to apply the effect to
     * @param inverted whether or not the effect should be inverted ({@code true} means the effect should be unapplied)
     */
    void apply(T effect, Player player, boolean inverted);
}
