package git.doomshade.professions.profession.professions.enchanting;

import git.doomshade.professions.profession.types.CraftableItemType;
import git.doomshade.professions.profession.types.IEnchanting;
import git.doomshade.professions.profession.types.IProfessionType;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * An enchant item type example for {@link EnchantingProfession}
 *
 * @author Doomshade
 */
public class EnchantedItemItemType extends CraftableItemType<Enchant> {
    private static String ENCHANT = "enchant";

    /**
     * Constructor for creation of the item type object
     *
     * @param object
     */
    public EnchantedItemItemType(Enchant object) {
        super(object);
    }

    @Override
    public boolean equalsObject(Enchant t) {
        Enchant ench = getObject();
        if (ench == null || t == null) {
            return false;
        }
        return ench.getItem().isSimilar(t.getItem());
    }

    @Override
    public Map<String, Object> getSerializedObject() {
        Map<String, Object> map = new HashMap<>();
        Enchant ench = getObject();
        if (ench != null)
            map.put(ENCHANT, ench.serialize());
        return map;
    }

    @Override
    protected Enchant deserializeObject(Map<String, Object> map) {
        return Enchant.deserialize(((MemorySection) map.get(ENCHANT)).getValues(true));
    }

    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return IEnchanting.class;
    }

    @Override
    public String toString() {
        return super.toString() + toStringFormat();
    }

    @Override
    public Function<ItemStack, PreEnchantedItem> getExtraInEvent() {
        return x -> new PreEnchantedItem(getObject(), x);
    }
}
