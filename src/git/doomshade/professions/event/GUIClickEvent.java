package git.doomshade.professions.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;

@Deprecated
public class GUIClickEvent extends Event {
    private static HandlerList handlerList = new HandlerList();
    private InventoryClickEvent e;
    private Player hrac;

    public GUIClickEvent(InventoryClickEvent e, Player hrac) {
        this.e = e;
        this.hrac = hrac;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public Player getPlayer() {
        return hrac;
    }

    public InventoryClickEvent getEvent() {
        return e;
    }

}
