package git.doomshade.professions.profession.professions.herbalism;

import git.doomshade.professions.profession.utils.MarkableLocationOptions;
import org.bukkit.Location;

public class HerbLocationOptions extends MarkableLocationOptions {

    HerbLocationOptions(Location location, Herb herb) {
        super(location, herb);
    }

    @Override
    public boolean isSpawnable() {
        return ((Herb) element).isSpawnEnabled();
    }

    @Override
    public String toString() {
        return "HerbLocationOptions{" +
                "location=" + location +
                ", herb=" + element +
                '}';
    }

    @Override
    public String getMarkerSetId() {
        return "herbalism";
    }
}
