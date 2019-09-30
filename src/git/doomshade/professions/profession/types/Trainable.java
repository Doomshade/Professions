package git.doomshade.professions.profession.types;

import git.doomshade.professions.Professions;
import git.doomshade.professions.user.UserProfessionData;
import net.milkbowl.vault.economy.EconomyResponse;

public interface Trainable {

    String VAR_TRAINABLE_COST = "\\{trainable-cost\\}";

    String getExtra();

    boolean isTrainable();

    void setTrainable(boolean trainable);

    int getCost();

    default boolean hasTrained(UserProfessionData upd) {
        return upd.hasExtra(getExtra());
    }

    default boolean train(UserProfessionData upd) {

        EconomyResponse response = Professions.getEconomy().withdrawPlayer(upd.getUser().getPlayer(), getCost());
        if (!response.transactionSuccess()) {
            return false;
        }
        upd.addExtra(getExtra());
        return true;
    }

}
