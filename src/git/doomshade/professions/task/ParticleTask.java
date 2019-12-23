package git.doomshade.professions.task;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleTask extends BukkitRunnable {
    private final Location location;
    private final Particle particle;
    private final int count;

    public ParticleTask(Location location, Particle particle, int count) {
        this.location = location;
        this.particle = particle;
        this.count = count;
    }

    @Override
    public void run() {
        location.getWorld().spawnParticle(particle, location, count);
    }
}
