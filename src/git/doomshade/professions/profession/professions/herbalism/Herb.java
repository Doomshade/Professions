package git.doomshade.professions.profession.professions.herbalism;

import com.google.common.collect.ImmutableMap;
import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.exceptions.SpawnException;
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
import java.io.IOException;
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
    public static final Herb EXAMPLE_HERB = new Herb(EXAMPLE_HERB_ID, ItemUtils.EXAMPLE_RESULT, Material.YELLOW_FLOWER, (byte) 0, new ArrayList<>(), false, new ParticleData(), 5);

    private final ItemStack gatherItem;
    private final Material herbMaterial;
    private final String id;
    private final HashMap<Location, HerbLocationOptions> LOCATION_OPTIONS = new HashMap<>();
    final ArrayList<SpawnPoint> spawnPoints;
    private final boolean enableSpawn;
    private final ParticleData particleData;
    private final byte materialData;
    private final int gatherTime;

    // TODO: 26.01.2020 make implementation of custom marker icons
    private final String markerIcon;

    private Herb(String id, ItemStack gatherItem, Material herbMaterial, byte materialData, ArrayList<SpawnPoint> spawnPoints, boolean enableSpawn, ParticleData particleData, int gatherTime) {
        this.id = id;
        this.materialData = materialData;
        this.gatherItem = gatherItem;
        this.herbMaterial = herbMaterial;
        this.spawnPoints = spawnPoints;
        this.enableSpawn = enableSpawn;
        this.particleData = particleData;
        this.gatherTime = gatherTime;
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

        // gather item
        MemorySection mem = (MemorySection) map.get(GATHER_ITEM.s);
        ItemStack gatherItem = ItemUtils.deserialize(mem.getValues(false));

        // herb material
        ItemStack herbMaterial = ItemUtils.deserializeMaterial((String) map.get(HERB_MATERIAL.s));

        // herb id
        String herbId = (String) map.get(ID.s);

        // spawn points
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

        // particles
        MemorySection particleSection = (MemorySection) map.get(PARTICLE.s);
        final ParticleData particleData = ParticleData.deserialize(particleSection.getValues(true));

        // gather time
        int gatherTime = (int) map.get(GATHER_TIME.s);

        return new Herb(herbId, gatherItem, herbMaterial.getType(), (byte) herbMaterial.getDurability(), spawnPoints, (boolean) map.get(ENABLE_SPAWN.s), particleData, gatherTime);
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
            try {
                entry.getKey().getHerbLocationOptions(entry.getValue()).spawn();
            } catch (SpawnException e) {
                e.printStackTrace();
            }
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

                // adds ":[0-9]+" to the material
                put(HERB_MATERIAL.s, herbMaterial.name() + (materialData != 0 ? ":" + materialData : ""));
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

    public void addSpawnPoint(SpawnPoint sp) {
        this.spawnPoints.add(sp);
        update();
    }

    public void removeSpawnPoint(int id) {
        SpawnPoint sp = spawnPoints.get(id);
        if (sp != null) {
            getHerbLocationOptions(sp.location).despawn();
            spawnPoints.remove(id);
            update();
        }
    }

    public void removeSpawnPoint(SpawnPoint sp) {
        if (!isSpawnPoint(sp.location)) {
            return;
        }
        getHerbLocationOptions(sp.location).despawn();
        spawnPoints.remove(sp);
        update();

    }

    public void update() {
        try {
            Professions.getProfessionManager().getItemTypeHolder(HerbItemType.class).save(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ItemStack getGatherItem() {
        return gatherItem;
    }

    @Override
    public Material getMaterial() {
        return herbMaterial;
    }

    @Override
    public byte getMaterialData() {
        return materialData;
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
        return String.format("Herb:\nID: %s\nName: %s\nMaterial: %s", this.getId(), this.getName(), this.getMaterial().name());
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

    public int getGatherTime() {
        return gatherTime;
    }

    enum HerbEnum implements FileEnum {
        GATHER_ITEM("gather-item"),
        HERB_MATERIAL("herb-material"),
        SPAWN_POINT("spawnpoint"),
        ENABLE_SPAWN("enable-spawn"),
        ID("id"),
        PARTICLE("particle"),
        GATHER_TIME("gather-duration");

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
                    put(HERB_MATERIAL, exampleResult.getType().name() + ":0");
                    put(SPAWN_POINT, SpawnPoint.EXAMPLE.serialize());
                    put(ENABLE_SPAWN, false);
                    put(ID, "herb_id");
                    put(PARTICLE, new ParticleData());
                    put(GATHER_TIME, 5);
                }
            };
        }
    }
}
