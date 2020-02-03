package git.doomshade.professions.profession.types.utils;

import git.doomshade.professions.utils.ParticleData;
import org.bukkit.Material;

import java.util.ArrayList;

public interface LocationElement extends Element {

    ParticleData getParticleData();

    Material getMaterial();

    String getName();

    ArrayList<SpawnPoint> getSpawnPoints();
}
