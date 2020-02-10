package git.doomshade.professions.profession.professions.herbalism;

import com.google.common.collect.ImmutableMap;
import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.utils.MarkableLocationElement;
import git.doomshade.professions.profession.utils.SpawnPoint;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.ParticleData;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;

import static git.doomshade.professions.profession.professions.herbalism.Herb.HerbEnum.*;

/**
 * A gather item type example for {@link HerbalismProfession}
 *
 * @author Doomshade
 */
public class Herb implements MarkableLocationElement, ConfigurationSerializable {

    public static final HashMap<String, Herb> HERBS = new HashMap<>();
    private static final String EXAMPLE_HERB_ID = "example-herb";
    public static final Herb EXAMPLE_HERB = new Herb(EXAMPLE_HERB_ID, ItemUtils.EXAMPLE_RESULT, Material.YELLOW_FLOWER, new ArrayList<>(), false, new ParticleData());

    private final ItemStack gatherItem;
    private final Material herbMaterial;
    private final String id;
    private final HashMap<Location, HerbLocationOptions> LOCATION_OPTIONS = new HashMap<>();
    final ArrayList<SpawnPoint> spawnPoints;
    private final boolean enableSpawn;
    private final ParticleData particleData;

    // TODO: 26.01.2020 make implementation of custom marker icons
    private final String markerIcon;

    private Herb(String id, ItemStack gatherItem, Material herbMaterial, ArrayList<SpawnPoint> spawnPoints, boolean enableSpawn, ParticleData particleData) {
        this.id = id;
        this.gatherItem = gatherItem;
        this.herbMaterial = herbMaterial;
        this.spawnPoints = spawnPoints;
        this.enableSpawn = enableSpawn;
        this.particleData = particleData;
        this.markerIcon = "flower";
        if (!id.equals(EXAMPLE_HERB_ID))
            HERBS.put(getId(), this);
    }

    public static Herb getHerb(Material herb, Location location) throws Utils.SearchNotFoundException {
        return Utils.findInIterable(HERBS.values(), x -> x.getMaterial() == herb && x.isSpawnPoint(location));
    }

    @Nullable
    public static Herb getHerb(String id) {
        return HERBS.get(id);
    }

    @SuppressWarnings("unused")
    public static boolean isHerb(Material herb, Location location) throws Utils.SearchNotFoundException {
        return HERBS.containsValue(getHerb(herb, location));
    }

    public static Herb deserialize(Map<String, Object> map) throws ProfessionObjectInitializationException {
        final Set<String> missingKeys = Utils.getMissingKeys(map, Arrays.stream(values()).filter(x -> x != SPAWN_POINT).toArray(HerbEnum[]::new));
        if (!missingKeys.isEmpty()) {
            throw new ProfessionObjectInitializationException(HerbItemType.class, missingKeys);
        }
        MemorySection mem = (MemorySection) map.get(GATHER_ITEM.s);
        ItemStack gatherItem = ItemUtils.deserialize(mem.getValues(false));
        Material herbMaterial = Material.getMaterial((String) map.get(HERB_MATERIAL.s));
        String herbId = (String) map.get(ID.s);

        int i = 0;
        MemorySection spawnSection;
        ArrayList<SpawnPoint> spawnPoints = new ArrayList<>();
        while ((spawnSection = (MemorySection) map.get(SPAWN_POINT.s.concat("-" + i))) != null) {
            SpawnPoint sp = SpawnPoint.deserialize(spawnSection.getValues(false));
            if (sp.location.clone().add(0, -1, 0).getBlock().getType() == Material.AIR) {
                final String message = String.format("Spawn point %d of herb %s set to air. Make sure you have a block below the herb!", i, herbId);
                Professions.log(message, Level.INFO);
                Professions.log(message, Level.CONFIG);

            }
            spawnPoints.add(sp);
            i++;
        }
        MemorySection particleSection = (MemorySection) map.get(PARTICLE.s);
        return new Herb(herbId, gatherItem, herbMaterial, spawnPoints, (boolean) map.get(ENABLE_SPAWN.s), ParticleData.deserialize(particleSection.getValues(true)));
    }

    @SuppressWarnings("unused")
    public static Map<Herb, Location> getSpawnedHerbs(World world) {
        HashMap<Herb, Location> herbs = new HashMap<>();
        for (Map.Entry<Herb, Location> entry : getHerbsInWorld(world).entrySet()) {
            Herb herb = entry.getKey();
            if (herb.getHerbLocationOptions(entry.getValue()).isSpawned()) {
                herbs.put(herb, entry.getValue());
            }
        }
        return herbs;
    }

