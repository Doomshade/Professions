package git.doomshade.professions.profession.professions;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.crafting.ICrafting;
import git.doomshade.professions.profession.types.enchanting.EnchantedItemType;
import git.doomshade.professions.profession.types.enchanting.IEnchanting;
import git.doomshade.professions.profession.types.enchanting.PreEnchantedItem;
import git.doomshade.professions.user.User;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class EnchantingProfession extends Profession<IEnchanting> implements ICrafting {

    @Override
    public void onLoad() {
        setName("&aEnchanting");
        setProfessionType(ProfessionType.PRIMARNI);
    }

    @Override
    public void onPostLoad() {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getColoredName());
        item.setItemMeta(meta);
        setIcon(item);
        addItems(EnchantedItemType.class);
    }

    @Override
    public String getID() {
        return "enchanting";
    }

    @Override
    @EventHandler
    public <A extends ItemType<?>> void onEvent(ProfessionEvent<A> e) {

        if (!isValidEvent(e, EnchantedItemType.class)) {
            return;
        }
        User user = e.getPlayer();
        if (!playerMeetsRequirements(e)) {
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
                addExp(preEnchantedItem.enchant.getCraftExpYield(), user, e.getObject());
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
