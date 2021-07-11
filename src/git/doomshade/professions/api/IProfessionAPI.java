package git.doomshade.professions.api;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.user.IUser;
import org.bukkit.entity.Player;

/**
 * The API starting point
 *
 * @author Doomshade
 * @version 1.0
 */
public interface IProfessionAPI {

    /**
     * @return the profession manager
     */
    IProfessionManager getProfessionManager();

    /**
     * @param player the player
     * @return the user
     */
    IUser getUser(Player player);

    /**
     * @return an implementation of this API
     */
    static IProfessionAPI getApi() {
        return Professions.getInstance();
    }
}
