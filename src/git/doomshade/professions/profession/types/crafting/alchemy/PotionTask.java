package git.doomshade.professions.profession.types.crafting.alchemy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class PotionTask extends BukkitRunnable implements Serializable {

    final Potion potion;
    final UUID uuid;
    final transient Player player;
    int duration;

    PotionTask(Potion potion, Player player) {
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

        if (duration <= 0) {
            potion.unApply(player);
            cancel();
            return;
        }
        duration--;
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
}
