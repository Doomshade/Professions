package git.doomshade.professions.profession.types;

import git.doomshade.professions.Professions;
import git.doomshade.professions.user.UserProfessionData;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.configuration.MemorySection;

import java.util.HashMap;
import java.util.Map;

public interface Trainable {
    String TRAINABLE = "trainable", COST = "trainable-cost", TRAINABLE_ID = "trainable-id";

    String VAR_TRAINABLE_COST = "\\{trainable-cost\\}";

    String getTrainableStringId();

    void setTrainableStringId(String id);

    boolean isTrainable();

    void setTrainable(boolean trainable);

    int getCost();

    void setCost(int cost);

    default boolean hasTrained(UserProfessionData upd) {
        return upd.hasExtra(getTrainableStringId());
    }

    default boolean train(UserProfessionData upd) {

        EconomyResponse response = Professions.getEconomy().withdrawPlayer(upd.getUser().getPlayer(), getCost());
        if (!response.transactionSuccess()) {
            return false;
        }
        upd.addExtra(getTrainableStringId());
        return true;
    }

    default Map<String, Object> serializeTrainable() {
        return new HashMap<String, Object>() {
            {
                put(TRAINABLE, isTrainable());
                put(COST, getCost());
                put(TRAINABLE_ID, getTrainableStringId());
            }
        };
    }

    default void deserializeTrainable(Map<String, Object> map) {
        setTrainable((boolean) map.get(TRAINABLE));
        setCost((int) map.get(COST));
        setTrainableStringId((String) map.get(TRAINABLE_ID));
    }
}
