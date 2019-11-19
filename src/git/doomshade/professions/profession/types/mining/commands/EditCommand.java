package git.doomshade.professions.profession.types.mining.commands;

import git.doomshade.professions.commands.AbstractCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Command for editing mining areas
 *
 * @author Doomshade
 */
public class EditCommand extends AbstractCommand {

    /**
     * Setup defaults for command
     */
    public EditCommand() {
        setCommand("edit");
        setRequiresPlayer(true);
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
        return "edit";
    }
}
