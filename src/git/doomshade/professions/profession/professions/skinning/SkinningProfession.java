package git.doomshade.professions.profession.professions.skinning;

import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.types.ItemType;

public final class SkinningProfession extends Profession {

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
        if (!playerMeetsLevelRequirements(e)) {
            return;
        }
        addExp(e);
    }

    @Override
    public boolean isSubprofession() {
        return false;
    }
}
