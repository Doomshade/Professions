package git.doomshade.professions.profession;


import git.doomshade.professions.exceptions.ProfessionInitializationException;
import git.doomshade.professions.profession.professions.enchanting.EnchantedItemItemType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.utils.DeserializeMethod;
import git.doomshade.professions.utils.SerializeMethod;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static git.doomshade.professions.utils.Strings.ITrainableEnum.*;

/**
 * Interface for train-able {@link ItemType}s
 *
 * @author Doomshade
 * @see EnchantedItemItemType on GitHub for an example
 */
public interface ITrainable extends ICustomType {

    /**
     * @return the serialized form of this class
     */
    @SerializeMethod
    default Map<String, Object> serializeTrainable() {
        Map<String, Object> map = new HashMap<>();


        map.put(TRAINABLE.s, isTrainable());
        map.put(COST.s, getCost());
        map.put(TRAINABLE_ID.s, getTrainableId());

        return map;
    }

    /**
     * @param map        the serialized version of this class
     * @param customType the trainable item
     * @throws ProfessionInitializationException if the deserialization was unsuccessful
     */
    @DeserializeMethod
    static void deserializeTrainable(Map<String, Object> map, ICustomType customType) throws ProfessionInitializationException {

        if (!(customType instanceof ITrainable)) {
            return;
        }

        ITrainable trainable = (ITrainable) customType;

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
