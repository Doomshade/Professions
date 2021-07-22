package git.doomshade.professions.profession.professions.jewelcrafting.commands;

import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.profession.professions.jewelcrafting.Gem;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("ALL")
public class InsertCommand extends AbstractCommand {

    public InsertCommand() {
        setCommand("insert");
        setDescription("Applies a gem's effect onto the item you are currently holding");
        setRequiresPlayer(true);
        setArg(true, "gem id");
        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        Optional<Gem> opt = Gem.getGem(args[1]);

        if (opt.isEmpty()) {
            sender.sendMessage("Invalid gem id");
            return;
        }

        Gem gem = opt.get();
        final PlayerInventory inventory = ((Player) sender).getInventory();
        //GetSet<ItemStack> item = new GetSet<>(inventory.getItemInMainHand());
        gem.insert(inventory.getItemInMainHand(), true);
        //inventory.setItemInMainHand(item.get());

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>(Gem.GEMS.keySet());
    }

    @Override
    public String getID() {
        return "insert";
    }
}
