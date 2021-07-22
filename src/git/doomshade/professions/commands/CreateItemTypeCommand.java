package git.doomshade.professions.commands;

import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.io.ProfessionLogger;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Command for creating of item types
 *
 * @author Doomshade
 * @version NOT_YET_IMPLEMENTED
 */
@SuppressWarnings("all")
public class CreateItemTypeCommand extends AbstractCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        try {
            Class<? extends ItemTypeHolder> clazz = Class.forName(args[1]).asSubclass(ItemTypeHolder.class);
            //ItemTypeHolder<?> holder = Professions.getItemTypeHolder(clazz);
            // final ItemType<?> o = holder.getRegisteredItemTypes().get(0);
        } catch (Exception e) {
            ProfessionLogger.logError(e);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "createitemtype";
    }
}
