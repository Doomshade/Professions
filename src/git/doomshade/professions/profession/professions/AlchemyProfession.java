package git.doomshade.professions.profession.professions;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.crafting.ICrafting;
import git.doomshade.professions.profession.types.crafting.alchemy.PotionItemType;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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
        if (!event.hasExtra(ItemStack.class)) {
            throw new IllegalStateException("No itemstack in alchemy given!");
        }

        ItemStack potion = event.getExtra(ItemStack.class);
        if (potion == null) {
            throw new IllegalStateException("No itemstack in alchemy given!");
        }

        final PlayerInventory inventory = e.getPlayer().getPlayer().getInventory();
        inventory.remove(event.getItemType().getResult());
        inventory.addItem(potion);

    }
}
