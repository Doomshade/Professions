package git.doomshade.professions.profession.utils;

import git.doomshade.professions.Professions;
import git.doomshade.professions.data.cache.CacheUtils;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.task.ParticleTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Objects;
import java.util.logging.Level;

/**
 * This class stores the location of spawn point, the
 */
public class SpawnPoint {

    public static final String CACHE_FOLDER = "spawned";

    /**
     * The location
     */
    public final Location location;

    /**
     * The location element containing data about the location
     */
    public final SpawnableElement<?> element;
    /**
     * Spawn task that spawns the herb once it's ready
     */
    protected SpawnTask spawnTask;
    /**
     * Particle task that spawns particles periodically
     */
    private ParticleTask particleTask;

    private boolean spawned = false;
    /**
     * Boolean to make sure that the error does not spam console
     */
    private boolean enableSpawn = true;

    public SpawnPoint(Location location, SpawnableElement<?> element) throws IllegalArgumentException {
        this.location = location;
        this.element = element;
        if (!element.getSpawnPointLocations().contains(new SpawnPointLocation(location))) {
            throw new IllegalArgumentException("No spawn point with " + location + " exists for " + element.getName() + "(" + element.getSpawnPointLocations() + ")");
        }
        particleTask = new ParticleTask(element.getParticleData(), location);
        spawnTask = new SpawnTask(this);
    }

    public SpawnTask getSpawnTask() {
        return spawnTask;
    }

    public boolean isSpawned() {
        return spawned;
    }

    public boolean isSpawnable() {
        return true;
    }

    public void scheduleSpawn(int respawnTime, int spawnPointId) {
        try {
            Bukkit.getScheduler().cancelTask(spawnTask.getTaskId());
        } catch (IllegalStateException ignored) {
        }
        spawnTask = new SpawnTask(spawnTask, respawnTime, spawnPointId);
        spawnTask.runTaskTimer(Professions.getInstance(), 0L, 20L);
    }

    public void scheduleSpawn() {
        scheduleSpawn(SpawnTask.RANDOM_RESPAWN_TIME, SpawnTask.getSpawnPointId(this));
    }

    public void spawn() throws SpawnException {
        if (isSpawnable() && !spawned) {
            forceSpawn();
        }
    }

    @SuppressWarnings("deprecation")
    public void forceSpawn() throws SpawnException {
        if (!enableSpawn) return;

        final Material material = element.getMaterial();
        if (material == null) {
            this.enableSpawn = false;
            throw new SpawnException(new NullPointerException(), SpawnException.SpawnExceptionReason.INVALID_MATERIAL, element);
        }
        final Block block;
        if (location == null || (block = location.getBlock()) == null) {
            this.enableSpawn = false;
            throw new SpawnException(new NullPointerException(), SpawnException.SpawnExceptionReason.INVALID_LOCATION, element);
        }
        final byte materialData = element.getMaterialData();

        block.setType(material, false);
        block.setData(materialData);
        if (material == Material.DOUBLE_PLANT) {
            final Block top = location.getWorld().getBlockAt(location.clone().add(0, 1, 0));
            top.setType(material, false);
            top.setData((byte) 10);
        }

        addParticles();
        unscheduleSpawn();
        spawned = true;

        Professions.log(String.format("Spawned %s at %s", element.getName(), location), Level.CONFIG);
    }

    private void unscheduleSpawn() {
        try {
            spawnTask.cancel();
        } catch (Exception ignored) {
        }
    }

    public void despawn() {
        removeParticles();
        location.getBlock().setType(Material.AIR);
        unscheduleSpawn();
        spawned = false;

        CacheUtils.clearCache(element.getId(), CACHE_FOLDER);
        Professions.log(String.format("Despawned %s at %s", element.getName(), location), Level.CONFIG);
    }

    private void addParticles() {
        if (!particleTask.isRunning()) {
            try {
                Bukkit.getScheduler().cancelTask(particleTask.getTaskId());
            } catch (IllegalStateException ignored) {
            }
            particleTask = new ParticleTask(particleTask);
            particleTask.runTaskTimer(Professions.getInstance(), 0L, element.getParticleData().getPeriod());
        }
    }

    public final void removeParticles() {
        try {
            if (particleTask.isRunning())
                particleTask.cancel();
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpawnPoint that = (SpawnPoint) o;
        return location.equals(that.location) &&
                element.equals(that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, element);
    }
}
