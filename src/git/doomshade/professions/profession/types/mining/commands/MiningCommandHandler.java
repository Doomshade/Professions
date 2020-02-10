package git.doomshade.professions.profession.types.mining.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.commands.AbstractCommandHandler;
import git.doomshade.professions.commands.CommandHandler;
import git.doomshade.professions.data.Settings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Custom command handler for mining
 *
 * @author Doomshade
 */
public class MiningCommandHandler extends AbstractCommandHandler {

    private static boolean isBackedUp = !Settings.isAutoSave();

    @Override
    protected String getCommandName() {
        return CommandHandler.EXTENDED_COMMAND.concat("mining");
    }

    @Override
    public void registerCommands() {
        registerCommand(new AddCommand());
        registerCommand(new RemoveCommand());
        registerCommand(new ReloadCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            return super.onCommand(sender, cmd, label, args);
        }

        for (AbstractCommand acmd : INSTANCE_COMMANDS) {
            if (acmd instanceof AbstractEditCommand && acmd.getCommand().equalsIgnoreCase(args[0]) && !isBackedUp) {
                Professions.getInstance().backup();
                isBackedUp = true;
                break;
            }
        }

        return super.onCommand(sender, cmd, label, args);
    }
}
