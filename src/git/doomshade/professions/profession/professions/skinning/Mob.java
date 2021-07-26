package git.doomshade.professions.profession.professions.skinning;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static git.doomshade.professions.profession.professions.skinning.PreyItemType.PreyEnum.CONFIG_NAME;
import static git.doomshade.professions.profession.professions.skinning.PreyItemType.PreyEnum.ENTITY;

/**
 * Custom class for {@link PreyItemType}
 * Here I needed mob's config name (if the Prey is a MythicMob), I'd have otherwise only passed {@link EntityType} as a generic argument to {@link PreyItemType}.
 *
 * @author Doomshade
 */
public class Mob implements ConfigurationSerializable {
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

    @Override
    public @NotNull Map<String, Object> serialize() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put(ENTITY.s, type.name());
        map.put(CONFIG_NAME.s, configName);
        return map;
    }
}
