/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package git.doomshade.professions.profession.professions.mining;

import git.doomshade.professions.api.profession.Profession;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.professions.smelting.SmeltingProfession;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class MiningProfession extends Profession {


    @Override
    public void onLoad() {
        utils.addItems(OreItemType.class);
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

    @Override
    public Collection<Class<? extends Profession>> getSubprofessions() {
        return Collections.singletonList(SmeltingProfession.class);
    }

    @Override
    public String getID() {
        return "mining";
    }

    @Override
    public <A extends ItemType<?>> void onEvent(ProfessionEventWrapper<A> event) {
        ProfessionLogger.log("Handling ore event...", Level.FINEST);

        final ProfessionEvent<OreItemType> e = utils.getEventUnsafe(event);

        final User user = e.getPlayer();
        final Player player = user.getPlayer();

        // if player does not have profession -> do not continue with experience/drops
        if (!utils.playerHasProfession(e)) {

            // if player at least has a permission of builder, do not cancel the event
            // cancel the event otherwise so players with no mining profession mine the ore!
            e.setCancelled(!Permissions.has(player, Permissions.BUILDER));
            ProfessionLogger.log(String.format("Player does not have the mining profession to mine %s...",
                    e.getItemType().getObject().getName()), Level.FINEST);
            return;
        }

        // UserProfessionData does not return null at this point as we checked that the player has this profession
        UserProfessionData upd = user.getProfessionData(getClass());
        if (!utils.playerMeetsLevelRequirements(e)) {
            e.setCancelled(true);
            e.printErrorMessage(upd);
            ProfessionLogger.log(String.format("Player does not meet requirements to mine %s...",
                    e.getItemType().getObject().getName()), Level.FINEST);
            return;
        }


        // if the event passes, drop ore regardless of XP
        if (!e.hasExtra(Location.class)) {
            ProfessionLogger.logError(new RuntimeException("Somehow mined an ore with a null location, this " +
                    "should not " +
                    "happen!"), true);
        }
        Location loc = e.getExtra(Location.class);
        final OreItemType itemType = e.getItemType();

        // FIXME npt exception
            /*int amount = getProfessionSettings().getSettings(ProfessionSpecificDropSettings.class)
                    .getDropAmount(upd, itemType);*/
        int amount = 1;
        Ore ore = itemType.getObject();

        if (ore == null) {
            return;
        }

        String message = player.getName() + " mined " + ore.getName();

        if (loc == null) {
            ProfessionLogger.logError(new RuntimeException("Somehow mined an ore with a null location, this " +
                    "should not " +
                    "happen!"), true);
            return;
        }

        final World world = loc.getWorld();
        if (world == null) {
            return;
        }

        // add Vector3 of 0.5 to the location so the drops do not fly away eks dee
        final Location dropLocation = loc.clone().add(0.5, 0.5, 0.5);

        // randomize drop for each drop amount
        for (int i = 0; i < amount; i++) {
            ItemStack miningResult = ore.getMiningResult();

            if (miningResult != null) {
                world.dropItem(dropLocation, miningResult);
            }
        }


        if (utils.addExp(e)) {
            message = message.concat(Utils.getReceiveXp(e.getExp()));
        }
        ProfessionLogger.log(message, Level.CONFIG);

    }

    @Override
    public boolean isSubprofession() {
        return false;
    }
}
