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

package git.doomshade.professions.task;

import git.doomshade.professions.utils.ExtendedBukkitRunnable;
import git.doomshade.professions.utils.ParticleData;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.Objects;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class ParticleTask extends ExtendedBukkitRunnable {
    private final ParticleData particle;
    private final Location location;
    private final Location particleOffsetLocation;


    public ParticleTask(ParticleData particle, Location location) {
        this.particle = particle;
        this.location = location;
        this.particleOffsetLocation =
                location.clone().add(particle.getXOffset(), particle.getYOffset(), particle.getZOffset());
    }

    public ParticleTask(ParticleTask copy) {
        this.particle = copy.particle;
        this.location = copy.location;
        this.particleOffsetLocation = copy.particleOffsetLocation;
    }

    @Override
    public void run() {
        Objects.requireNonNull(location.getWorld()).spawnParticle(
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
