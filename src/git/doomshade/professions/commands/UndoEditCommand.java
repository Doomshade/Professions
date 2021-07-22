package git.doomshade.professions.commands;

import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Undoes latest edit to a file. Can be executed as long as there were edits
 *
 * @author Doomshade
 * @version 1.0
 * @see git.doomshade.professions.profession.professions.mining.commands.EditCommand
 */
@SuppressWarnings("ALL")
public class UndoEditCommand extends AbstractCommand {

    public UndoEditCommand() {
        setArg(true, EditItemTypeCommand.ARG_FILE);
        setCommand("undo");
        setDescription("Undoes previous actions made to an item type file");
        setRequiresPlayer(false);

        addPermission(Permissions.ADMIN);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        File file = EditItemTypeCommand.getFile(args);
        if (!file.exists()) {
            sender.sendMessage(file.getName() + " does not exist!");
            return;
        }
        FileConfiguration loader = EditItemTypeCommand.getAndRemoveLastUndo(file);
        if (loader != null) {
            try {
                loader.save(file);
                sender.sendMessage("Undid previous action made to " + file.getName());
            } catch (IOException e) {
                sender.sendMessage("Could not undo previous actions! Check console for more output.");
                ProfessionLogger.logError(e);
            }
        } else {
            sender.sendMessage("Nothing to undo");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        EditItemTypeCommand.getFile(args);
        List<String> list = EditItemTypeCommand.files.stream().filter(x -> x.startsWith(args[EditItemTypeCommand.i[0] - 1])).distinct().collect(Collectors.toList());
        return list.isEmpty() ? null : list;
    }

    @Override
    public String getID() {
        return "undoedit";
    }
}
