package git.doomshade.professions.profession.professions.mining;

import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;

import java.util.HashMap;
import java.util.Map;

/**
 * An {@link Ore} item type example for {@link MiningProfession}.
 *
 * @author Doomshade
 */
public class OreItemType extends ItemType<Ore> {

    /**
     * Constructor for creation of the item type object
     *
     * @param object
     */
    public OreItemType(Ore object) {
        super(object);
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
        return Ore.deserialize(map, getName());
    }
}
