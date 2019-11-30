package git.doomshade.professions.profession.types.gathering;

import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * A gather item type example for {@link git.doomshade.professions.profession.professions.HerbalismProfession}
 *
 * @author Doomshade
 */
public class GatherItem extends ItemType<ItemStack> {

    /**
     * Required constructor
     */
    public GatherItem() {
        super();
    }

    /**
     * Required constructor
     *
     * @param object
     * @param exp
     */
    public GatherItem(ItemStack object, int exp) {
        super(object, exp);
    }

    @Override
    public boolean equalsObject(ItemStack t) {
        return getObject().isSimilar(t);
    }

    @Override
    public Map<String, Object> getSerializedObject() {
        return getObject().serialize();
    }

    @Override
    protected ItemStack deserializeObject(Map<String, Object> map) {
        return ItemStack.deserialize(map);
    }

    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return IGathering.class;
    }

}
