package git.doomshade.professions.profession.professions.blacksmithing;

import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.api.item.CraftableItemType;
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
        try {
            return ItemUtils.deserialize(map);
        } catch (ConfigurationException | InitializationException e) {
            Professions.logError(e, false);
            throw new ProfessionObjectInitializationException("Could not deserialize blacksmith ItemStack from file.");
        }
    }
}
