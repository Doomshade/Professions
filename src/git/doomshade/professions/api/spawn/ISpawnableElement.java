package git.doomshade.professions.api.spawn;

import org.bukkit.Location;

/**
 * A spawnable element
 *
 * @param <SpawnPointType>
 * @author Doomshade
 * @version 1.0
 */
public interface ISpawnableElement<SpawnPointType extends ISpawnPoint> extends ILocationElement {

    /**
     * @param location the location
     * @return a spawn point based on location
     */
    SpawnPointType createSpawnPoint(Location location);
}
