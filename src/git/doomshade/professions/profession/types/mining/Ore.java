package git.doomshade.professions.profession.types.mining;

import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class Ore extends ItemType<Material> {
    private static final String KEY_MATERIAL = "material";

    public Ore() {
        super();
    }

    public Ore(Material object, int exp) {
        super(object, exp);
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
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return IMining.class;
    }


}
