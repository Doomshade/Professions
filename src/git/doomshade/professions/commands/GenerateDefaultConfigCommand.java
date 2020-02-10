package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class GenerateDefaultConfigCommand extends AbstractCommand {

    public GenerateDefaultConfigCommand() {
        setCommand("generate-default-cfg");
        setDescription("Generates a 'defaultconfig.yml' file");
        addPermission(Permissions.ADMIN);
        setRequiresPlayer(false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        try {
            Professions.getInstance().saveResource("config.yml", "defaultconfig.yml", true);
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage("Error creating default config file");
            return false;
        }
        sender.sendMessage("Successfully created default config file");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "generate-default-cfg";
    }
}
