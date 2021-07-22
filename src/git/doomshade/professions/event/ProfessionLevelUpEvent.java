package git.doomshade.professions.event;

import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player levels up
 *
 * @author Doomshade
 * @version 1.0
 */
public class ProfessionLevelUpEvent extends AbstractProfessionEvent {
    private static HandlerList handlerList = new HandlerList();
    private int before, after;

    protected ProfessionLevelUpEvent(UserProfessionData data) {
        super(data);
    }

    public ProfessionLevelUpEvent(UserProfessionData data, int before, int after) {
        super(data);
        this.before = before;
        this.after = after;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public int getBefore() {
        return before;
    }

    public int getAfter() {
        return after;
    }

}
