package git.doomshade.professions.utils;

import git.doomshade.professions.Professions;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

public final class Permissions {

    private static final LuckPerms manager = Professions.getPermissionManager();
    public static String
            ADMIN = "prof.*",
            BUILDER = "prof.builder",
            HELPER = "prof.helper",
            DEFAULT_COMMAND_USAGE = "prof.commands";

    private Permissions() {
    }

    public static boolean has(Player player, String permission) {
        User user = manager.getPlayerAdapter(Player.class).getUser(player);
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

}
