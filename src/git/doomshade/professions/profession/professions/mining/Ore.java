package git.doomshade.professions.profession.professions.mining;

import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.professions.mining.spawn.OreLocationOptions;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.profession.utils.LocationElement;
import git.doomshade.professions.profession.utils.SpawnPoint;
import git.doomshade.professions.profession.utils.SpawnableElement;
import git.doomshade.professions.profession.utils.YieldResult;
import git.doomshade.professions.utils.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

import static git.doomshade.professions.profession.professions.mining.Ore.OreEnum.*;

/**
 * Custom class for {@link git.doomshade.professions.profession.types.ItemType}.
 * Here I wanted to have a custom mining result, I'd have otherwise only passed {@link Material} as a generic argument to {@link OreItemType}.
 *
 * @author Doomshade
 */
public class Ore extends SpawnableElement<OreLocationOptions> implements ConfigurationSerializable, LocationElement {

    public static final HashMap<String, Ore> ORES = new HashMap<>();
    private static final String EXAMPLE_ORE_ID = "example-ore";
    public static final Ore EXAMPLE_ORE = new Ore(EXAMPLE_ORE_ID, "Example ore name", Material.COAL_ORE, new SortedList<>(Comparator.naturalOrder()), new ArrayList<>(), new ParticleData());
    private Material oreMaterial;
    private SortedList<YieldResult> results;
    private final String id;
    private ParticleData particleData;
    private String name;

    private Ore(String id, String name, Material oreMaterial, SortedList<YieldResult> results, List<SpawnPoint> spawnPoints, ParticleData particleData) {
        super(spawnPoints);
        this.id = id;
        this.name = name;
        this.oreMaterial = oreMaterial;
        this.particleData = particleData;
        this.results = results;
        if (!id.equals(EXAMPLE_ORE_ID))
            ORES.put(id, this);
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
    public static Ore deserialize(Map<String, Object> map, String name) throws ProfessionObjectInitializationException {
        Set<String> list = Utils.getMissingKeys(map,
                Arrays.stream(OreEnum.values())
                        .filter(x -> x != SPAWN_POINT && x != RESULT)
                        .toArray(OreEnum[]::new));
        if (!list.isEmpty()) {
            throw new ProfessionObjectInitializationException(OreItemType.class, list);
        }

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
    }

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

        return new HashMap<String, Object>() {
            {
                put(ID.s, id);
                put(MATERIAL.s, oreMaterial.name());
                for (int i = 0; i < results.size(); i++) {
                    put(RESULT.s.concat("-" + i), results.get(i).serialize());
                }

                int i = 0;

                for (SpawnPoint spawnPoint : getSpawnPoints()) {
                    put(SPAWN_POINT.s.concat("-" + i), spawnPoint.serialize());
                    i++;
                }
                put(PARTICLE.s, particleData.serialize());
            }

        };
    }

    /**
     * @return the ore material
     */
    public Material getOreMaterial() {
        return oreMaterial;
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

    @Override
    public ParticleData getParticleData() {
        return particleData;
    }

    @Override
    public Material getMaterial() {
        return oreMaterial;
    }

    @Override
    public byte getMaterialData() {
        return 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Enum for keys in file
     */
    enum OreEnum implements FileEnum {
        SPAWN_POINT("spawnpoint"),
        ID("id"),
        MATERIAL("material"),
        PARTICLE("particle"),
        RESULT("drop");

        final String s;

        OreEnum(String s) {
            this.s = s;
        }

        @Override
        public EnumMap<OreEnum, Object> getDefaultValues() {
            return new EnumMap<OreEnum, Object>(OreEnum.class) {
                {
                    put(SPAWN_POINT, SpawnPoint.EXAMPLE.serialize());
                    put(ID, "some_id");
                    put(MATERIAL, Material.GLASS);
                    put(PARTICLE, new ParticleData());
                    put(RESULT, new YieldResult(40d, ItemUtils.EXAMPLE_RESULT));
                }
            };
        }

        @Override
        public String toString() {
            return s;
        }
    }
}
