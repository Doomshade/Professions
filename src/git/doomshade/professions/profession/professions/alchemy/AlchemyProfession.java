package git.doomshade.professions.profession.professions.alchemy;

import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.event.ProfessionEventWrapper;

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
