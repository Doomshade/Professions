package git.doomshade.professions.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandsCommand extends AbstractCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage("Available commands:");
        for (AbstractCommandHandler ach : AbstractCommandHandler.INSTANCES.values()) {
            sender.sendMessage(ChatColor.DARK_AQUA + "/" + ach.getCommandName());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "commands";
    }
}
