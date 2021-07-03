package git.doomshade.professions.profession.professions.blacksmithing;

import git.doomshade.professions.Professions;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.types.ItemType;
import git.doomshade.professions.utils.Utils;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class BlacksmithingProfession extends Profession {


    @Override
    public void onLoad() {
        addItems(BSItemType.class);
    }

    @Override
    public String getID() {
        return "blacksmithing";
    }

    @Override
    public <IType extends ItemType<?>> void onEvent(ProfessionEventWrapper<IType> ev) {
        final ProfessionEvent<BSItemType> event = getEventUnsafe(ev, BSItemType.class);
        String expMsg = "";
        if (addExp(event)) {
            expMsg = Utils.getReceiveXp(event.getExp());
        }
        ItemStack item = event.getItemType().getObject();
        String itemName = item != null ? item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name() : "NULL";
        Professions.log(String.format("%s smithed %s".concat(expMsg), event.getPlayer().getPlayer().getName(), itemName), Level.CONFIG);
    }

    @Override
    public boolean isSubprofession() {
        return false;
    }
}
