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

import git.doomshade.professions.api.spawn.impl.SpawnPoint;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ExtendedBukkitRunnable;
import org.bukkit.Bukkit;

public class SpawnTask extends ExtendedBukkitRunnable {

    private final SpawnPoint spawnPoint;
    // because of cache
    private transient final int generatedRespawnTime;
    transient int id = -1;
    private transient int respawnTime;
    /**
     * Particle task that spawns particles periodically
     */
    private ParticleTask particleTask;

    public SpawnTask(SpawnPoint spawnPoint) {
        this.spawnPoint = spawnPoint;
        this.generatedRespawnTime = spawnPoint.getSpawnTime().getRandom();
        this.particleTask = new ParticleTask(spawnPoint.getSpawnableElement().getParticleData(),
                spawnPoint.getLocation());
    }

    public int getGeneratedRespawnTime() {
        return generatedRespawnTime;
    }

    public int getRespawnTime() {
        return respawnTime;
    }

    public int getId() {
        return id;
    }

    @Override
    public void run() {
        if (spawnPoint.isSpawned()) {
            return;
        }
        if (respawnTime <= 0) {
            try {
                spawnPoint.spawn();
            } catch (SpawnException e) {
                ProfessionLogger.logError(e);
            } finally {
                // spawn does cancel this task, however if an exception happened we need to
                // cancel it here
                if (isRunning()) {
                    cancel();
                }
            }
            return;
        }
        respawnTime--;
    }

    @Override
    protected void onStart() {
        addParticles();
    }

    @Override
    protected void onCancel() {
        removeParticles();
    }

    @Override
    protected long delay() {
        return 0;
    }

    @Override
    protected long period() {
        return 20L;
    }

    private void removeParticles() throws IllegalStateException {
        if (particleTask.isRunning()) {
            particleTask.cancel();
        }
    }

    private void addParticles() {
        if (!particleTask.isRunning()) {
            try {
                Bukkit.getScheduler().cancelTask(particleTask.getTaskId());
            } catch (IllegalStateException ignored) {
            }
            particleTask = new ParticleTask(particleTask);
            particleTask.startTask();
        }
    }
}
