package git.doomshade.professions.utils;

import git.doomshade.professions.Professions;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * An extension of bukkit runnable that disallows usage of external delays/periods and keeps track of current state of runnable.
 *
 * @author Doomshade
 * @version 1.0
 */
public abstract class ExtendedBukkitRunnable extends BukkitRunnable {

    private final long delay, period;
    protected boolean running = false;

    public ExtendedBukkitRunnable() {
        this.delay = delay();
        this.period = period();
    }

    public final BukkitTask startTask() throws IllegalArgumentException, IllegalStateException {
        running = true;

        onStart();
        if (period <= 0) {
            return super.runTaskLater(Professions.getInstance(), delay);
        }
        return super.runTaskTimer(Professions.getInstance(), delay, period);
    }

    protected void onStart() {

    }

    protected void onCancel() {

    }

    protected abstract long delay();

    protected abstract long period();


    @Deprecated
    @Override
    public final synchronized BukkitTask runTaskTimer(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        return null;
    }

    @Deprecated
    @Override
    public final synchronized BukkitTask runTask(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        return null;
    }

    @Deprecated
    @Override
    public final synchronized BukkitTask runTaskAsynchronously(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        return null;
    }

    @Deprecated
    @Override
    public final synchronized BukkitTask runTaskLater(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        return null;
    }

    @Deprecated
    @Override
    public final synchronized BukkitTask runTaskLaterAsynchronously(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        return null;
    }

    @Deprecated
    @Override
    public final synchronized BukkitTask runTaskTimerAsynchronously(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        return null;
    }

    @Override
    public synchronized final void cancel() throws IllegalStateException {
        if (!running) throw new IllegalStateException("Task not running");
        running = false;
        onCancel();
        super.cancel();
    }

    public boolean isRunning() {
        return running;
    }
}
