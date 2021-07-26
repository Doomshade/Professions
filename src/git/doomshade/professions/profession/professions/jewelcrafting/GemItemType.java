package git.doomshade.professions.profession.professions.jewelcrafting;

import com.google.common.collect.Maps;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.api.item.CraftableItemType;

import java.util.Map;

public class GemItemType extends CraftableItemType<Gem> {

    public GemItemType(Gem object) {
        super(object);
    }
    @Override
    protected Gem deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        return Gem.deserialize(map);
    }

    @Override
    public void onPluginDisable() {
        Gem.GEMS.clear();
    }
}
