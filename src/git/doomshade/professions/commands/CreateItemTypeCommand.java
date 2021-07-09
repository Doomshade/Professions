package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ItemTypeHolder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Command for creating of item types
 *
 * @author Doomshade
 * @version NOT_YET_IMPLEMENTED
 */
public class CreateItemTypeCommand extends AbstractCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            Class<? extends ItemTypeHolder> clazz = Class.forName(args[1]).asSubclass(ItemTypeHolder.class);
            //ItemTypeHolder<?> holder = Professions.getItemTypeHolder(clazz);
            // final ItemType<?> o = holder.getRegisteredItemTypes().get(0);
        } catch (Exception e) {
            Professions.logError(e);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "createitemtype";
    }
}
