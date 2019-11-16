package git.doomshade.professions.profession.professions;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.hunting.IHunting;
import git.doomshade.professions.profession.types.hunting.Prey;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SkinningProfession extends Profession<IHunting> {

    @Override
    public void onLoad() {
        setName("&6Skinning");
        setProfessionType(ProfessionType.SECONDARY);
    }

    @Override
    public void onPostLoad() {
        ItemStack item = new ItemStack(Material.LEATHER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getColoredName());
        item.setItemMeta(meta);
        setIcon(item);
    }

    @Override
    public String getID() {
        // TODO Auto-generated method stub
        return "skinning";
    }

    @Override
    @EventHandler
    public <A extends ItemType<?>> void onEvent(ProfessionEvent<A> e) {
        // TODO Auto-generated method stub
        ProfessionEvent<Prey> event = getEvent(e, Prey.class);
        if (!isValidEvent(event, Prey.class) || !playerMeetsRequirements(e)) {
            return;
        }
        addExp(event);
    }

}
