package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.List;

/**
 * Saves current player data from memory to file
 *
 * @author Doomshade
 * @version 1.0
 */
public class SaveCommand extends AbstractCommand {

    public SaveCommand() {
        setCommand("save");
        setDescription("Saves player data");
        setRequiresPlayer(false);

        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        try {
            sender.sendMessage("Saving files...");
            Professions.saveFiles();
            sender.sendMessage(ChatColor.GREEN + "Files saved successfully");
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Error! Check console for error stack trace.");
            Professions.logError(e);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "save";
    }

}
