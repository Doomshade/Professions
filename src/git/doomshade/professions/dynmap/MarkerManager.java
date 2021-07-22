package git.doomshade.professions.dynmap;

import git.doomshade.professions.api.spawn.ISpawnPoint;
import org.bukkit.Location;
import org.dynmap.bukkit.DynmapPlugin;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The marker manager that displays icons (herbs only currently) on dynmap
 *
 * @author Doomshade
 * @version 1.0
 */
public final class MarkerManager {

    private static final Map<String, MarkerSet> MARKERS = new HashMap<>();
    public static String EMPTY_MARKER_SET_ID = "";
    private static MarkerManager instance = null;
    private final MarkerAPI markerApi;

    private MarkerManager(DynmapPlugin dynmapPlugin) {
        this.markerApi = dynmapPlugin.getMarkerAPI();
    }

    public static MarkerManager getInstance() {
        return instance;
    }

    public static void createInstance(DynmapPlugin dynmapPlugin) {
        if (instance == null) {
            instance = new MarkerManager(dynmapPlugin);
        }
    }

    /**
     * Registers an icon on dynmap
     *
     * @param exampleSpawnPoint the example spawn point
     */
    public void register(ISpawnPoint exampleSpawnPoint, String globalLabel) {
        final String id = exampleSpawnPoint.getMarkerSetId();
        if (id.equalsIgnoreCase(EMPTY_MARKER_SET_ID)) {
            return;
        }

        MarkerSet set = markerApi.getMarkerSet(id);
        if (set == null) {
            set = markerApi.createMarkerSet(id, globalLabel,
                    null, true);
        }
        set.setHideByDefault(true);
        set.setLabelShow(false);

        MARKERS.put(id, set);
    }

    /**
     * Shows the icon on dynmap
     *
     * @param spawnPoint the icon
     */
    public void show(ISpawnPoint spawnPoint) {
        final String id = spawnPoint.getMarkerSetId();
        if (id.equalsIgnoreCase(EMPTY_MARKER_SET_ID)) {
            return;
        }

        MarkerSet set = MARKERS.get(id);
        if (set == null) {
            return;
        }

        final Location location = spawnPoint.getLocation();
        set.createMarker(spawnPoint.getMarkerId(), spawnPoint.getMarkerLabel(),
                Objects.requireNonNull(location.getWorld()).getName(), location.getX(), location.getY(),
                location.getZ(), markerApi.getMarkerIcon(spawnPoint.getMarkerIcon()), false);
    }

    /**
     * Hides the icon on dynmap
     *
     * @param spawn the icon
     */
    public void hide(ISpawnPoint spawn) {
        final String id = spawn.getMarkerSetId();
        if (id.equalsIgnoreCase(EMPTY_MARKER_SET_ID)) {
            return;
        }

        MarkerSet set = MARKERS.get(id);
        if (set == null) {
            return;
        }

        final Marker marker = set.findMarker(spawn.getMarkerId());
        if (marker != null) {
            marker.deleteMarker();
        }
    }
}
