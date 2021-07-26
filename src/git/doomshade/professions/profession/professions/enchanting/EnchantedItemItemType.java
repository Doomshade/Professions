package git.doomshade.professions.profession.professions.enchanting;

import git.doomshade.professions.api.item.CraftableItemType;
import git.doomshade.professions.exceptions.InitializationException;
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
    private static final String ENCHANT = "enchant";

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
    protected Enchant deserializeObject(Map<String, Object> map) {
        try {
            return Enchant.deserialize(map);
        } catch (InitializationException e) {
            return null;
        }
    }

    @Override
    public Function<ItemStack, Collection<?>> getExtraInEvent() {

        // we can add a collection as an argument
        return x -> Arrays.asList(
                new PreEnchantedItem(getObject(), x),
                EnchantingProfession.ProfessionEventType.CRAFT);
    }
}
