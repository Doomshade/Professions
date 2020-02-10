package git.doomshade.professions.utils;

import git.doomshade.professions.Professions;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionManager;

public final class Permissions {

    private static final PermissionManager manager = Professions.getPermissionManager();
    public static String
            ADMIN = "prof.*",
            BUILDER = "prof.builder",
            HELPER = "prof.helper",
            DEFAULT_COMMAND_USAGE = "prof.commands";

    private Permissions() {
    }

    public static boolean has(Player player, String permission) {
        if (manager != null) {
            return manager.has(player, permission);
        }
        return player.hasPermission(permission);
    }

    public static boolean has(Player player, String permission, World world) {
        return manager != null && manager.has(player, permission, world.getName());
    }
}
