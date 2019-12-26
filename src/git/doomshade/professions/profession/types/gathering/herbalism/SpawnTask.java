package git.doomshade.professions.profession.types.gathering.herbalism;

import org.bukkit.scheduler.BukkitRunnable;

class SpawnTask extends BukkitRunnable {
    public final HerbLocationOptions herb;
    public int respawnTime;

    SpawnTask(HerbLocationOptions herb) throws IllegalArgumentException {
        this.herb = herb;
        SpawnPoint example = new SpawnPoint(herb.location);

        for (SpawnPoint sp : SpawnPoint.SPAWN_POINTS) {
            if (sp.equals(example)) {
                this.respawnTime = sp.respawnTime.getRandom();
                return;
            }
        }
        throw new IllegalArgumentException("No spawn point exists with location " + herb.location + "!");
    }

    SpawnTask(SpawnTask copy) {
        this(copy.herb);
    }

    @Override
    public void run() {
        if (respawnTime <= 0) {
            herb.spawn();
            cancel();
            return;
        }
        respawnTime--;
    }
}
