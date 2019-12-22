package git.doomshade.professions.profession.professions;

import git.doomshade.professions.Profession;
import git.doomshade.professions.data.ProfessionSpecificDropSettings;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.mining.IMining;
import git.doomshade.professions.profession.types.mining.Ore;
import git.doomshade.professions.profession.types.mining.OreItemType;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

public final class MiningProfession extends Profession<IMining> {

    @Override
    public void onLoad() {
        addItems(OreItemType.class);
    }


    @Override
    public String getID() {
        return "mining";
    }

    @Override
    @EventHandler
    public <A extends ItemType<?>> void onEvent(ProfessionEvent<A> ev) {
        if (!isValidEvent(ev, OreItemType.class)) {
            return;
        }
        ProfessionEvent<OreItemType> e = getEvent(ev, OreItemType.class);
        User hrac = e.getPlayer();
        UserProfessionData upd = hrac.getProfessionData(getClass());
        if (!playerMeetsLevelRequirements(e)) {
            e.setCancelled(true);
            e.printErrorMessage(upd);
            return;
        }


        // if the event passes, drop ore
        if (addExp(e)) {
            Location loc = e.getExtra(Location.class);
            final OreItemType itemType = e.getItemType();
            int amount = getProfessionSettings().getSettings(ProfessionSpecificDropSettings.class).getDropAmount(upd, itemType);
            Ore ore = itemType.getObject();

            // randomize drop for each drop amount
            for (int i = 0; i < amount; i++) {
                ItemStack miningResult = null;
                if (ore != null) {
                    miningResult = ore.getMiningResult();
                }

                if (miningResult != null) {
                    loc.getWorld().dropItem(loc, miningResult);
                }
            }
        }
    }


}
