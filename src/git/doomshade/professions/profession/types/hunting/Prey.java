package git.doomshade.professions.profession.types.hunting;

import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class Prey extends ItemType<Mob> {
    private static final String ENTITY = "entity";
    public static final String CONFIG_NAME = "config-name";

    protected Prey(Mob item, int exp) {
        super(item, exp);
    }

    protected Prey() {
        super();
    }

    protected Prey(Map<String, Object> map, int id) {
        super(map, id);
    }

    @Override
    protected Map<String, Object> getSerializedObject(Mob object) {
        Map<String, Object> map = new HashMap<>();
        map.put(ENTITY, object.type.name());
        map.put(CONFIG_NAME, object.configName);
        return map;
    }

    @Override
    protected Mob deserializeObject(Map<String, Object> map) {
        String entityTypeName = (String) map.get(ENTITY);
        String configName = (String) map.get(CONFIG_NAME);
        for (EntityType et : EntityType.values()) {
            if (et.name().equals(entityTypeName)) {
                return new Mob(et, configName);
            }
        }
        throw new IllegalArgumentException(entityTypeName + " is not a valid entity type name!");
    }

    @Override
    public Class<? extends ItemTypeHolder<?>> getHolder() {
        return PreyHolder.class;
    }

    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        // TODO Auto-generated method stub
        return IHunting.class;
    }

}
