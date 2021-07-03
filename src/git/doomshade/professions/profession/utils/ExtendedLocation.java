package git.doomshade.professions.profession.utils;

import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Range;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

import static git.doomshade.professions.profession.utils.ExtendedLocation.SpawnPointEnum.*;
import static git.doomshade.professions.profession.utils.SpawnableElement.SpawnableElementEnum.SPAWN_POINT;

/**
 * Class made purely for serialization purposes. This class allows having location and respawn time together in a single segment.<br>
 * For example:<br>
 * <code>
 * spawnpoint-0:<br>
 * respawn-time: '5'<br>
 * location:<br>
 * world: world<br>
 * x: 50.0<br>
 * y: 71.0<br>
 * z: 413.0<br>
 * pitch: 0.0<br>
 * yaw: 0.0</code>
 */
public class ExtendedLocation extends Location implements ConfigurationSerializable {

    public static final HashSet<ExtendedLocation> SPAWN_POINTS;
    public static final ExtendedLocation EXAMPLE;

    static {
        SPAWN_POINTS = new HashSet<>();
        EXAMPLE = new ExtendedLocation(ItemUtils.EXAMPLE_LOCATION, new Range(5));
    }

    final Range respawnTime;

    /**
     * Use this constructor to create a new spawn point
     *
     * @param location
     * @param respawnTime
     */
    public ExtendedLocation(Location location, Range respawnTime) {
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
    public ExtendedLocation(Location location) {
        this(location, new Range(-1));
    }

    public static List<ExtendedLocation> deserializeAll(Map<String, Object> map) throws ProfessionObjectInitializationException {

        ProfessionObjectInitializationException ex = null;
        List<ExtendedLocation> spawnPointLocations = new ArrayList<>();
        for (int i = 0; i < map.size(); i++) {
            final Object o = map.get(SPAWN_POINT.s.concat("-") + i);
            if (o instanceof MemorySection) {
                try {
                    spawnPointLocations.add(ExtendedLocation.deserializeSpawnPoint(((MemorySection) o).getValues(false)));
                } catch (ProfessionObjectInitializationException e) {
                    ex = new ProfessionObjectInitializationException(ExtendedLocation.class, Collections.emptyList(), ProfessionObjectInitializationException.ExceptionReason.KEY_ERROR);
                    e.setAdditionalMessage("Spawn point ID: " + i);
                    Professions.logError(e, false);
                }
            }
        }

        if (ex != null) {
            throw ex;
        }

        return spawnPointLocations;
    }

    /**
     * Overrides the Location's equals method. This allows SpawnPoint.equals(Location), but not Location.equals(SpawnPoint)!
     *
     * @param o the object to check equals for
     * @return based on {@link Location}
     * @see Location#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;

        if (o instanceof ExtendedLocation) {
            return super.equals(o);
        } else if (o instanceof Location) {

            // not the greatest, but oh well, bukkit decided to check for class equality with !=, not with Class#isAssignableFrom(Class) :/
            return new Location(getWorld(), getX(), getY(), getZ(), getYaw(), getPitch()).equals(o);
        }
        return false;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public static ExtendedLocation deserializeSpawnPoint(Map<String, Object> map) throws ProfessionObjectInitializationException {
        final Set<String> missingKeysEnum = Utils.getMissingKeys(map, values());
        if (!missingKeysEnum.isEmpty()) {
            throw new ProfessionObjectInitializationException(
                    ExtendedLocation.class,
                    missingKeysEnum,
                    ProfessionObjectInitializationException.ExceptionReason.MISSING_KEYS);
        }
        MemorySection mem = (MemorySection) map.get(LOCATION.s);
        Location loc = deserialize(mem.getValues(false));
        Range range;
        Object obj = map.get(RESPAWN_TIME.s);
        if (obj instanceof String) {
            try {
                range = Range.fromString((String) obj);
            } catch (Exception e) {
                throw new ProfessionObjectInitializationException(
                        ExtendedLocation.class,
                        Collections.singletonList(RESPAWN_TIME.s),
                        ProfessionObjectInitializationException.NO_ID, map.toString(),
                        ProfessionObjectInitializationException.ExceptionReason.KEY_ERROR);
            }
        } else {
            try {
                range = new Range((int) obj);
            } catch (Exception e) {
                throw new ProfessionObjectInitializationException(
                        ExtendedLocation.class,
                        Collections.singletonList(RESPAWN_TIME.s),
                        ProfessionObjectInitializationException.NO_ID, map.toString(),
                        ProfessionObjectInitializationException.ExceptionReason.KEY_ERROR);
            }
        }
        return new ExtendedLocation(loc, range);
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
                put(LOCATION.s, ExtendedLocation.super.serialize());
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
