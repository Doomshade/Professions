package git.doomshade.professions.profession.types.mining;

import git.doomshade.professions.profession.types.ItemTypeHolder;
import org.bukkit.Material;

public class OreHolder extends ItemTypeHolder<Ore> {

    @Override
    public void init() {
        // TODO Auto-generated method stub
        registerObject(new Ore(Material.STONE, 10));
        registerObject(new Ore(Material.GRASS, 69));
        registerObject(new Ore(Material.OBSIDIAN, 100));
    }
}
