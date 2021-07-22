package git.doomshade.professions.commands;

import git.doomshade.professions.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Prints the player available commands (not the subcommands)
 *
 * @author Doomshade
 * @version 1.0
 */
@SuppressWarnings("ALL")
public class CommandsCommand extends AbstractCommand {

    public CommandsCommand() {
        setCommand("cmds");
        setDescription("Shows all available commands");
        setRequiresPlayer(false);
        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sender.sendMessage("Available commands:");
        for (AbstractCommandHandler ach : AbstractCommandHandler.getInstances()) {
            sender.sendMessage(ChatColor.DARK_AQUA + "/" + ach.getCommandExecutorName());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "commands";
    }
}
