package git.doomshade.professions.profession.professions;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.gathering.IGathering;
import git.doomshade.professions.profession.types.gathering.herbalism.Herb;
import git.doomshade.professions.profession.types.gathering.herbalism.HerbItemType;
import git.doomshade.professions.profession.types.gathering.herbalism.HerbLocationOptions;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;

import java.util.logging.Level;

public class HerbalismProfession extends Profession<IGathering> {
    @Override
    public String getID() {
        return "herbalism";
    }

    @Override
    public void onLoad() {
        addItems(HerbItemType.class);
    }

    @Override
    @EventHandler
    public <T extends ItemType<?>> void onEvent(ProfessionEvent<T> e) {
        if (!isValidEvent(e, HerbItemType.class)) {
            return;
        }

        ProfessionEvent<HerbItemType> event = getEvent(e, HerbItemType.class);

        if (e.hasExtra(Location.class)) {
            final Location location = e.getExtra(Location.class);
            final Herb herb = event.getItemType().getObject();
            if (herb != null && location != null) {
                final HerbLocationOptions herbLocationOptions = herb.getHerbLocationOptions(location);
                herbLocationOptions.despawn();
                herbLocationOptions.scheduleSpawn();
                e.getPlayer().getPlayer().getInventory().addItem(herb.getGatherItem());
                String expMsg = "";
                if (addExp(e)) {
                    expMsg = Utils.getReceiveXp(e.getExp());
                }
                Professions.log(String.format("%s gathered %s on spawnpoint %s".concat(expMsg), e.getPlayer().getPlayer().getName(), herb.getMarkerName(), herbLocationOptions.location), Level.CONFIG);
            }
        }
    }
}
