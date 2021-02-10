package git.doomshade.professions.task;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.ParticleData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleTask extends BukkitRunnable {
    private final ParticleData particle;
    private final Location location;
    private final Location particleOffsetLocation;

    private boolean running = false;

    public ParticleTask(ParticleData particle, Location location) {
        this.particle = particle;
        this.location = location;
        this.particleOffsetLocation = location.clone().add(particle.getxOffset(), particle.getyOffset(), particle.getzOffset());
    }

    public ParticleTask(ParticleTask copy) {
        this(copy.particle, copy.location);
        this.running = copy.running;
    }

    @Override
    public void run() {
        running = true;
        location.getWorld().spawnParticle(Particle.valueOf(particle.getParticle()), particleOffsetLocation, particle.getCount(), particle.getxOffset(), particle.getyOffset(), particle.getzOffset(), particle.getSpeed());
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        running = false;
        super.cancel();
    }

    public boolean isRunning() {
        return running;
    }
}
