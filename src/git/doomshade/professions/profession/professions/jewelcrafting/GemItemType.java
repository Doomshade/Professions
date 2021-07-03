package git.doomshade.professions.profession.professions.jewelcrafting;

import com.google.common.collect.Maps;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.api.types.CraftableItemType;

import java.util.Map;

public class GemItemType extends CraftableItemType<Gem> {

    public GemItemType(Gem object) {
        super(object);
    }

    @Override
    public Map<String, Object> getSerializedObject() {

        final Gem object = getObject();
        if (object == null) {
            return Maps.newHashMap();
        }

        return object.serialize();
    }

    @Override
    protected Gem deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        return Gem.deserialize(map);
    }

    @Override
    public void onDisable() {
        Gem.GEMS.clear();
    }
}
