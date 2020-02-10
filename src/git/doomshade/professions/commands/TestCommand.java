package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class TestCommand extends AbstractCommand {

    public TestCommand() {
        setCommand("test");
        setRequiresPlayer(false);
        addPermission(Permissions.ADMIN);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        ItemStack item;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            item = player.getInventory().getItemInMainHand();
            if (item == null) {
                return true;
            }
        } else {

            item = new ItemStack(Material.POTION);

        }

        final Map<String, Object> serialize = ItemUtils.serialize(item);
        if (serialize != null) {
            Professions.log(serialize.toString());
            final ItemStack deserialize = ItemUtils.deserialize(serialize);
            if (deserialize != null) {
                Professions.log(deserialize);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "test";
    }
}
