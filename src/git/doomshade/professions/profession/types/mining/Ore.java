package git.doomshade.professions.profession.types.mining;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.types.mining.spawn.OreLocationOptions;
import git.doomshade.professions.profession.types.utils.LocationElement;
import git.doomshade.professions.profession.types.utils.SpawnPoint;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.ParticleData;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
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
    public static final Ore EXAMPLE_ORE = new Ore(EXAMPLE_ORE_ID, "Example ore name", Material.COAL_ORE, Maps.newTreeMap(), new ArrayList<>(), new ParticleData());
    private final HashMap<Location, OreLocationOptions> LOCATION_OPTIONS = new HashMap<>();
    private Material oreMaterial;
    private TreeMap<Double, ItemStack> miningResults;
    private final String id;
    private ParticleData particleData;
    private ArrayList<SpawnPoint> spawnPoints;
    private String name;

    private Ore(String id, String name, Material oreMaterial, TreeMap<Double, ItemStack> miningResults, ArrayList<SpawnPoint> spawnPoints, ParticleData particleData) {
        this.id = id;
        this.name = name;
        this.oreMaterial = oreMaterial;
        this.miningResults = new TreeMap<>(Comparator.naturalOrder());
        this.miningResults.putAll(miningResults);
        this.spawnPoints = spawnPoints;
        this.particleData = particleData;

        if (!id.equals(EXAMPLE_ORE_ID))
            ORES.put(id, this);
    }

    /**
     * Required deserialize method of {@link ConfigurationSerializable}
     *
     * @param map serialized Ore
     * @return deserialized Ore
     * @throws ProfessionObjectInitializationException when Ore is not initialized correctly
     */
    public static Ore deserialize(Map<String, Object> map) throws ProfessionObjectInitializationException {
        Set<String> list = Utils.getMissingKeys(map,
                Arrays.stream(OreEnum.values())
                        .filter(x -> x != KEY_ITEMSTACK && x != KEY_CHANCE && x != KEY_MINING_RESULT)
                        .toArray(OreEnum[]::new));
        if (!list.isEmpty()) {
            throw new ProfessionObjectInitializationException(OreItemType.class, list);
        }

        Material mat = Material.getMaterial((String) map.get(KEY_MATERIAL.s));
        MemorySection memorySection = (MemorySection) map.get(KEY_MINING_RESULT.s);

        TreeMap<Double, ItemStack> miningResults = new TreeMap<>(Comparator.naturalOrder());

        for (String s : memorySection.getKeys(false)) {
            ConfigurationSection resultSection = memorySection.getConfigurationSection(s);
            double chance = resultSection.getDouble(KEY_CHANCE.s);

            try {
                ItemStack item = ItemStack.deserialize(resultSection.getConfigurationSection(KEY_ITEMSTACK.s).getValues(true));
                miningResults.put(chance, item);

            } catch (NullPointerException e) {
                throw new ProfessionObjectInitializationException(OreItemType.class, Collections.singletonList(KEY_ITEMSTACK.s));
            }
        }


        // TODO
        return null;
    }

    @Override
    public Map<String, Object> serialize() {

        return new HashMap<String, Object>() {
            {
                put(KEY_MATERIAL.s, oreMaterial.name());

                final Iterator<Entry<Double, ItemStack>> iterator = miningResults.entrySet().iterator();
                for (int i = 0; i < miningResults.size(); i++) {
                    String s = KEY_MINING_RESULT.s.concat(String.valueOf(i));
                    Entry<Double, ItemStack> entry = iterator.next();
                    put(s.concat(KEY_CHANCE.s), entry.getKey());
                    put(s.concat(KEY_ITEMSTACK.s), entry.getValue().serialize());
                }
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

        for (Map.Entry<Double, ItemStack> entry : miningResults.entrySet()) {
            if (random < entry.getKey()) {
                return entry.getValue();
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
    public ArrayList<SpawnPoint> getSpawnPoints() {
        return spawnPoints;
    }

    @Override
    public String getId() {
        return id;
    }

    public OreLocationOptions getLocationOptions(Location location) {
        if (!LOCATION_OPTIONS.containsKey(location)) {
            LOCATION_OPTIONS.put(location, new OreLocationOptions(location, this));
        }
        return LOCATION_OPTIONS.get(location);
    }

    public ImmutableMap<Location, OreLocationOptions> getOreLocationOptions() {
        return ImmutableMap.copyOf(LOCATION_OPTIONS);
    }

    /**
     * Enum for keys in file
     */
    enum OreEnum implements FileEnum {
        KEY_MATERIAL("material"), KEY_MINING_RESULT("mining-result"), KEY_CHANCE("chance"), KEY_ITEMSTACK("item");

        final String s;

        OreEnum(String s) {
            this.s = s;
        }

        @Override
        public EnumMap<OreEnum, Object> getDefaultValues() {
            return new EnumMap<OreEnum, Object>(OreEnum.class) {
                {
                    put(KEY_MATERIAL, Material.GLASS);
                    TreeMap<Double, ItemStack> map = new TreeMap<>(Comparator.naturalOrder());
                    map.put(0d, new ItemStack(Material.BED));
                    put(KEY_MINING_RESULT, Maps.asMap(Sets.newHashSet(0d), x -> ItemUtils.EXAMPLE_RESULT.serialize()));
                }
            };
        }

        @Override
        public String toString() {
            return s;
        }
    }
}