    public static void spawnHerbs(World world) {
        for (Map.Entry<Herb, Location> entry : getHerbsInWorld(world).entrySet()) {
            entry.getKey().getHerbLocationOptions(entry.getValue()).spawn();
        }
    }

    @SuppressWarnings("unused")
    public static void despawnHerbs(World world) {
        for (Map.Entry<Herb, Location> entry : getHerbsInWorld(world).entrySet()) {
            entry.getKey().getHerbLocationOptions(entry.getValue()).despawn();
        }
    }

    private static Map<Herb, Location> getHerbsInWorld(World world) {
        HashMap<Herb, Location> herbs = new HashMap<>();
        for (Herb herb : HERBS.values()) {
            for (SpawnPoint spawnPoint : herb.spawnPoints) {
                if (spawnPoint.location.getWorld().equals(world)) {
                    herbs.put(herb, spawnPoint.location);
                }
            }
        }
        return herbs;
    }

    private boolean isSpawnPoint(Location location) {
        return spawnPoints.contains(new SpawnPoint(location));
    }

    boolean isSpawnEnabled() {
        return enableSpawn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Herb herb = (Herb) o;
        return gatherItem.equals(herb.gatherItem) &&
                herbMaterial == herb.herbMaterial &&
                id.equals(herb.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatherItem, herbMaterial, id);
    }

    @Override
    public Map<String, Object> serialize() {
        return new HashMap<String, Object>() {
            {
                put(GATHER_ITEM.s, ItemUtils.serialize(gatherItem));
                put(HERB_MATERIAL.s, herbMaterial.name());
                for (int i = 0; i < spawnPoints.size(); i++) {
                    put(SPAWN_POINT.s.concat("-" + i), spawnPoints.get(i).serialize());
                }
                put(ENABLE_SPAWN.s, enableSpawn);
                put(ID.s, id);
                put(PARTICLE.s, particleData.serialize());
            }
        };
    }

    public HerbLocationOptions getHerbLocationOptions(Location location) {
        if (!LOCATION_OPTIONS.containsKey(location)) {
            LOCATION_OPTIONS.put(location, new HerbLocationOptions(location, this));
        }
        return LOCATION_OPTIONS.get(location);
    }

    public ImmutableMap<Location, HerbLocationOptions> getHerbLocationOptions() {
        return ImmutableMap.copyOf(LOCATION_OPTIONS);
    }

    public ItemStack getGatherItem() {
        return gatherItem;
    }

    @Override
    public Material getMaterial() {
        return herbMaterial;
    }

    @Override
    public List<SpawnPoint> getSpawnPoints() {
        return spawnPoints;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ParticleData getParticleData() {
        return particleData;
    }

    @Override
    public String getMarkerIcon() {
        return markerIcon;
    }

    @Override
    public String toString() {
        return "Herb{" +
                "gatherItem=" + gatherItem +
                ", herbMaterial=" + herbMaterial +
                ", id='" + id + '\'' +
                ", spawnPoints=" + spawnPoints +
                ", enableSpawn=" + enableSpawn +
                ", particleData=" + particleData +
                '}';
    }

    /**
     * This method checks whether or not the item has meta every time it's called.
     *
     * @return the name of this herb
     */
    @Override
    public String getName() {
        final String material = gatherItem.getType().name();
        if (!gatherItem.hasItemMeta()) {
            return material;
        }
        ItemMeta meta = gatherItem.getItemMeta();
        if (!meta.hasDisplayName()) {
            return material;
        }
        return meta.getDisplayName();
    }

    enum HerbEnum implements FileEnum {
        GATHER_ITEM("gather-item"),
        HERB_MATERIAL("herb-material"),
        SPAWN_POINT("spawnpoint"),
        ENABLE_SPAWN("enable-spawn"),
        ID("id"),
        PARTICLE("particle");

        private final String s;

        HerbEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public EnumMap<HerbEnum, Object> getDefaultValues() {
            return new EnumMap<HerbEnum, Object>(HerbEnum.class) {
                {
                    ItemStack exampleResult = ItemUtils.EXAMPLE_RESULT;
                    put(GATHER_ITEM, exampleResult.serialize());
                    put(HERB_MATERIAL, exampleResult.getType().name());
                    put(SPAWN_POINT, SpawnPoint.EXAMPLE.serialize());
                    put(ENABLE_SPAWN, false);
                    put(ID, "herb_id");
                    put(PARTICLE, new ParticleData());
                }
            };
        }
    }
}
