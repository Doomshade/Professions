package git.doomshade.professions.profession.professions.herbalism;

import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.profession.utils.MarkableLocationElement;
import git.doomshade.professions.profession.utils.SpawnPoint;
import git.doomshade.professions.profession.utils.SpawnableElement;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

import static git.doomshade.professions.profession.professions.herbalism.Herb.HerbEnum.*;

/**
 * A gather item type example for {@link HerbalismProfession}
 *
 * @author Doomshade
 */
public class Herb extends SpawnableElement<HerbLocationOptions> implements MarkableLocationElement, ConfigurationSerializable {

    public static final HashMap<String, Herb> HERBS = new HashMap<>();
    private static final String EXAMPLE_HERB_ID = "example-herb";
    public static final Herb EXAMPLE_HERB = new Herb(EXAMPLE_HERB_ID, ItemUtils.EXAMPLE_RESULT.getItemMeta().getDisplayName(), ItemUtils.EXAMPLE_RESULT, Material.YELLOW_FLOWER, (byte) 0, new ArrayList<>(), false, new ParticleData(), 5);

    private final ItemStack gatherItem;
    private final boolean enableSpawn;
    private final int timeGather;

    // TODO: 26.01.2020 make implementation of custom marker icons
    private final String markerIcon;

    private Herb(String id, String name, ItemStack gatherItem, Material herbMaterial, byte materialData, List<SpawnPoint> spawnPoints, boolean enableSpawn, ParticleData particleData, int gatherTime) {
        super(id, name, herbMaterial, materialData, spawnPoints, particleData);
        this.gatherItem = gatherItem;
        this.enableSpawn = enableSpawn;
        this.timeGather = gatherTime;
        this.markerIcon = "flower";
        if (!rejectedIds().contains(id))
            HERBS.put(getId(), this);
    }

    @Override
    protected Set<String> rejectedIds() {
        return Collections.singleton(EXAMPLE_HERB_ID);
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
        final Set<String> missingKeys = Utils.getMissingKeys(map, HerbEnum.values());
        if (!missingKeys.isEmpty()) {
            throw new ProfessionObjectInitializationException(HerbItemType.class, missingKeys);
        }
        return SpawnableElement.deserialize(map, Herb.class, x -> {
            int gatherTime = (int) map.get(TIME_GATHER.s);
            MemorySection mem = (MemorySection) map.get(GATHER_ITEM.s);
            ItemStack gatherItem = ItemUtils.deserialize(mem.getValues(false));
            String displayName = gatherItem.getType().name();
            if (gatherItem.hasItemMeta()) {
                ItemMeta meta = gatherItem.getItemMeta();
                if (meta.hasDisplayName()) {
                    displayName = meta.getDisplayName();
                }
            }
            return new Herb(x.getId(), displayName, gatherItem, x.getMaterial(), x.getMaterialData(), x.getSpawnPoints(), (boolean) map.get(ENABLE_SPAWN.s), x.getParticleData(), gatherTime);
        });
    }
        /*
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

        String displayName = gatherItem.getType().name();
        if (gatherItem.hasItemMeta()) {
            ItemMeta meta = gatherItem.getItemMeta();
            if (meta.hasDisplayName()) {
                displayName = meta.getDisplayName();
            }
        }

        return new Herb(herbId, displayName, gatherItem, herbMaterial.getType(), (byte) herbMaterial.getDurability(), spawnPoints, (boolean) map.get(ENABLE_SPAWN.s), particleData, gatherTime);
    }*/

    @SuppressWarnings("unused")
    public static Map<Herb, Location> getSpawnedHerbs(World world) {
        HashMap<Herb, Location> herbs = new HashMap<>();
        for (Map.Entry<Herb, Location> entry : getHerbsInWorld(world).entrySet()) {
            Herb herb = entry.getKey();
            if (herb.getLocationOptions(entry.getValue()).isSpawned()) {
                herbs.put(herb, entry.getValue());
            }
        }
        return herbs;
    }

    public static void spawnHerbs(World world) {
        for (Map.Entry<Herb, Location> entry : getHerbsInWorld(world).entrySet()) {
            try {
                entry.getKey().getLocationOptions(entry.getValue()).spawn();
            } catch (SpawnException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unused")
    public static void despawnHerbs(World world) {
        for (Map.Entry<Herb, Location> entry : getHerbsInWorld(world).entrySet()) {
            entry.getKey().getLocationOptions(entry.getValue()).despawn();
        }
    }

    private static Map<Herb, Location> getHerbsInWorld(World world) {
        HashMap<Herb, Location> herbs = new HashMap<>();
        for (Herb herb : HERBS.values()) {
            for (SpawnPoint spawnPoint : herb.getSpawnPoints()) {
                if (spawnPoint.getWorld().equals(world)) {
                    herbs.put(herb, spawnPoint);
                }
            }
        }
        return herbs;
    }

    boolean isSpawnEnabled() {
        return enableSpawn;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && gatherItem != null && gatherItem.equals(((Herb) o).gatherItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatherItem, getMaterial(), getId());
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = super.serialize();

        map.put(GATHER_ITEM.s, ItemUtils.serialize(gatherItem));
        map.put(ENABLE_SPAWN.s, enableSpawn);
        map.put(TIME_GATHER.s, timeGather);
        return map;
        // adds ":[0-9]+" to the material
    }

    @Override
    protected HerbLocationOptions createLocationOptions(Location location) {
        return new HerbLocationOptions(location, this);
    }

    @NotNull
    @Override
    protected ItemTypeHolder<HerbItemType> getItemTypeHolder() {
        return Professions.getProfessionManager().getItemTypeHolder(HerbItemType.class);
    }

    public ItemStack getGatherItem() {
        return gatherItem;
    }

    @Override
    public String getMarkerIcon() {
        return markerIcon;
    }

    @Override
    public String toString() {
        return String.format("\nHerb:\nID: %s\nName: %s\nMaterial: %s", this.getId(), this.getName(), this.getMaterial().name());
    }

    public int getGatherTime() {
        return timeGather;
    }

    enum HerbEnum implements FileEnum {
        GATHER_ITEM("gather-item"),
        ENABLE_SPAWN("enable-spawn"),
        TIME_GATHER("gather-duration");

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
                    put(ENABLE_SPAWN, false);
                    put(TIME_GATHER, 5);
                }
            };
        }
    }
}
