package git.doomshade.professions.api.spawn;

import git.doomshade.professions.exceptions.SpawnException;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * Data of a spawn point
 */
public interface ISpawnPoint extends ConfigurationSerializable {

    /**
     * @return the internal marker id
     */
    String getMarkerId();

    /**
     * @return the marker label on dynmap
     */
    String getMarkerLabel();

    /**
     * @return the marker icon on dynmap
     */
    String getMarkerIcon();

    /**
     * @return the marker set id on dynmap
     */
    String getMarkerSetId();

    /**
     * @return the serial number of this spawn point
     */
    int getSerialNumber();

    /**
     * @return the location of this spawn point
     */
    Location getLocation();

    /**
     * @return the spawn time range
     */
    Range getSpawnTime();

    /**
     * @return the spawnable element
     */
    ISpawnable getSpawnableElement();

    /**
     * Schedules the spawn
     *
     * @throws SpawnException if the spawn did not fucking go well
     */
    void scheduleSpawn() throws SpawnException;

    /**
     * Spawns the element
     *
     * @throws SpawnException if the spawn did not fucking go well
     */
    void spawn() throws SpawnException;

    /**
     * Forces the spawn of the element
     *
     * @throws SpawnException if the spawn did not fucking go well
     */
    void forceSpawn() throws SpawnException;

    /**
     * Despawns the element
     */
    void despawn();

    /**
     * @return {@code true} if the spawnable element was spawned, {@code false otherwise}
     */
    boolean isSpawned();

    /**
     * @return whether or not this spawn point can spawn elements
     */
    boolean isSpawnable();

    void setSpawnable(boolean canSpawn);
}
