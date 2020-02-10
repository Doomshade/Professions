package git.doomshade.professions.profession.professions.jewelcrafting;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.professions.crafting.CustomRecipe;
import git.doomshade.professions.profession.types.ICrafting;
import git.doomshade.professions.profession.types.ItemType;
import org.bukkit.event.EventHandler;

public final class JewelcraftingProfession extends Profession<ICrafting> {

    @Override
    public void onLoad() {
        addItems(GemItemType.class);
    }

    @Override
    public String getID() {
        return "jewelcrafting";
    }

    @Override
    @EventHandler
    public <A extends ItemType<?>> void onEvent(ProfessionEvent<A> e) {
        if (!isValidEvent(e, CustomRecipe.class)) {
            return;
        }

        if (!playerMeetsLevelRequirements(e)) {
            e.setCancelled(true);
            e.printErrorMessage(getUserProfessionData(e.getPlayer()));
            return;
        }

        addExp(e);
    }

}
