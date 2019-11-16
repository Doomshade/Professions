package git.doomshade.professions.profession.types.gathering;

import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class GatherItem extends ItemType<ItemStack> {

    public GatherItem() {
        super();
    }

    public GatherItem(ItemStack object, int exp) {
        super(object, exp);
    }

    @Override
    public boolean equals(ItemStack t) {
        return getObject().isSimilar(t);
    }

    @Override
    protected Map<String, Object> getSerializedObject(ItemStack object) {
        return object.serialize();
    }

    @Override
    protected ItemStack deserializeObject(Map<String, Object> map) {
        return ItemStack.deserialize(map);
    }

    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        // TODO Auto-generated method stub
        return IGathering.class;
    }

}
