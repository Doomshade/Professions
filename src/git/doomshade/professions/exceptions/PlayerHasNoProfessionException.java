package git.doomshade.professions.exceptions;

import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.user.User;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlayerHasNoProfessionException extends RuntimeException {

    public PlayerHasNoProfessionException(User user, Profession profession) {
        this(user, profession.getColoredName());
    }

    public PlayerHasNoProfessionException(User user, String profession) {
        this(user.getPlayer(), profession);
    }

    public PlayerHasNoProfessionException(Player player, Profession profession) {
        this(player, profession.getColoredName());
    }

    public PlayerHasNoProfessionException(Player player, String profession) {
        super(player.getName() + ChatColor.RESET + " (" + player.getUniqueId() + ") does not have " + profession + ChatColor.RESET + " profession!");
    }
}
