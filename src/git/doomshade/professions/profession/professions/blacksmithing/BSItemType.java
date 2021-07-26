package git.doomshade.professions.profession.professions.blacksmithing;

import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.api.item.CraftableItemType;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class BSItemType extends CraftableItemType<BSItemStack> {
    /**
     * Constructor for creation of the item type object
     *
     * @param object
     */
    public BSItemType(BSItemStack object) {
        super(object);
    }

    @Override
    protected BSItemStack deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        try {
            return new BSItemStack(ItemUtils.deserialize(map));
        } catch (ConfigurationException | InitializationException e) {
            ProfessionLogger.logError(e, false);
            throw new ProfessionObjectInitializationException("Could not deserialize blacksmith ItemStack from file.");
        }
    }
}
