package git.doomshade.professions.profession.types.enchanting;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * Basically an enchant manager
 *
 * @author Doomshade
 */
public class EnchantManager {
    private static EnchantManager instance;

    static {
        instance = new EnchantManager();
    }

    private EnchantManager() {
    }

    /**
     * @return the instance of this class
     */
    public static EnchantManager getInstance() {
        return instance;
    }

    /**
     * @param item the ItemStack
     * @param <T>  the Enchant
     * @return an Enchant if the ItemStack is one, {@code null} otherwise
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Enchant> T getEnchantFromItem(ItemStack item) {
        for (Enchant ench : Enchant.ENCHANTS.values()) {
            if (ench.getItem().isSimilar(item)) {
                return (T) ench;
            }
        }
        return null;
    }

    /**
     * @param enchant the enchant class
     * @param <T>     the enchant
     * @return an Enchant instance
     */
    @SuppressWarnings("unchecked")
    public <T extends Enchant> T getEnchant(Class<T> enchant) {
        return (T) Enchant.ENCHANTS.get(enchant);
    }

    /**
     * Registers an Enchant
     *
     * @param enchant the enchant class to register
     */
    public <T extends Enchant> void registerEnchant(Enchant enchant) {
        enchant.init();
        Enchant.ENCHANTS.putIfAbsent(enchant.getClass(), enchant);
    }

    /**
     * Enchants an item
     *
     * @param enchant the enchant
     * @param on      the item
     * @see Enchant#use(ItemStack)
     */
    public void enchant(Enchant enchant, ItemStack on) {
        enchant.use(on);
    }

    /**
     * Enchants an item
     *
     * @param enchant   the enchant
     * @param on        the item
     * @param intensity the intensity of enchant
     * @see Enchant#use(ItemStack, int)
     */
    public void enchant(Enchant enchant, ItemStack on, int intensity) {
        enchant.use(on, intensity);
    }

}
