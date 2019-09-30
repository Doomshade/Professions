package git.doomshade.professions.profession.types.gathering;

import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class GatherItem extends ItemType<ItemStack> {

    protected GatherItem(ItemStack item, int exp) {
        super(item, exp);
    }

    protected GatherItem() {
        super();
    }

    protected GatherItem(Map<String, Object> map, int id) {
        super(map, id);
    }

    @Override
    public boolean isValid(ItemStack t) {
        return getObject().isSimilar(t);
    }

    @Override
    protected Map<String, Object> getSerializedObject(ItemStack object) {
        return null;
    }

    @Override
    protected ItemStack deserializeObject(Map<String, Object> map) {
        return null;
    }

    @Override
    public Class<? extends ItemTypeHolder<?>> getHolder() {
        return GatherItemHolder.class;
    }

    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        // TODO Auto-generated method stub
        return IGathering.class;
    }

}
