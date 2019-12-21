package git.doomshade.professions.profession.types.mining;

import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;

import java.util.HashMap;
import java.util.Map;

/**
 * An {@link Ore} item type example for {@link git.doomshade.professions.profession.professions.MiningProfession}.
 *
 * @author Doomshade
 */
public class OreItemType extends ItemType<Ore> {


    /**
     * Required constructor
     */
    public OreItemType() {
        super();
    }

    /**
     * Required constructor
     *
     * @param object
     * @param exp
     */
    public OreItemType(Ore object, int exp) {
        super(object, exp);
    }

    @Override
    public Map<String, Object> getSerializedObject() {
        final Ore ore = getObject();
        if (ore == null) {
            return new HashMap<>();
        }
        return ore.serialize();
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
