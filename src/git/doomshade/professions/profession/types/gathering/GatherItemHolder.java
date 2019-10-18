package git.doomshade.professions.profession.types.gathering;

import git.doomshade.professions.profession.types.ItemTypeHolder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GatherItemHolder extends ItemTypeHolder<GatherItem> {

    @Override
    public void init() {
        ItemStack item = new ItemStack(Material.GLASS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + "Test gathered item");
        meta.setLore(Arrays.asList("Yay"));
        item.setItemMeta(meta);
        GatherItem gatherItem = new GatherItem(item, 500);
        gatherItem.setName(ChatColor.DARK_AQUA + "Test gather item");
        registerObject(gatherItem);
    }

}
