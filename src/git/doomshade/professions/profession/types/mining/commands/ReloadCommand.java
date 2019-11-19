package git.doomshade.professions.profession.types.mining.commands;

import git.doomshade.professions.commands.AbstractCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Simple reload command
 *
 * @author Doomshade
 */
public class ReloadCommand extends AbstractCommand {

    public ReloadCommand() {
        setRequiresOp(true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return false;
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
