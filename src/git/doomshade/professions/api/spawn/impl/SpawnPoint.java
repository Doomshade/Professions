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

package git.doomshade.professions.api.spawn.impl;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Range;
import git.doomshade.professions.api.spawn.ISpawnPoint;
import git.doomshade.professions.dynmap.MarkerManager;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.task.ParticleTask;
import git.doomshade.professions.task.SpawnTask;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.MemorySection;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

import static git.doomshade.professions.utils.Strings.SpawnableElementEnum.SPAWN_POINT;


/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class SpawnPoint implements ISpawnPoint {
    public static final SpawnPoint EXAMPLE = new SpawnPoint();
    private static final MarkerManager MARKER_MANAGER = Professions.getMarkerManager();
    private static final HashMap<Location, Collection<SpawnPoint>> SPAWN_POINTS = new HashMap<>();
    private static final HashMap<Spawnable, Integer> SERIAL_NUMBER_CACHE = new HashMap<>();
    private final Location location;
    private final Range spawnTime;
    private final Spawnable element;
    private final int serialNumber;
    private final String markerId;
    private final String markerLabel;
    private final String markerSetId;
    private final String markerIcon;
    private SpawnTask spawnTask;
    private ParticleTask particleTask;
    private boolean spawned = false;
    private boolean enableSpawn = true;

    /**
     * Default spawn point (for example serialization purposes)
     */
    private SpawnPoint() {
        this.enableSpawn = false;
        this.markerId = null;
        this.markerLabel = null;
        this.markerSetId = null;
        this.markerIcon = null;
        this.location = ItemUtils.EXAMPLE_LOCATION;
        this.spawnTime = new Range(0);
        this.element = null;
        this.spawnTask = null;
        this.particleTask = null;
        this.serialNumber = -1;
    }

    /**
     * @param location  the location
     * @param spawnTime the spawn time
     * @param spawnable the spawnable element
     */
    public SpawnPoint(Location location, Range spawnTime, Spawnable spawnable) {
        this(location, spawnTime, spawnable, MarkerManager.EMPTY_MARKER_SET_ID);
    }

    /**
     * @param location    the location
     * @param spawnTime   the spawn time
     * @param spawnable   the spawnable element
     * @param markerSetId the dynmap marker set ID
     */
    public SpawnPoint(Location location, Range spawnTime, Spawnable spawnable, String markerSetId) {
        this(location, spawnTime, spawnable, markerSetId, generateSerialNumber(spawnable));
    }

    /**
     * The main spawn point
     *
     * @param location     the location
     * @param spawnTime    the spawn time
     * @param spawnable    the spawnable element
     * @param markerSetId  the dynmap marker set ID
     * @param serialNumber the serial number of this spawn point
     */
    public SpawnPoint(Location location, Range spawnTime, Spawnable spawnable, String markerSetId,
                      int serialNumber)
            throws IllegalArgumentException {
        final int cacheNum = SERIAL_NUMBER_CACHE.getOrDefault(spawnable, -1);
        if (cacheNum == serialNumber) {
            throw new IllegalArgumentException(
                    String.format("A spawn point for %s element with serial number %d already exists!", spawnable,
                            serialNumber));
        }
        SERIAL_NUMBER_CACHE.put(spawnable, Math.max(cacheNum, serialNumber));
        this.location = location;
        this.spawnTime = spawnTime;
        this.element = spawnable;
        this.element.addSpawnPoint(this);
        this.serialNumber = serialNumber;
        SPAWN_POINTS.putIfAbsent(location, new ArrayList<>());
        SPAWN_POINTS.computeIfPresent(location, (k, v) -> {
            v.add(this);
            return v;
        });
        this.spawnTask = new SpawnTask(this);
        this.particleTask = new ParticleTask(spawnable.getParticleData(), location);
        this.markerSetId = markerSetId;
        this.markerId = spawnable.getId().concat("-").concat(String.valueOf(getSerialNumber()));
        this.markerLabel = ChatColor.stripColor(spawnable.getName());
        this.markerIcon = spawnable.getMarkerIcon();
    }

    public static int generateSerialNumber(Spawnable forElement) {
        return SERIAL_NUMBER_CACHE.getOrDefault(forElement, 0) + 1;
    }

    public SpawnPoint(Location location, Range spawnTime, Spawnable element, int serialNumber) {
        this(location, spawnTime, element, MarkerManager.EMPTY_MARKER_SET_ID, serialNumber);
    }

    public static void unloadAll() {
        SERIAL_NUMBER_CACHE.clear();
        SPAWN_POINTS.clear();
    }

    public static Collection<SpawnPoint> deserializeAll(Map<String, Object> map, Spawnable spawnable)
            throws ProfessionObjectInitializationException {
        return deserializeAll(map, spawnable, MarkerManager.EMPTY_MARKER_SET_ID);
    }

    public static Collection<SpawnPoint> deserializeAll(Map<String, Object> map, Spawnable spawnable,
                                                        String markerSetId)
            throws ProfessionObjectInitializationException {
        ProfessionObjectInitializationException ex = null;
        Collection<SpawnPoint> spawnPointLocations = new ArrayList<>();
        /*
         * spawnpoint-0:
         *      respawn-time:
         *          from: 0
         *          to: 0
         *      location:
         *          world: world
         *          x: 0
         *          y: 0
         *          z: 0
         *          pitch: 0
         *          yaw: 0
         */
        for (int i = 0; i < map.size(); i++) {

            final Object o = map.get(SPAWN_POINT.s.concat("-") + i);
            if (o instanceof MemorySection) {
                try {
                    spawnPointLocations.add(SpawnPoint.deserialize(((MemorySection) o).getValues(false), spawnable,
                            markerSetId, i));
                } catch (ProfessionObjectInitializationException e) {
                    if (ex == null) {
                        ex = new ProfessionObjectInitializationException(SpawnPoint.class, Collections.emptyList(),
                                ProfessionObjectInitializationException.ExceptionReason.KEY_ERROR);
                    }
                    e.setAdditionalMessage("Spawn point ID: " + i);
                    ProfessionLogger.logError(e, false);
                }
            }
        }

        if (ex != null) {
            throw ex;
        }

        return spawnPointLocations;
    }

    public static SpawnPoint deserialize(Map<String, Object> map, Spawnable spawnable,
                                         String markerSetId, int serialNumber)
            throws ProfessionObjectInitializationException {
        final Set<String> missingKeysEnum = Utils.getMissingKeys(map, Strings.SpawnPointEnum.values());
        if (!missingKeysEnum.isEmpty()) {
            throw new ProfessionObjectInitializationException(
                    SpawnPoint.class,
                    missingKeysEnum,
                    ProfessionObjectInitializationException.ExceptionReason.MISSING_KEYS);
        }
        /*
         *  respawn-time:
         *      from: 0
         *      to: 0
         *  location:
         *      world: world
         *      x: 0
         *      y: 0
         *      z: 0
         *      pitch: 0
         *      yaw: 0
         */
        Range r = Range.fromString((String) map.get(Strings.SpawnPointEnum.RESPAWN_TIME.s))
                .orElseThrow(IllegalArgumentException::new);
        Location loc =
                Location.deserialize(((MemorySection) map.get(Strings.SpawnPointEnum.LOCATION.s)).getValues(false));

        return new SpawnPoint(loc, r, spawnable, markerSetId, serialNumber);
    }

    public static SpawnPoint deserialize(Map<String, Object> map, Spawnable spawnable, int serialNumber)
            throws ProfessionObjectInitializationException {
        return deserialize(map, spawnable, MarkerManager.EMPTY_MARKER_SET_ID, serialNumber);
    }

    @Override
    public String getMarkerId() {
        return markerId;
    }

    @Override
    public String getMarkerLabel() {
        return markerLabel;
    }

    @Override
    public String getMarkerIcon() {
        return markerIcon;
    }

    @Override
    public String getMarkerSetId() {
        return markerSetId;
    }

    @Override
    public final int getSerialNumber() {
        return serialNumber;
    }

    @Override
    public final Location getLocation() {
        return location;
    }

    @Override
    public final Range getSpawnTime() {
        return spawnTime;
    }

    @Override
    public final Spawnable getSpawnableElement() {
        return element;
    }

    @Override
    public void scheduleSpawn() {
        cancelSpawnTask();
        removeParticles();
        this.spawnTask = new SpawnTask(this);
        spawnTask.startTask();
    }

    @Override
    public void spawn() throws SpawnException {
        if (isSpawnable() && !spawned) {
            forceSpawn();
        }
    }

    @Override
    public void forceSpawn() throws SpawnException {
        if (!isSpawnable()) {
            return;
        }

        final Material material = element.getMaterial();
        if (material == null) {
            this.setSpawnable(false);
            throw new SpawnException(new NullPointerException(), SpawnException.SpawnExceptionReason.INVALID_MATERIAL,
                    element);
        }
        if (location == null) {
            this.setSpawnable(false);
            throw new SpawnException(new NullPointerException(), SpawnException.SpawnExceptionReason.INVALID_LOCATION,
                    element);
        }
        //final byte materialData = element.getMaterialData();

        final Block block = location.getBlock();
        final BlockData blockData = block.getBlockData();

        if (blockData instanceof Bisected) {
            final Bisected bdBottom = (Bisected) blockData;
            bdBottom.setHalf(Bisected.Half.BOTTOM);

            final Block top = Objects.requireNonNull(location.getWorld()).getBlockAt(location.clone().add(0, 1, 0));
            top.setType(material, false);
            final Bisected bdTop = (Bisected) top.getBlockData();
            bdTop.setHalf(Bisected.Half.TOP);
            //top.setData((byte) 10);
        } else {
            block.setType(material, false);
        }

        cancelSpawnTask();
        addParticles();
        setMarkerVisible(true);
        ProfessionLogger.log(String.format("Spawned %s at %s", element.getName(), location), Level.CONFIG);
        spawned = true;
    }

    public void setMarkerVisible(boolean visible) {
        if (MARKER_MANAGER == null) {
            return;
        }

        if (visible) {
            MARKER_MANAGER.show(this);
        } else {
            MARKER_MANAGER.hide(this);
        }
    }

    @Override
    public void despawn() {
        //location.getBlock().breakNaturally(new ItemStack(Material.AIR));
        location.getBlock().setType(Material.AIR);
        cancelSpawnTask();
        removeParticles();
        spawned = false;
        setMarkerVisible(false);

        ProfessionLogger.log(String.format("Despawned %s at %s", element.getName(), location), Level.CONFIG);
    }

    @Override
    public final boolean isSpawned() {
        return spawned;
    }

    @Override
    public boolean isSpawnable() {
        return enableSpawn;
    }

    @Override
    public void setSpawnable(boolean spawnable) {
        this.enableSpawn = spawnable;
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

    private void cancelSpawnTask() {
        try {
            spawnTask.cancel();
        } catch (Exception ignored) {
        }
    }

    private void removeParticles() throws IllegalStateException {
        if (particleTask.isRunning()) {
            particleTask.cancel();
        }
    }

    public final SpawnTask getSpawnTask() {
        return spawnTask;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, element, serialNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpawnPoint that = (SpawnPoint) o;
        return location.equals(that.location) &&
                element.equals(that.element) &&
                serialNumber == that.serialNumber;
    }

    @Override
    public String toString() {
        return "SpawnPoint{" +
                "location=" + location +
                ", spawnTime=" + spawnTime +
                ", serialNumber=" + serialNumber +
                ", spawnTask=" + spawnTask +
                '}';
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return new HashMap<>() {
            {
                put(Strings.SpawnPointEnum.LOCATION.s, location.serialize());
                put(Strings.SpawnPointEnum.RESPAWN_TIME.s, spawnTime.serialize());
            }
        };
    }
}
