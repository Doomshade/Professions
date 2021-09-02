/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package git.doomshade.professions.utils;

import git.doomshade.professions.Professions;
import git.doomshade.professions.io.ProfessionLogger;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

/**
 * An extension of bukkit runnable that disallows usage of external delays/periods and keeps track of current state of
 * runnable.
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public abstract class ExtendedBukkitRunnable extends BukkitRunnable {

    private boolean running = false;

    public final synchronized BukkitTask startTaskAsync() throws IllegalArgumentException, IllegalStateException {
        return startTask(false);
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
        } catch (Exception e) {
            if (isRunning()) {
                try {
                    cancel();
                } catch (IllegalStateException ex) {
                    ProfessionLogger.logError(ex);
                }
            }
            // TODO
            ProfessionLogger.logError(e);
            throw e;
        }
    }

    protected void onStart() {

    }

    @Override
    public final synchronized void cancel() throws IllegalStateException {
        if (!isRunning()) {
            throw new IllegalStateException("Task is not running!");
        }
        running = false;
        onCancel();
        super.cancel();
    }

    protected void onCancel() {

    }

    @Deprecated
    @Override
    public final synchronized @NotNull BukkitTask runTask(@NotNull Plugin plugin)
            throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final synchronized @NotNull BukkitTask runTaskAsynchronously(@NotNull Plugin plugin)
            throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final synchronized @NotNull BukkitTask runTaskLater(@NotNull Plugin plugin, long delay)
            throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final synchronized @NotNull BukkitTask runTaskLaterAsynchronously(@NotNull Plugin plugin, long delay)
            throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final synchronized @NotNull BukkitTask runTaskTimer(@NotNull Plugin plugin, long delay, long period)
            throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final synchronized @NotNull BukkitTask runTaskTimerAsynchronously(@NotNull Plugin plugin, long delay,
                                                                             long period)
            throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
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

    public final synchronized BukkitTask startTask() throws IllegalArgumentException, IllegalStateException {
        return startTask(true);
    }
}
