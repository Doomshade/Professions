package git.doomshade.professions.profession.types.gathering.herbalism;

import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.gathering.IGathering;

import java.util.HashMap;
import java.util.Map;

public class HerbItemType extends ItemType<Herb> {


    public HerbItemType() {
        super();
    }

    public HerbItemType(Herb object, int exp) {
        super(object, exp);
    }

    @Override
    public Map<String, Object> getSerializedObject() {
        if (getObject() != null) {
            return getObject().serialize();
        }
        return new HashMap<>();
    }

    @Override
    protected Herb deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        return Herb.deserialize(map);
    }

    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return IGathering.class;
    }
}
