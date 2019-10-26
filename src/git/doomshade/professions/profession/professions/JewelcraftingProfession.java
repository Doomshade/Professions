package git.doomshade.professions.profession.professions;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.crafting.CustomRecipe;
import git.doomshade.professions.profession.types.crafting.ICrafting;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class JewelcraftingProfession extends Profession<ICrafting> {

    @Override
    public void onLoad() {
        setName("&bJewelcrafting");
        setProfessionType(ProfessionType.PRIMARNI);
    }

    @Override
    public void onPostLoad() {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getColoredName());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        setIcon(item);
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

        if (!playerMeetsRequirements(e)) {
            e.setCancelled(true);
            e.printErrorMessage(getUserProfessionData(e.getPlayer()));
            return;
        }

        addExp(e);
    }

}
