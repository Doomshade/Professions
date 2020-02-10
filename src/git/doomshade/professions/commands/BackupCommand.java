package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.task.BackupTask;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class BackupCommand extends AbstractCommand {

    public BackupCommand() {
        setRequiresPlayer(false);
        setDescription("Backs up needed files");
        setCommand("backup");
        addPermission(Permissions.ADMIN);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        BackupTask.Result result = Professions.getInstance().backup();
        if (result == BackupTask.Result.SUCCESS) {
            sender.sendMessage("Successfully backed up files");
        } else {
            sender.sendMessage("Could not backup files. Check console for error output.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "backup";
    }


}
