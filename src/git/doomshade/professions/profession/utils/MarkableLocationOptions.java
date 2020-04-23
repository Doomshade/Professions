package git.doomshade.professions.profession.utils;

import git.doomshade.professions.Professions;
import git.doomshade.professions.dynmap.IMarkable;
import git.doomshade.professions.dynmap.MarkerManager;
import git.doomshade.professions.dynmap.MarkerWrapper;
import git.doomshade.professions.exceptions.SpawnException;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public abstract class MarkableLocationOptions extends LocationOptions implements IMarkable {

    private static final MarkerManager MARKER_MANAGER = Professions.getMarkerManager();
    private final MarkerWrapper marker;

    public MarkableLocationOptions(Location location, MarkableLocationElement element) throws IllegalArgumentException {
        super(location, element);
        final int id = spawnTask.id;
        this.marker = new MarkerWrapper(element.getId().concat("-").concat(String.valueOf(id)), element.getMarkerIcon(), location);
        marker.setLabel(ChatColor.stripColor(element.getName()));
    }

    @Override
    public void forceSpawn() throws SpawnException {
        super.forceSpawn();
        setMarkerVisible(true);
    }

    public void setMarkerVisible(boolean visible) {
        if (MARKER_MANAGER == null) return;

        if (visible) {
            MARKER_MANAGER.show(this);
        } else {
            MARKER_MANAGER.hide(this);
        }
    }

    public void despawn(boolean hideOnDynmap) {
        super.despawn();
        if (hideOnDynmap) {
            setMarkerVisible(false);
        }
    }

    @Override
    public final MarkerWrapper getMarker() {
        return marker;
    }
}
