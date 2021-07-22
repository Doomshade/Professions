package git.doomshade.professions.data.cache;

import git.doomshade.professions.profession.spawn.SpawnPoint;
import git.doomshade.professions.profession.spawn.SpawnTask;

import java.io.Serializable;

public class LocationOptionsCache implements Serializable {
    private final String elementId;
    private final int generatedRespawnTime, spawnPointIndex;

    public LocationOptionsCache(SpawnPoint spawnPoint) {
        this.elementId = spawnPoint.getSpawnableElement().getId();
        SpawnTask task = spawnPoint.getSpawnTask();

        this.generatedRespawnTime = task.getGeneratedRespawnTime();
        this.spawnPointIndex = task.getId();
    }

    public int getGeneratedRespawnTime() {
        return generatedRespawnTime;
    }

    public int getSpawnPointIndex() {
        return spawnPointIndex;
    }
}
