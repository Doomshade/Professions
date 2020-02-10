package git.doomshade.professions.profession.types.utils;

import git.doomshade.professions.Professions;
import git.doomshade.professions.task.ParticleTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Objects;
import java.util.logging.Level;

public abstract class LocationOptions {

    public final Location location;
    public final LocationElement element;
    private ParticleTask particleTask;
    protected SpawnTask spawnTask;
    private boolean spawned = false;

    public LocationOptions(Location location, LocationElement element) throws IllegalArgumentException {
        this.location = location;
        this.element = element;
        if (!element.getSpawnPoints().contains(new SpawnPoint(location))) {
            throw new IllegalArgumentException("No spawn point with that location exists for " + element.getName());
        }
        particleTask = new ParticleTask(element.getParticleData(), location);
        spawnTask = new SpawnTask(this);
    }

    public boolean isSpawned() {
        return spawned;
    }

    public boolean isSpawnable() {
        return true;
    }

    public void scheduleSpawn() {
        try {
            Bukkit.getScheduler().cancelTask(spawnTask.getTaskId());
        } catch (IllegalStateException ignored) {
        }
        spawnTask = new SpawnTask(spawnTask);
        spawnTask.runTaskTimer(Professions.getInstance(), 0L, 20L);
    }

    public void spawn() {
        if (isSpawnable() && !spawned) {
            forceSpawn();
        }
    }

    public void forceSpawn() {
        addParticles();
        location.getBlock().setType(element.getMaterial());
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
        LocationOptions that = (LocationOptions) o;
        return location.equals(that.location) &&
                element.equals(that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, element);
    }
}
