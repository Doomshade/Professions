package git.doomshade.professions.dynmap;

import org.bukkit.Location;
import org.dynmap.bukkit.DynmapPlugin;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import javax.annotation.Nullable;
import java.util.HashMap;

public final class MarkerManager {

    private static MarkerManager instance = null;
    private static HashMap<String, MarkerSet> MARKERS = new HashMap<>();
    private final MarkerAPI markerApi;

    private MarkerManager(DynmapPlugin dynmapPlugin) {
        this.markerApi = dynmapPlugin.getMarkerAPI();
    }

    private static void createInstance(DynmapPlugin dynmapPlugin) {
        if (instance == null) {
            instance = new MarkerManager(dynmapPlugin);
        }
    }

    @Nullable
    public static MarkerManager getInstance() {
        return instance;
    }

    @Nullable
    public static MarkerManager getInstance(DynmapPlugin dynmapPlugin) {
        createInstance(dynmapPlugin);
        return instance;
    }

    public void register(IMarkable markable, String label) {
        final String markerSetId = markable.getMarkerSetId();
        MarkerSet set = markerApi.getMarkerSet(markerSetId);

        if (set != null) {
            return;
        }
        set = markerApi.createMarkerSet(markerSetId, label, null, true);
        set.setHideByDefault(true);
        set.setLabelShow(false);
        MARKERS.put(markerSetId, set);
    }

    public void show(IMarkable markable) {
        MarkerSet set = MARKERS.get(markable.getMarkerSetId());
        if (set == null) {
            return;
        }

        final MarkerWrapper markerWrapper = markable.getMarker();
        if (markerWrapper == null) {
            return;
        }

        final Location location = markerWrapper.location;
        set.createMarker(markerWrapper.id, markerWrapper.getLabel(), location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), markerApi.getMarkerIcon(markerWrapper.markerIcon), false);
    }

    public void hide(IMarkable markable) {
        MarkerSet set = MARKERS.get(markable.getMarkerSetId());
        if (set == null) {
            return;
        }

        final MarkerWrapper markerWrapper = markable.getMarker();
        if (markerWrapper == null) return;
        final Marker marker = set.findMarker(markerWrapper.id);
        if (marker != null) marker.deleteMarker();
    }
}
