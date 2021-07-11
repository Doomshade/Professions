package git.doomshade.professions.api.item;

import git.doomshade.professions.utils.Requirements;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Interface for craftable {@link ItemType}s
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
     * @return the internal sounds to play
     */
    Map<Sound, String> getSounds();

    enum Sound {
        CRAFTING, ON_CRAFT
    }
}
