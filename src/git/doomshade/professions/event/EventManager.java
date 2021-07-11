package git.doomshade.professions.event;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;
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

    private static final EventManager em = new EventManager();
    private static final PluginManager pm = Bukkit.getPluginManager();

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

        // search in item type holders
        for (ItemTypeHolder<?, ?> itemHolder : Professions.getProfMan().getItemTypeHolders()) {

            // make sure we got the right item type
            if (!itemHolder.getItemType().getClass().equals(itemTypeClass)) {
                continue;
            }

            // loop through item types and search for one that equals to the
            for (ItemType<?> item : itemHolder) {
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
