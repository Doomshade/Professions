package git.doomshade.professions.profession.professions;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.mining.IMining;
import git.doomshade.professions.profession.types.mining.Ore;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MiningProfession extends Profession<IMining> {

    @Override
    public void onLoad() {
        setName("&aKopani");
        setProfessionType(ProfessionType.SEKUNDARNI);
    }

    @Override
    public void onPostLoad() {
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getColoredName());
        item.setItemMeta(meta);
        setIcon(item);
        addItems(Ore.class);
    }

    @Override
    public String getID() {
        return "mining";
    }

    @Override
    @EventHandler
    public <A extends ItemType<?>> void onEvent(ProfessionEvent<A> e) {
        if (!isValidEvent(e, Ore.class)) {
            return;
        }
        User hrac = e.getPlayer();
        UserProfessionData upd = hrac.getProfessionData(getClass());
        if (!playerMeetsRequirements(e)) {
            e.setCancelled(true);
            e.printErrorMessage(upd);
            return;
        }

        addExp(e);
    }

}
