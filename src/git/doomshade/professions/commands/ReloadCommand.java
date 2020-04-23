package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Reloads the plugin
 *
 * @author Doomshade
 * @version 1.0
 */
public class ReloadCommand extends AbstractCommand {

    public ReloadCommand() {
        setCommand("reload");
        setDescription("Reloads plugin");
        setRequiresPlayer(false);

        // TODO take into consideration
        addPermission(Permissions.HELPER);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Professions plugin = Professions.getInstance();
        if (plugin.reload()) {
            sender.sendMessage(ChatColor.GREEN + "Plugin reloaded.");
        } else {
            sender.sendMessage(ChatColor.RED + "Plugin reloaded with errors. Check console for further information.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "reload";
    }

}
