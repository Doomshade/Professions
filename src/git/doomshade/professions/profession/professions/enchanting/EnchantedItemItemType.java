package git.doomshade.professions.profession.professions.enchanting;

import git.doomshade.professions.profession.types.CraftableItemType;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;

import java.util.*;
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
    public String toString() {
        return super.toString() + toStringFormat();
    }

    @Override
    public Function<ItemStack, Collection<?>> getExtraInEvent() {

        // we can add a collection as an argument
        return x -> Arrays.asList(
                new PreEnchantedItem(getObject(), x),
                EnchantingProfession.ProfessionEventType.CRAFT);
    }
}
