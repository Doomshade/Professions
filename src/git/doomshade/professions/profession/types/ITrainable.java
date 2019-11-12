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

    static void deserializeTrainable(Map<String, Object> map, ITrainable trainable) throws Exception {

        if (!map.containsKey(TRAINABLE) || !map.containsKey(COST) || !map.containsKey(TRAINABLE_ID)) {
            trainable.setTrainable(true);
            trainable.setCost(0);
            trainable.setTrainableId("NO_ID");
            throw new Exception(String.format("Could not deserialize %s because some of the keys are missing! - %s (boolean type) %s (int type) %s (String type), using default values (true, 0, \"NO_ID\")", trainable.getClass().getSimpleName(), TRAINABLE, COST, TRAINABLE_ID));
        }
        trainable.setTrainable((boolean) map.get(TRAINABLE));
        trainable.setCost((int) map.get(COST));
        trainable.setTrainableId((String) map.get(TRAINABLE_ID));
    }
}
