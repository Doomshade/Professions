package git.doomshade.professions.profession.professions;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.hunting.IHunting;
import git.doomshade.professions.profession.types.hunting.Prey;
import org.bukkit.event.EventHandler;

public final class SkinningProfession extends Profession<IHunting> {

    @Override
    public void onLoad() {
        addItems(Prey.class);
    }

    @Override
    public String getID() {
        return "skinning";
    }

    @Override
    @EventHandler
    public <A extends ItemType<?>> void onEvent(ProfessionEvent<A> e) {
        ProfessionEvent<Prey> event = getEvent(e, Prey.class);
        if (!isValidEvent(event, Prey.class) || !playerMeetsLevelRequirements(e)) {
            return;
        }
        addExp(event);
    }

}
