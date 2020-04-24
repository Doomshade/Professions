package git.doomshade.professions.profession.professions.mining;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.data.ProfessionSpecificDropSettings;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.profession.types.IMining;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.logging.Level;

public final class MiningProfession extends Profession<IMining> {


    @Override
    public void onLoad() {
        addItems(OreItemType.class);
    }


    @Override
    public String getID() {
        return "mining";
    }

    @Override
    public <A extends ItemType<?>> void onEvent(ProfessionEventWrapper<A> event) {
        ProfessionEvent<OreItemType> e;
        try {
            e = getEvent(event.event, OreItemType.class);
        } catch (ClassCastException ex) {
            return;
        }

        final User user = e.getPlayer();
        final Player player = user.getPlayer();

        // if player does not have profession -> do not continue with experience/drops
        if (!playerHasProfession(e)) {

            // if player at least has a permission of builder, do not cancel the event
            // cancel the event otherwise so players with no mining profession mine the ore!
            e.setCancelled(!Permissions.has(player, Permissions.BUILDER));
            return;
        }

        // UserProfessionData does not return null at this point as we checked that the player has this profession
        UserProfessionData upd = user.getProfessionData(getClass());
        if (!playerMeetsLevelRequirements(e)) {
            e.setCancelled(true);
            e.printErrorMessage(upd);
            return;
        }


        // if the event passes, drop ore regardless of XP
        if (e.hasExtra(Location.class)) {
            Location loc = e.getExtra(Location.class);
            final OreItemType itemType = e.getItemType();
            int amount = getProfessionSettings().getSettings(ProfessionSpecificDropSettings.class).getDropAmount(upd, itemType);
            Ore ore = itemType.getObject();

            if (ore == null) {
                return;
            }

            String message = player.getName() + " mined " + ore.getName();

            if (loc == null) {
                Professions.log("Somehow mined an ore with a null location, this should not happen!", Level.WARNING);
                return;
            }

            final World world = loc.getWorld();

            // add Vector3 of 0.5 to the location so the drops do not fly away eks dee
            final Location dropLocation = loc.clone().add(0.5, 0.5, 0.5);

            // randomize drop for each drop amount
            for (int i = 0; i < amount; i++) {
                ItemStack miningResult = ore.getMiningResult();

                if (miningResult != null) {
                    world.dropItem(dropLocation, miningResult);
                }
            }


            if (addExp(e)) {
                message = message.concat(Utils.getReceiveXp(e.getExp()));
            }
            Professions.log(message, Level.CONFIG);
        }
    }

    @Override
    public List<String> getProfessionInformation(UserProfessionData upd) {
       /* CraftPlayer pl;
        pl.getAttribute(Attribute.).
        MobEffectList mel = MobEffects.FASTER_DIG;
        AttributeModifier mod;
        //Bukkit.getPlayer("").addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0));
        pl.getAttribute(Attribute.GENERIC_ARMOR).get
        AttributeInstance ai;
        AttributeBase base;*/
        return null;
    }
}
