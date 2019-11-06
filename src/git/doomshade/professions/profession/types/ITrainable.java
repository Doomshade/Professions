package git.doomshade.professions.profession.types;

import java.util.HashMap;
import java.util.Map;

public interface ITrainable {
    String TRAINABLE = "trainable", COST = "trainable-cost", TRAINABLE_ID = "trainable-id";

    String VAR_TRAINABLE_COST = "\\{trainable-cost\\}";

    String getTrainableId();

    void setTrainableId(String id);

    boolean isTrainable();

    void setTrainable(boolean trainable);

    int getCost();

    void setCost(int cost);

    static Map<String, Object> serializeTrainable(final ITrainable trainable) {
        return new HashMap<String, Object>() {
            {
                put(TRAINABLE, trainable.isTrainable());
                put(COST, trainable.getCost());
                put(TRAINABLE_ID, trainable.getTrainableId());
            }
        };
    }

    static void deserializeTrainable(Map<String, Object> map, ITrainable trainable) {
        trainable.setTrainable((boolean) map.get(TRAINABLE));
        trainable.setCost((int) map.get(COST));
        trainable.setTrainableId((String) map.get(TRAINABLE_ID));
    }
}
