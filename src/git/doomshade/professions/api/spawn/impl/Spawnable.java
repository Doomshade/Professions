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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.api.spawn.ISpawnPoint;
import git.doomshade.professions.api.spawn.ISpawnable;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.task.BackupTask;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.ParticleData;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static git.doomshade.professions.utils.Strings.SpawnableElementEnum.*;

/**
 * Manages spawns of spawnable elements. This class already implements {@link ISpawnable} interface.
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public abstract class Spawnable extends Element
        implements ConfigurationSerializable, ISpawnable {

    //private final List<? extends ISpawnPoint> spawnPointLocations;
    private final HashMap<Location, ISpawnPoint> spawnPoints = new HashMap<>();

    private final ParticleData particleData;
    private final Material material;
    private final byte materialData;
    private final String name;
    private final String markerIcon;
    private final String id;
    private boolean canSpawn = true;

    protected Spawnable(String id, String name, Material material, byte materialData, ParticleData particleData,
                        String markerIcon)
            throws IllegalArgumentException {
        this(id, name, material, materialData, particleData, markerIcon, true);
    }

    private Spawnable(String id, String name, Material material, byte materialData, ParticleData particleData,
                      String markerIcon,
                      boolean registerElement) throws IllegalArgumentException {
        super(id, registerElement);
        this.id = id;
        this.particleData = particleData;
        this.material = material;
        this.materialData = materialData;
        this.name = name;
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
                return Utils.findInIterable(getAllSpawnableElements().values(),
                        x -> x.getMaterial() == mat && x.isSpawnPoint(location));
            } catch (Utils.SearchNotFoundException ignored) {
            }
            return null;
        };
    }

    public static <E extends Spawnable> Map<String, E> getAllSpawnableElements() {
        @SuppressWarnings("unchecked") final Map<String, E> map = getAllElements().values().stream()
                .flatMap(m -> m.entrySet().stream())
                .filter(entry -> entry.getValue() instanceof Spawnable)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (E) entry.getValue(),
                        (a, b) -> b,
                        LinkedHashMap::new));
        return ImmutableMap.copyOf(map);
    }

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

        final Spawnable el = iterate(block, getAllSpawnableElements().values());

        if (el != null) {
            return el;
        }
        throw new Utils.SearchNotFoundException();
    }

    /**
     * Deserializes any implementations of this class
     *
     * @param map                the serialization map
     * @param clazz              the class implementing this one that this will convert to
     * @param conversionFunction the conversion function that converts a spawnable into the given class
     * @param <T>                the desired class type
     *
     * @return the desired object
     *
     * @throws ProfessionObjectInitializationException if an exception occured during deserialization
     */
    public static <T extends Spawnable> T deserializeSpawnable(
            Map<String, Object> map,
            Class<T> clazz,
            Function<Spawnable, T> conversionFunction)
            throws ProfessionObjectInitializationException {

        return deserializeSpawnable(map, clazz, (el, e) -> conversionFunction.apply(el));
    }

    /**
     * Deserializes the spawnable element. This is a helper method for other deserialization methods implementing this
     * class, allows them to deserialize this object's and the implementing class' serialization in one method. This
     * method checks for missing keys and will add an exception to the function if some keys are missing.
     *
     * @param map                the serialized version of the spawnable element
     * @param conversionFunction the function that converts a spawnable element into the desired object (you are given a
     *                           SpawnableElement wrapper that returns {@code null} in {@link #getItemTypeHolder()}, but
     *                           everything else is a non-null, thus usable) If all keys are present, the exception
     *                           argument is {@code null}
     * @param clazz              the class we are converting to (here just for stack trace purposes)
     * @param <T>                the desired class type
     *
     * @return the desired object
     *
     * @throws ProfessionObjectInitializationException if an exception occurred during deserialization
     */
    public static <T extends Spawnable> T deserializeSpawnable(
            Map<String, Object> map,
            Class<T> clazz,
            BiFunction<Spawnable, ProfessionObjectInitializationException, T> conversionFunction)
            throws ProfessionObjectInitializationException {


        // get missing keys and initialize exception
        final Set<String> missingKeys = Utils.getMissingKeys(map, Arrays.stream(Strings.SpawnableElementEnum.values())
                .filter(x -> x != SPAWN_POINT)
                .toArray(Strings.SpawnableElementEnum[]::new));

        final ProfessionObjectInitializationException ex = new ProfessionObjectInitializationException(
                clazz,
                missingKeys,
                ProfessionObjectInitializationException.ExceptionReason.MISSING_KEYS
        );
        if (!missingKeys.isEmpty()) {
            return conversionFunction.apply(null, ex);
        }

        // get all possible data from the map
        String id = (String) map.get(ID.s);
        ItemStack material = ItemUtils.deserializeMaterial((String) map.get(MATERIAL.s));
        MemorySection particleSection = (MemorySection) map.get(PARTICLE.s);
        final ParticleData particleData = ParticleData.deserialize(particleSection.getValues(true));

        // create a dummy spawnable
        @SuppressWarnings("deprecation") final Spawnable spawnable =
                new Spawnable(id, "SpawnableElementName", material.getType(), (byte) material.getDurability(),
                        particleData, "", false) {

                    @Override
                    protected @NotNull ItemTypeHolder<?, ?> getItemTypeHolder() {
                        throw new UnsupportedOperationException();
                    }
                };

        // convert the spawnable to an object of programmer's desire
        final T convertedSpawnable = conversionFunction.apply(spawnable, ex);

        // TODO add marker set ID SOMEHOW
        // add the spawn points to the spawnable
        /*String markerSetId = ProfessionManager.getInstance()
                .getProfession(convertedSpawnable.getClass())
                .orElseThrow(() -> new ProfessionObjectInitializationException("Sth wrong happened"))
                .getProfessionSettings()
                .getSettings(ProfessionSpecificDefaultsSettings.class)
                .getMarkerSetId();*/
        Collection<ISpawnPoint> spawnPointLocations;
        try {
            spawnPointLocations = new ArrayList<>(SpawnPoint.deserializeAll(map, convertedSpawnable, convertedSpawnable.getItemTypeHolder().getMarkerSetId()));
        } catch (ProfessionObjectInitializationException e) {

            // set the exception class to the deserialization object for further clearance
            e.setClazz(clazz);
            return conversionFunction.apply(null, e);
        }
        convertedSpawnable.addSpawnPoints(spawnPointLocations);
        return convertedSpawnable;
    }

    /**
     * Spawns all elements with given filter (e.g. you are able to spawn only elements that are inside some kind of
     * world)
     *
     * @param spawnPointFilter the filter to use
     */
    public static void scheduleSpawnAll(Predicate<ISpawnPoint> spawnPointFilter) {
        ProfessionLogger.log("Schedule spawn all", Level.CONFIG);
        // for each registered spawnable element
        // for each ID of spawnable elements
        // for each spawn point spawn
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
        final Map<String, Spawnable> elements = getAllSpawnableElements();
        for (Map.Entry<String, Spawnable> entry : elements.entrySet()) {
            Spawnable spawn = entry.getValue();
            for (ISpawnPoint sp : spawn.getSpawnPoints()) {
                if (!spawnPointFilter.test(sp)) {
                    continue;
                }
                ProfessionLogger.log(sp);
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

    @Override
    public final String getName() {
        return name;
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
        map.put(ID.s, getId());

        map.put(MATERIAL.s, ItemUtils.serializeMaterial(getMaterial(), getMaterialData()));
        map.put(PARTICLE.s, getParticleData().serialize());


        int i = 0;
        for (ISpawnPoint spawnPointLocation : getSpawnPoints()) {
            map.put(SPAWN_POINT.s.concat("-" + i++), spawnPointLocation.serialize());
        }
        return map;
    }

    @Override
    public String getId() {
        return id;
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
                "particleData=" + particleData +
                ", material=" + material +
                ", materialData=" + materialData +
                ", name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
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
            ProfessionLogger.log(String.format("Trying to add a spawn point with %s (%s) location, but it already " +
                                    "exists!",
                            Utils.locationToString(sp.getLocation()), sp.getLocation().getWorld()),
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

    /**
     * We need to save spawn points every time they are modified -  the item type holder provides {@link
     * ItemTypeHolder#save(boolean)} method
     *
     * @return the item type holder of this object
     */
    @NotNull
    protected abstract ItemTypeHolder<?, ?> getItemTypeHolder();


}
