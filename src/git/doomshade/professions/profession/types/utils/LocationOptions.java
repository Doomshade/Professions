package git.doomshade.professions.profession.types.utils;

import git.doomshade.professions.Professions;
import git.doomshade.professions.dynmap.IMarkable;
import git.doomshade.professions.dynmap.MarkerWrapper;
import git.doomshade.professions.task.ParticleTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Objects;
import java.util.logging.Level;

public abstract class LocationOptions implements IMarkable {

    public final Location location;
    public final LocationElement element;
    private final MarkerWrapper marker;
    private ParticleTask particleTask;
    private SpawnTask spawnTask;
    private boolean spawned = false;

    public LocationOptions(Location location, LocationElement element) {
        this.location = location;
        this.element = element;
        particleTask = new ParticleTask(element.getParticleData(), location);
        spawnTask = new SpawnTask(this);
        final int id = spawnTask.id;
        if (id != -1 && element instanceof MarkableLocationElement) {
            this.marker = new MarkerWrapper(element.getId().concat("-").concat(String.valueOf(id)), ((MarkableLocationElement) element).getMarkerIcon(), location);
        } else {
            marker = null;
        }
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
        spawnTask.runTaskTimer(Professions.getInstance(), 20L, 20L);
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
        if (marker != null) {
            Professions.getMarkerManager().show(this);
        }
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
        if (marker != null) {
            Professions.getMarkerManager().hide(this);
        }
        spawned = false;
        Professions.log(String.format("Herb %s despawned at %s", element.getName(), location), Level.CONFIG);
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

    @Override
    public final MarkerWrapper getMarker() {
        return marker;
    }
}
