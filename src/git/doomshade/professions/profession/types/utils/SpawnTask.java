package git.doomshade.professions.profession.types.utils;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class SpawnTask extends BukkitRunnable {
    public final LocationOptions locationOptions;
    private transient int respawnTime;
    public transient int id = -1;

    public SpawnTask(LocationOptions locationOptions) throws IllegalArgumentException {
        this.locationOptions = locationOptions;
        SpawnPoint example = new SpawnPoint(locationOptions.location);

        final ArrayList<SpawnPoint> spawnPoints = locationOptions.element.getSpawnPoints();
        for (int i = 0; i < spawnPoints.size(); i++) {
            SpawnPoint sp = spawnPoints.get(i);
            if (sp.equals(example)) {
                this.id = i;
                this.respawnTime = sp.respawnTime.getRandom();
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
}
