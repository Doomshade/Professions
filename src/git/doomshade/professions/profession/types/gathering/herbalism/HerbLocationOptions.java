package git.doomshade.professions.profession.types.gathering.herbalism;

import git.doomshade.professions.Professions;
import git.doomshade.professions.task.ParticleTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Objects;
import java.util.logging.Level;

public class HerbLocationOptions {
    public final Location location;
    private final Herb herb;
    private SpawnTask spawnTask;
    private ParticleTask particleTask;
    private boolean spawned = false;

    HerbLocationOptions(Location location, Herb herb) {
        this.location = location;
        this.herb = herb;

        particleTask = new ParticleTask(herb.getParticleData(), location);
        spawnTask = new SpawnTask(this);
    }

    public boolean isSpawned() {
        return spawned;
    }

    public void spawn() {
        if (!herb.isSpawnEnabled()) {
            return;
        }
        forceSpawn();
    }


    public void forceSpawn() {
        addParticles();
        location.getBlock().setType(herb.getHerbMaterial());
        unscheduleSpawn();
        spawned = true;
        Professions.log(String.format("Herb %s spawned at %s", herb.getName(), location), Level.CONFIG);
    }

    public void despawn() {
        removeParticles();
        location.getBlock().setType(Material.AIR);
        unscheduleSpawn();
        spawned = false;
        Professions.log(String.format("Herb %s despawned at %s", herb.getName(), location), Level.CONFIG);
    }

    public void scheduleSpawn() throws IllegalArgumentException {
        try {
            Bukkit.getScheduler().cancelTask(spawnTask.getTaskId());
        } catch (IllegalStateException ignored) {
        }
        spawnTask = new SpawnTask(spawnTask);
        spawnTask.runTaskTimer(Professions.getInstance(), 20L, 20L);
    }

    private void unscheduleSpawn() {
        try {
            spawnTask.cancel();
        } catch (Exception ignored) {
        }
    }


    private void addParticles() {
        if (!particleTask.isRunning()) {
            try {
                Bukkit.getScheduler().cancelTask(particleTask.getTaskId());
            } catch (IllegalStateException ignored) {
            }
            particleTask = new ParticleTask(particleTask);
            particleTask.runTaskTimer(Professions.getInstance(), 0L, herb.getParticleData().getPeriod());
        }
    }

    public void removeParticles() {
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
        HerbLocationOptions that = (HerbLocationOptions) o;
        return location.equals(that.location) &&
                herb.equals(that.herb);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, herb);
    }

    @Override
    public String toString() {
        return "HerbLocationOptions{" +
                "location=" + location +
                ", herb=" + herb +
                '}';
    }
}
