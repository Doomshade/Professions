package git.doomshade.professions.api.spawn;

import git.doomshade.professions.api.IParticleData;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * A location element containing an ID and needed information about a {@link Location}
 *
 * @author Doomshade
 * @version 1.0
 */
public interface LocationElement extends Element {

    /**
     * @return the particle data about this location element if there should be particles played, {@code null} otherwise
     */
    IParticleData getParticleData();

    /**
     * @return the material of the location's block
     */
    Material getMaterial();

    /**
     * @return the material data because of special blocks suck as flowers
     */
    byte getMaterialData();

    /**
     * @return the name of the element
     */
    String getName();

    boolean isSpawnPointLocation(Location location);

    /**
     * @return the spawn points of this element
     */
    /*List<SpawnPointLocation> getSpawnPointLocations();*/

}
