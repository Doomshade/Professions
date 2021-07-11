package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * @author Doomshade
 * @version TEST_VERSION
 */
public class TestCommand extends AbstractCommand {

    public TestCommand() {
        setCommand("test");
        setRequiresPlayer(false);
        addPermission(Permissions.ADMIN);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        ItemStack item;
        if (sender instanceof Player) {
            Player player = (Player) sender;
            item = player.getInventory().getItemInMainHand();
            if (item.getType().isAir()) {
                return;
            }
        } else {

            item = new ItemStack(Material.POTION);

        }

        final Map<String, Object> serialize = ItemUtils.serialize(item);
        if (serialize != null) {
            Professions.log(serialize.toString());
            final ItemStack deserialize;
            try {
                deserialize = ItemUtils.deserialize(serialize);
                Professions.log(deserialize);
            } catch (ConfigurationException | InitializationException e) {
                Professions.logError(e, false);
            }

        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "test";
    }
}
