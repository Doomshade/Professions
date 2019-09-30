package git.doomshade.professions.profession.types.enchanting;

import org.bukkit.inventory.ItemStack;

public class PreEnchantedItem {

    public final Enchant enchant;
    public final ItemStack preEnchantedItemStack;

    public PreEnchantedItem(Enchant enchant, ItemStack preEnchantedItemStack) {
        this.enchant = enchant;
        this.preEnchantedItemStack = preEnchantedItemStack;
    }

    public void enchant() {
        enchant.use(preEnchantedItemStack);
    }

}
