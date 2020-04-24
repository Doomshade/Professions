package git.doomshade.professions.profession.professions.smelting;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.profession.types.ICrafting;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.utils.Utils;
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
    public <T extends ItemType<?>> void onEvent(ProfessionEventWrapper<T> ev) {
        final ProfessionEvent<T> e = ev.event;
        ProfessionEvent<BarItemType> event;
        try {
            event = getEvent(e, BarItemType.class);
        } catch (ClassCastException ex) {
            return;
        }
        String expMsg = "";
        if (addExp(e)) {
            expMsg = Utils.getReceiveXp(e.getExp());
        }
        ItemStack item = event.getItemType().getObject();
        String itemName = item != null ? item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name() : "NULL";
        Professions.log(String.format("%s smelted %s".concat(expMsg), e.getPlayer().getPlayer().getName(), itemName), Level.CONFIG);
    }
}
