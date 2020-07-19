package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            sender.sendMessage("[Professions] Saving files...");
            Professions.getInstance().saveFiles();
            sender.sendMessage(ChatColor.GREEN + "[Professions] Files saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "[Professions] Error! Check console for error stack trace.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "save";
    }

}
