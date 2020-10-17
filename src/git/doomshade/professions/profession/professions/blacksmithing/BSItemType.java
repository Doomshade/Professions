package git.doomshade.professions.profession.professions.blacksmithing;

import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.types.CraftableItemType;
import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class BSItemType extends CraftableItemType<ItemStack> {
    /**
     * Constructor for creation of the item type object
     *
     * @param object
     */
    public BSItemType(ItemStack object) {
        super(object);
    }

    @Override
    public Map<String, Object> getSerializedObject() {
        if (getObject() != null) {
            return ItemUtils.serialize(getObject());
        }
        return new HashMap<>();
    }

    @Override
    protected ItemStack deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        return ItemUtils.deserialize(map);
    }
}
