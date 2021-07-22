package git.doomshade.professions.profession.spawn;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.api.spawn.ISpawnPoint;
import git.doomshade.professions.api.spawn.ISpawnable;
import git.doomshade.professions.data.ProfessionSpecificDefaultsSettings;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.ProfessionManager;
import git.doomshade.professions.profession.professions.mining.MiningProfession;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static git.doomshade.professions.profession.spawn.Spawnable.SpawnableElementEnum.*;

/**
 * Manages spawns of spawnable elements. This class already implements {@link ISpawnable} interface.
 *
 * @author Doomshade
 * @version 1.0
 */
public abstract class Spawnable
        implements ConfigurationSerializable, ISpawnable {

    private static final HashMap<
            Class<? extends Spawnable>,
            HashMap<String, Spawnable>
            > SPAWNABLE_ELEMENTS = new HashMap<>();
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
        this.id = id;
        this.particleData = particleData;
        this.material = material;
        this.materialData = materialData;
        this.name = name;
        this.markerIcon = markerIcon;
        //this.spawnPoints = new ArrayList<>(spawnPoints);

        if (!Utils.EXAMPLE_ID.equalsIgnoreCase(id) && registerElement) {
            final HashMap<String, Spawnable> map =
                    SPAWNABLE_ELEMENTS.getOrDefault(getClass(), new HashMap<>());
            final Spawnable spEl = map.putIfAbsent(id, this);
            if (spEl != null) {
                throw new IllegalArgumentException(String.format("An element %s with ID %s already exists!",
                        spEl.getName(), id));
            }
            SPAWNABLE_ELEMENTS.put(getClass(), map);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E extends Spawnable> Map<String, E> getAllElements() {
        final Collection<HashMap<String, Spawnable>> values = SPAWNABLE_ELEMENTS.values();

        final HashMap<String, Spawnable> map = new HashMap<>();
        for (HashMap<String, Spawnable> v : values) {
            map.putAll(v);
        }
        return (ImmutableMap<String, E>) ImmutableMap.copyOf(map);
    }

    public static <E extends Spawnable> E get(Class<E> of, String id) {
        return getElements(of).get(id);
    }

    @SuppressWarnings("unchecked")
    public static <E extends Spawnable> Map<String, E> getElements(Class<E> of) {
        return (Map<String, E>) ImmutableMap.copyOf(SPAWNABLE_ELEMENTS.get(of));
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

        final Spawnable el = iterate(block, SPAWNABLE_ELEMENTS.get(elementClass).values());

        if (el != null) {
            return (T) el;
        }

        throw new Utils.SearchNotFoundException();
    }

    private static <T extends Spawnable> Spawnable iterate(
            Block block, Iterable<T> iterable) {
        for (T el : iterable) {

            // el.get() = function, that transforms a spawnable element instance into elementClass instance
            if (el.get() != null) {
                final Spawnable spawn = el.get().apply(block);

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
            for (HashMap<String, Spawnable> v : SPAWNABLE_ELEMENTS.values()) {
                try {
                    return Utils.findInIterable(v.values(),
                            x -> x.getMaterial() == mat && x.isSpawnPoint(location));
                } catch (Utils.SearchNotFoundException ignored) {
                }
            }
            return null;
        };
    }

    public static Spawnable of(Block block) throws Utils.SearchNotFoundException {
        for (HashMap<String, Spawnable> e : SPAWNABLE_ELEMENTS.values()) {

            final Spawnable el = iterate(block, e.values());

            if (el != null) {
                return el;
            }
        }
        throw new Utils.SearchNotFoundException();
    }

    public static <T extends Spawnable> T deserialize(
            Map<String, Object> map,
            Class<T> clazz,
            Function<Spawnable, T> conversionFunction)
            throws ProfessionObjectInitializationException {

        return deserialize(map, clazz, (el, e) -> conversionFunction.apply(el));
    }

    /**
     * Deserializes the spawnable element. This is a helper method for other deserialization methods implementing this
     * class, allows them to deserialize this object's and the implementing class' serialization in one method. This
     * method checks for missing keys and will add an exception to the function if some keys are missing.
     *
     * @param map                the serialized version of the spawnable element
     * @param conversionFunction the function that converts a spawnable element into the desired object (you are given a
     *                           SpawnableElement wrapper that returns {@code null} in {@link #getItemTypeHolder()}, but
     *                           everything else is an non-null, thus usable) If all keys are present, the exception
     *                           argument is {@code null}
     * @param clazz              the class we are converting to (here just for stack trace purposes)
     * @param <T>                the desired object type
     *
     * @return the desired object
     */
    public static <T extends Spawnable> T deserialize(
            Map<String, Object> map,
            Class<T> clazz,
            BiFunction<Spawnable, ProfessionObjectInitializationException, T> conversionFunction)
            throws ProfessionObjectInitializationException {

        String markerSetId = ProfessionManager.getInstance()
                .getProfession(MiningProfession.class)
                .orElseThrow(() -> new ProfessionObjectInitializationException("Sth wrong happened"))
                .getProfessionSettings()
                .getSettings(
                        ProfessionSpecificDefaultsSettings.class)
                .getMarkerSetId();
        // get missing keys and initialize exception
        final Set<String> missingKeys = Utils.getMissingKeys(map, Arrays.stream(SpawnableElementEnum.values())
                .filter(x -> x != SPAWN_POINT)
                .toArray(SpawnableElementEnum[]::new));

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
        final Spawnable spawnable =
                new Spawnable(id, "SpawnableElementName", material.getType(), (byte) material.getDurability(),
                        particleData, "", false) {

                    @Override
                    protected @NotNull ItemTypeHolder<?, ?> getItemTypeHolder() {
                        throw new UnsupportedOperationException();
                    }
                };

        // convert the spawnable to an object of programmer's desire
        final T convertedSpawnable = conversionFunction.apply(spawnable, ex);

        // add the spawn points to the spawnable
        Collection<ISpawnPoint> spawnPointLocations;
        try {
            spawnPointLocations = new ArrayList<>(SpawnPoint.deserializeAll(map, convertedSpawnable, markerSetId));
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

    private static void doForAllElements(Predicate<ISpawnPoint> spawnPointFilter, Consumer<ISpawnPoint> action) {
        SPAWNABLE_ELEMENTS.values()
                .forEach(x -> x.values()
                        .forEach(y -> y.getSpawnPoints()
                                .stream()
                                .filter(spawnPointFilter)
                                .forEach(action)));
    }

    /**
     * Despawns all elements with given filter (e.g. you are able to spawn only elements that are inside some kind of
     * world)
     *
     * @param spawnPointFilter the filter to use
     */
    public static void despawnAll(Predicate<ISpawnPoint> spawnPointFilter) {
        // for each registered spawnable element
        // for each ID of spawnable elements
        // for each spawn point spawn
        final Consumer<ISpawnPoint> action = ISpawnPoint::despawn;
        doForAllElements(spawnPointFilter, action);
    }

    public Map<String, Spawnable> getElements() {
        return ImmutableMap.copyOf(SPAWNABLE_ELEMENTS.get(getClass()));
    }

    /**
     * @param serialNumber the serialNumber
     *
     * @return {@code true} if a spawn point with that serialNumber exists, {@code false} otherwise
     */
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
        return new HashMap<>() {
            {
                put(ID.s, getId());

                put(MATERIAL.s, ItemUtils.serializeMaterial(getMaterial(), getMaterialData()));

                int i = 0;

                for (ISpawnPoint spawnPointLocation : getSpawnPoints()) {
                    put(SPAWN_POINT.s.concat("-" + i++), spawnPointLocation.serialize());
                }
                put(PARTICLE.s, getParticleData().serialize());
            }

        };
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
            return new EnumMap<>(SpawnableElementEnum.class) {
                {
                    put(SPAWN_POINT, SpawnPoint.EXAMPLE.serialize());
                    put(ID, Utils.EXAMPLE_ID);
                    put(MATERIAL, ItemUtils.EXAMPLE_RESULT.getType());
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
    public boolean canSpawn() {
        return canSpawn;
    }


    @Override
    public void setCanSpawn(boolean canSpawn) {
        this.canSpawn = canSpawn;
        spawnPoints.values().forEach(x -> x.setSpawnable(canSpawn));
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getMarkerIcon() {
        return markerIcon;
    }

    /**
     * @return the particle data about this location element if there should be particles played, {@code null} otherwise
     */
    @Override
    public final ParticleData getParticleData() {
        return particleData;
    }

    /**
     * @return the material of the location's block
     */
    @Override
    public final Material getMaterial() {
        return material;
    }

    /**
     * @return the material data because of special blocks suck as flowers
     */
    @Override
    public final byte getMaterialData() {
        return materialData;
    }

    /**
     * @return the name of the element
     */
    @Override
    public final String getName() {
        return name;
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
            /*ProfessionLogger.log(String.format("A spawn point with %s (%s) location already exists!",
                    Utils.locationToString(sp.getLocation()), sp.getLocation().getWorld()),
                    Level.WARNING);*/
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
     * Saves this to file
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
     * @return the item type holder of this class
     */
    @NotNull
    protected abstract ItemTypeHolder<?, ?> getItemTypeHolder();


}
