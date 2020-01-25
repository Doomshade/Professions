package git.doomshade.professions.profession.professions;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.crafting.ICrafting;
import git.doomshade.professions.profession.types.enchanting.EnchantedItemItemType;
import git.doomshade.professions.profession.types.enchanting.IEnchanting;
import git.doomshade.professions.profession.types.enchanting.PreEnchantedItem;
import git.doomshade.professions.user.User;
import org.bukkit.event.EventHandler;

public final class EnchantingProfession extends Profession<IEnchanting> implements ICrafting {

    @Override
    public void onLoad() {
        addItems(EnchantedItemItemType.class);

    }

    @Override
    public String getID() {
        return "enchanting";
    }

    @Override
    @EventHandler
    public <A extends ItemType<?>> void onEvent(ProfessionEvent<A> event) {

        if (!isValidEvent(event, EnchantedItemItemType.class)) {
            return;
        }

        ProfessionEvent<EnchantedItemItemType> e = getEvent(event, EnchantedItemItemType.class);

        User user = e.getPlayer();
        if (!playerMeetsLevelRequirements(e)) {
            e.setCancelled(true);
            e.printErrorMessage(getUserProfessionData(user));
            return;
        }

        PreEnchantedItem preEnchantedItem = e.getExtra(PreEnchantedItem.class);
        if (preEnchantedItem == null) {
            return;
        }
        ProfessionEventType profEventType = e.getExtra(ProfessionEventType.class);
        if (profEventType == null) {
            return;
        }

        switch (profEventType) {
            case CRAFT:
                addExp(preEnchantedItem.enchant.getCraftExpYield(), user, e.getItemType());
                break;
            case ENCHANT:
                preEnchantedItem.enchant();
                addExp(e);
                break;
        }

    }


    public enum ProfessionEventType {
        CRAFT, ENCHANT
    }

}
