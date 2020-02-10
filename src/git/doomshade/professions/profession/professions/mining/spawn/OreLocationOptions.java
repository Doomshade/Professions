package git.doomshade.professions.profession.professions.mining.spawn;

import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.profession.utils.LocationOptions;
import org.bukkit.Location;

public class OreLocationOptions extends LocationOptions {
    public OreLocationOptions(Location location, Ore element) throws IllegalArgumentException {
        super(location, element);
    }
}
