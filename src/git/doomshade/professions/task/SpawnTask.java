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

import git.doomshade.professions.api.spawn.ext.SpawnPoint;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ExtendedBukkitRunnable;

import java.util.logging.Level;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class SpawnTask extends ExtendedBukkitRunnable {

    private final SpawnPoint spawnPoint;
    // because of cache
    private transient final int generatedRespawnTime;
    private transient int respawnTime;

    public SpawnTask(SpawnPoint spawnPoint) {
        this.spawnPoint = spawnPoint;
        this.generatedRespawnTime = spawnPoint.getSpawnTime().getRandom();
        this.respawnTime = generatedRespawnTime;
    }

    /**
     * @return the generated (random) respawn time
     */
    public int getGeneratedRespawnTime() {
        return generatedRespawnTime;
    }

    /**
     * @return the current respawn time
     */
    public int getRespawnTime() {
        return respawnTime;
    }

    /**
     * Sets the current respawn time
     *
     * @param respawnTime the current respawn time
     */
    public void setRespawnTime(int respawnTime) {
        this.respawnTime = respawnTime;
    }

    @Override
    public void run() {
        if (spawnPoint.isSpawned()) {
            return;
        }
        if (respawnTime <= 0) {
            try {
                ProfessionLogger.log("SpawnTask spawning... " + spawnPoint, Level.CONFIG);
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
    }

    @Override
    protected void onCancel() {
    }

    @Override
    protected long delay() {
        return 20L;
    }

    @Override
    protected long period() {
        return 20L;
    }
}
