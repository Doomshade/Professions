package git.doomshade.professions.profession.utils;

import git.doomshade.professions.Professions;
import git.doomshade.professions.dynmap.IMarkable;
import git.doomshade.professions.dynmap.MarkerManager;
import git.doomshade.professions.dynmap.MarkerWrapper;
import org.bukkit.Location;

public abstract class MarkableLocationOptions extends LocationOptions implements IMarkable {

    private static final MarkerManager MARKER_MANAGER = Professions.getMarkerManager();
    private final MarkerWrapper marker;

    public MarkableLocationOptions(Location location, MarkableLocationElement element) throws IllegalArgumentException {
        super(location, element);
        final int id = spawnTask.id;
        this.marker = new MarkerWrapper(element.getId().concat("-").concat(String.valueOf(id)), element.getMarkerIcon(), location);
    }

    @Override
    public void forceSpawn() {
        super.forceSpawn();
        if (MARKER_MANAGER != null)
            MARKER_MANAGER.show(this);
    }

    @Override
    public void despawn() {
        super.despawn();
        if (MARKER_MANAGER != null)
            MARKER_MANAGER.hide(this);
    }

    @Override
    public final MarkerWrapper getMarker() {
        return marker;
    }
}
