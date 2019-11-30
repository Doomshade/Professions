package git.doomshade.professions.profession.types;


import git.doomshade.professions.exceptions.ProfessionInitializationException;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static git.doomshade.professions.utils.Strings.ITrainableEnum.*;

/**
 * Interface for trainable {@link ItemType}s. Implement this in a class extending {@link ItemType},
 * then override {@link ItemType#deserialize(Map)} and call {@link #deserializeTrainable(Map, ITrainable)}
 * with the map and {@code this} argument inside the {@link ItemType#deserialize(Map)} method.
 * Override {@link ItemType#serialize()} as well and call {@link Map#putAll(Map)} on a {@code super.}{@link ItemType#serialize()} {@link Map} variable with an argument
 * of {@link #serializeTrainable(ITrainable)} and return the map.
 *
 * @author Doomshade
 * @see git.doomshade.professions.profession.types.enchanting.EnchantedItemType on GitHub for an example
 */
public interface ITrainable {

    /**
     * Make sure to override the {@link ItemType#serialize()} method and call and call {@link Map#putAll(Map)} of this map.
     *
     * @param trainable the trainable item
     * @return the serialized form of this class
     */
    static Map<String, Object> serializeTrainable(final ITrainable trainable) {
        return new HashMap<String, Object>() {
            {
                put(TRAINABLE.s, trainable.isTrainable());
                put(COST.s, trainable.getCost());
                put(TRAINABLE_ID.s, trainable.getTrainableId());
            }
        };
    }

    /**
     * Make sure to override the {@link ItemType#deserialize(Map)} method and call this method.
     *
     * @param map       the serialized version of this class
     * @param trainable the trainable item
     * @throws ProfessionInitializationException if the deserialization was unsuccessful
     */
    static void deserializeTrainable(Map<String, Object> map, ITrainable trainable) throws ProfessionInitializationException {


        trainable.setTrainable((boolean) map.getOrDefault(TRAINABLE.s, true));
        trainable.setCost((int) map.getOrDefault(COST.s, -1));
        trainable.setTrainableId((String) map.getOrDefault(TRAINABLE_ID.s, "NO_ID"));

        Set<String> list = Utils.getMissingKeys(map, Strings.ITrainableEnum.values()).stream().filter(x -> !x.equalsIgnoreCase(VAR_TRAINABLE_COST.s)).collect(Collectors.toSet());
        if (!list.isEmpty()) {
            throw new ProfessionInitializationException((Class<? extends ItemType>) trainable.getClass(), list);
        }
    }

    /**
     * @return the trainable id of this item type
     */
    String getTrainableId();

    /**
     * Sets the trainable id of this item type
     *
     * @param id the id to set
     */
    void setTrainableId(String id);

    /**
     * Set this to be {@code true} as default.
     *
     * @return {@code true} if it is trainable (for temporal purposes)
     */
    boolean isTrainable();

    /**
     * Sets the item type to be or not to be trainable.
     *
     * @param trainable whether or not it should be trainable
     */
    void setTrainable(boolean trainable);

    /**
     * @return the cost of training this item type
     */
    int getCost();

    /**
     * Sets the training cost of this item type
     *
     * @param cost the cost to set
     */
    void setCost(int cost);
}
