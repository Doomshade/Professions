package git.doomshade.professions.profession.professions.mining.spawn;

import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.api.spawn.SpawnPoint;
import org.bukkit.Location;

public class OreSpawnPoint extends SpawnPoint {
    public OreSpawnPoint(Location location, Ore element) throws IllegalArgumentException {
        super(location, element);
    }
}
