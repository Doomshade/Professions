package git.doomshade.professions.profession.types.mining;

import com.google.common.collect.ImmutableMap;
import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.types.mining.spawn.OreLocationOptions;
import git.doomshade.professions.profession.types.utils.LocationElement;
import git.doomshade.professions.profession.types.utils.SpawnPoint;
import git.doomshade.professions.profession.types.utils.YieldResult;
import git.doomshade.professions.utils.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

import static git.doomshade.professions.profession.types.mining.Ore.OreEnum.*;

/**
 * Custom class for {@link git.doomshade.professions.profession.types.ItemType}.
 * Here I wanted to have a custom mining result, I'd have otherwise only passed {@link Material} as a generic argument to {@link OreItemType}.
 *
 * @author Doomshade
 */
public class Ore implements ConfigurationSerializable, LocationElement {

    public static final HashMap<String, Ore> ORES = new HashMap<>();
    private static final String EXAMPLE_ORE_ID = "example-ore";
    public static final Ore EXAMPLE_ORE = new Ore(EXAMPLE_ORE_ID, "Example ore name", Material.COAL_ORE, new SortedList<>(Comparator.naturalOrder()), new ArrayList<>(), new ParticleData());
    private final HashMap<Location, OreLocationOptions> LOCATION_OPTIONS = new HashMap<>();
    private Material oreMaterial;
    private SortedList<YieldResult> results;
    private final String id;
    private ParticleData particleData;
    private List<SpawnPoint> spawnPoints;
    private String name;

    private Ore(String id, String name, Material oreMaterial, SortedList<YieldResult> results, List<SpawnPoint> spawnPoints, ParticleData particleData) {
        this.id = id;
        this.name = name;
        this.oreMaterial = oreMaterial;
        this.spawnPoints = spawnPoints;
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

    public void addSpawnPoint(SpawnPoint sp) {
        spawnPoints.add(sp);
        update();
    }

    public void update() {
        try {
            Professions.getProfessionManager().getItemTypeHolder(OreItemType.class).save(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeSpawnPoint(int id) {
        SpawnPoint sp = spawnPoints.get(id);
        if (sp != null) {
            getLocationOptions(sp.location).despawn();
            spawnPoints.remove(id);
            update();
        }
    }

    public void removeSpawnPoint(SpawnPoint sp) {
        if (!isSpawnPoint(sp.location)) {
            return;
        }
        getLocationOptions(sp.location).despawn();
        spawnPoints.remove(sp);
        update();

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

                for (SpawnPoint spawnPoint : spawnPoints) {
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
    public String getName() {
        return name;
    }

    @Override
    public List<SpawnPoint> getSpawnPoints() {
        return spawnPoints;
    }

    @Override
    public String getId() {
        return id;
    }

    public OreLocationOptions getLocationOptions(Location location) throws IllegalArgumentException {
        if (!LOCATION_OPTIONS.containsKey(location)) {
            LOCATION_OPTIONS.put(location, new OreLocationOptions(location, this));
        }
        return LOCATION_OPTIONS.get(location);
    }

    public ImmutableMap<Location, OreLocationOptions> getOreLocationOptions() {
        return ImmutableMap.copyOf(LOCATION_OPTIONS);
    }

    public boolean isSpawnPoint(Location location) {
        return spawnPoints.contains(new SpawnPoint(location));
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
