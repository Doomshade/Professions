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
import git.doomshade.professions.api.IProfession;
import git.doomshade.professions.api.item.ext.ItemType;
import git.doomshade.professions.event.EventManager;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.user.User;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("unused")
public abstract class AbstractProfessionListener implements Listener {

    private static final Map<UUID, List<UUID>> PICKUPS = new HashMap<>();
    private static final EventManager EM = Professions.getEventManager();

    /**
     * @param type
     * @param player
     * @param extras
     *
     * @return
     */
    @SafeVarargs
    @Nullable
    private static <T extends ItemType<?>> ProfessionEvent<T> callEvent(T type, Player player, List<Object> extras, Class<?
            extends IProfession>... professions) {
        if (type != null) {
            return EM.callEvent(type, User.getUser(player), extras, professions);
        }
        return null;
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {

    }

    @EventHandler
    public void onEnchant(PlayerInteractEvent e) {
    }

    @EventHandler
    public void onGather(PlayerInteractEvent e) {

    }

    /**
     * @param e
     */
    @EventHandler
    public void onKill(EntityDeathEvent e) {

    }

    /**
     * @param e
     */
    @EventHandler
    public void onMine(BlockBreakEvent e) {

    }

    @EventHandler
    public void onGatherPickup(EntityPickupItemEvent e) {
        if (e.isCancelled() || !(e.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) e.getEntity();
        Item item = e.getItem();
        /*
        if (callEvent(player, new Herb(item.getItemStack(), null), HerbItemType.class, IGathering.class) == null) {
            return;
        }

        List<UUID> uuids = PICKUPS.getOrDefault(player.getUniqueId(), new ArrayList<>());
        uuids.add(item.getUniqueId());

        PICKUPS.put(player.getUniqueId(), uuids);
         */

    }

    /**
     * @param player        the player that calls this event
     * @param item          the generic object of {@link ItemType}
     * @param itemTypeClass the custom ItemType class
     * @param extras        the extras to retrieve in profession class
     *
     * @return an event that gets called or {@code null} if invalid parameters were provided
     */
    @SafeVarargs
    protected final <Obj extends ConfigurationSerializable, T extends ItemType<Obj>> ProfessionEvent<T> callEvent(
            Player player, Obj item,
            Class<T> itemTypeClass, List<Object> extras, Class<?
            extends IProfession>... professions) {
        final ProfessionEvent<T> event = getEvent(player, item, itemTypeClass, extras, professions);

        return event == null ? null : callEvent(event);
    }

    protected final <T extends ItemType<?>> ProfessionEvent<T> callEvent(ProfessionEvent<T> event) {
        return EM.callEvent(event);
    }

    /**
     * @param player        the player that calls this event
     * @param item          the generic object of {@link ItemType}
     * @param itemTypeClass the custom ItemType class
     * @param extras        the extras to retrieve in profession class
     *
     * @return an event or {@code null} if invalid parameters were provided
     */
    @SafeVarargs
    @Nullable
    protected final <Obj extends ConfigurationSerializable, T extends ItemType<Obj>> ProfessionEvent<T> getEvent(
            Player player, Obj item,
            Class<T> itemTypeClass, List<Object> extras, Class<?
            extends IProfession>... professions) {
        if (player == null || item == null) {
            return null;
        }

        T itemType = EM.getItemType(item, itemTypeClass);
        if (itemType == null) {
            return null;
        }
        return EM.getEvent(itemType, User.getUser(player), extras, professions);
    }

}
