package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.List;

public class ReloadCommand extends AbstractCommand {

    public ReloadCommand() {
        setCommand("reload");
        setDescription("Reloads plugin");
        setRequiresPlayer(false);

        // TODO take into consideration
        addPermission(Permissions.HELPER);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            Professions plugin = Professions.getInstance();
            plugin.reload();
            sender.sendMessage(ChatColor.GREEN + "Plugin reloaded.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "reload";
    }

}
