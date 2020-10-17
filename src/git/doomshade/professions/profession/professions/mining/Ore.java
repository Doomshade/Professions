package git.doomshade.professions.profession.professions.mining;

import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.professions.mining.spawn.OreLocationOptions;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.profession.utils.SpawnPoint;
import git.doomshade.professions.profession.utils.SpawnableElement;
import git.doomshade.professions.profession.utils.YieldResult;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.ParticleData;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;

import static git.doomshade.professions.profession.professions.mining.Ore.OreEnum.RESULT;

/**
 * Custom class for {@link git.doomshade.professions.profession.types.ItemType}.
 * Here I wanted to have a custom mining result, I'd have otherwise only passed {@link Material} as a generic argument to {@link OreItemType}.
 *
 * @author Doomshade
 */
public class Ore extends SpawnableElement<OreLocationOptions> implements ConfigurationSerializable {

    public static final HashMap<String, Ore> ORES = new HashMap<>();
    private static final String EXAMPLE_ORE_ID = "example-ore";
    public static final Ore EXAMPLE_ORE = new Ore(EXAMPLE_ORE_ID, "Example ore name", Material.COAL_ORE, Collections.emptySortedSet(), new ArrayList<>(), new ParticleData());
    private SortedSet<YieldResult> results;

    private Ore(String id, String name, Material oreMaterial, SortedSet<YieldResult> results, List<SpawnPoint> spawnPoints, ParticleData particleData) {
        super(id, name, oreMaterial, (byte) 0, spawnPoints, particleData);
        this.results = results;
        if (!rejectedIds().contains(id))
            ORES.put(id, this);
    }

    @Override
    protected Set<String> rejectedIds() {
        return Collections.singleton(EXAMPLE_ORE_ID);
    }

    @Nullable
    public static Ore getOre(String id) {
        return ORES.get(id);
    }

    /**
     * Required deserialize method of {@link ConfigurationSerializable}
     *
     * @param map serialized Ore
     * @return deserialized Ore
     * @throws ProfessionObjectInitializationException when Ore is not initialized correctly
     */
    public static Ore deserialize(Map<String, Object> map, final String name) throws ProfessionObjectInitializationException {

        final ProfessionObjectInitializationException ex = new ProfessionObjectInitializationException(OreItemType.class, new HashSet<>());


        final BiFunction<SpawnableElement<?>, ProfessionObjectInitializationException, Ore> func = (x, y) -> {
            // if there are missing keys, add it to exception
            if (y != null) {
                ex.add(y);
            }
            SortedSet<YieldResult> results = new TreeSet<>();

            MemorySection dropSection;

            int i = 0;
            while ((dropSection = (MemorySection) map.get(RESULT.s.concat("-" + i))) != null) {
                results.add(YieldResult.deserialize(dropSection.getValues(false)));
                i++;
            }
            return new Ore(x.getId(), name, x.getMaterial(), results, x.getSpawnPoints(), x.getParticleData());
        };

        // if there are missing keys, throw ex
        if (!ex.getKeys().isEmpty()) {
            throw ex;
        }
        return SpawnableElement.deserialize(map, Ore.class, func);
    }
    /*
        String id = (String) map.get(ID.s);
        Material mat = Material.getMaterial((String) map.get(MATERIAL.s));

        SortedList<YieldResult> results = new SortedList<>(Comparator.naturalOrder());

        MemorySection dropSection;

        int i = 0;
        while ((dropSection = (MemorySection) map.get(RESULT.s.concat("-" + i))) != null) {
            results.add(YieldResult.deserialize(dropSection.getValues(false)));
            i++;
        }

        List<SpawnPoint> spawnPoints = new ArrayList<>();
        MemorySection spawnSection;
        int x = 0;
        while ((spawnSection = ((MemorySection) map.get(SPAWN_POINT.s.concat("-" + x)))) != null) {
            spawnPoints.add(SpawnPoint.deserialize(spawnSection.getValues(false)));
            x++;
        }

        MemorySection particleSection = (MemorySection) map.get(PARTICLE.s);

        return new Ore(id, name, mat, results, spawnPoints, ParticleData.deserialize(particleSection.getValues(true)));
    }*/

    @Override
    protected OreLocationOptions createLocationOptions(Location location) {
        return new OreLocationOptions(location, this);
    }

    @NotNull
    @Override
    protected ItemTypeHolder<OreItemType> getItemTypeHolder() {
        return Professions.getProfessionManager().getItemTypeHolder(OreItemType.class);
    }

    @Override
    public Map<String, Object> serialize() {

        final Map<String, Object> map = super.serialize();

        int i = 0;
        for (YieldResult result : results) {
            map.put(RESULT.s.concat("-" + i++), result.serialize());
        }

        return map;
    }

    /**
     * @return the mining result
     */
    @Nullable
    public ItemStack getMiningResult() {
        double random = Math.random() * 100;

        for (YieldResult result : results) {
            if (random < result.chance) {
                return result.drop;
            }
        }

        return null;
    }

    /**
     * Enum for keys in file
     */
    enum OreEnum implements FileEnum {
        RESULT("drop");

        final String s;

        OreEnum(String s) {
            this.s = s;
        }

        @Override
        public EnumMap<OreEnum, Object> getDefaultValues() {
            return new EnumMap<OreEnum, Object>(OreEnum.class) {
                {
                    put(RESULT, new YieldResult(40d, ItemUtils.EXAMPLE_RESULT));
                }
            };
        }

        @Override
        public String toString() {
            return s;
        }
    }

    @Override
    public String toString() {
        return String.format("Ore{ID=%s\nName=%s\nMaterial=%s\nSpawnPoints=%s}", getId(), getName(), getMaterial(), getSpawnPoints());
    }
}
