package git.doomshade.professions.profession.types.mining;

import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;

import java.util.Map;

public class OreItemType extends ItemType<Ore> {


    public OreItemType() {
        super();
    }

    public OreItemType(Ore object, int exp) {
        super(object, exp);
    }

    @Override
    protected Map<String, Object> getSerializedObject(Ore object) {
        return object.serialize();
    }

    @Override
    protected Ore deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        return Ore.deserialize(map);
    }

    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return IMining.class;
    }


}
