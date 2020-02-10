package git.doomshade.professions.profession.professions.alchemy;

import org.bukkit.entity.Player;

public class PotionPlayerOptions {
    public final Potion potion;
    public final Player player;

    public PotionPlayerOptions(Potion potion, Player player) {
        this.potion = potion;
        this.player = player;
    }
}
