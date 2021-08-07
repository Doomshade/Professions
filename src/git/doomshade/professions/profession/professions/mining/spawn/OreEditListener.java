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

package git.doomshade.professions.profession.professions.mining.spawn;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.api.Range;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.gui.mining.OreGUI;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.profession.professions.mining.OreItemType;
import git.doomshade.professions.api.spawn.ext.SpawnPoint;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class OreEditListener implements Listener {

    private static final HashMap<UUID, OreLocation> CHAT = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        if (CHAT.containsKey(uuid)) {
            event.setCancelled(true);
            player.sendMessage("You must first write range (either number or number-number) (e.g. 5 or 8-11)");
            return;
        }
        try {
            final ItemTypeHolder<Ore, OreItemType> itemTypeHolder =
                    Professions.getProfMan().getItemTypeHolder(OreItemType.class);
            final ItemStack itemInHand = event.getItemInHand();
            if (!itemInHand.hasItemMeta() || !Objects.requireNonNull(itemInHand.getItemMeta()).hasDisplayName()) {
                return;
            }
            final String displayName = itemInHand.getItemMeta().getDisplayName();
            OreItemType oreItemType = Utils.findInIterable(itemTypeHolder, x -> Objects.requireNonNull(
                    x.getIcon(null).getItemMeta()).getDisplayName().equals(displayName));

            final Location location = event.getBlock().getLocation();
            final Ore ore = oreItemType.getObject();
            if (ore != null) {
                final PersistentDataContainer pdc = itemInHand.getItemMeta().getPersistentDataContainer();

                final Byte b = pdc.get(OreGUI.NBT_KEY,
                        PersistentDataType.BYTE);
                if (pdc.has(OreGUI.NBT_KEY, PersistentDataType.BYTE) && b != null && b == 1) {
                    final SpawnPoint sp = new SpawnPoint(location, new Range(0), ore);
                    ore.addSpawnPoint(sp);
                    try {
                        sp.spawn();
                    } catch (SpawnException e) {
                        ProfessionLogger.logError(e);
                    }
                    player.sendMessage("Přidán nový spawn point pro " + ore.getName());
                } else {
                    CHAT.put(uuid, new OreLocation(ore, location));
                    player.sendMessage("Nyní napiš range (buď číslo nebo číslo-číslo) (např. 5 nebo 8-11)");
                }
            }
        } catch (Utils.SearchNotFoundException ignored) {
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        final OreLocation oreLocation = CHAT.remove(uuid);
        if (oreLocation != null) {
            event.setCancelled(true);
            Pattern rangePattern = Range.RANGE_PATTERN;
            Matcher matcher = rangePattern.matcher(event.getMessage());
            if (!matcher.find()) {
                CHAT.put(uuid, oreLocation);
                player.sendMessage("Neplatná range");
                return;
            }
            Ore ore = oreLocation.ore;
            Range respawnTime = null;
            try {
                respawnTime = Range.fromString(event.getMessage()).orElseThrow(() -> new IllegalArgumentException(
                        String.format("Could not get " +
                                "range from '%s'", event.getMessage())));
            } catch (Exception e) {
                ProfessionLogger.logError(e);
            }
            if (ore != null && respawnTime != null) {
                final SpawnPoint sp = new SpawnPoint(oreLocation.location, respawnTime, oreLocation.ore);
                ore.addSpawnPoint(sp);
                try {
                    sp.spawn();
                } catch (SpawnException e) {
                    ProfessionLogger.logError(e);
                }
                player.sendMessage("Přidán nový spawn point pro " + ore.getName());
            } else {
                player.sendMessage("Nastala neočekávaná chyba, ore == null || respawnTime == null");
            }

        }
    }

    private static class OreLocation {
        private final Ore ore;
        private final Location location;

        private OreLocation(Ore ore, Location location) {
            this.ore = ore;
            this.location = location;
        }
    }
}
