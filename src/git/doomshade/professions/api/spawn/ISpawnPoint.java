package git.doomshade.professions.api.spawn;

import git.doomshade.professions.exceptions.SpawnException;
import org.bukkit.Location;

public interface ISpawnPoint {

    /**
     * @return the element that's supposed to spawn
     */
    ISpawnableElement<?> getSpawnableElement();

    /**
     * @return the location of this spawn point
     */
    Location getLocation();

    /**
     * @return {@code true} if the spawnable element was spawned, {@code false otherwise}
     */
    boolean isSpawned();

    /**
     * @return whether or not this spawn point can spawn elements
     */
    default boolean isSpawnable() {
        return true;
    }

    /**
     * Schedules the spawn
     */
    void scheduleSpawn();

    void spawn() throws SpawnException;

    void despawn();
}
