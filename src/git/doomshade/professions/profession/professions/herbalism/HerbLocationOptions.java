package git.doomshade.professions.profession.professions.herbalism;

import git.doomshade.professions.profession.utils.MarkableLocationOptions;
import org.bukkit.Location;

public class HerbLocationOptions extends MarkableLocationOptions {

    /*@SuppressWarnings("unused")
    public static final Pattern MARK_ID_PATTERN = Pattern.compile("[\\w]+-[0-9]+");*/

    HerbLocationOptions(Location location, Herb herb) {
        super(location, herb);
    }

    /*public void spawn() {
        if (!herb.isSpawnEnabled()) {
            return;
        }
        forceSpawn();
    }


    /*public void forceSpawn() {
        addParticles();
        location.getBlock().setType(herb.getMaterial());
        unscheduleSpawn();
        spawned = true;
        if (marker != null) {
            Professions.getMarkerManager().show(this);
        }
        Professions.log(String.format("Herb %s spawned at %s", herb.getName(), location), Level.CONFIG);
    }*/

    /*public void despawn() {
        removeParticles();
        location.getBlock().setType(Material.AIR);
        unscheduleSpawn();
        if (marker != null) {
            Professions.getMarkerManager().hide(this);
        }
        spawned = false;
        Professions.log(String.format("Herb %s despawned at %s", herb.getName(), location), Level.CONFIG);
    }*/

    @Override
    public boolean isSpawnable() {
        return ((Herb) element).isSpawnEnabled();
    }

    /*public void scheduleSpawn() throws IllegalArgumentException {
        try {
            Bukkit.getScheduler().cancelTask(spawnTask.getTaskId());
        } catch (IllegalStateException ignored) {
        }
        spawnTask = new SpawnTask(spawnTask);
        spawnTask.runTaskTimer(Professions.getInstance(), 20L, 20L);
    }*/

    /*private void unscheduleSpawn() {
        try {
            spawnTask.cancel();
        } catch (Exception ignored) {
        }
    }*/


    /*private void addParticles() {
        if (!particleTask.isRunning()) {
            try {
                Bukkit.getScheduler().cancelTask(particleTask.getTaskId());
            } catch (IllegalStateException ignored) {
            }
            particleTask = new ParticleTask(particleTask);
            particleTask.runTaskTimer(Professions.getInstance(), 0L, herb.getParticleData().getPeriod());
        }
    }*/

    /*public void removeParticles() {
        try {
            if (particleTask.isRunning())
                particleTask.cancel();
        } catch (Exception ignored) {
        }
    }*/

    /*@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HerbLocationOptions that = (HerbLocationOptions) o;
        return location.equals(that.location) &&
                herb.equals(that.herb);
    }*/

    /*@Override
    public int hashCode() {
        return Objects.hash(location, herb);
    }*/

    @Override
    public String toString() {
        return "HerbLocationOptions{" +
                "location=" + location +
                ", herb=" + element +
                '}';
    }

    @Override
    public String getMarkerSetId() {
        return "herbalism";
    }
}
