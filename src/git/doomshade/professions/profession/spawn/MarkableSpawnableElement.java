package git.doomshade.professions.profession.spawn;

import git.doomshade.professions.api.spawn.IMarkableSpawnableElement;
import git.doomshade.professions.profession.utils.ExtendedLocation;
import git.doomshade.professions.api.spawn.IMarkableLocationElement;
import git.doomshade.professions.utils.ParticleData;
import org.bukkit.Material;

import java.util.List;

public abstract class MarkableSpawnableElement<SpawnPointType extends SpawnPoint>
        extends SpawnableElement<SpawnPointType> implements IMarkableLocationElement, IMarkableSpawnableElement<SpawnPointType> {
    private final String markerIcon;

    protected MarkableSpawnableElement(String id, String name, Material material, byte materialData, List<ExtendedLocation> spawnPointLocations, ParticleData particleData, String markerIcon) {
        super(id, name, material, materialData, spawnPointLocations, particleData);
        this.markerIcon = markerIcon;
    }

    @Override
    public final String getMarkerIcon() {
        return markerIcon;
    }
}
