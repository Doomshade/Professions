package git.doomshade.professions.profession.professions.alchemy;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.profession.types.ICrafting;
import git.doomshade.professions.profession.types.ItemType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class AlchemyProfession extends Profession<ICrafting> {

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
        ProfessionEvent<PotionItemType> event;
        try {
            event = getEvent(ev.event, PotionItemType.class);
        } catch (ClassCastException e) {
            return;
        }
        final PotionItemType itemType = event.getItemType();

        final ItemStack craftedItem = itemType.getResult();
        final Potion potionObject = itemType.getObject();

        if (potionObject != null) {
            Optional<ItemStack> optionalPotion = potionObject.getPotionItem(craftedItem);
            if (!optionalPotion.isPresent()) {
                return;
            }

            event.setCancelled(true);
            final Player player = event.getPlayer().getPlayer();

            itemType.removeCraftingRequirements(player);
            optionalPotion.ifPresent(potion -> player.getInventory().addItem(potion));
            addExp(event);
        }
    }
}
