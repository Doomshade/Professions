package git.doomshade.professions.profession.types.gathering.herbalism;

import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

import static git.doomshade.professions.profession.types.gathering.herbalism.SpawnPoint.SpawnPointEnum.*;

public class SpawnPoint implements ConfigurationSerializable {
    public static final HashSet<SpawnPoint> SPAWN_POINTS = new HashSet<>();
    public final Location location;
    public final int respawnTime;

    SpawnPoint(Location location, int respawnTime) {
        this.location = location;
        this.respawnTime = respawnTime;
        if (respawnTime != -1)
            SPAWN_POINTS.add(this);
    }

    public SpawnPoint(Location location) {
        this(location, -1);
    }

    static SpawnPoint deserialize(Map<String, Object> map) throws ProfessionObjectInitializationException {
        final Set<String> missingKeysEnum = Utils.getMissingKeys(map, values());
        if (!missingKeysEnum.isEmpty()) {
            throw new ProfessionObjectInitializationException(HerbItemType.class, missingKeysEnum);
        }
        MemorySection mem = (MemorySection) map.get(LOCATION.s);
        Location loc = Location.deserialize(mem.getValues(false));
        return new SpawnPoint(loc, (int) map.get(RESPAWN_TIME.s));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpawnPoint that = (SpawnPoint) o;
        return location.equals(that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }

    @Override
    public Map<String, Object> serialize() {
        return new HashMap<String, Object>() {
            {
                put(LOCATION.s, location.serialize());
                put(RESPAWN_TIME.s, respawnTime);
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
        public Map<Enum, Object> getDefaultValues() {
            return new HashMap<Enum, Object>() {
                {
                    put(LOCATION, ItemUtils.EXAMPLE_LOCATION.serialize());
                    put(RESPAWN_TIME, 60);
                }
            };
        }
    }
}
