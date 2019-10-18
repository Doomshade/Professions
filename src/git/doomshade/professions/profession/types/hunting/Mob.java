package git.doomshade.professions.profession.types.hunting;

import org.bukkit.entity.EntityType;

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
