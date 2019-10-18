package git.doomshade.professions.commands;

import git.doomshade.professions.profession.types.ItemTypeHolder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CreateItemTypeCommand extends AbstractCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            Class<? extends ItemTypeHolder> clazz = Class.forName(args[1]).asSubclass(ItemTypeHolder.class);
            //ItemTypeHolder<?> holder = Professions.getItemTypeHolder(clazz);
            // final ItemType<?> o = holder.getRegisteredItemTypes().get(0);
        } catch (Exception e) {
            e.printStackTrace();
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
