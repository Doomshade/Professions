package git.doomshade.professions.profession.spawn;

import git.doomshade.professions.api.spawn.ILocationElement;
import git.doomshade.professions.utils.ParticleData;
import org.bukkit.Material;

public abstract class LocationElement extends Element implements ILocationElement {
    private final ParticleData particleData;
    private final Material material;
    private final byte materialData;
    private final String name;


    public LocationElement(String id, String name, Material material, byte materialData, ParticleData particleData) {
        super(id);
        this.particleData = particleData;
        this.material = material;
        this.materialData = materialData;
        this.name = name;
    }

    @Override
    public final ParticleData getParticleData() {
        return particleData;
    }

    @Override
    public final Material getMaterial() {
        return material;
    }

    @Override
    public final byte getMaterialData() {
        return materialData;
    }

    @Override
    public final String getName() {
        return name;
    }
}
