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

package git.doomshade.professions.listeners;

import git.doomshade.professions.Professions;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.profession.professions.jewelcrafting.Gem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class JewelcraftingListener implements Listener {

    private static final HashMap<UUID, Gem> RIGHT_CLICK = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final Player player = event.getPlayer();
        final PlayerInventory inventory = player.getInventory();
        final ItemStack hand = inventory.getItemInMainHand();
        final Optional<Gem> opt = Gem.getGem(hand);
        final UUID uniqueId = player.getUniqueId();

        Messages.MessageBuilder msg = new Messages.MessageBuilder()
                .player(player);

        // right clicked with a gem in hand
        if (opt.isPresent()) {
            if (RIGHT_CLICK.containsKey(uniqueId)) {
                return;
            }
            player.sendMessage(msg.message(Messages.JewelcraftingMessages.CLICK_ON_ITEM_WITH_GEM_SLOT).build());
            RIGHT_CLICK.put(uniqueId, opt.get());
            return;
        }

        // check if player clicked before
        final Gem gem = RIGHT_CLICK.remove(uniqueId);

        // the player did did not
        if (gem == null) {
            return;
        }

        // did but no item in hand afterwards
        if (hand.getItemMeta() == null || hand.getType() == Material.AIR) {
            player.sendMessage(msg.message(Messages.JewelcraftingMessages.INVALID_ITEM).build());
            return;
        }

        final ItemStack barrier = new ItemStack(Material.BARRIER);

        // clicked a gem before and has an item in hand -> try to insert
        Gem.InsertResult result = gem.insert(hand);

        switch (result) {
            case INVALID_ITEM:
                player.sendMessage(msg.message(Messages.JewelcraftingMessages.INVALID_ITEM).build());
                break;
            case NO_GEM_SPACE:
                player.sendMessage(msg.message(Messages.JewelcraftingMessages.NO_GEM_SPACE).build());
                break;
            case SUCCESS:

                // fill player's armor with barriers so that the right click
                // does not equip the armor and then add gem
                // after 1 tick delay, remove barriers
                ItemStack[] armorContents = inventory.getArmorContents();
                ItemStack[] copy = Arrays.copyOf(armorContents, armorContents.length);
                for (int i = 0; i < copy.length; i++) {
                    ItemStack item = copy[i];
                    if (item == null) {
                        copy[i] = barrier;
                    }
                }
                inventory.setArmorContents(copy);
                inventory.removeItem(gem.getGem());
                inventory.setItemInMainHand(hand);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        inventory.setArmorContents(armorContents);
                    }
                }.runTaskLater(Professions.getInstance(), 1);
                player.sendMessage(msg.message(Messages.JewelcraftingMessages.ADDED_GEM_SUCCESSFUL).build());
                break;
        }
    }
}
