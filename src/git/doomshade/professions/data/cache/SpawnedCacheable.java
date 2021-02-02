package git.doomshade.professions.data.cache;

import git.doomshade.professions.profession.utils.LocationOptions;
import git.doomshade.professions.profession.utils.SpawnTask;

public class SpawnedCacheable extends Cacheable {
    private final String elementId;
    private final int generatedRespawnTime, respawnTime, spawnPointIndex;

    public SpawnedCacheable(LocationOptions locationOptions) {
        this.elementId = locationOptions.element.getId();
        SpawnTask task = locationOptions.getSpawnTask();

        this.generatedRespawnTime = task.getGeneratedRespawnTime();
        this.respawnTime = task.getRespawnTime();
        this.spawnPointIndex = task.id;
    }

    public int getGeneratedRespawnTime() {
        return generatedRespawnTime;
    }

    public int getCurrentRespawnTime() {
        return respawnTime;
    }

    public int getSpawnPointIndex() {
        return spawnPointIndex;
    }

    @Override
    public String getId() {
        return elementId;
    }
}
