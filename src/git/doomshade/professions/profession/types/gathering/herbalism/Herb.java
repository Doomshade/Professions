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
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

import static git.doomshade.professions.profession.types.gathering.herbalism.Herb.HerbEnum.*;

/**
 * A gather item type example for {@link git.doomshade.professions.profession.professions.HerbalismProfession}
 *
 * @author Doomshade
 */
public class Herb implements ConfigurationSerializable {

    public static final HashMap<String, Herb> HERBS = new HashMap<>();
    private static final String EXAMPLE_HERB_ID = "example-herb";
    public static final Herb EXAMPLE_HERB = new Herb(EXAMPLE_HERB_ID, ItemUtils.EXAMPLE_RESULT, Material.YELLOW_FLOWER);

    private final ItemStack gatherItem;
    private final Material herbMaterial;
    private final String id;
    private final ArrayList<SpawnPoint> spawnPoints = new ArrayList<>();
    private final HashMap<Location, ParticleTask> particleTasks = new HashMap<>();

    private boolean enableSpawn = false;

    private Herb(String id, ItemStack gatherItem, Material herbMaterial) {
        this.id = id;
        this.gatherItem = gatherItem;
        this.herbMaterial = herbMaterial;
        if (!id.equals(EXAMPLE_HERB_ID))
            updateHerbs();
    }

    public static Herb getHerb(Material herb, Location location) throws Utils.SearchNotFoundException {
        return Utils.findInIterable(HERBS.values(), x -> x.getHerbMaterial() == herb && x.isSpawnPoint(location));
    }

    @Nullable
    public static Herb getHerb(String id) {
        return HERBS.get(id);
    }

    public static boolean isHerb(Material herb, Location location) throws Utils.SearchNotFoundException {
        return HERBS.containsValue(getHerb(herb, location));
    }

    public static Herb deserialize(Map<String, Object> map) throws ProfessionObjectInitializationException {
        final Set<String> missingKeys = Utils.getMissingKeys(map, Arrays.stream(values()).filter(x -> x != SPAWN_POINT).toArray(HerbEnum[]::new));
        if (!missingKeys.isEmpty()) {
            throw new ProfessionObjectInitializationException(HerbItemType.class, missingKeys);
        }
        MemorySection mem = (MemorySection) map.get(GATHER_ITEM.s);
        ItemStack gatherItem = ItemStack.deserialize(mem.getValues(false));
        Material herbMaterial = Material.getMaterial((String) map.get(HERB_MATERIAL.s));
        String herbId = (String) map.get(ID.s);

        int i = 0;
        MemorySection spawnSection;
        ArrayList<SpawnPoint> spawnPoints = new ArrayList<>();
        while ((spawnSection = ((MemorySection) map.get(SPAWN_POINT.s.concat("-" + i)))) != null) {
            spawnPoints.add(SpawnPoint.deserialize(spawnSection.getValues(false)));
            i++;
        }
        Herb herb = new Herb(herbId, gatherItem, herbMaterial);
        herb.setSpawnPoints(spawnPoints);
        herb.enableSpawn = (boolean) map.get(ENABLE_SPAWN.s);
        return herb;
    }

    public static Map<Herb, Location> getSpawnedHerbs(World world) {
        HashMap<Herb, Location> herbs = new HashMap<>();
        for (Map.Entry<Herb, Location> entry : getHerbsInWorld(world).entrySet()) {
            Herb herb = entry.getKey();
            if (!SpawnTask.isSpawning(herb)) {
                herbs.put(herb, entry.getValue());
            }
        }
        return herbs;
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

    public boolean isSpawnEnabled() {
        return enableSpawn;
    }

    private void updateHerbs() {
        HERBS.remove(getId());
        HERBS.put(getId(), this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Herb herb = (Herb) o;
        return gatherItem.isSimilar(herb.gatherItem) && herbMaterial == herb.herbMaterial;
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

    public String getId() {
        return id;
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
                put(ID.s, id);
            }
        };
    }

    public void despawn(Location location) {
        removeParticles(location);
        location.getBlock().setType(Material.AIR);
        unscheduleSpawn(location);
    }

    public void spawn(Location location) {
        if (!isSpawnEnabled()) {
            return;
        }
        forceSpawn(location);
    }

    public void unscheduleSpawn(Location location) {
        if (!SpawnTask.isSpawning(this)) {
            return;
        }
        SpawnTask task;
        try {
            task = SpawnTask.getSpawnTask(this, location);
        } catch (Utils.SearchNotFoundException e) {
            return;
        }
        if (task != null) {
            try {
                task.cancel();
            } catch (Exception ignored) {
            }
        }
    }

    public void scheduleSpawn(Location location) throws IllegalArgumentException {
        if (SpawnTask.isSpawning(this)) {
            return;
        }
        SpawnTask task = new SpawnTask(this, location);
        location.getBlock().setType(Material.AIR);
        task.runTaskTimer(Professions.getInstance(), 20L, 20L);
    }

    public void removeParticles(Location location) {
        ParticleTask particleTask = particleTasks.get(location);
        if (particleTask != null) {
            try {
                particleTask.cancel();
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void addParticles(Location location) {

        // TODO add variables
        ParticleTask particleTask = new ParticleTask(new Location(location.getWorld(), location.getX(), location.getY() + 0.5, location.getZ(), location.getYaw(), location.getPitch()), Particle.EXPLOSION_NORMAL, 1);
        particleTasks.put(location, particleTask);
        particleTask.runTaskTimer(Professions.getInstance(), 0L, 5L);
    }

    public void forceSpawn(Location location) {
        addParticles(location);
        location.getBlock().setType(herbMaterial);
        unscheduleSpawn(location);
    }

    @Override
    public String toString() {
        return "Herb{" +
                "gatherItem=" + gatherItem +
                ", herbMaterial=" + herbMaterial +
                ", id='" + id + '\'' +
                ", spawnPoints=" + spawnPoints +
                ", particleTasks=" + particleTasks +
                ", enableSpawn=" + enableSpawn +
                '}';
    }

    enum HerbEnum implements FileEnum {
        GATHER_ITEM("gather-item"), HERB_MATERIAL("herb-material"), SPAWN_POINT("spawnpoint"), ENABLE_SPAWN("enable-spawn"), ID("id");

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
                    put(ID, "herb_identificator");
                }
            };
        }
    }
}
