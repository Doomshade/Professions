package git.doomshade.professions.api.spawn;

import org.bukkit.Location;

/**
 * @param <SpawnPointType>
 */
public interface ISpawnableElement<SpawnPointType extends ISpawnPoint> extends LocationElement {

    /**
     * @param location the location
     * @return a spawn point based on location
     */
    SpawnPointType createSpawnPoint(Location location);
}
