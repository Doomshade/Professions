package git.doomshade.professions.profession.professions.jewelcrafting;

import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.profession.Profession;
import git.doomshade.professions.profession.types.ItemType;

public final class JewelcraftingProfession extends Profession {

    @Override
    public void onLoad() {
        addItems(GemItemType.class);
    }

    @Override
    public String getID() {
        return "jewelcrafting";
    }

    @Override
    public <A extends ItemType<?>> void onEvent(ProfessionEventWrapper<A> event) {
        final ProfessionEvent<A> e = event.event;

        if (!playerMeetsLevelRequirements(e)) {
            e.setCancelled(true);
            e.printErrorMessage(getUserProfessionData(e.getPlayer()));
            return;
        }

        addExp(e);
    }

    @Override
    public boolean isSubprofession() {
        return false;
    }
}
