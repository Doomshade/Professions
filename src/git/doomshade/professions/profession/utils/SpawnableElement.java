package git.doomshade.professions.profession.utils;

import com.google.common.collect.ImmutableMap;
import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.types.ItemTypeHolder;
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

import static git.doomshade.professions.profession.utils.SpawnableElement.SpawnableElementEnum.*;

/**
 * Manages spawns of spawnable elements. This class already implements {@link LocationElement} interface.
 *
 * @param <LocOptions> the location options type (the type is useful only if you have custom class extending {@link LocationOptions})
 * @author Doomshade
 * @version 1.0
 */
public abstract class SpawnableElement<LocOptions extends LocationOptions> implements LocationElement, ConfigurationSerializable {
    private final List<SpawnPoint> spawnPoints;
    private final HashMap<Location, LocOptions> locationOptions = new HashMap<>();
    private static final HashMap<Class<? extends SpawnableElement>, HashMap<String, SpawnableElement<? extends LocationOptions>>> SPAWNABLE_ELEMENTS = new HashMap<>();
    private final String id;
    private final String name;
    private final Material material;
    private final byte materialData;
    private final ParticleData particleData;


    protected SpawnableElement(String id, String name, Material material, byte materialData, List<SpawnPoint> spawnPoints, ParticleData particleData) {
        this(id, name, material, materialData, spawnPoints, particleData, true);
    }

    private SpawnableElement(String id, String name, Material material, byte materialData, List<SpawnPoint> spawnPoints, ParticleData particleData, boolean registerElement) {
        this.spawnPoints = new ArrayList<>(spawnPoints);
        this.id = id;
        this.name = name;
        this.material = material;
        this.materialData = materialData;
        this.particleData = particleData;

        if (!rejectedIds().contains(id) && registerElement) {
            final HashMap<String, SpawnableElement<? extends LocationOptions>> map = SPAWNABLE_ELEMENTS.getOrDefault(getClass(), new HashMap<>());
            map.put(id, this);
            SPAWNABLE_ELEMENTS.put(getClass(), map);
        }
    }

    public static ImmutableMap<String, SpawnableElement<? extends LocationOptions>> getSpawnableElements(Class<? extends SpawnableElement> of) {
        return ImmutableMap.copyOf(SPAWNABLE_ELEMENTS.get(of));
    }

