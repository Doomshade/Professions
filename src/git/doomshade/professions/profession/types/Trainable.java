package git.doomshade.professions.profession.types;

import java.util.HashMap;
import java.util.Map;

public interface Trainable {
    String TRAINABLE = "trainable", COST = "trainable-cost", TRAINABLE_ID = "trainable-id";

    String VAR_TRAINABLE_COST = "\\{trainable-cost\\}";

    String getTrainableId();

    void setTrainableId(String id);

    boolean isTrainable();

    void setTrainable(boolean trainable);

    int getCost();

    void setCost(int cost);

    default Map<String, Object> serializeTrainable() {
        return new HashMap<String, Object>() {
            {
                put(TRAINABLE, isTrainable());
                put(COST, getCost());
                put(TRAINABLE_ID, getTrainableId());
            }
        };
    }

    default void deserializeTrainable(Map<String, Object> map) {
        setTrainable((boolean) map.get(TRAINABLE));
        setCost((int) map.get(COST));
        setTrainableId((String) map.get(TRAINABLE_ID));
    }
}
