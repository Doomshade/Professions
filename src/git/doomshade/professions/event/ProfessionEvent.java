package git.doomshade.professions.event;

import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * An event that professions handle
 *
 * @param <T> an item type
 * @author Doomshade
 * @version 1.0
 * @see git.doomshade.professions.Profession#onEvent(ProfessionEvent)
 */
public class ProfessionEvent<T extends ItemType<?>> extends Event implements Cancellable {
    private static HandlerList handlerList = new HandlerList();
    private final User user;
    private final Collection<Object> extras = new ArrayList<>();
    private T t;
    private boolean cancel = false;
    private int exp;
    private List<String> errorMessage;

    public ProfessionEvent(T t, User user) {
        this.t = t;
        this.user = user;
        this.exp = t.getExp();
        this.errorMessage = Professions.getProfessionManager().getItemTypeHolder(t.getClass()).getErrorMessage();
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public List<String> getErrorMessage(UserProfessionData upd) {
        return ItemUtils.getDescription(t, errorMessage, upd);
    }

    public void printErrorMessage(UserProfessionData upd) {
        List<String> message = getErrorMessage(upd);
        if (message.isEmpty()) {
            return;
        }
        upd.getUser().sendMessage(message.toArray(new String[0]));
    }

    public void addExtra(Object extra) {
        extras.add(extra);
    }

    public Collection<Object> getExtras() {
        return extras;
    }


    public void setExtras(Collection<Object> extras) {
        this.extras.clear();
        this.extras.addAll(extras);
    }

    @SuppressWarnings("unchecked")
    public <A> Collection<A> getExtras(Class<A> clazz) {
        Collection<A> coll = new ArrayList<>();
        Iterator<Object> it = extras.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o.getClass().getName().equals(clazz.getName())) {
                coll.add((A) o);
            }
        }

        return coll;
    }

    @Nullable
    public <A> A getExtra(Class<A> clazz) {
        Collection<A> collectionExtras = getExtras(clazz);
        if (collectionExtras.isEmpty()) {
            return null;
        }
        return collectionExtras.iterator().next();
    }

    public boolean hasExtra(Class<?> clazz) {
        return getExtra(clazz) != null;
    }

    public T getItemType() {
        return t;
    }

    public void setItemType(T t) {
        this.t = t;
    }

    public User getPlayer() {
        return user;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.cancel = arg0;
    }
}
