package git.doomshade.professions.profession.utils;

import com.google.common.collect.ImmutableMap;
import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.task.BackupTask;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages spawns of spawnable elements. This class already implements {@link LocationElement} interface.
 *
 * @param <LocOptions> the location options type (the type is useful only if you have custom class extending {@link LocationOptions})
 * @author Doomshade
 * @version 1.0
 */
public abstract class SpawnableElement<LocOptions extends LocationOptions> implements LocationElement {
    private final ArrayList<SpawnPoint> spawnPoints;
    private final HashMap<Location, LocOptions> locationOptions = new HashMap<>();


    public SpawnableElement(List<SpawnPoint> spawnPoints) {
        this.spawnPoints = new ArrayList<>(spawnPoints);
    }

    public final void addSpawnPoint(SpawnPoint sp) {
        this.spawnPoints.add(sp);
        update();
    }

    public final void removeSpawnPoint(int id) {
        if (!isSpawnPoint(id)) return;
        removeSpawnPoint(spawnPoints.get(id));
    }

    public final void removeSpawnPoint(SpawnPoint sp) {
        if (sp == null || !isSpawnPoint(sp.location)) {
            return;
        }
        final BackupTask.Result result = Professions.getInstance().backupFirst();
        if (result != null) {
            if (result == BackupTask.Result.SUCCESS)
                Professions.log(ChatColor.GREEN + "Backed up files before editing file.");
            else
                Professions.log(ChatColor.RED + "Failed to back up files. Contact admins to check console output!");
        }
        spawnPoints.remove(sp);
        getLocationOptions(sp.location).despawn();
        update();
    }

    public final void update() {
        try {
            getItemTypeHolder().save(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final LocOptions getLocationOptions(Location location) {
        if (!locationOptions.containsKey(location)) {
            locationOptions.put(location, createLocationOptions(location));
        }
        return locationOptions.get(location);
    }

    public final ImmutableMap<Location, LocOptions> getLocationOptions() {
        return ImmutableMap.copyOf(locationOptions);
    }

    protected abstract LocOptions createLocationOptions(Location location);

    /**
     * We need to save spawn points every time they are modified -  the item type holder provides {@link ItemTypeHolder#save(boolean)} method
     *
     * @return the item type holder of this class
     */
    @NotNull
    protected abstract ItemTypeHolder<?> getItemTypeHolder();

    /**
     * @param location the location to check for
     * @return {@code true} if the location is a spawn point, {@code false} otherwise
     */
    public final boolean isSpawnPoint(Location location) {
        return spawnPoints.contains(new SpawnPoint(location));
    }

    /**
     * @param id the id
     * @return {@code true} if a spawn point with that id exists, {@code false} otherwise
     */
    public final boolean isSpawnPoint(int id) {
        return spawnPoints.get(id) != null;
    }

    /**
     * @return a list of spawn points
     */
    public final List<SpawnPoint> getSpawnPoints() {
        return spawnPoints;
    }

    /**
     * Schedules a spawn on all {@link SpawnableElement}s on the server
     */
    public final void scheduleSpawns() {
        for (SpawnPoint sp : spawnPoints) {
            LocOptions locationOptions = getLocationOptions(sp.location);
            locationOptions.scheduleSpawn();

        }
    }

    /**
     * Despawns all {@link SpawnableElement}s on the server
     *
     * @param hideOnDynmap whether or not to hide a marker icon on dynmap, this boolean has no effect if the provided {@code LocOptions} is not an instance of {@link MarkableLocationOptions}
     */
    public final void despawnAll(boolean hideOnDynmap) {
        for (SpawnPoint sp : spawnPoints) {
            LocOptions locationOptions = getLocationOptions(sp.location);
            if (locationOptions instanceof MarkableLocationOptions) {
                ((MarkableLocationOptions) locationOptions).despawn(hideOnDynmap);
            } else {
                locationOptions.despawn();
            }
        }
    }

    /**
     * Despawns all {@link SpawnableElement}s on the server
     */
    public final void despawnAll() {
        despawnAll(true);
    }

    /**
     * Spawns all {@link SpawnableElement}s on the server
     */
    public final void spawnAll() {
        for (SpawnPoint sp : spawnPoints) {
            LocOptions locationOptions = getLocationOptions(sp.location);
            locationOptions.scheduleSpawn();

        }
    }
}
