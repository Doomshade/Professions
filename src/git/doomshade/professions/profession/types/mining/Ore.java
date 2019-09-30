package git.doomshade.professions.profession.types.mining;

import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class Ore extends ItemType<Material> {
    private static final String KEY_MATERIAL = "material";

    protected Ore(Material item, int exp) {
        super(item, exp);
    }

    protected Ore() {
        super();
    }

    protected Ore(Map<String, Object> map, int id) {
        super(map, id);
    }

    @Override
    protected Map<String, Object> getSerializedObject(Material object) {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_MATERIAL, object.name());
        return map;
    }

    @Override
    protected Material deserializeObject(Map<String, Object> map) {
        return Material.getMaterial((String) map.get(KEY_MATERIAL));
    }

    @Override
    public Class<? extends ItemTypeHolder<?>> getHolder() {
        return OreHolder.class;
    }

    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return IMining.class;
    }


}
