package git.doomshade.professions.profession.types.enchanting;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;

public class EnchantManager {
    private static EnchantManager instance;

    static {
        instance = new EnchantManager();
    }

    private EnchantManager() {
    }

    public static EnchantManager getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T extends Enchant> T getEnchantFromItem(ItemStack item) {
        for (Enchant ench : Enchant.ENCHANTS.values()) {
            if (ench.getItem().isSimilar(item)) {
                return (T) ench;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Enchant> T getEnchant(Class<T> enchant) {
        return (T) Enchant.ENCHANTS.get(enchant);
    }

    public <T extends Enchant> void registerEnchant(Class<T> enchant, ItemStack enchantItem) throws Exception {
        Class<? super T> superClass = enchant.getSuperclass();
        if (superClass == null) {
            return;
        }
        Constructor<T> constructor = enchant.getDeclaredConstructor(ItemStack.class);
        constructor.setAccessible(true);
        T enchantToRegister = constructor.newInstance(enchantItem);
        enchantToRegister.init();
        Enchant.ENCHANTS.putIfAbsent(enchantToRegister.getClass(), enchantToRegister);
    }

    public void enchant(Enchant enchant, ItemStack on) {
        enchant.use(on);
    }

}