    public static ImmutableMap<String, SpawnableElement<? extends LocationOptions>> getSpawnableElements() {
        final Collection<HashMap<String, SpawnableElement<? extends LocationOptions>>> values = SPAWNABLE_ELEMENTS.values();

        final HashMap<String, SpawnableElement<? extends LocationOptions>> map = new HashMap<>();
        for (HashMap<String, SpawnableElement<? extends LocationOptions>> v : values) {
            map.putAll(v);
        }
        return ImmutableMap.copyOf(map);
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
    public static <T extends SpawnableElement<? extends LocationOptions>> T of(Block block, Class<T> elementClass) throws Utils.SearchNotFoundException {

        final SpawnableElement<? extends LocationOptions> el = iterate(block, SPAWNABLE_ELEMENTS.get(elementClass).values());

        if (el != null) {
            return (T) el;
        }

        /*
        for (SpawnableElement<? extends LocationOptions> el : SPAWNABLE_ELEMENTS.get(elementClass).values()) {


            // el.get() = function, that transforms a spawnable element instance into elementClass instance
            if (el.get() != null) {
                final SpawnableElement<? extends LocationOptions> spawn = el.get().apply(block);

                if (spawn != null && spawn.getClass().equals(elementClass)) {

                    // log
                    Professions.log(spawn);
                    return (T) spawn;
                }
            }
        }*/
        throw new Utils.SearchNotFoundException();
    }

    public static SpawnableElement<? extends LocationOptions> of(Block block) throws Utils.SearchNotFoundException {
        for (HashMap<String, SpawnableElement<? extends LocationOptions>> e : SPAWNABLE_ELEMENTS.values()) {

            final SpawnableElement<? extends LocationOptions> el = iterate(block, e.values());

            if (el != null) {
                return el;
            }
            /*
            for (SpawnableElement<? extends LocationOptions> el : e.values()) {

                // el.get() = function, that transforms a spawnable element instance into elementClass instance
                if (el.get() != null) {
                    final SpawnableElement<? extends LocationOptions> spawn = el.get().apply(block);

                    if (spawn != null) {

                        // log
                        Professions.log(spawn);
                        return spawn;
                    }
                }
            }*/
        }
        throw new Utils.SearchNotFoundException();
    }

    private static <T extends SpawnableElement<? extends LocationOptions>> SpawnableElement<? extends LocationOptions> iterate(Block block, Iterable<T> iterable) {
        for (T el : iterable) {

            // el.get() = function, that transforms a spawnable element instance into elementClass instance
            if (el.get() != null) {
                final SpawnableElement<? extends LocationOptions> spawn = el.get().apply(block);

                if (spawn != null) {

                    return spawn;
                }
            }
        }
        return null;
    }


    public final void addSpawnPoint(SpawnPoint sp) {
        this.spawnPoints.add(sp);
        update();
    }

    public final void removeSpawnPoint(int id) {
        if (!isSpawnPoint(id)) return;
        removeSpawnPoint(spawnPoints.get(id));
    }

    public final void removeSpawnPoint(SpawnPoint sp) {
        if (sp == null || !isSpawnPoint(sp)) {
            return;
        }
        final BackupTask.Result result = Professions.getInstance().backupFirst();
        if (result != null) {
            if (result == BackupTask.Result.SUCCESS)
                Professions.log(ChatColor.GREEN + "Backed up files before editing file.");
            else
                Professions.log(ChatColor.RED + "Failed to back up files. Contact admins to check console output!");
        }
        spawnPoints.remove(sp);
        getLocationOptions(sp).despawn();
        update();
    }

    public final void update() {
        try {
            getItemTypeHolder().save(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final LocOptions getLocationOptions(Location location) {
        if (!locationOptions.containsKey(location)) {
            locationOptions.put(location, createLocationOptions(location));
        }
        return locationOptions.get(location);
    }

    public final ImmutableMap<Location, LocOptions> getLocationOptions() {
        return ImmutableMap.copyOf(locationOptions);
    }

    protected abstract LocOptions createLocationOptions(Location location);

    /**
     * We need to save spawn points every time they are modified -  the item type holder provides {@link ItemTypeHolder#save(boolean)} method
     *
     * @return the item type holder of this class
     */
    @NotNull
    protected abstract ItemTypeHolder<?> getItemTypeHolder();

    /**
     * @param location the location to check for
     * @return {@code true} if the location is a spawn point, {@code false} otherwise
     */
    public final boolean isSpawnPoint(Location location) {
        return spawnPoints.contains(new SpawnPoint(location));
    }

    /**
     * @param id the id
     * @return {@code true} if a spawn point with that id exists, {@code false} otherwise
     */
    public final boolean isSpawnPoint(int id) {
        return spawnPoints.get(id) != null;
    }

    /**
     * @return a list of spawn points
     */
    public final List<SpawnPoint> getSpawnPoints() {
        return spawnPoints;
    }

    /**
     * Schedules a spawn on all {@link SpawnableElement}s on the server
     */
    public final void scheduleSpawns() {
        for (SpawnPoint sp : spawnPoints) {
            LocOptions locationOptions = getLocationOptions(sp);
            locationOptions.scheduleSpawn();

        }
    }

    /**
     * Despawns all {@link SpawnableElement}s on the server
     *
     * @param hideOnDynmap whether or not to hide a marker icon on dynmap, this boolean has no effect if the provided {@code LocOptions} is not an instance of {@link MarkableLocationOptions}
     */
    public final void despawnAll(boolean hideOnDynmap) {
        for (SpawnPoint sp : spawnPoints) {
            LocOptions locationOptions = getLocationOptions(sp);
            if (locationOptions instanceof MarkableLocationOptions) {
                ((MarkableLocationOptions) locationOptions).despawn(hideOnDynmap);
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
    public final void spawnAll() {
        for (SpawnPoint sp : spawnPoints) {
            LocOptions locationOptions = getLocationOptions(sp);
            locationOptions.scheduleSpawn();

        }
    }

    protected Set<String> rejectedIds() {
        return new HashSet<>();
    }

    /**
     * @return the spawnable location element if the provided argument matched a location element
     */
    protected Function<Block, ? extends SpawnableElement<? extends LocationOptions>> get() {
        return block -> {
            Material mat = block.getType();
            Location location = block.getLocation();
            for (HashMap<String, SpawnableElement<? extends LocationOptions>> v : SPAWNABLE_ELEMENTS.values()) {
                try {
                    return Utils.findInIterable(v.values(), x -> x.material == mat && x.isSpawnPoint(location));
                } catch (Utils.SearchNotFoundException ignored) {
                }
            }
            return null;
        };
    }

    @Override
    public final String getId() {
        return id;
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
    public final ParticleData getParticleData() {
        return particleData;
    }

    @Override
    public Map<String, Object> serialize() {
        return new HashMap<String, Object>() {
            {
                put(ID.s, getId());

                put(MATERIAL.s, ItemUtils.serializeMaterial(getMaterial(), getMaterialData()));

                int i = 0;

                for (SpawnPoint spawnPoint : getSpawnPoints()) {
                    put(SPAWN_POINT.s.concat("-" + i++), spawnPoint.serialize());
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
     * @param clazz              the class we are converting to
     * @param <T>                the desired object type
     * @return the desired object
     */
    public static <T extends SpawnableElement<? extends LocationOptions>> T deserialize(Map<String, Object> map, Class<T> clazz, BiFunction<SpawnableElement<?>, ProfessionObjectInitializationException, T> conversionFunction) {
        final Set<String> missingKeys = getMissingKeys(map);
        ProfessionObjectInitializationException ex = null;
        if (!missingKeys.isEmpty()) {
            ex = new ProfessionObjectInitializationException(clazz, missingKeys);
            ;
        }

        String id = (String) map.get(ID.s);
        ItemStack material = ItemUtils.deserializeMaterial((String) map.get(MATERIAL.s));

        List<SpawnPoint> spawnPoints = new ArrayList<>(SpawnPoint.deserializeAll(map));

        MemorySection particleSection = (MemorySection) map.get(PARTICLE.s);
        final ParticleData particleData = ParticleData.deserialize(particleSection.getValues(true));
        SpawnableElement<?> spawnableElement = new SpawnableElementImpl<>(id, "SpawnableElementName", material.getType(), (byte) material.getDurability(), spawnPoints, particleData);

        return conversionFunction.apply(spawnableElement, ex);
    }

    // worst case scenario (if this does not work) - replace BiFunction with Function..
    public static <T extends SpawnableElement<? extends LocationOptions>> T deserialize(Map<String, Object> map, Class<T> clazz, Function<SpawnableElement<?>, T> conversionFunction) {
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
                    put(SPAWN_POINT, SpawnPoint.EXAMPLE.serialize());
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

    @Override
    public final String getName() {
        return name;
    }

    private static class SpawnableElementImpl<T extends LocationOptions> extends SpawnableElement<T> {

        protected SpawnableElementImpl(String id, String name, Material material, byte materialData, List<SpawnPoint> spawnPoints, ParticleData particleData) {
            super(id, name, material, materialData, spawnPoints, particleData, false);
        }

        @Override
        protected T createLocationOptions(Location location) {
            return null;
        }

        @Override
        protected @NotNull ItemTypeHolder<?> getItemTypeHolder() {
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
