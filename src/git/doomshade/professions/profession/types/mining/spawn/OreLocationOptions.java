package git.doomshade.professions.profession.types.mining.spawn;

import git.doomshade.professions.profession.types.mining.Ore;
import git.doomshade.professions.profession.types.utils.LocationOptions;
import org.bukkit.Location;

public class OreLocationOptions extends LocationOptions {
    public OreLocationOptions(Location location, Ore element) {
        super(location, element);
    }

    @Override
    public String getMarkerSetId() {
        return "mining";
    }
}
