package git.doomshade.professions.profession.types.hunting;

import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class Prey extends ItemType<EntityType> {
    private static final String ENTITY = "entity";

    protected Prey(EntityType item, int exp) {
        super(item, exp);
    }

    protected Prey() {
        super();
    }

    protected Prey(Map<String, Object> map, int id) {
        super(map, id);
    }

    @Override
    protected Map<String, Object> getSerializedObject(EntityType object) {
        Map<String, Object> map = new HashMap<>();
        map.put(ENTITY, object.name());
        return map;
    }

    @Override
    protected EntityType deserializeObject(Map<String, Object> map) {
        String entityTypeName = (String) map.get(ENTITY);
        for (EntityType et : EntityType.values()) {
            if (et.name().equals(entityTypeName)) {
                return et;
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
