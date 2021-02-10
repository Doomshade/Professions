package git.doomshade.professions.profession.professions.herbalism;

import git.doomshade.professions.Professions;
import git.doomshade.professions.data.cache.LocationOptionsCache;
import git.doomshade.professions.dynmap.MarkerManager;
import git.doomshade.professions.dynmap.MarkerWrapper;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.utils.SpawnPointLocation;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HerbItemType extends ItemType<Herb> {

    public static final String CACHE_FOLDER = "herb";

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

        /*for (Herb herb : Herb.HERBS.values()) {
            try {
                Collection<LocationOptionsCache> cachedHerbs = CacheUtils.readCache(herb.getId(), CACHE_FOLDER);

                boolean found = false;
                for (final LocationOptionsCache c : cachedHerbs) {
                    if (herb.getId().equals(c.getData())) {
                        herb.scheduleSpawns(c.getCurrentRespawnTime());
                        found = true;
                        break;
                    }
                }

                // not cached
                if (!found) {
                    herb.scheduleSpawns();
                }
            } catch (IOException e) {
                Professions.logError(e);
                herb.scheduleSpawns();
            }
            // herb.scheduleSpawns();
        }*/

        Herb herb = getObject();

        if (herb == null) {
            return;
        }

        final String name = getName();

        MarkerManager markMan = Professions.getMarkerManager();
        if (markMan != null) {
            Location exampleLocation = null;
            for (Map.Entry<Location, HerbSpawnPoint> entry : herb.getSpawnPoints().entrySet()) {
                final MarkerWrapper marker = entry.getValue().getMarker();
                if (exampleLocation == null) {
                    exampleLocation = entry.getKey();
                }
                if (marker != null)
                    marker.setLabel(name.isEmpty() ? "Herb" : ChatColor.stripColor(name));
            }
            if (exampleLocation != null)
                markMan.register(herb.getSpawnPoints(exampleLocation), "Herbalism");
        }
    }


    @Override
    public void onDisable() {
        for (Herb herb : Herb.HERBS.values()) {
            Collection<LocationOptionsCache> cache = new ArrayList<>();
            for (Map.Entry<Location, HerbSpawnPoint> entry : herb.getSpawnPoints().entrySet()) {
                HerbSpawnPoint hlo = entry.getValue();
                cache.add(new LocationOptionsCache(hlo));
            }
            /*try {
                CacheUtils.cache(cache, herb.getId(), CACHE_FOLDER);
            } catch (IOException e) {
                Professions.logError(e);
            }*/
            herb.despawnAll(true);
        }
        Herb.HERBS.clear();
        SpawnPointLocation.SPAWN_POINTS.clear();
    }
}
