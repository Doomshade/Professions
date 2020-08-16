package git.doomshade.professions.profession.professions.herbalism;

import git.doomshade.professions.Professions;
import git.doomshade.professions.dynmap.MarkerManager;
import git.doomshade.professions.dynmap.MarkerWrapper;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.utils.SpawnPoint;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class HerbItemType extends ItemType<Herb> {

    /**
     * Constructor for creation of the item type object
     *
     * @param object
     */
    public HerbItemType(Herb object) {
        super(object);
    }

    @Override
    public Map<String, Object> getSerializedObject() {
        if (getObject() != null) {
            return getObject().serialize();
        }
        return new HashMap<>();
    }

    @Override
    protected Herb deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        return Herb.deserialize(map);
    }

    @Override
    public void onLoad() {

        for (Herb herb : Herb.HERBS.values()) {
            herb.scheduleSpawns();
        }

        Herb herb = getObject();

        if (herb == null) {
            return;
        }

        final String name = getName();

        MarkerManager markMan = Professions.getMarkerManager();
        if (markMan != null) {
            Location exampleLocation = null;
            for (Map.Entry<Location, HerbLocationOptions> entry : herb.getLocationOptions().entrySet()) {
                final MarkerWrapper marker = entry.getValue().getMarker();
                if (exampleLocation == null) {
                    exampleLocation = entry.getKey();
                }
                if (marker != null)
                    marker.setLabel(name.isEmpty() ? "Herb" : ChatColor.stripColor(name));
            }
            markMan.register(new HerbLocationOptions(exampleLocation, getObject()), "Herbalism");
        }
    }


    @Override
    public void onDisable() {
        for (Herb herb : Herb.HERBS.values()) {
            herb.despawnAll(true);
        }
        Herb.HERBS.clear();
        SpawnPoint.SPAWN_POINTS.clear();
    }
}
