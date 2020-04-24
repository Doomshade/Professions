package git.doomshade.professions.profession.professions.skinning;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.profession.types.IHunting;
import git.doomshade.professions.profession.types.ItemType;

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
    public <A extends ItemType<?>> void onEvent(ProfessionEventWrapper<A> ev) {
        final ProfessionEvent<A> e = ev.event;
        ProfessionEvent<PreyItemType> event;
        try {
            event = getEvent(e, PreyItemType.class);
        } catch (ClassCastException ex) {
            return;
        }
        if (!playerMeetsLevelRequirements(e)) {
            return;
        }
        addExp(event);
    }

}
