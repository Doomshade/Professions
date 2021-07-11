package git.doomshade.professions.profession.spawn;

import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.utils.ExtendedLocation;
import git.doomshade.professions.task.ParticleTask;
import git.doomshade.professions.utils.ExtendedBukkitRunnable;
import org.bukkit.Bukkit;

import java.util.List;

public class SpawnTask extends ExtendedBukkitRunnable {

    /**
     * Particle task that spawns particles periodically
     */
    private ParticleTask particleTask;
    private final SpawnPoint spawnPoint;
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
        final List<ExtendedLocation> spawnPointLocations = options.element.getSpawnPointLocations();
        ExtendedLocation example = new ExtendedLocation(options.location);
        for (int i = 0; i < spawnPointLocations.size(); i++) {
            ExtendedLocation sp = spawnPointLocations.get(i);
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
        ExtendedLocation example = new ExtendedLocation(spawnPoint.location);

        final ExtendedLocation el = spawnPoint.element.getSpawnPointLocations().get(id);
        if (el.equals(example)) {
            this.id = id;

            // TODO: 09.02.2021 duplicate spawn tasks 
            ProfessionLogger.log("New Spawn Task for " + spawnPoint.location + " with ID" + id);
            this.respawnTime = respawnTime >= 0 ? respawnTime : el.getRespawnTime().getRandom() + 1;
            this.generatedRespawnTime = respawnTime;
            this.particleTask = new ParticleTask(spawnPoint.element.getParticleData(), spawnPoint.location);
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
    protected long delay() {
        return 0;
    }

    @Override
    protected long period() {
        return 20L;
    }

    @Override
    protected void onStart() {
        addParticles();
    }

    @Override
    protected void onCancel() {
        removeParticles();
    }

    private void removeParticles() throws IllegalStateException {
        if (particleTask.isRunning())
            particleTask.cancel();
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
