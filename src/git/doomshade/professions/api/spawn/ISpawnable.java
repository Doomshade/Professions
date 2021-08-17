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

package git.doomshade.professions.api.spawn;

import git.doomshade.professions.api.IParticleData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 * A spawnable element
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public interface ISpawnable extends Iterable<ISpawnPoint>, IElement {

    /**
     * @return whether the spawnable can spawn elements
     */
    boolean canSpawn();

    /**
     * Sets whether the spawnable can spawn
     *
     * @param canSpawn whether it can spawn
     */
    void setCanSpawn(boolean canSpawn);

    /**
     * @return the marker icon on dynmap for all spawn points
     */
    String getMarkerIcon();

    /**
     * @return the particle data about this location element if there should be particles played, {@code null} otherwise
     */
    IParticleData getParticleData();

    /**
     * @return the material of the location's block
     */
    Material getMaterial();

    /**
     * @return the material data because of special blocks suck as flowers
     */
    byte getMaterialData();

    /**
     * @param location the location to check for
     *
     * @return {@code true} if the location is a spawn point, {@code false} otherwise
     */
    boolean isSpawnPoint(Location location);

    /**
     * @param serialNumber the serial number
     *
     * @return {@code true} if a spawn point with this serial number exists, {@code false} otherwise
     */
    boolean isSpawnPoint(int serialNumber);

    @NotNull
    @Override
    default Iterator<ISpawnPoint> iterator() {
        return getSpawnPoints().iterator();
    }

    /**
     * @return the spawn points of this element
     */
    Collection<ISpawnPoint> getSpawnPoints();

    /**
     * @return the spawned elements
     */
    Collection<ISpawnPoint> getSpawnedElements();

    /**
     * @param location the location
     *
     * @return the spawn point based on location or {@code null}
     */
    ISpawnPoint getSpawnPoint(Location location);

    /**
     * @param serialNumber the serial number
     *
     * @return the spawn point based on serial number or {@code null}
     */
    ISpawnPoint getSpawnPoint(int serialNumber);

    /**
     * Adds a spawn point
     *
     * @param spawnPoint the spawn point
     */
    void addSpawnPoint(ISpawnPoint spawnPoint);

    /**
     * Adds a collection of spawn points
     *
     * @param spawnPoints the spawn points
     */
    void addSpawnPoints(Iterable<ISpawnPoint> spawnPoints);

    /**
     * Removes given spawn point
     *
     * @param spawnPoint the spawn point
     */
    void removeSpawnPoint(ISpawnPoint spawnPoint);

    /**
     * Removes a spawn point based on the location
     *
     * @param location the location
     */
    void removeSpawnPoint(Location location);

    /**
     * Removes a spawn point with given serialNumber
     *
     * @param serialNumber the serialNumber of the spawn point
     */
    void removeSpawnPoint(int serialNumber);
}
