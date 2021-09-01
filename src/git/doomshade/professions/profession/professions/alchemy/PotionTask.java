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

package git.doomshade.professions.profession.professions.alchemy;

import git.doomshade.professions.Professions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class PotionTask extends BukkitRunnable implements Serializable {

    final Potion potion;
    final UUID uuid;
    final transient Player player;
    int duration;

    public PotionTask(Potion potion, Player player) {
        this(potion, player, potion.getDuration());
    }

    PotionTask(Potion potion, Player player, int duration) {
        this.potion = potion;
        this.uuid = player.getUniqueId();

        // because of serialization
        this.player = Bukkit.getPlayer(uuid);
        this.duration = duration;
    }

    PotionTask(PotionTask copy) {
        this(copy.potion, copy.player, copy.duration);
    }

    @Override
    public void run() {
        if (--duration <= 0) {
            cancel();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PotionTask that = (PotionTask) o;
        return potion.equals(that.potion) &&
                uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(potion, uuid);
    }

    public synchronized BukkitTask runTask() {
        potion.addAttributes(player, false);
        return super.runTaskLater(Professions.getInstance(), 20L);
    }

    @Deprecated
    @Override
    public synchronized @NotNull BukkitTask runTask(@NotNull Plugin plugin)
            throws IllegalArgumentException, IllegalStateException {
        return super.runTask(plugin);
    }

    @Deprecated
    @Override
    public synchronized @NotNull BukkitTask runTaskAsynchronously(@NotNull Plugin plugin)
            throws IllegalArgumentException, IllegalStateException {
        return super.runTaskAsynchronously(plugin);
    }

    @Deprecated
    @Override
    public synchronized @NotNull BukkitTask runTaskLater(@NotNull Plugin plugin, long delay)
            throws IllegalArgumentException, IllegalStateException {
        return super.runTaskLater(plugin, delay);
    }

    @Deprecated
    @Override
    public synchronized @NotNull BukkitTask runTaskLaterAsynchronously(@NotNull Plugin plugin, long delay)
            throws IllegalArgumentException, IllegalStateException {
        return super.runTaskLaterAsynchronously(plugin, delay);
    }

    @Deprecated
    @Override
    public synchronized @NotNull BukkitTask runTaskTimer(@NotNull Plugin plugin, long delay, long period)
            throws IllegalArgumentException, IllegalStateException {
        return super.runTaskTimer(plugin, delay, period);
    }

    @Deprecated
    @Override
    public synchronized @NotNull BukkitTask runTaskTimerAsynchronously(@NotNull Plugin plugin, long delay, long period)
            throws IllegalArgumentException, IllegalStateException {
        return super.runTaskTimerAsynchronously(plugin, delay, period);
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        potion.addAttributes(player, true);
        super.cancel();
    }
}
