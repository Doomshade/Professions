package git.doomshade.professions.profession.types.hunting;

import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.utils.Utils;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static git.doomshade.professions.profession.types.hunting.Prey.PreyEnum.CONFIG_NAME;
import static git.doomshade.professions.profession.types.hunting.Prey.PreyEnum.ENTITY;

public class Prey extends ItemType<Mob> {

    @Override
    protected Map<String, Object> getSerializedObject(Mob object) {
        Map<String, Object> map = new HashMap<>();
        map.put(ENTITY.s, object.type.name());
        map.put(CONFIG_NAME.s, object.configName);
        return map;
    }

    public Prey() {
        super();
    }

    public Prey(Mob object, int exp) {
        super(object, exp);
    }

    @Override
    protected Mob deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {

        Set<String> list = Utils.getMissingKeys(map, PreyEnum.values());

        if (!list.isEmpty()) {
            throw new ProfessionObjectInitializationException(getClass(), list, getId());
        }

        String entityTypeName = (String) map.get(ENTITY.s);
        String configName = (String) map.get(CONFIG_NAME.s);
        for (EntityType et : EntityType.values()) {
            if (et.name().equals(entityTypeName)) {
                return new Mob(et, configName);
            }
        }
        throw new IllegalArgumentException(entityTypeName + " is not a valid entity type name!");
    }

    enum PreyEnum {
        ENTITY("entity"), CONFIG_NAME("config-name");

        public final String s;

        PreyEnum(String s) {
            this.s = s;
        }
    }

    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return IHunting.class;
    }

}
