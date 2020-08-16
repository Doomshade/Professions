package git.doomshade.professions.profession;

import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.SerializeField;

public final class ItemModifying<T> extends ICustomTypeNew<T> {

    @SerializeField("minimum-item-level")
    private int getMinimumLevel;

    public ItemModifying(ItemType<T> itemType) {
        super(itemType);
    }
}
