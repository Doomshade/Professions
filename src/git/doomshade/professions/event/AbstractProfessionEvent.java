package git.doomshade.professions.event;

import git.doomshade.professions.Profession;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * An extension of {@link Event}
 *
 * @author Doomshade
 * @version 1.0
 */
abstract class AbstractProfessionEvent extends Event implements Cancellable {
    protected final User user;
    protected final Profession<? extends IProfessionType> profession;
    protected final UserProfessionData userProfessionData;
    private boolean cancelled;

    protected AbstractProfessionEvent(UserProfessionData data) {
        this.user = data.getUser();
        this.profession = data.getProfession();
        userProfessionData = data;
        this.cancelled = false;
    }

    public final Profession<? extends IProfessionType> getProfession() {
        return profession;
    }

    public final User getUser() {
        return user;
    }

    public final UserProfessionData getUserProfessionData() {
        return userProfessionData;
    }

    @Override
    public final boolean isCancelled() {
        return cancelled;
    }

    @Override
    public final void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
