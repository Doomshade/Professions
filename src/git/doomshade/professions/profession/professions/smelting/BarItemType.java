package git.doomshade.professions.profession.professions.smelting;

import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.types.CraftableItemType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class BarItemType extends CraftableItemType<ItemStack> {

    /**
     * Constructor for creation of the item type object
     *
     * @param object
     */
    public BarItemType(ItemStack object) {
        super(object);
    }

    @Override
    public Map<String, Object> getSerializedObject() {
        return new HashMap<>();
    }

    @Override
    protected ItemStack deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        return null;
    }

    @Override
    public Function<ItemStack, ItemStack> getExtraInEvent() {
        return x -> x;
    }
}
