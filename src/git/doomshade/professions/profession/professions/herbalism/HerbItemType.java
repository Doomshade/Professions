package git.doomshade.professions.profession.professions.herbalism;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.spawn.ISpawnPoint;
import git.doomshade.professions.dynmap.MarkerManager;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;

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
    public void onPluginEnable() {

        Herb herb = getObject();
        if (herb == null) {
            return;
        }

        MarkerManager markMan = Professions.getMarkerManager();
        if (markMan == null) {
            return;
        }
        ISpawnPoint exampleLocation = herb.getSpawnPoints().iterator().next();
        if (exampleLocation != null) {
            markMan.register(exampleLocation, "Herbalism");
        }

    }
}
