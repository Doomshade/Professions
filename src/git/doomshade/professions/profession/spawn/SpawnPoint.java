package git.doomshade.professions.profession.spawn;

import git.doomshade.professions.api.spawn.ISpawnPoint;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.utils.ExtendedLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;

import java.util.Objects;
import java.util.logging.Level;

/**
 * This class stores the location of spawn point, the
 */
public abstract class SpawnPoint implements ISpawnPoint {

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

    private boolean spawned = false;
    /**
     * Boolean to make sure that the error does not spam console
     */
    private boolean enableSpawn = true;

    public SpawnPoint(Location location, SpawnableElement<?> element) throws IllegalArgumentException {
        this.location = location;
        this.element = element;
        if (!element.getSpawnPointLocations().contains(new ExtendedLocation(location))) {
            throw new IllegalArgumentException("No spawn point with " + location + " exists for " + element.getName() + "(" + element.getSpawnPointLocations() + ")");
        }
        spawnTask = new SpawnTask(this);
    }

    public SpawnTask getSpawnTask() {
        return spawnTask;
    }

    public Location getLocation() {
        return location;
    }

    public SpawnableElement<?> getSpawnableElement() {
        return element;
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
        spawnTask.startTask();
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
        //final byte materialData = element.getMaterialData();

        final BlockData blockData = block.getBlockData();
        if (blockData instanceof Bisected) {
            final Bisected bdBottom = (Bisected) blockData;
            bdBottom.setHalf(Bisected.Half.BOTTOM);
            final Block top = location.getWorld().getBlockAt(location.clone().add(0, 1, 0));
            top.setType(material, false);
            final Bisected bdTop = (Bisected) top.getBlockData();
            bdTop.setHalf(Bisected.Half.TOP);
            //top.setData((byte) 10);
        } else {
            block.setType(material, false);
        }

        unscheduleSpawn();
        spawned = true;

        ProfessionLogger.log(String.format("Spawned %s at %s", element.getName(), location), Level.CONFIG);
    }

    private void unscheduleSpawn() {
        try {
            spawnTask.cancel();
        } catch (Exception ignored) {
        }
    }

    public void despawn() {
        location.getBlock().setType(Material.AIR);
        unscheduleSpawn();
        spawned = false;

        ProfessionLogger.log(String.format("Despawned %s at %s", element.getName(), location), Level.CONFIG);
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
