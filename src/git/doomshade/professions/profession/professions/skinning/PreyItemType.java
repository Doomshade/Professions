package git.doomshade.professions.profession.professions.skinning;

import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.Utils;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static git.doomshade.professions.profession.professions.skinning.PreyItemType.PreyEnum.CONFIG_NAME;
import static git.doomshade.professions.profession.professions.skinning.PreyItemType.PreyEnum.ENTITY;

/**
 * A prey (mob hunting) example for {@link SkinningProfession}
 *
 * @author Doomshade
 */
public class PreyItemType extends ItemType<Mob> {

    public PreyItemType(Mob object) {
        super(object);
    }

    @Override
    protected Mob deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {

        Set<String> list = Utils.getMissingKeys(map, PreyEnum.values());

        if (!list.isEmpty()) {
            throw new ProfessionObjectInitializationException(getClass(), list, getFileId());
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

    enum PreyEnum implements FileEnum {
        ENTITY("entity"), CONFIG_NAME("config-name");

        public final String s;

        PreyEnum(String s) {
            this.s = s;
        }

        @Override
        public EnumMap<PreyEnum, Object> getDefaultValues() {
            return new EnumMap<>(PreyEnum.class) {
                {
                    put(ENTITY, EntityType.SKELETON.name());
                    put(CONFIG_NAME, "cfg-name");
                }
            };
        }

        @Override
        public String toString() {
            return s;
        }
    }

}
