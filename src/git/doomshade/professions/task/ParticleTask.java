package git.doomshade.professions.task;

import git.doomshade.professions.utils.ExtendedBukkitRunnable;
import git.doomshade.professions.utils.ParticleData;
import org.bukkit.Location;
import org.bukkit.Particle;

public class ParticleTask extends ExtendedBukkitRunnable {
    private final ParticleData particle;
    private final Location location;
    private final Location particleOffsetLocation;


    public ParticleTask(ParticleData particle, Location location) {
        this.particle = particle;
        this.location = location;
        this.particleOffsetLocation = location.clone().add(particle.getXOffset(), particle.getYOffset(), particle.getZOffset());
    }

    public ParticleTask(ParticleTask copy) {
        this.particle = copy.particle;
        this.location = copy.location;
        this.particleOffsetLocation = copy.particleOffsetLocation;
    }

    @Override
    public void run() {
        location.getWorld().spawnParticle(
                Particle.valueOf(particle.getParticle()),
                particleOffsetLocation,
                particle.getCount(),
                particle.getXOffset(),
                particle.getYOffset(),
                particle.getZOffset(),
                particle.getSpeed()
        );
    }

    @Override
    protected long delay() {
        return 0;
    }

    @Override
    protected long period() {
        return particle.getPeriod();
    }
}
