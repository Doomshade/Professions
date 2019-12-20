package git.doomshade.professions.profession.professions;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.crafting.ICrafting;
import git.doomshade.professions.profession.types.mining.smelting.BarItemType;

public class SmeltingProfession extends Profession<ICrafting> {

    @Override
    public void onLoad() {
        addItems(BarItemType.class);
    }

    @Override
    public String getID() {
        return "smelting";
    }

    @Override
    public <T extends ItemType<?>> void onEvent(ProfessionEvent<T> e) {
        if (!isValidEvent(e, BarItemType.class)) {
            return;
        }
        addExp(e);
    }
}
