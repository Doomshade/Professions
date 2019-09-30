package git.doomshade.professions.profession.types.gathering;

import git.doomshade.professions.profession.types.ItemTypeHolder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GatherItemHolder extends ItemTypeHolder<GatherItem> {

    @Override
    public void init() {
        registerObject(new GatherItem(new ItemStack(Material.GLASS), 500));
    }

}
