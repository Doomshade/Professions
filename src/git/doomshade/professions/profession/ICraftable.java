package git.doomshade.professions.profession;

import git.doomshade.professions.profession.types.CraftableItemType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.utils.Requirements;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Interface for craft-able {@link ItemType}s
 *
 * @author Doomshade
 * @version 1.0
 * @see CraftableItemType
 */
public interface ICraftable {

    /**
     * @return the crafting time of the item type
     */
    double getCraftingTime();

    /**
     * Sets the crafting time of the craft
     *
     * @param craftingTime the crafting time
     */
    void setCraftingTime(double craftingTime);

    /**
     * @return the result of crafting
     */
    ItemStack getResult();

    /**
     * Sets the result of the the craft
     *
     * @param result the result
     */
    void setResult(ItemStack result);

    /**
     * @return the crafting requirements
     */
    Requirements getCraftingRequirements();

    /**
     * Sets the crafting requirements
     *
     * @param craftingRequirements the crafting requirements to set
     */
    void setCraftingRequirements(Requirements craftingRequirements);

    Map<Sound, String> getSounds();

    void setSounds(Map<Sound, String> sounds);

    enum Sound {
        CRAFTING, ON_CRAFT
    }
}
