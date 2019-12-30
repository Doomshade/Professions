package git.doomshade.professions.profession.professions;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.crafting.ICrafting;
import git.doomshade.professions.profession.types.crafting.alchemy.Potion;
import git.doomshade.professions.profession.types.crafting.alchemy.PotionItemType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
    @EventHandler
    public <T extends ItemType<?>> void onEvent(ProfessionEvent<T> e) {
        if (!isValidEvent(e, PotionItemType.class)) {
            return;
        }
        ProfessionEvent<PotionItemType> event = getEvent(e, PotionItemType.class);
        final PotionItemType itemType = event.getItemType();

        final ItemStack craftedItem = itemType.getResult();
        final Potion potionObject = itemType.getObject();

        if (potionObject != null) {
            Optional<ItemStack> optionalPotion = potionObject.getPotionItem(craftedItem);
            if (!optionalPotion.isPresent()) {
                return;
            }

            e.setCancelled(true);
            ItemStack potion = optionalPotion.get();
            final Player player = e.getPlayer().getPlayer();

            itemType.removeCraftingRequirements(player);
            player.getInventory().addItem(potion);
        }


    }
}
