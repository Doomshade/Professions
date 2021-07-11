package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Command for "defaultconfig.yml" file generation (creates a copy of plugin's config.yml)
 *
 * @author Doomshade
 * @version 1.0
 */
public class GenerateDefaultConfigCommand extends AbstractCommand {

    public GenerateDefaultConfigCommand() {
        setCommand("generate-default-cfg");
        setDescription("Generates a 'defaultconfig.yml' file");
        addPermission(Permissions.ADMIN);
        setRequiresPlayer(false);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        try {
            Professions.getInstance().saveResource("config.yml", "defaultconfig.yml", true);
        } catch (Exception e) {
            sender.sendMessage("Error creating default config file");
            ProfessionLogger.logError(e);
            return;
        }
        sender.sendMessage("Successfully created default config file");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "generate-default-cfg";
    }
}
