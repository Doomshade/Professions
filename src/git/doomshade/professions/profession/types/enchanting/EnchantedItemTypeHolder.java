package git.doomshade.professions.profession.types.enchanting;

import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.profession.types.enchanting.enchants.RandomAttributeEnchant;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
        eit.addCraftRequirement(new ItemStack(Material.GLASS));
        registerObject(eit);
    }

}
