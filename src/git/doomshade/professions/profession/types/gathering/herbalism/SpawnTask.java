package git.doomshade.professions.profession.types.gathering.herbalism;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;

class SpawnTask extends BukkitRunnable {
    private static final HashSet<Herb> SPAWNING_HERBS = new HashSet<>();
    private final SpawnPoint spawnPoint;
    private final Herb herb;
    private int respawnTime;

    SpawnTask(Herb herb, Location spawnPoint) throws IllegalArgumentException {
        this.herb = herb;
        SpawnPoint example = new SpawnPoint(spawnPoint, 0);

        for (SpawnPoint sp : SpawnPoint.SPAWN_POINTS) {
            if (sp.equals(example)) {
                this.spawnPoint = sp;
                this.respawnTime = sp.respawnTime;
                SPAWNING_HERBS.add(herb);
                break;
            }
        }
        throw new IllegalArgumentException();
    }

    static boolean isSpawning(Herb herb) {
        return SPAWNING_HERBS.contains(herb);
    }

    @Override
    public void run() {
        if (respawnTime <= 0) {
            SPAWNING_HERBS.remove(herb);
            herb.spawn(spawnPoint.location);
            cancel();
            return;
        }
        respawnTime--;
    }


}
