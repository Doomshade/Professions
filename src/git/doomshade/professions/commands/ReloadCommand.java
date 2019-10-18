package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.data.Settings;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.List;

public class ReloadCommand extends AbstractCommand {

    public ReloadCommand() {
        setCommand("reload");
        setDescription("Reloads plugin");
        setRequiresOp(true);
        setRequiresPlayer(false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // TODO Auto-generated method stub
        Settings.getInstance().reload();
        Professions plugin = Professions.getInstance();
        plugin.cleanup();
        plugin.setup();
        try {
            Professions.saveUsers();
            sender.sendMessage(ChatColor.GREEN + "Plugin reloaded.");
        } catch (IOException e) {
            e.printStackTrace();
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
        return "reload";
    }

}
