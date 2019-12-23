package git.doomshade.professions.profession.types.gathering.herbalism;

import com.avaje.ebean.validation.NotNull;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

class SpawnTask extends BukkitRunnable {
    private static final HashMap<Herb, Set<SpawnTask>> SPAWNING_HERBS = new HashMap<>();
    public final SpawnPoint spawnPoint;
    public final Herb herb;
    public int respawnTime;

    SpawnTask(Herb herb, Location spawnPoint) throws IllegalArgumentException {
        this.herb = herb;
        SpawnPoint example = new SpawnPoint(spawnPoint);

        for (SpawnPoint sp : SpawnPoint.SPAWN_POINTS) {
            if (sp.equals(example)) {
                this.spawnPoint = sp;
                this.respawnTime = sp.respawnTime;
                return;
            }
        }
        throw new IllegalArgumentException("No spawn point exists with location " + spawnPoint + "!");
    }

    static boolean isSpawning(Herb herb) {
        return SPAWNING_HERBS.containsKey(herb);
    }

    @NotNull
    static Set<SpawnTask> getSpawnTasks(Herb herb) {
        return SPAWNING_HERBS.getOrDefault(herb, new HashSet<>());
    }

    static SpawnTask getSpawnTask(Herb herb, Location location) throws Utils.SearchNotFoundException {
        return Utils.findInIterable(getSpawnTasks(herb), x -> x.spawnPoint.location.equals(location));
    }

    @Override
    public void run() {
        if (respawnTime <= 0) {
            herb.spawn(spawnPoint.location);
            cancel();
            return;
        }
        respawnTime--;
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {

        // TODO FIX
        Set<SpawnTask> tasks = SPAWNING_HERBS.getOrDefault(herb, new HashSet<>());
        tasks.remove(this);
        SPAWNING_HERBS.put(herb, tasks);
        super.cancel();
    }

    @Override
    public synchronized BukkitTask runTask(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        addTask();
        return super.runTask(plugin);
    }

    @Override
    public synchronized BukkitTask runTaskAsynchronously(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        addTask();
        return super.runTaskAsynchronously(plugin);
    }

    @Override
    public synchronized BukkitTask runTaskLater(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        addTask();
        return super.runTaskLater(plugin, delay);
    }

    @Override
    public synchronized BukkitTask runTaskLaterAsynchronously(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        addTask();
        return super.runTaskLaterAsynchronously(plugin, delay);
    }

    @Override
    public synchronized BukkitTask runTaskTimer(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        addTask();
        return super.runTaskTimer(plugin, delay, period);
    }

    @Override
    public synchronized BukkitTask runTaskTimerAsynchronously(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        addTask();
        return super.runTaskTimerAsynchronously(plugin, delay, period);
    }

    private void addTask() {
        Set<SpawnTask> tasks = SPAWNING_HERBS.getOrDefault(herb, new HashSet<>());
        tasks.add(this);
        SPAWNING_HERBS.put(herb, tasks);
    }
}
