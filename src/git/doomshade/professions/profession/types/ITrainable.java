package git.doomshade.professions.profession.types;


import git.doomshade.professions.exceptions.ProfessionInitializationException;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static git.doomshade.professions.utils.Strings.ITrainableEnum.*;

public interface ITrainable {

    String getTrainableId();

    void setTrainableId(String id);

    boolean isTrainable();

    void setTrainable(boolean trainable);

    int getCost();

    void setCost(int cost);

    static Map<String, Object> serializeTrainable(final ITrainable trainable) {
        return new HashMap<String, Object>() {
            {
                put(TRAINABLE.s, trainable.isTrainable());
                put(COST.s, trainable.getCost());
                put(TRAINABLE_ID.s, trainable.getTrainableId());
            }
        };
    }

    static void deserializeTrainable(Map<String, Object> map, ITrainable trainable) throws ProfessionInitializationException {


        trainable.setTrainable((boolean) map.getOrDefault(TRAINABLE.s, true));
        trainable.setCost((int) map.getOrDefault(COST.s, -1));
        trainable.setTrainableId((String) map.getOrDefault(TRAINABLE_ID.s, "NO_ID"));

        List<String> list = Utils.getMissingKeys(map, Strings.ITrainableEnum.values());
        if (!list.isEmpty()) {
            throw new ProfessionInitializationException(trainable.getClass(), list);
        }
    }
}
