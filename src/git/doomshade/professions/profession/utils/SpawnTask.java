package git.doomshade.professions.profession.utils;

import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.SpawnException;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class SpawnTask extends BukkitRunnable {

    public final SpawnPoint spawnPoint;
    private transient int respawnTime;
    public transient int id = -1;

    // because of cache
    private transient final int generatedRespawnTime;

    public static final int RANDOM_RESPAWN_TIME = -1;

    public int getGeneratedRespawnTime() {
        return generatedRespawnTime;
    }

    public int getRespawnTime() {
        return respawnTime;
    }

    /**
     * @param options the location options
     * @return the spawn point ID from given location options if exists, otherwise -1
     */
    public static int getSpawnPointId(SpawnPoint options) {
        final List<SpawnPointLocation> spawnPointLocations = options.element.getSpawnPointLocations();
        SpawnPointLocation example = new SpawnPointLocation(options.location);
        for (int i = 0; i < spawnPointLocations.size(); i++) {
            SpawnPointLocation sp = spawnPointLocations.get(i);
            if (sp.equals(example)) {
                return i;
            }
        }
        return -1;
    }

    SpawnTask(SpawnPoint spawnPoint, int respawnTime, int id) {
        IllegalArgumentException e = new IllegalArgumentException("No spawn point exists with location " + spawnPoint.location + "!");
        if (id < 0) throw e;
        this.spawnPoint = spawnPoint;
        SpawnPointLocation example = new SpawnPointLocation(spawnPoint.location);

        final SpawnPointLocation sp = spawnPoint.element.getSpawnPointLocations().get(id);
        if (sp.equals(example)) {
            this.id = id;

            // TODO: 09.02.2021 duplicate spawn tasks 
            Professions.log("New Spawn Task for " + spawnPoint.location + " with ID" + id);
            this.respawnTime = respawnTime >= 0 ? respawnTime : sp.respawnTime.getRandom() + 1;
            this.generatedRespawnTime = respawnTime;
            return;
        }
        throw e;
    }

    SpawnTask(SpawnPoint spawnPoint) throws IllegalArgumentException {
        this(spawnPoint, RANDOM_RESPAWN_TIME, getSpawnPointId(spawnPoint));
    }

    SpawnTask(SpawnTask copy) {
        this(copy, RANDOM_RESPAWN_TIME, getSpawnPointId(copy.spawnPoint));
    }

    SpawnTask(SpawnTask copy, int respawnTime, int id) {
        this(copy.spawnPoint, respawnTime, id);
    }

    @Override
    public void run() {
        if (respawnTime <= 0) {
            try {
                spawnPoint.spawn();
            } catch (SpawnException e) {
                Professions.logError(e);
                cancel();
                return;
            }
            cancel();
            return;
        }
        respawnTime--;
    }

    @Override
    public synchronized BukkitTask runTaskTimer(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        return super.runTaskTimer(plugin, 0L, 20L);
    }
}
