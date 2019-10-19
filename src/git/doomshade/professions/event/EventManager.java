package git.doomshade.professions.event;

import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.user.User;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import javax.annotation.Nullable;
import java.util.Arrays;

public final class EventManager {

    private static EventManager em;
    private static PluginManager pm = Bukkit.getPluginManager();

    static {
        em = new EventManager();
    }

    private EventManager() {
    }

    public static EventManager getInstance() {
        return em;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T, Item extends ItemType<T>> Item getItemType(T object, Class<Item> itemTypeClass) {
        if (object == null) {
            return null;
        }
        String itemTypeClassName = itemTypeClass.getSimpleName();
        for (ItemTypeHolder<?> itemHolder : Professions.getProfessionManager().getItemTypeHolders().keySet()) {
            String[] typeNameSplit = itemHolder.getClass().getGenericSuperclass().getTypeName().split("[.]");
            String itemHolderItemTypeName = typeNameSplit[typeNameSplit.length - 1].substring(0, typeNameSplit[typeNameSplit.length - 1].length() - 1);
            if (!itemHolderItemTypeName.equalsIgnoreCase(itemTypeClassName)) {
                continue;
            }
            for (ItemType<?> item : itemHolder.getRegisteredItemTypes()) {
                Item itemReturn = (Item) item;
                T itemReturnObject = itemReturn.getObject();
                if (itemReturnObject == null) {
                    continue;
                }
                if (itemReturnObject.equals(object) || itemReturn.isValid(object)) {
                    return itemReturn;
                }
            }

        }
        return null;
    }

    public <T extends ItemType<?>> ProfessionEvent<T> callEvent(T t, User user, Object... extras) {
        return callEvent(getEvent(t, user, extras));
    }

    public <T extends ItemType<?>> ProfessionEvent<T> getEvent(T t, User user, Object... extras) {
        ProfessionEvent<T> pe = new ProfessionEvent<>(t, user);
        pe.setExtras(Arrays.asList(extras));
        return pe;
    }

    public <T extends ItemType<?>> ProfessionEvent<T> callEvent(ProfessionEvent<T> event) {
        // TODO Auto-generated method stub
        pm.callEvent(event);
        return event;
    }
}
