package git.doomshade.professions.profession.professions;

import git.doomshade.professions.Profession;
import git.doomshade.professions.data.ProfessionSpecificDropSettings;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.mining.IMining;
import git.doomshade.professions.profession.types.mining.Ore;
import git.doomshade.professions.profession.types.mining.OreItemType;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class MiningProfession extends Profession<IMining> {

    @Override
    public void onLoad() {
        setName("&aKopani");
        setProfessionType(ProfessionType.SECONDARY);
    }

    @Override
    public void onPostLoad() {
        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getColoredName());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        setIcon(item);
        addItems(OreItemType.class);
    }


    @Override
    public String getID() {
        return "mining";
    }

    @Override
    @EventHandler
    public <A extends ItemType<?>> void onEvent(ProfessionEvent<A> ev) {
        if (!isValidEvent(ev, OreItemType.class)) {
            return;
        }
        ProfessionEvent<OreItemType> e = getEvent(ev, OreItemType.class);
        User hrac = e.getPlayer();
        UserProfessionData upd = hrac.getProfessionData(getClass());
        if (!playerMeetsRequirements(e)) {
            e.setCancelled(true);
            e.printErrorMessage(upd);
            return;
        }


        if (addExp(e)) {
            Location loc = e.getExtra(Location.class);
            Ore ore = e.getItemType().getObject();
            final ItemStack miningResult = ore.getMiningResult();

            miningResult.setAmount(getProfessionSettings().getSettings(ProfessionSpecificDropSettings.class).getDropAmount(upd, e.getItemType()));
            loc.getWorld().dropItem(loc, miningResult);


        }
    }


}
