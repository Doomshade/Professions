package git.doomshade.professions.profession.professions.herbalism;

import git.doomshade.professions.Professions;
import git.doomshade.professions.dynmap.MarkerManager;
import git.doomshade.professions.dynmap.MarkerWrapper;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.ITrainable;
import git.doomshade.professions.profession.types.IGathering;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.utils.SpawnPoint;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class HerbItemType extends ItemType<Herb> implements ITrainable {

    private String trainableId;
    private int cost;
    private boolean trainable;

    @Override
    public String getTrainableId() {
        return trainableId;
    }

    @Override
    public void setTrainableId(String trainableId) {
        this.trainableId = trainableId;
    }

    @Override
    public int getCost() {
        return cost;
    }

    @Override
    public void setCost(int cost) {
        this.cost = cost;
    }

    @Override
    public boolean isTrainable() {
        return trainable;
    }

    @Override
    public void setTrainable(boolean trainable) {
        this.trainable = trainable;
    }

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
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return IGathering.class;
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
