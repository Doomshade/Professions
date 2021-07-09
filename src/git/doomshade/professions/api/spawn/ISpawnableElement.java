package git.doomshade.professions.api.spawn;

import org.bukkit.Location;

public interface ISpawnableElement<SpawnPointType extends ISpawnPoint> {
    SpawnPointType createSpawnPoint(Location location);
}
