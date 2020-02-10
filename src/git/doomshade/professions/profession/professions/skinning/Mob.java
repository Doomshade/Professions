package git.doomshade.professions.profession.professions.skinning;

import org.bukkit.entity.EntityType;

/**
 * Custom class for {@link Prey}
 * Here I needed mob's config name (if the Prey is a MythicMob), I'd have otherwise only passed {@link EntityType} as a generic argument to {@link Prey}.
 *
 * @author Doomshade
 */
public class Mob {
    final String configName;
    final EntityType type;

    Mob(EntityType type, String configName) {
        this.type = type;
        this.configName = configName;
    }

    public Mob(EntityType type) {
        this(type, "");
    }

    boolean isMythicMob() {
        return !configName.isEmpty();
    }

}
