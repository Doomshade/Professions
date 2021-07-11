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

    private boolean running = false;

    public synchronized final BukkitTask startTaskAsync() throws IllegalArgumentException, IllegalStateException {
        return startTask(false);
    }

    public synchronized final BukkitTask startTask() throws IllegalArgumentException, IllegalStateException {
        return startTask(true);
    }

    public synchronized BukkitTask startTask(boolean sync) throws IllegalArgumentException, IllegalStateException {
        try {
            if (isRunning()) {
                throw new IllegalStateException("Task is already running!");
            }
            running = true;
            long delay = delay();
            long period = period();

            onStart();
            if (period <= 0) {
                return sync
                        ? super.runTaskLater(Professions.getInstance(), delay)
                        : super.runTaskLaterAsynchronously(Professions.getInstance(), delay);
            }
            return sync
                    ? super.runTaskTimer(Professions.getInstance(), delay, period)
                    : super.runTaskTimerAsynchronously(Professions.getInstance(), delay, period);
        } catch (Throwable e) {
            cancel();

            // TODO
            Professions.logError(e);
            throw e;
        }
    }

    protected void onStart() {

    }

    protected void onCancel() {

    }


    @Override
    public synchronized final void cancel() throws IllegalStateException {
        if (!isRunning()) {
            throw new IllegalStateException("Task is not running!");
        }
        running = false;
        onCancel();
        super.cancel();
    }

    public final boolean isRunning() {
        return running;
    }

    /**
     * @return delay in ticks
     */
    protected abstract long delay();

    /**
     * If the period is <=0, the runnable will run only once
     *
     * @return period in ticks
     */
    protected abstract long period();

    @Deprecated
    @Override
    public final synchronized BukkitTask runTaskTimer(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final synchronized BukkitTask runTask(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final synchronized BukkitTask runTaskAsynchronously(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final synchronized BukkitTask runTaskLater(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final synchronized BukkitTask runTaskLaterAsynchronously(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final synchronized BukkitTask runTaskTimerAsynchronously(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }
}
