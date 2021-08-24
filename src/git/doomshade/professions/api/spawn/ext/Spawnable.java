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

package git.doomshade.professions.api.spawn.ext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.api.spawn.ISpawnPoint;
import git.doomshade.professions.api.spawn.ISpawnable;
import git.doomshade.professions.cache.Cacheable;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.task.BackupTask;
import git.doomshade.professions.utils.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static git.doomshade.professions.utils.Strings.SpawnableElementEnum.*;

/**
 * A spawnable element.
 * <p>
 * This can be for example be an ore or a herb
 * <p>
 * Extend this class to make an {@link Element} that can be spawned in a world.
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public abstract class Spawnable extends Element
        implements ConfigurationSerializable, ISpawnable {

    private final HashMap<Location, ISpawnPoint> spawnPoints = new HashMap<>();

    private final ParticleData particleData;
    private final Material material;
    private final byte materialData;
    private final String markerIcon;
    private boolean canSpawn = true;

    protected Spawnable(String id, String name, Material material, byte materialData, ParticleData particleData,
                        String markerIcon)
            throws IllegalArgumentException {
        this(id, name, material, materialData, particleData, markerIcon, true);
    }

    protected Spawnable(String id, String name, Material material, byte materialData, ParticleData particleData,
                        String markerIcon,
                        boolean registerElement) throws IllegalArgumentException {
        super(id, name, registerElement);
        this.particleData = particleData;
        this.material = material;
        this.materialData = materialData;
        this.markerIcon = markerIcon;
    }

    /**
     * <p>Now this might be a little confusing
     * <p>The way this works: we retrieve information from block (the location and material by default, additional args
     * can be added by overriding {@link Spawnable#get()})
     * <p>and then we make sure that the material is equal to this object, and that location is one of its spawn points
     * <p>If these two conditions are met, the object holding the correct spawn points and material is returned.
     * <p>e.g. A block on {0,0,0} with yellow_flower material will return the herb that is the correct material,
     * <p>and contains the spawn point {0,0,0}
     *
     * @param block the block
     * @param <T>   the returned type (so there's no cast needed)
     *
     * @return the spawnable element
     *
     * @throws Utils.SearchNotFoundException if the block is not a spawnable element
     */
    @SuppressWarnings("unchecked")
    public static <T extends Spawnable> T of(Block block, Class<T> elementClass)
            throws Utils.SearchNotFoundException {

        final Spawnable el = iterate(block, getSpawnableElements(elementClass).values());

        if (el != null) {
            return (T) el;
        }

        throw new Utils.SearchNotFoundException();
    }

    private static <T extends Spawnable> Spawnable iterate(
            Block block, Iterable<T> iterable) {
        ProfessionLogger.log(String.format("Iterating through %s for block %s...", iterable, block), Level.CONFIG);
        for (T el : iterable) {

            // el.get() = function, that transforms a spawnable element instance into elementClass instance
            final Function<Block, ? extends Spawnable> func = el.get();
            if (func != null) {
                final Spawnable spawn = func.apply(block);
                if (spawn != null) {
                    return spawn;
                }
            }
        }
        return null;
    }

    /**
     * @return the spawnable location element if the provided argument matched a location element
     */
    protected Function<Block, ? extends Spawnable> get() {
        return block -> {
            Material mat = block.getType();
            Location location = block.getLocation();
            try {
                final Map<Class<? extends Spawnable>, Map<String, Spawnable>> els = getAllSpawnableElements();
                final Collection<Map<String, Spawnable>> vals = els.values();
                for (Map<String, Spawnable> val : vals) {
                    return Utils.findInIterable(val.values(),
                            x -> x.getMaterial() == mat && x.isSpawnPoint(location));
                }
            } catch (Utils.SearchNotFoundException ignored) {
            }
            return null;
        };
    }

    /**
     * @return a map of spawnable elements where the key is the ID of the spawnable element and value is the
     * corresponding element
     *
     * @see Element#getId()
     */
    @SuppressWarnings("unchecked")
    public static Map<Class<? extends Spawnable>, Map<String, Spawnable>> getAllSpawnableElements() {
        final Map<Class<? extends Element>, Map<String, Element>> allElements = getAllElements();
        final Map<Class<? extends Spawnable>, Map<String, Spawnable>> map = new HashMap<>();

        for (Map.Entry<Class<? extends Element>, Map<String, Element>> entry : allElements.entrySet()) {

            final Map<String, Spawnable> s = new HashMap<>();
            final Map<String, Element> v = entry.getValue();

            for (Map.Entry<String, Element> vEntry : v.entrySet()) {
                final Element el = vEntry.getValue();
                if (el instanceof Spawnable) {
                    s.put(vEntry.getKey(), (Spawnable) el);
                }
            }

            map.put((Class<? extends Spawnable>) entry.getKey(), s);
        }
        return ImmutableMap.copyOf(map);
    }

    /**
     * @param clazz the class
     * @param <E>   the spawnable type
     *
     * @return a map of spawnable elements based on the given spawnable class
     */
    public static <E extends Spawnable> Map<String, E> getSpawnableElements(Class<E> clazz) {
        return ImmutableMap.copyOf(getElements(clazz));
    }

    /**
     * Attempts to retrieve a spawnable element based on the block data
     *
     * @param block the block to check for
     *
     * @return the spawnable element
     *
     * @throws Utils.SearchNotFoundException if the block is not a spawnable element
     */
    public static Spawnable of(Block block) throws Utils.SearchNotFoundException {

        final Spawnable el = iterate(block, getSpawnableElements());

        if (el != null) {
            return el;
        }
        throw new Utils.SearchNotFoundException();
    }

    /**
     * @return a collection of spawnable elements
     */
    public static Collection<Spawnable> getSpawnableElements() {
        final Map<Class<? extends Spawnable>, Map<String, Spawnable>> allSpawnableElements = getAllSpawnableElements();
        final Collection<Map<String, Spawnable>> vals = allSpawnableElements.values();

        final Collection<Spawnable> spawnables = new ArrayList<>();
        for (Map<String, Spawnable> map : vals) {
            for (Map.Entry<String, Spawnable> entry : map.entrySet()) {
                spawnables.add(entry.getValue());
            }
        }
        return spawnables;
    }

    /**
     * Deserializes the spawnable element. This is a helper method for other deserialization methods implementing this
     * class, allows them to deserialize this object's and the implementing class' serialization in one method. This
     * method checks for missing keys and will add an exception to the function if some keys are missing.
     *
     * @param map                the serialized version of the spawnable element
     * @param markerIcon         the dynmap marker icon
     * @param name               the name of this spawnable
     * @param clazz              the class we are converting to (here just for stack trace purposes)
     * @param conversionFunction the function that converts a spawnable element into the desired object (you are given a
     *                           SpawnableElement wrapper that returns {@code null} in {@link #getItemTypeHolder()}, but
     *                           everything else is a non-null, thus usable) If all keys are present, the exception
     *                           argument is {@code null}
     * @param <T>                the desired class type
     *
     * @return the desired object
     *
     * @throws ProfessionObjectInitializationException if an exception occurred during deserialization or there are
     *                                                 missing keys
     */
    @NotNull
    protected static <T extends Spawnable> T deserializeSpawnable(
            final Map<String, Object> map,
            final String markerIcon,
            final String name,
            final Class<T> clazz,
            final Function<Spawnable, T> conversionFunction) throws ProfessionObjectInitializationException {
        return deserializeSpawnable(map, markerIcon, name, clazz, conversionFunction,
                Collections.emptyList());
    }

    /**
     * Deserializes the spawnable element. This is a helper method for other deserialization methods implementing this
     * class, allows them to deserialize this object's and the implementing class' serialization in one method. This
     * method checks for missing keys and will add an exception to the function if some keys are missing.
     *
     * @param map                the serialized version of the spawnable element
     * @param markerIcon         the dynmap marker icon
     * @param name               the name of this spawnable
     * @param clazz              the class we are converting to (here just for stack trace purposes)
     * @param conversionFunction the function that converts a spawnable element into the desired object (you are given a
     *                           SpawnableElement wrapper that returns {@code null} in {@link #getItemTypeHolder()}, but
     *                           everything else is a non-null, thus usable) If all keys are present, the exception
     *                           argument is {@code null}
     * @param ignoredKeys        the ignored keys to check for in the file enums
     * @param keys               the file enums
     * @param <T>                the desired class type
     *
     * @return the desired object
     *
     * @throws ProfessionObjectInitializationException if an exception occurred during deserialization or there are
     *                                                 missing keys
     */
    @NotNull
    @SafeVarargs
    protected static <T extends Spawnable> T deserializeSpawnable(
            final Map<String, Object> map,
            final String markerIcon,
            final String name,
            final Class<T> clazz,
            final Function<Spawnable, T> conversionFunction,
            final Collection<String> ignoredKeys,
            final Class<? extends FileEnum>... keys)
            throws ProfessionObjectInitializationException {

        // first check if there are missing keys
        checkForMissingKeys(map, clazz, List.of(SPAWN_POINT.s), Strings.SpawnableElementEnum.class);

        // get all possible data from the map
        final ItemStack material = ItemUtils.deserializeMaterial((String) map.get(MATERIAL.s));
        final MemorySection particleSection = (MemorySection) map.get(PARTICLE.s);
        final ParticleData particleData = ParticleData.deserialize(particleSection.getValues(true));

        // create a dummy spawnable
        // this will also check for missing element keys in the map
        final Spawnable spawnable = deserializeElement(map, name, Spawnable.class, x ->
                new Spawnable(x.getId(), x.getName(), material.getType(), (byte) material.getDurability(),
                        particleData, markerIcon, false) {

                    @Override
                    protected @NotNull ItemTypeHolder<?, ?> getItemTypeHolder() {
                        throw new UnsupportedOperationException();
                    }
                }, ignoredKeys, keys);

        // convert the spawnable to an object of programmer's desire
        final T convertedSpawnable = conversionFunction.apply(spawnable);

        // the spawnable could not somehow be converted, throw an ex
        if (convertedSpawnable == null) {
            throw new ProfessionObjectInitializationException("Could not deserialize a spawnable element due to an " +
                    "error");
        }

        // then get spawn points
        final Collection<ISpawnPoint> spawnPointLocations;
        try {
            spawnPointLocations = new ArrayList<>(SpawnPoint.deserializeAll(map, convertedSpawnable,
                    convertedSpawnable.getItemTypeHolder().getMarkerSetId()));
        } catch (ProfessionObjectInitializationException e) {

            // set the exception class to the deserialization object for further clearance
            e.setClazz(clazz);
            throw e;
        }
        convertedSpawnable.addSpawnPoints(spawnPointLocations);
        return convertedSpawnable;
    }

    /**
     * We need to save spawn points every time they are modified -  the item type holder provides {@link
     * ItemTypeHolder#save(boolean)} method
     *
     * @return the item type holder of this object
     */
    @NotNull
    protected abstract ItemTypeHolder<?, ?> getItemTypeHolder();

    /**
     * Spawns all elements with given filter (e.g. you are able to spawn only elements that are inside some kind of
     * world)
     *
     * @param spawnPointFilter the filter to use
     */
    public static void scheduleSpawnAll(Predicate<ISpawnPoint> spawnPointFilter) {
        ProfessionLogger.log("Schedule spawn all", Level.CONFIG);
        final Consumer<ISpawnPoint> action = z -> {
            try {
                z.scheduleSpawn();
            } catch (SpawnException e) {
                ProfessionLogger.logError(e);
            }
        };
        doForAllElements(spawnPointFilter, action);
    }

    /**
     * Performs an action for all elements with a filter
     *
     * @param spawnPointFilter the filter
     * @param action           the action
     */
    private static void doForAllElements(Predicate<ISpawnPoint> spawnPointFilter, Consumer<ISpawnPoint> action) {
        final Collection<Spawnable> spawnables = getSpawnableElements();
        for (Spawnable spawn : spawnables) {
            for (ISpawnPoint sp : spawn.getSpawnPoints()) {
                if (!spawnPointFilter.test(sp)) {
                    continue;
                }
                // log
                ProfessionLogger.log(sp, Level.CONFIG);
                action.accept(sp);
            }
        }
    }

    public static void unloadSpawnables() {
        despawnAll(x -> true);
        SpawnPoint.unloadAll();
        unloadElements();
    }

    /**
     * Despawns all elements with given filter (e.g. you are able to spawn only elements that are inside some kind of
     * world)
     *
     * @param spawnPointFilter the filter to use
     */
    public static void despawnAll(Predicate<ISpawnPoint> spawnPointFilter) {
        ProfessionLogger.log("Despawn all", Level.CONFIG);

        // for each registered spawnable element
        // for each ID of spawnable elements
        // for each spawn point spawn
        final Consumer<ISpawnPoint> action = ISpawnPoint::despawn;
        doForAllElements(spawnPointFilter, action);
    }

    /**
     * @param serialNumber the serialNumber
     *
     * @return {@code true} if a spawn point with that serialNumber exists, {@code false} otherwise
     */
    @SuppressWarnings("unused")
    public final boolean isSpawnPoint(Location location, int serialNumber) {
        ISpawnPoint sp = getSpawnPoint(location);
        // checks for both null and just makes sure that this is the implemented spawn point
        if (sp == null) {
            return false;
        }
        return sp.getSerialNumber() == serialNumber;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put(MATERIAL.s, ItemUtils.serializeMaterial(getMaterial(), getMaterialData()));
        map.put(PARTICLE.s, getParticleData().serialize());

        int i = 0;
        for (ISpawnPoint spawnPointLocation : getSpawnPoints()) {
            map.put(SPAWN_POINT.s.concat("-" + i++), spawnPointLocation.serialize());
        }
        return map;
    }

    @Override
    public void loadCache(Serializable[] data) {
        super.loadCache(data);
        final int index = super.getOffset();
        final int lastIndex = this.getOffset();

        for (int i = index; i < lastIndex; i += 2) {
            int serialNumber = (int) data[i];
            int respawnTime = (int) data[i + 1];

            final SpawnPoint sp = (SpawnPoint) getSpawnPoint(serialNumber);
            sp.getSpawnTask().setRespawnTime(respawnTime);
        }
    }

    @Override
    public Serializable[] cache() {
        // extend the cache
        final Serializable[] cache = Cacheable.prepareCache(super.cache(), getOffset());

        // the index to write to is equal to parent offset
        int idx = super.getOffset();
        for (ISpawnPoint s : getSpawnPoints()) {
            SpawnPoint sp = (SpawnPoint) s;
            cache[idx++] = sp.getSerialNumber();
            cache[idx++] = sp.getSpawnTask().getRespawnTime();
        }

        return cache;
    }

    @Override
    public int getOffset() {
        return super.getOffset() + getSpawnPoints().size() * 2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMaterial(), getMaterialData());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Spawnable that = (Spawnable) o;
        return getMaterialData() == that.getMaterialData() &&
                getId().equals(that.getId()) &&
                getMaterial() == that.getMaterial();
    }

    @Override
    public String toString() {
        return "Spawnable{" +
                "spawnPoints=" + spawnPoints.size() +
                ", material=" + material +
                ", materialData=" + materialData +
                ", markerIcon='" + markerIcon + '\'' +
                ", canSpawn=" + canSpawn +
                "} " + super.toString();
    }

    @Override
    public boolean canSpawn() {
        return canSpawn;
    }


    @Override
    public void setCanSpawn(boolean canSpawn) {
        this.canSpawn = canSpawn;
        spawnPoints.values().forEach(x -> x.setSpawnable(canSpawn));
    }


    @Override
    public String getMarkerIcon() {
        return markerIcon;
    }

    @Override
    public final ParticleData getParticleData() {
        return particleData;
    }

    @Override
    public final Material getMaterial() {
        return material;
    }

    @Override
    public final byte getMaterialData() {
        return materialData;
    }


    @Override
    public final boolean isSpawnPoint(Location location) {
        return spawnPoints.containsKey(location);
    }

    @Override
    public boolean isSpawnPoint(int serialNumber) {
        return getSpawnPoint(serialNumber) != null;
    }

    @Override
    public Collection<ISpawnPoint> getSpawnPoints() {
        return ImmutableList.copyOf(spawnPoints.values());
    }

    @Override
    public Collection<ISpawnPoint> getSpawnedElements() {
        return spawnPoints.values()
                .stream()
                .filter(ISpawnPoint::isSpawned)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public ISpawnPoint getSpawnPoint(Location location) {
        return spawnPoints.get(location);
    }

    @Override
    public ISpawnPoint getSpawnPoint(int serialNumber) {
        return getSpawnPoints()
                .stream()
                .filter(sp -> sp.getSerialNumber() == serialNumber)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addSpawnPoint(ISpawnPoint sp) {
        final ISpawnPoint prev = this.spawnPoints.putIfAbsent(sp.getLocation(), sp);
        if (prev != null) {
            ProfessionLogger.log(String.format("Trying to add a spawn point ID %d with %s location for %s, but it " +
                                    "already exists!", sp.getSerialNumber(),
                            Utils.locationToString(sp.getLocation()), sp.getSpawnableElement()),
                    Level.CONFIG);
            return;
        }
        sp.setSpawnable(canSpawn());
        update();
    }

    @Override
    public void addSpawnPoints(Iterable<ISpawnPoint> spawnPoints) {
        for (ISpawnPoint sp : spawnPoints) {
            addSpawnPoint(sp);
        }
        // log
        ProfessionLogger.log(String.format("Spawn point total for %s: %d", this.getId(),
                this.getSpawnPoints().size()), Level.CONFIG);
    }

    @Override
    public final void removeSpawnPoint(ISpawnPoint spawnPoint) {
        if (spawnPoint == null || !isSpawnPoint(spawnPoint.getLocation())) {
            return;
        }
        final BackupTask.Result result = IOManager.backupFirst();
        if (result != null) {
            if (result == BackupTask.Result.SUCCESS) {
                ProfessionLogger.log(ChatColor.GREEN + "Backed up files before editing file.");
            } else {
                ProfessionLogger.log(
                        ChatColor.RED + "Failed to back up files. Contact admins to check console output!");
            }
        }
        spawnPoint.despawn();
        spawnPoints.remove(spawnPoint.getLocation());
        update();
    }

    @Override
    public void removeSpawnPoint(Location location) {
        ISpawnPoint sp = getSpawnPoint(location);
        if (sp != null) {
            removeSpawnPoint(sp);
        }
    }

    @Override
    public final void removeSpawnPoint(int serialNumber) {
        ISpawnPoint sp = getSpawnPoint(serialNumber);
        if (sp != null) {
            removeSpawnPoint(sp);
        }
    }

    /**
     * Saves this object to file
     */
    public final void update() {
        try {
            getItemTypeHolder().save(false);
        } catch (IOException e) {
            ProfessionLogger.logError(e);
        }
    }


}
