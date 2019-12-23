package git.doomshade.professions.profession.types.gathering.herbalism;

import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.task.ParticleTask;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static git.doomshade.professions.profession.types.gathering.herbalism.Herb.HerbEnum.*;

/**
 * A gather item type example for {@link git.doomshade.professions.profession.professions.HerbalismProfession}
 *
 * @author Doomshade
 */
public class Herb implements ConfigurationSerializable {

    private static final HashSet<Herb> HERBS = new HashSet<>();

    private final ItemStack gatherItem;
    private final Material herbMaterial;
    private final ArrayList<SpawnPoint> spawnPoints = new ArrayList<>();
    private final HashMap<Location, ParticleTask> particleTasks = new HashMap<>();

    private boolean enableSpawn = false;

    public Herb(ItemStack gatherItem, Material herbMaterial) {
        this.gatherItem = gatherItem;
        this.herbMaterial = herbMaterial;
        updateHerbs();
    }

    public Herb(ItemStack gatherItem) {
        this(gatherItem, gatherItem.getType());
    }

    public static boolean isHerb(Herb herb) {
        return HERBS.contains(herb);
    }

    public static Herb deserialize(Map<String, Object> map) throws ProfessionObjectInitializationException {
        final Set<String> missingKeys = Utils.getMissingKeys(map, Arrays.stream(values()).filter(x -> x != SPAWN_POINT).toArray(HerbEnum[]::new));
        if (!missingKeys.isEmpty()) {
            throw new ProfessionObjectInitializationException(HerbItemType.class, missingKeys);
        }
        MemorySection mem = (MemorySection) map.get(GATHER_ITEM.s);
        ItemStack gatherItem = ItemStack.deserialize(mem.getValues(false));
        Material herbMaterial = Material.getMaterial((String) map.get(HERB_MATERIAL.s));

        int i = 0;
        ConfigurationSection spawnSection;
        ArrayList<SpawnPoint> spawnPoints = new ArrayList<>();
        while ((spawnSection = ((MemorySection) map.get(SPAWN_POINT.s.concat("-" + i))).getConfigurationSection(SPAWN_POINT.s)) != null) {
            spawnPoints.add(SpawnPoint.deserialize(spawnSection.getValues(false)));
            i++;
        }
        Herb herb = new Herb(gatherItem, herbMaterial);
        herb.setSpawnPoints(spawnPoints);
        herb.enableSpawn = (boolean) map.get(ENABLE_SPAWN.s);
        return herb;
    }

    public static void spawnHerbs(World world) {
        for (Map.Entry<Herb, Location> entry : getHerbsInWorld(world).entrySet()) {
            entry.getKey().spawn(entry.getValue());
        }
    }

    public static void despawnHerbs(World world) {
        for (Map.Entry<Herb, Location> entry : getHerbsInWorld(world).entrySet()) {
            entry.getKey().despawn(entry.getValue());
        }
    }

    public static Map<Herb, Location> getSpawnedHerbs(World world) {
        HashMap<Herb, Location> herbs = new HashMap<>();
        for (Map.Entry<Herb, Location> entry : getHerbsInWorld(world).entrySet()) {
            Herb herb = entry.getKey();
            if (!herb.canSpawn()) {
                herbs.put(herb, entry.getValue());
            }
        }
        return herbs;
    }

    private static Map<Herb, Location> getHerbsInWorld(World world) {
        HashMap<Herb, Location> herbs = new HashMap<>();
        for (Herb herb : HERBS) {
            for (SpawnPoint spawnPoint : herb.spawnPoints) {
                if (spawnPoint.location.getWorld().equals(world)) {
                    herbs.put(herb, spawnPoint.location);
                }
            }
        }
        return herbs;
    }

    public boolean isSpawnEnabled() {
        return enableSpawn;
    }

    private void addSpawnPoint(SpawnPoint spawnPoint) {
        spawnPoints.add(spawnPoint);
        updateHerbs();
    }

    private void updateHerbs() {
        HERBS.remove(this);
        HERBS.add(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Herb herb = (Herb) o;
        return gatherItem.equals(herb.gatherItem) &&
                herbMaterial == herb.herbMaterial;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gatherItem, herbMaterial);
    }

    public ItemStack getGatherItem() {
        return gatherItem;
    }

    public Material getHerbMaterial() {
        return herbMaterial;
    }

    public ArrayList<SpawnPoint> getSpawnPoints() {
        return spawnPoints;
    }

    private void setSpawnPoints(Collection<SpawnPoint> spawnPoints) {
        this.spawnPoints.clear();
        this.spawnPoints.addAll(spawnPoints);
        updateHerbs();
    }

    @Override
    public Map<String, Object> serialize() {
        return new HashMap<String, Object>() {
            {
                put(GATHER_ITEM.s, gatherItem.serialize());
                put(HERB_MATERIAL.s, herbMaterial.name());
                for (int i = 0; i < spawnPoints.size(); i++) {
                    put(SPAWN_POINT.s.concat("-" + i), spawnPoints.get(i).serialize());
                }
                put(ENABLE_SPAWN.s, enableSpawn);
            }
        };
    }

    public void despawn(Location location) {
        if (!canSpawn()) {
            return;
        }
        try {
            SpawnTask task = new SpawnTask(this, location);
            task.runTaskTimer(Professions.getInstance(), 20L, 20L);
            ParticleTask particleTask = particleTasks.get(location);
            if (particleTask != null) {
                particleTask.cancel();
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    private boolean canSpawn() {
        return !SpawnTask.isSpawning(this) && isSpawnEnabled();
    }

    public void spawn(Location location) {
        if (!canSpawn()) {
            return;
        }

        // TODO add variables
        ParticleTask particleTask = new ParticleTask(location.add(0d, 0.5d, 0d), Particle.BARRIER, 1);
        location.getBlock().setType(herbMaterial);
        particleTask.runTaskTimer(Professions.getInstance(), 0L, 5L);
        particleTasks.put(location, particleTask);
    }

    enum HerbEnum implements FileEnum {
        GATHER_ITEM("gather-item"), HERB_MATERIAL("herb-material"), SPAWN_POINT("spawnpoint"), ENABLE_SPAWN("enable-spawn");

        private final String s;

        HerbEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public Map<Enum, Object> getDefaultValues() {
            return new HashMap<Enum, Object>() {
                {
                    ItemStack exampleResult = ItemUtils.EXAMPLE_RESULT;
                    put(GATHER_ITEM, exampleResult.serialize());
                    put(HERB_MATERIAL, exampleResult.getType().name());
                    put(SPAWN_POINT, ItemUtils.EXAMPLE_LOCATION.serialize());
                    //put(SPAWN_POINT, new SpawnPoint(ItemUtils.EXAMPLE_LOCATION, 60).serialize());
                    put(ENABLE_SPAWN, false);
                }
            };
        }
    }
}
