package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.List;

public class SaveCommand extends AbstractCommand {

    public SaveCommand() {
        // TODO Auto-generated constructor stub
        setCommand("save");
        setDescription("Saves player data");
        setRequiresOp(true);
        setRequiresPlayer(false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // TODO Auto-generated method stub
        try {
            sender.sendMessage("Saving files...");
            Professions.getInstance().saveFiles();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            sender.sendMessage("Error! Check console for error stack trace.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getID() {
        // TODO Auto-generated method stub
        return "save";
    }

}
