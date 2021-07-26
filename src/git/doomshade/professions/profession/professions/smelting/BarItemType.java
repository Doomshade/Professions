package git.doomshade.professions.profession.professions.smelting;

import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.api.item.CraftableItemType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class BarItemType extends CraftableItemType<BarItemStack> {

    /**
     * Constructor for creation of the item type object
     *
     * @param object
     */
    public BarItemType(BarItemStack object) {
        super(object);
    }

    @Override
    protected BarItemStack deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        return null;
    }

    @Override
    public Function<ItemStack, ItemStack> getExtraInEvent() {
        return Function.identity();
    }
}
