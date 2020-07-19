package git.doomshade.professions.profession.professions.enchanting;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.profession.types.ICrafting;
import git.doomshade.professions.profession.types.IEnchanting;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.user.User;

import java.util.Optional;

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
    public <T extends ItemType<?>> void onEvent(ProfessionEventWrapper<T> e) {
        final Optional<ProfessionEvent<EnchantedItemItemType>> opt = getEvent(e, EnchantedItemItemType.class);
        if (!opt.isPresent()) return;

        final ProfessionEvent<EnchantedItemItemType> event = opt.get();

        User user = event.getPlayer();
        if (!playerMeetsLevelRequirements(event)) {
            event.setCancelled(true);
            event.printErrorMessage(getUserProfessionData(user));
            return;
        }

        PreEnchantedItem preEnchantedItem = event.getExtra(PreEnchantedItem.class);
        if (preEnchantedItem == null) {
            return;
        }
        ProfessionEventType profEventType = event.getExtra(ProfessionEventType.class);
        if (profEventType == null) {
            return;
        }

        switch (profEventType) {
            case CRAFT:
                addExp(preEnchantedItem.enchant.getCraftExpYield(), user, event.getItemType());
                break;
            case ENCHANT:
                preEnchantedItem.enchant();
                addExp(event);
                break;
        }

    }


    public enum ProfessionEventType {
        CRAFT, ENCHANT
    }

}
