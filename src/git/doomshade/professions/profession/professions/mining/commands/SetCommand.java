package git.doomshade.professions.profession.professions.mining.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

@SuppressWarnings("ALL")
public class SetCommand extends AbstractEditCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "set";
    }
}
