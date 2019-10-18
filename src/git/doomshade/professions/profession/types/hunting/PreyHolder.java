package git.doomshade.professions.profession.types.hunting;

import git.doomshade.professions.profession.types.ItemTypeHolder;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;

public class PreyHolder extends ItemTypeHolder<Prey> {

    @Override
    public void init() {
        // TODO Auto-generated method stub
        Prey prey = new Prey(new Mob(EntityType.SKELETON), 10);
        prey.setName(ChatColor.YELLOW + "Kostlivec");
        registerObject(prey);
    }

}
