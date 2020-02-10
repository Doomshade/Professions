package git.doomshade.professions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class GiveRecipeCommand extends AbstractCommand {
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
        return null;
    }
}
