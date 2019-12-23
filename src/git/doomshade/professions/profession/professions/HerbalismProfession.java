package git.doomshade.professions.profession.professions;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.gathering.IGathering;
import git.doomshade.professions.profession.types.gathering.herbalism.Herb;
import git.doomshade.professions.profession.types.gathering.herbalism.HerbItemType;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;

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
        if (addExp(e) && event.hasExtra(Location.class)) {
            final Herb herb = event.getItemType().getObject();
            if (herb != null) {
                herb.despawn(event.getExtra(Location.class));
            }
        }
    }
}
