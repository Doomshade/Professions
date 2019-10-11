package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class BackupCommand extends AbstractCommand {

    public BackupCommand() {
        // TODO Auto-generated constructor stub
        setRequiresPlayer(false);
        setRequiresOp(true);
        setDescription("Backs up needed files");
        setCommand("backup");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // TODO Auto-generated method stub
        Professions.getInstance().backup();
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
        return "backup";
    }


}
