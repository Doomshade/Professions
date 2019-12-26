package git.doomshade.professions.profession.professions;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.crafting.ICrafting;
import git.doomshade.professions.profession.types.mining.smelting.BarItemType;
import git.doomshade.professions.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

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
    @EventHandler
    public <T extends ItemType<?>> void onEvent(ProfessionEvent<T> e) {

        if (!isValidEvent(e, BarItemType.class)) {
            return;
        }
        ProfessionEvent<BarItemType> event = getEvent(e, BarItemType.class);
        String expMsg = "";
        if (addExp(e)) {
            expMsg = Utils.getReceiveXp(e.getExp());
        }
        ItemStack item = event.getItemType().getObject();
        String itemName = item != null ? item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name() : "NULL";
        Professions.log(String.format("%s smelted %s".concat(expMsg), e.getPlayer().getPlayer().getName(), itemName), Level.CONFIG);
    }
}
