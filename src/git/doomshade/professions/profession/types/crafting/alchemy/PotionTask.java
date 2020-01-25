package git.doomshade.professions.profession.types.crafting.alchemy;

import git.doomshade.professions.Professions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
    public synchronized BukkitTask runTask(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        return super.runTask(plugin);
    }

    @Deprecated
    @Override
    public synchronized BukkitTask runTaskAsynchronously(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        return super.runTaskAsynchronously(plugin);
    }

    @Deprecated
    @Override
    public synchronized BukkitTask runTaskLater(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        return super.runTaskLater(plugin, delay);
    }

    @Deprecated
    @Override
    public synchronized BukkitTask runTaskLaterAsynchronously(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        return super.runTaskLaterAsynchronously(plugin, delay);
    }

    @Deprecated
    @Override
    public synchronized BukkitTask runTaskTimer(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        return super.runTaskTimer(plugin, delay, period);
    }

    @Deprecated
    @Override
    public synchronized BukkitTask runTaskTimerAsynchronously(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        return super.runTaskTimerAsynchronously(plugin, delay, period);
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        potion.addAttributes(player, true);
        super.cancel();
    }
}
