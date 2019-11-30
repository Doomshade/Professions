package git.doomshade.professions.listeners;

import git.doomshade.professions.Professions;
import git.doomshade.professions.event.EventManager;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.gathering.GatherItem;
import git.doomshade.professions.profession.types.gathering.IGathering;
import git.doomshade.professions.user.User;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author Doomshade
 */
@SuppressWarnings("unused")
public abstract class AbstractProfessionListener implements Listener {

    private static final HashMap<UUID, List<UUID>> PICKUPS = new HashMap<>();
    private static final EventManager em = Professions.getEventManager();

    /**
     * Null-able
     *
     * @param type
     * @param player
     * @param extras
     * @return
     */
    private static final <T extends ItemType<?>> ProfessionEvent<T> callEvent(T type, Player player, Object... extras) {
        if (type != null) {
            return em.callEvent(type, User.getUser(player), extras);
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
    public void onGatherPickup(PlayerPickupItemEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player hrac = e.getPlayer();
        Item item = e.getItem();
        if (callEvent(hrac, item.getItemStack(), GatherItem.class, IGathering.class) == null) {
            return;
        }

        List<UUID> uuids = PICKUPS.getOrDefault(hrac.getUniqueId(), new ArrayList<>());
        uuids.add(item.getUniqueId());

        PICKUPS.put(hrac.getUniqueId(), uuids);

    }

    /**
     * Null-able
     *
     * @param player    the player that calls this event
     * @param item      the generic object of {@link ItemType}
     * @param itemClass the custom ItemType class
     * @param extras
     * @return
     */
    public final <Obj, T extends ItemType<Obj>> ProfessionEvent<T> callEvent(Player player, Obj item,
                                                                             Class<T> itemClass, Object... extras) {

        if (player == null || item == null) {
            return null;
        }

        T itemType = em.getItemType(item, itemClass);
        if (itemType == null) {
            return null;
        }

        return callEvent(itemType, player, extras);
    }

    protected final <T extends ItemType<?>> ProfessionEvent<T> callEvent(ProfessionEvent<T> event) {
        return em.callEvent(event);
    }

    /**
     * Null-able
     *
     * @param type
     * @param player
     * @param extras
     * @return
     */
    protected final <Obj, T extends ItemType<Obj>> ProfessionEvent<T> getEvent(Player player, Obj item,
                                                                               Class<T> itemClass, Object... extras) {
        if (player == null || item == null) {
            return null;
        }

        T itemType = em.getItemType(item, itemClass);
        if (itemType == null) {
            return null;
        }
        return em.getEvent(itemType, User.getUser(player), extras);
    }

}
