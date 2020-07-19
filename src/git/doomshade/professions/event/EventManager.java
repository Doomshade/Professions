package git.doomshade.professions.event;

import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.user.User;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * A helper class for event managing
 *
 * @author Doomshade
 * @version 1.0
 */
public final class EventManager {

    // singleton
    private static EventManager em = new EventManager();
    private static PluginManager pm = Bukkit.getPluginManager();

    private EventManager() {
    }

    public static EventManager getInstance() {
        return em;
    }

    /**
     * @param object        the generic type object inside an item type
     * @param itemTypeClass the item type class
     * @param <T>           the generic type object
     * @param <Item>        the item type
     * @return an item type if such exists or {@code null}
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T, Item extends ItemType<T>> Item getItemType(T object, Class<Item> itemTypeClass) {
        if (object == null) {
            return null;
        }
        for (ItemTypeHolder<?> itemHolder : Professions.getProfessionManager().getItemTypeHolders()) {
            if (!itemHolder.getItemType().getClass().equals(itemTypeClass)) {
                continue;
            }
            for (ItemType<?> item : itemHolder.getRegisteredItemTypes()) {
                Item itemReturn = (Item) item;
                T itemReturnObject = itemReturn.getObject();
                if (itemReturnObject == null) {
                    continue;
                }
                if (itemReturnObject.equals(object) || itemReturn.equalsObject(object)) {
                    return itemReturn;
                }
            }

        }
        return null;
    }

    /**
     * Calls a profession event that professions handle
     *
     * @param itemType the item type
     * @param user     the user
     * @param extras   extras if needed
     * @param <T>      the item type
     * @return the called profession event
     * @see ProfessionEvent#getExtras(Class)
     */
    public <T extends ItemType<?>> ProfessionEvent<T> callEvent(T itemType, User user, Object... extras) {
        return callEvent(getEvent(itemType, user, extras));
    }

    /**
     * Gets a profession event that professions handle (this does <b>NOT</b> call the event)
     *
     * @param itemType the item type
     * @param user     the user
     * @param extras   extras if needed
     * @param <T>      the item type
     * @return the called profession event
     * @see ProfessionEvent#getExtras(Class)
     */
    public <T extends ItemType<?>> ProfessionEvent<T> getEvent(T itemType, User user, Object... extras) {
        ProfessionEvent<T> pe = new ProfessionEvent<>(itemType, user);
        pe.setExtras(Arrays.asList(extras));
        return pe;
    }

    /**
     * Calls the event
     *
     * @param event the event to call
     * @param <T>   the item type
     * @return the called profession event
     */
    public <T extends ItemType<?>> ProfessionEvent<T> callEvent(ProfessionEvent<T> event) {
        pm.callEvent(event);
        return event;
    }
}
