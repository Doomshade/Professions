package git.doomshade.professions.profession.types.crafting.jewelcrafting.commands;

import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.profession.types.crafting.jewelcrafting.Gem;
import git.doomshade.professions.utils.GetSet;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class InsertCommand extends AbstractCommand {

    public InsertCommand() {
        setCommand("insert");
        setDescription("Applies a gem's effect onto the item you are currently holding");
        setRequiresPlayer(true);
        setArg(true, Collections.singletonList("gem id"));
        addPermission(Permissions.HELPER);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        Optional<Gem> opt = Gem.getGem(args[1]);

        if (!opt.isPresent()) {
            sender.sendMessage("Invalid gem id");
            return true;
        }

        Gem gem = opt.get();
        final PlayerInventory inventory = ((Player) sender).getInventory();
        GetSet<ItemStack> item = new GetSet<>(inventory.getItemInMainHand());
        gem.insert(item, true);
        inventory.setItemInMainHand(item.t);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return new ArrayList<>(Gem.GEMS.keySet());
    }

    @Override
    public String getID() {
        return "insert";
    }
}
