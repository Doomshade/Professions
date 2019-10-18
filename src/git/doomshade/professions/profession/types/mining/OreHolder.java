package git.doomshade.professions.profession.types.mining;

import git.doomshade.professions.profession.types.ItemTypeHolder;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class OreHolder extends ItemTypeHolder<Ore> {

    @Override
    public void init() {
        Ore ore = new Ore(Material.OBSIDIAN, 100);
        ore.setName(ChatColor.GRAY + "Obsidian");
        registerObject(ore);
    }
}
