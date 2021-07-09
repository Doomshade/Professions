package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.task.BackupTask;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Backs up files to a zip in "backup" folder
 *
 * @author Doomshade
 * @version 1.0
 */
public class BackupCommand extends AbstractCommand {

    public BackupCommand() {
        setRequiresPlayer(false);
        setDescription("Backs up needed files");
        setCommand("backup");
        addPermission(Permissions.ADMIN);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        BackupTask.Result result = Professions.getInstance().backup();
        if (result == BackupTask.Result.SUCCESS) {
            sender.sendMessage("Successfully backed up files");
        } else {
            sender.sendMessage("Could not backup files. Check console for error output.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "backup";
    }


}
