package git.doomshade.professions.event;

import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.event.HandlerList;

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
    public HandlerList getHandlers() {
        return handlerList;
    }

    public int getBefore() {
        return before;
    }

    public int getAfter() {
        return after;
    }

}
