package git.doomshade.professions.profession.professions.mining.commands;

import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Simple reload command
 *
 * @author Doomshade
 */
@SuppressWarnings("ALL")
public class ReloadCommand extends AbstractCommand {

    public ReloadCommand() {
        setCommand("reload");
        addPermission(Permissions.BUILDER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "reload";
    }
}
