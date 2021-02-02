package git.doomshade.professions.profession.professions.mining.spawn;

import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.profession.professions.mining.OreItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.profession.utils.SpawnPoint;
import git.doomshade.professions.utils.Range;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            final ItemTypeHolder<OreItemType> itemTypeHolder = Professions.getProfessionManager().getItemTypeHolder(OreItemType.class);
            final ItemStack itemInHand = event.getItemInHand();
            if (itemInHand == null || !itemInHand.hasItemMeta() || !itemInHand.getItemMeta().hasDisplayName()) {
                return;
            }
            final String displayName = itemInHand.getItemMeta().getDisplayName();
            OreItemType oreItemType = Utils.findInIterable(itemTypeHolder.getRegisteredItemTypes(), x -> x.getIcon(null).getItemMeta().getDisplayName().equals(displayName));
            net.minecraft.server.v1_9_R1.ItemStack nms = CraftItemStack.asNMSCopy(itemInHand);
            final Location location = event.getBlock().getLocation();
            final Ore ore = oreItemType.getObject();
            if (ore != null) {
                if (nms.hasTag() && nms.getTag().hasKey("ignoreRange") && nms.getTag().getByte("ignoreRange") == 1) {
                    final SpawnPoint sp = new SpawnPoint(location, new Range(0));
                    ore.addSpawnPoint(sp);
                    try {
                        ore.getLocationOptions(sp).spawn();
                    } catch (SpawnException e) {
                        Professions.logError(e);
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
                respawnTime = Range.fromString(event.getMessage());
            } catch (Exception e) {
                Professions.logError(e);
            }
            if (ore != null && respawnTime != null) {
                final SpawnPoint sp = new SpawnPoint(oreLocation.location, respawnTime);
                ore.addSpawnPoint(sp);
                try {
                    ore.getLocationOptions(sp).spawn();
                } catch (SpawnException e) {
                    Professions.logError(e);
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
