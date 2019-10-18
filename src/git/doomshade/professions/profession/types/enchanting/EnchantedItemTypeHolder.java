package git.doomshade.professions.profession.types.enchanting;

import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.profession.types.enchanting.enchants.RandomAttributeEnchant;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class EnchantedItemTypeHolder extends ItemTypeHolder<EnchantedItemType> {

    @Override
    public void init() {
        EnchantManager enchm = EnchantManager.getInstance();
        try {
            enchm.registerEnchant(RandomAttributeEnchant.class, new ItemStack(Material.GLASS));
        } catch (Exception e) {
            e.printStackTrace();
        }
        RandomAttributeEnchant ench = enchm.getEnchant(RandomAttributeEnchant.class);
        EnchantedItemType eit = new EnchantedItemType(ench, 69);
        ItemStack craftRequirement = new ItemStack(Material.GLASS);
        ItemMeta craftRequirementMeta = craftRequirement.getItemMeta();
        craftRequirementMeta.setDisplayName(ChatColor.WHITE + "Sklo");
        craftRequirementMeta.setLore(Arrays.asList("Japato"));
        craftRequirement.setItemMeta(craftRequirementMeta);
        eit.addCraftRequirement(craftRequirement);
        eit.setName(ChatColor.RED + "Test random attribute enchantment");
        registerObject(eit);
    }

}
