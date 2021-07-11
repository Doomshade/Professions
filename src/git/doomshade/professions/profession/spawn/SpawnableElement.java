package git.doomshade.professions.profession.spawn;

import com.google.common.collect.ImmutableMap;
import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.api.spawn.ILocationElement;
import git.doomshade.professions.api.spawn.ISpawnableElement;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.utils.ExtendedLocation;
import git.doomshade.professions.task.BackupTask;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.ParticleData;
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
import java.util.function.Function;

import static git.doomshade.professions.profession.spawn.SpawnableElement.SpawnableElementEnum.*;

/**
 * Manages spawns of spawnable elements. This class already implements {@link ILocationElement} interface.
 *
 * @param <SpawnPointType> the spawn point type (the type is useful only if you have custom class extending {@link SpawnPoint})
 * @author Doomshade
 * @version 1.0
 */
public abstract class SpawnableElement<SpawnPointType extends SpawnPoint> extends LocationElement
        implements ConfigurationSerializable, ISpawnableElement<SpawnPointType> {

    private final List<ExtendedLocation> spawnPointLocations;
    private final HashMap<Location, SpawnPointType> spawnPoints = new HashMap<>();
    private static final HashMap<
            Class<? extends SpawnableElement>,
            HashMap<String, SpawnableElement<? extends SpawnPoint>>
            > SPAWNABLE_ELEMENTS = new HashMap<>();

    protected SpawnableElement(String id, String name, Material material, byte materialData,
                               List<ExtendedLocation> spawnPointLocations, ParticleData particleData) {
        this(id, name, material, materialData, spawnPointLocations, particleData, true);
    }

    private SpawnableElement(String id, String name, Material material, byte materialData,
                             List<ExtendedLocation> spawnPointLocations, ParticleData particleData, boolean registerElement) {
        super(id, name, material, materialData, particleData);
        this.spawnPointLocations = new ArrayList<>(spawnPointLocations);

        if (!rejectedIds().contains(id) && registerElement) {
            final HashMap<String, SpawnableElement<? extends SpawnPoint>> map = SPAWNABLE_ELEMENTS.getOrDefault(getClass(), new HashMap<>());
            map.put(id, this);
            SPAWNABLE_ELEMENTS.put(getClass(), map);
        }
    }

    public static <T extends SpawnPoint, E extends SpawnableElement<T>> Map<String, E> getElements(Class<E> of) {
        return (Map<String, E>) SPAWNABLE_ELEMENTS.get(of);
    }

    public static <T extends SpawnPoint, E extends SpawnableElement<T>> ImmutableMap<String, E> getAllElements() {
        final Collection<HashMap<String, SpawnableElement<? extends SpawnPoint>>> values = SPAWNABLE_ELEMENTS.values();

        final HashMap<String, SpawnableElement<? extends SpawnPoint>> map = new HashMap<>();
        for (HashMap<String, SpawnableElement<? extends SpawnPoint>> v : values) {
            map.putAll(v);
        }
        return (ImmutableMap<String, E>) ImmutableMap.copyOf(map);
    }

    public <E extends SpawnableElement<SpawnPointType>> Map<String, E> getElements() {
        return (Map<String, E>) SPAWNABLE_ELEMENTS.get(getClass());
    }

    public static <T extends SpawnPoint, E extends SpawnableElement<T>> E get(Class<E> of, String id) {
        return getElements(of).get(id);
    }

    /**
     * <p>Now this might be a little confusing
     * <p>The way this works: we retrieve information from block (the location and material by default, additional args can be added by overriding {@link SpawnableElement#get()})
     * <p>and then we make sure that the material is equal to this object, and that location is one of its spawn points
     * <p>If these two conditions are met, the object holding the correct spawn points and material is returned.
     * <p>e.g. A block on {0,0,0} with yellow_flower material will return the herb that is the correct material,
     * <p>and contains the spawn point {0,0,0}
     *
     * @param block the block
     * @param <T>   the returned type (so there's no cast needed)
     * @return the spawnable element
     * @throws Utils.SearchNotFoundException if the block is not a spawnable element
     */
    public static <T extends SpawnableElement<? extends SpawnPoint>> T of(Block block, Class<T> elementClass) throws Utils.SearchNotFoundException {

        final SpawnableElement<? extends SpawnPoint> el = iterate(block, SPAWNABLE_ELEMENTS.get(elementClass).values());

        if (el != null) {
            return (T) el;
        }

        throw new Utils.SearchNotFoundException();
    }

    public static SpawnableElement<? extends SpawnPoint> of(Block block) throws Utils.SearchNotFoundException {
        for (HashMap<String, SpawnableElement<? extends SpawnPoint>> e : SPAWNABLE_ELEMENTS.values()) {

            final SpawnableElement<? extends SpawnPoint> el = iterate(block, e.values());

            if (el != null) {
                return el;
            }
        }
        throw new Utils.SearchNotFoundException();
    }

    private static <T extends SpawnableElement<? extends SpawnPoint>> SpawnableElement<? extends SpawnPoint> iterate(Block block, Iterable<T> iterable) {
        for (T el : iterable) {

            // el.get() = function, that transforms a spawnable element instance into elementClass instance
            if (el.get() != null) {
                final SpawnableElement<? extends SpawnPoint> spawn = el.get().apply(block);

                if (spawn != null) {
                    return spawn;
                }
            }
        }
        return null;
    }


    /**
     * Adds a spawn point
     *
     * @param sp the spawn point
     */
    public final void addSpawnPoint(ExtendedLocation sp) {
        this.spawnPointLocations.add(sp);
        update();
    }

    /**
     * Removes a spawn point with given id
     *
     * @param id the id of the spawn point
     */
    public final void removeSpawnPoint(int id) {
        if (!isSpawnPoint(id)) return;
        removeSpawnPoint(spawnPointLocations.get(id));
    }

    /**
     * Removes given spawn point
     *
     * @param sp the spawn point
     */
    public final void removeSpawnPoint(ExtendedLocation sp) {
        if (sp == null || !isSpawnPointLocation(sp)) {
            return;
        }
        final BackupTask.Result result = IOManager.backupFirst();
        if (result != null) {
            if (result == BackupTask.Result.SUCCESS)
                ProfessionLogger.log(ChatColor.GREEN + "Backed up files before editing file.");
            else
                ProfessionLogger.log(ChatColor.RED + "Failed to back up files. Contact admins to check console output!");
        }
        spawnPointLocations.remove(sp);
        getSpawnPoints(sp).despawn();
        update();
    }

    /**
     * Saves this to file
     */
    public final void update() {
        try {
            getItemTypeHolder().save(false);
        } catch (IOException e) {
            ProfessionLogger.logError(e);
        }
    }

    public final SpawnPointType getSpawnPoints(Location location) {
        if (!spawnPoints.containsKey(location)) {
            spawnPoints.put(location, createSpawnPoint(location));
        }
        return spawnPoints.get(location);
    }

    public final ImmutableMap<Location, SpawnPointType> getSpawnPoints() {
        return ImmutableMap.copyOf(spawnPoints);
    }

    /**
     * We need to save spawn points every time they are modified -  the item type holder provides {@link ItemTypeHolder#save(boolean)} method
     *
     * @return the item type holder of this class
     */
    @NotNull
    protected abstract ItemTypeHolder<?, ?> getItemTypeHolder();

    /**
     * @param location the location to check for
     * @return {@code true} if the location is a spawn point, {@code false} otherwise
     */
    public final boolean isSpawnPointLocation(Location location) {
        return spawnPointLocations.contains(new ExtendedLocation(location));
    }

    /**
     * @param id the id
     * @return {@code true} if a spawn point with that id exists, {@code false} otherwise
     */
    public final boolean isSpawnPoint(int id) {
        return spawnPointLocations.get(id) != null;
    }

    /**
     * @return a list of spawn points
     */
    public final List<ExtendedLocation> getSpawnPointLocations() {
        return spawnPointLocations;
    }

    /**
     * Schedules a spawn on all {@link SpawnableElement}s on the server
     */
    public final void scheduleSpawns() {
        for (ExtendedLocation sp : spawnPointLocations) {
            SpawnPointType locationOptions = getSpawnPoints(sp);
            locationOptions.scheduleSpawn();

        }
    }

    public final void scheduleSpawns(int respawnTime) {
        for (int i = 0; i < spawnPointLocations.size(); i++) {
            ExtendedLocation sp = spawnPointLocations.get(i);
            SpawnPointType locationOptions = getSpawnPoints(sp);
            locationOptions.scheduleSpawn(respawnTime, i);
        }
    }

    /**
     * Despawns all {@link SpawnableElement}s on the server
     *
     * @param hideOnDynmap whether or not to hide a marker icon on dynmap, this boolean has no effect if the provided {@code LocOptions} is not an instance of {@link MarkableSpawnPoint}
     */
    public final void despawnAll(boolean hideOnDynmap) {
        for (ExtendedLocation sp : spawnPointLocations) {
            SpawnPointType locationOptions = getSpawnPoints(sp);
            if (locationOptions instanceof MarkableSpawnPoint) {
                ((MarkableSpawnPoint) locationOptions).despawn(hideOnDynmap);
            } else {
                locationOptions.despawn();
            }
        }
    }

    /**
     * Despawns all {@link SpawnableElement}s on the server
     */
    public final void despawnAll() {
        despawnAll(true);
    }

    /**
     * Spawns all {@link SpawnableElement}s on the server
     */
    public final void spawnAll() throws SpawnException {
        for (ExtendedLocation sp : spawnPointLocations) {
            SpawnPointType locationOptions = getSpawnPoints(sp);
            locationOptions.spawn();

        }
    }

    protected Set<String> rejectedIds() {
        return new HashSet<>();
    }

    /**
     * @return the spawnable location element if the provided argument matched a location element
     */
    protected Function<Block, ? extends SpawnableElement<? extends SpawnPoint>> get() {
        return block -> {
            Material mat = block.getType();
            Location location = block.getLocation();
            for (HashMap<String, SpawnableElement<? extends SpawnPoint>> v : SPAWNABLE_ELEMENTS.values()) {
                try {
                    return Utils.findInIterable(v.values(), x -> x.getMaterial() == mat && x.isSpawnPointLocation(location));
                } catch (Utils.SearchNotFoundException ignored) {
                }
            }
            return null;
        };
    }

    @Override
    public Map<String, Object> serialize() {
        return new HashMap<String, Object>() {
            {
                put(ID.s, getId());

                put(MATERIAL.s, ItemUtils.serializeMaterial(getMaterial(), getMaterialData()));

                int i = 0;

                for (ExtendedLocation spawnPointLocation : getSpawnPointLocations()) {
                    put(SPAWN_POINT.s.concat("-" + i++), spawnPointLocation.serialize());
                }
                put(PARTICLE.s, getParticleData().serialize());
            }

        };
    }

    public static Set<String> getMissingKeys(Map<String, Object> map) {
        return Utils.getMissingKeys(map, Arrays.stream(SpawnableElementEnum.values()).filter(x -> x != SPAWN_POINT).toArray(SpawnableElementEnum[]::new));
    }

    /**
     * Deserializes the spawnable element. This is a helper method for other deserialization methods implementing this class,
     * allows them to deserialize this class' serialization and the implementing class' serialization in one method.
     * This method checks for missing keys and will add an exception to the function if some keys are missing.
     *
     * @param map                the serialized version of the spawnable element
     * @param conversionFunction the function that converts a spawnable element into the desired object (you are given a SpawnableElement wrapper
     *                           that does not create location options and returns {@code null} in {@link #getItemTypeHolder()}, but
     *                           everything else is an non-null, thus usable)
     *                           If all keys are present, the exception argument is {@code null}
     * @param clazz              the class we are converting to (here just for stack trace purposes)
     * @param <T>                the desired object type
     * @return the desired object
     */
    public static <A extends SpawnPoint, T extends SpawnableElement<A>> T deserialize(
            Map<String, Object> map,
            Class<T> clazz,
            BiFunction<SpawnableElement<A>, ProfessionObjectInitializationException, T> conversionFunction)
            throws ProfessionObjectInitializationException {

        // get missing keys and initialize exception
        final Set<String> missingKeys = getMissingKeys(map);

        // deserialize spawn points before checking for missing keys as missing keys have no way of checking whether the spawn points deserialized correctly
        List<ExtendedLocation> spawnPointLocations;
        try {
            spawnPointLocations = new ArrayList<>(ExtendedLocation.deserializeAll(map));
        } catch (ProfessionObjectInitializationException e) {

            // set the exception class to the deserialization object for further clearance
            e.setClazz(clazz);
            return conversionFunction.apply(null, e);
        }


        final ProfessionObjectInitializationException ex = new ProfessionObjectInitializationException(
                clazz,
                missingKeys,
                ProfessionObjectInitializationException.ExceptionReason.MISSING_KEYS
        );
        if (!missingKeys.isEmpty()) {
            return conversionFunction.apply(null, ex);
        }

        String id = (String) map.get(ID.s);
        ItemStack material = ItemUtils.deserializeMaterial((String) map.get(MATERIAL.s));
        MemorySection particleSection = (MemorySection) map.get(PARTICLE.s);
        final ParticleData particleData = ParticleData.deserialize(particleSection.getValues(true));
        SpawnableElement<A> spawnableElement = new SpawnableElement<>(id, "SpawnableElementName", material.getType(), (byte) material.getDurability(), spawnPointLocations, particleData) {

            @Override
            public A createSpawnPoint(Location location) {
                return null;
            }

            @Override
            protected @NotNull ItemTypeHolder<?, ?> getItemTypeHolder() {
                return null;
            }
        };

        return conversionFunction.apply(spawnableElement, ex);
    }

    // worst case scenario (if this does not work) - replace BiFunction with Function..

    public static <A extends SpawnPoint, T extends SpawnableElement<A>> T deserialize(Map<String, Object> map, Class<T> clazz, Function<SpawnableElement<A>, T> conversionFunction) throws ProfessionObjectInitializationException {
        return deserialize(map, clazz, (el, e) -> conversionFunction.apply(el));
    }

    /**
     * Enum for keys in file
     */
    public enum SpawnableElementEnum implements FileEnum {
        SPAWN_POINT("spawnpoint"),
        ID("id"),
        MATERIAL("material"),
        PARTICLE("particle");

        public final String s;

        SpawnableElementEnum(String s) {
            this.s = s;
        }

        @Override
        public EnumMap<SpawnableElementEnum, Object> getDefaultValues() {
            return new EnumMap<SpawnableElementEnum, Object>(SpawnableElementEnum.class) {
                {
                    put(SPAWN_POINT, ExtendedLocation.EXAMPLE.serialize());
                    put(ID, "some_id");
                    put(MATERIAL, Material.GLASS);
                    put(PARTICLE, new ParticleData());
                }
            };
        }

        @Override
        public String toString() {
            return s;
        }

    }

    @Deprecated
    private static class SpawnableElementImpl<T extends SpawnPoint> extends SpawnableElement<T> {

        protected SpawnableElementImpl(String id, String name, Material material, byte materialData, List<ExtendedLocation> spawnPointLocations, ParticleData particleData) {
            super(id, name, material, materialData, spawnPointLocations, particleData, false);
        }

        @Override
        public T createSpawnPoint(Location location) {
            return null;
        }

        @Override
        protected @NotNull ItemTypeHolder<?, ?> getItemTypeHolder() {
            return null;
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpawnableElement<?> that = (SpawnableElement<?>) o;
        return getMaterialData() == that.getMaterialData() &&
                getId().equals(that.getId()) &&
                getMaterial() == that.getMaterial();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMaterial(), getMaterialData());
    }

    // was originally used to validate args inside staticGet() function
   /* protected final void validateArguments(List<?> args, Class<?>... desiredInstances) {
        if (args.size() != desiredInstances.length)
            throw new IllegalArgumentException(
                    String.format("The args size does not match (%s = %d, %s = %d)", "list size", args.size(), "desired implementations", desiredInstances.length)
            );

        StringBuilder invalidArgs = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            if (!args.get(i).getClass().isAssignableFrom(desiredInstances[i])) {
                invalidArgs.append(String.format("\narg %d = %s, desired = %s", i, args.get(i).getClass().getSimpleName(), desiredInstances[i]));
            }
        }

        if (invalidArgs.length() > 0){
            throw new IllegalArgumentException(invalidArgs.toString());
        }
    }*/
}
