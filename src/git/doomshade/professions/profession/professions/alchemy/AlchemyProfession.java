package git.doomshade.professions.profession.professions.alchemy;

import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.profession.Profession;
import git.doomshade.professions.profession.types.ItemType;

public class AlchemyProfession extends Profession {

    @Override
    public void onLoad() {
        addItems(PotionItemType.class);
    }

    @Override
    public String getID() {
        return "alchemy";
    }

    @Override
    public <T extends ItemType<?>> void onEvent(ProfessionEventWrapper<T> ev) {

        addExp(ev.event);
    }

    @Override
    public boolean isSubprofession() {
        return false;
    }
}
