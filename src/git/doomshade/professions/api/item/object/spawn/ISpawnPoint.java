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

package git.doomshade.professions.api.item.object.spawn;

import git.doomshade.professions.utils.Range;
import git.doomshade.professions.exceptions.SpawnException;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public interface ISpawnPoint extends ConfigurationSerializable {

    /**
     * @return the internal marker id
     */
    String getMarkerId();

    /**
     * @return the marker label on dynmap
     */
    String getMarkerLabel();

    /**
     * @return the marker icon on dynmap
     */
    String getMarkerIcon();

    /**
     * @return the marker set id on dynmap
     */
    String getMarkerSetId();

    /**
     * @return the serial number of this spawn point
     */
    int getSerialNumber();

    /**
     * @return the location of this spawn point
     */
    Location getLocation();

    /**
     * @return the spawn time range
     */
    Range getSpawnTime();

    /**
     * @return the spawnable element
     */
    ISpawnable getSpawnableElement();

    /**
     * Schedules the spawn
     *
     * @throws SpawnException if the spawn did not fucking go well
     */
    void scheduleSpawn() throws SpawnException;

    /**
     * Spawns the element
     *
     * @throws SpawnException if the spawn did not fucking go well
     */
    void spawn() throws SpawnException;

    /**
     * Forces the spawn of the element
     *
     * @throws SpawnException if the spawn did not fucking go well
     */
    void forceSpawn() throws SpawnException;

    /**
     * Despawns the element
     */
    void despawn();

    /**
     * @return {@code true} if the spawnable element was spawned, {@code false otherwise}
     */
    boolean isSpawned();

    /**
     * @return whether this spawn point can spawn elements
     */
    boolean isSpawnable();

    void setSpawnable(boolean canSpawn);
}
