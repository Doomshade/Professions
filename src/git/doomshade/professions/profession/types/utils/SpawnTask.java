package git.doomshade.professions.profession.types.utils;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class SpawnTask extends BukkitRunnable {
    public final LocationOptions locationOptions;
    private transient int respawnTime;
    public transient int id = -1;

    public SpawnTask(LocationOptions locationOptions) throws IllegalArgumentException {
        this.locationOptions = locationOptions;
        SpawnPoint example = new SpawnPoint(locationOptions.location);

        final List<SpawnPoint> spawnPoints = locationOptions.element.getSpawnPoints();
        for (int i = 0; i < spawnPoints.size(); i++) {
            SpawnPoint sp = spawnPoints.get(i);
            if (sp.equals(example)) {
                this.id = i;
                this.respawnTime = sp.respawnTime.getRandom() + 1;
                return;
            }
        }
        throw new IllegalArgumentException("No spawn point exists with location " + locationOptions.location + "!");
    }

    public SpawnTask(SpawnTask copy) {
        this(copy.locationOptions);
    }

    @Override
    public void run() {
        if (respawnTime <= 0) {
            locationOptions.spawn();
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
