package git.doomshade.professions.profession.professions.skinning;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.IHunting;
import git.doomshade.professions.profession.types.ItemType;
import org.bukkit.event.EventHandler;

public final class SkinningProfession extends Profession<IHunting> {

    @Override
    public void onLoad() {
        addItems(PreyItemType.class);
    }

    @Override
    public String getID() {
        return "skinning";
    }

    @Override
    @EventHandler
    public <A extends ItemType<?>> void onEvent(ProfessionEvent<A> e) {
        ProfessionEvent<PreyItemType> event = getEvent(e, PreyItemType.class);
        if (!isValidEvent(event, PreyItemType.class) || !playerMeetsLevelRequirements(e)) {
            return;
        }
        addExp(event);
    }

}
