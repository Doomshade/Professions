package git.doomshade.professions.profession.utils;

import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.SpawnException;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class SpawnTask extends BukkitRunnable {
    public final LocationOptions locationOptions;
    private transient int respawnTime;
    public transient int id = -1;
    private transient final int generatedRespawnTime;

    public static final int RANDOM_RESPAWN_TIME = -1;

    public int getGeneratedRespawnTime() {
        return generatedRespawnTime;
    }

    public int getRespawnTime() {
        return respawnTime;
    }

    public static int getSpawnTaskIdFromSpawnPoint(LocationOptions options) {
        final List<SpawnPoint> spawnPoints = options.element.getSpawnPoints();
        SpawnPoint example = new SpawnPoint(options.location);
        for (int i = 0; i < spawnPoints.size(); i++) {
            SpawnPoint sp = spawnPoints.get(i);
            if (sp.equals(example)) {
                return i;
            }
        }
        return -1;
    }

    public SpawnTask(LocationOptions locationOptions, int respawnTime, int id) {
        this.locationOptions = locationOptions;
        SpawnPoint example = new SpawnPoint(locationOptions.location);

        final SpawnPoint sp = locationOptions.element.getSpawnPoints().get(id);
        if (sp.equals(example)) {
            this.id = id;
            this.respawnTime = respawnTime >= 0 ? respawnTime : sp.respawnTime.getRandom() + 1;
            this.generatedRespawnTime = respawnTime;
            return;
        }
        throw new IllegalArgumentException("No spawn point exists with location " + locationOptions.location + "!");
    }

    public SpawnTask(LocationOptions locationOptions) throws IllegalArgumentException {
        this(locationOptions, RANDOM_RESPAWN_TIME, getSpawnTaskIdFromSpawnPoint(locationOptions));
    }

    public SpawnTask(SpawnTask copy) {
        this(copy, RANDOM_RESPAWN_TIME, getSpawnTaskIdFromSpawnPoint(copy.locationOptions));
    }

    public SpawnTask(SpawnTask copy, int respawnTime, int id) {
        this(copy.locationOptions, respawnTime, id);
    }

    @Override
    public void run() {
        if (respawnTime <= 0) {
            try {
                locationOptions.spawn();
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
