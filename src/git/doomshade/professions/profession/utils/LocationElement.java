package git.doomshade.professions.profession.utils;

import git.doomshade.professions.utils.ParticleData;
import org.bukkit.Material;

import java.util.List;

public interface LocationElement extends Element {

    ParticleData getParticleData();

    Material getMaterial();

    byte getMaterialData();

    String getName();

    List<SpawnPoint> getSpawnPoints();
}
