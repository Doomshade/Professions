/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
 * @since 1.0
 */
public final class MarkerManager {

    private static final Map<String, MarkerSet> MARKERS = new HashMap<>();
    private static final String EMPTY_MARKER_SET_ID = "";
    private static MarkerManager instance = null;
    private final MarkerAPI markerApi;

    private MarkerManager(DynmapPlugin dynmapPlugin) {
        this.markerApi = dynmapPlugin.getMarkerAPI();
    }

    public static MarkerManager getInstance() {
        return instance;
    }

    public static void createInstance(DynmapPlugin dynmapPlugin) {
        if (dynmapPlugin == null) {
            return;
        }
        if (instance == null) {
            instance = new MarkerManager(dynmapPlugin);
        }
    }

    public static String getEmptyMarkerSetId() {
        return EMPTY_MARKER_SET_ID;
    }

    /**
     * Registers an icon on dynmap
     *
     * @param exampleSpawnPoint the example spawn point
     * @param globalLabel       the global marker label
     */
    public void register(ISpawnPoint exampleSpawnPoint, String globalLabel) {
        final String id = exampleSpawnPoint.getMarkerSetId();
        if (id.equalsIgnoreCase(getEmptyMarkerSetId())) {
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
        if (id.equalsIgnoreCase(getEmptyMarkerSetId())) {
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
        if (id.equalsIgnoreCase(getEmptyMarkerSetId())) {
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
