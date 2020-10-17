package git.doomshade.professions.profession.utils;

import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Range;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

import static git.doomshade.professions.profession.utils.SpawnPoint.SpawnPointEnum.*;
import static git.doomshade.professions.profession.utils.SpawnableElement.SpawnableElementEnum.SPAWN_POINT;

public class SpawnPoint extends Location implements ConfigurationSerializable {

    public static final HashSet<SpawnPoint> SPAWN_POINTS;
    public static final SpawnPoint EXAMPLE;

    static {
        SPAWN_POINTS = new HashSet<>();
        EXAMPLE = new SpawnPoint(ItemUtils.EXAMPLE_LOCATION, new Range(5));
    }

    final Range respawnTime;

    /**
     * Use this constructor to create a new spawn point
     *
     * @param location
     * @param respawnTime
     */
    public SpawnPoint(Location location, Range respawnTime) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.respawnTime = respawnTime;
        if (respawnTime.getMin() != -1)
            SPAWN_POINTS.add(this);
    }

    /**
     * For hashcode purposes only! Makes checking collections with {@code contains} method easier.
     *
     * @param location
     */
    public SpawnPoint(Location location) {
        this(location, new Range(-1));
    }

    public static List<SpawnPoint> deserializeAll(Map<String, Object> map) {
        List<SpawnPoint> spawnPoints = new ArrayList<>();
        for (int i = 0; i < map.size(); i++) {
            final Object o = map.get(SPAWN_POINT.s.concat("-") + i);
            if (o instanceof MemorySection) {
                try {
                    spawnPoints.add(SpawnPoint.deserializeSpawnPoint(((MemorySection) o).getValues(false)));
                } catch (ProfessionObjectInitializationException e) {
                    e.printStackTrace();
                }
            }
        }
        return spawnPoints;
    }

    public static SpawnPoint deserializeSpawnPoint(Map<String, Object> map) throws ProfessionObjectInitializationException {
        final Set<String> missingKeysEnum = Utils.getMissingKeys(map, values());
        if (!missingKeysEnum.isEmpty()) {
            throw new ProfessionObjectInitializationException("Could not deserialize spawn point because of missing keys");
        }
        MemorySection mem = (MemorySection) map.get(LOCATION.s);
        Location loc = deserialize(mem.getValues(false));
        Range range = null;
        Object obj = map.get(RESPAWN_TIME.s);
        if (obj instanceof String) {
            try {
                range = Range.fromString((String) obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            range = new Range((int) obj);
        }
        if (range == null) {
            throw new ProfessionObjectInitializationException("Could not deserialize spawn point because of invalid range format");
        }
        return new SpawnPoint(loc, range);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return super.equals(o);
    }

    @Override
    public String toString() {
        return "SpawnPoint{" +
                "location=" + super.toString() +
                ", respawnTime=" + respawnTime +
                '}';
    }

    @Override
    public Map<String, Object> serialize() {
        return new HashMap<String, Object>() {
            {
                put(LOCATION.s, SpawnPoint.super.serialize());
                put(RESPAWN_TIME.s, respawnTime.toString());
            }
        };
    }

    enum SpawnPointEnum implements FileEnum {
        LOCATION("location"), RESPAWN_TIME("respawn-time");

        private final String s;

        SpawnPointEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public EnumMap<SpawnPointEnum, Object> getDefaultValues() {
            return new EnumMap<SpawnPointEnum, Object>(SpawnPointEnum.class) {
                {
                    put(LOCATION, ItemUtils.EXAMPLE_LOCATION.serialize());
                    put(RESPAWN_TIME, 60);
                }
            };
        }
    }
}
