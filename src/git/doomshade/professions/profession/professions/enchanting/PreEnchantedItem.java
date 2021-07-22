package git.doomshade.professions.profession.professions.enchanting;

import org.bukkit.inventory.ItemStack;

/**
 * Class for storing an {@link Enchant} and {@link ItemStack} before enchanting
 *
 * @author Doomshade
 */
public class PreEnchantedItem {

    /**
     * The enchant
     */
    public final Enchant enchant;

    /**
     * The pre-enchanted item
     */
    public final ItemStack preEnchantedItemStack;

    public PreEnchantedItem(Enchant enchant, ItemStack preEnchantedItemStack) {
        this.enchant = enchant;
        this.preEnchantedItemStack = preEnchantedItemStack;
    }

    /**
     * Enchants the item
     *
     * @see Enchant#use(ItemStack)
     */
    public void enchant() {
        enchant.use(preEnchantedItemStack);
    }

}
