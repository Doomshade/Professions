package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.List;

public class SaveCommand extends AbstractCommand {

    public SaveCommand() {
        setCommand("save");
        setDescription("Saves player data");
        setRequiresOp(true);
        setRequiresPlayer(false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            sender.sendMessage("Saving files...");
            Professions.getInstance().saveFiles();
            sender.sendMessage(ChatColor.GREEN + "Files saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage("Error! Check console for error stack trace.");
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
