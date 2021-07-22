package git.doomshade.professions.profession.spawn;

import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.task.ParticleTask;
import git.doomshade.professions.utils.ExtendedBukkitRunnable;
import org.bukkit.Bukkit;

public class SpawnTask extends ExtendedBukkitRunnable {

    private final SpawnPoint spawnPoint;
    // because of cache
    private transient final int generatedRespawnTime;
    private transient int respawnTime;
    transient int id = -1;
    /**
     * Particle task that spawns particles periodically
     */
    private ParticleTask particleTask;

    SpawnTask(SpawnPoint spawnPoint) {
        this.spawnPoint = spawnPoint;
        this.generatedRespawnTime = spawnPoint.getSpawnTime().getRandom();
    }

    public int getGeneratedRespawnTime() {
        return generatedRespawnTime;
    }

    public int getRespawnTime() {
        return respawnTime;
    }

    public int getId() {
        return id;
    }

    @Override
    public void run() {
        if (spawnPoint.isSpawned()) {
            return;
        }
        if (respawnTime <= 0) {
            try {
                spawnPoint.spawn();
            } catch (SpawnException e) {
                ProfessionLogger.logError(e);
            } finally {
                // spawn does cancel this task, however if an exception happened we need to
                // cancel it here
                if (isRunning()) {
                    cancel();
                }
            }
            return;
        }
        respawnTime--;
    }

    @Override
    protected void onStart() {
        addParticles();
    }

    @Override
    protected void onCancel() {
        removeParticles();
    }

    @Override
    protected long delay() {
        return 0;
    }

    @Override
    protected long period() {
        return 20L;
    }

    private void removeParticles() throws IllegalStateException {
        if (particleTask.isRunning()) {
            particleTask.cancel();
        }
    }

    private void addParticles() {
        if (!particleTask.isRunning()) {
            try {
                Bukkit.getScheduler().cancelTask(particleTask.getTaskId());
            } catch (IllegalStateException ignored) {
            }
            particleTask = new ParticleTask(particleTask);
            particleTask.startTask();
        }
    }
}
