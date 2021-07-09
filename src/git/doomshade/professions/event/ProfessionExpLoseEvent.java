package git.doomshade.professions.event;

import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.event.HandlerList;
/**
 * Called when a player loses exp
 *
 * @author Doomshade
 * @version 1.0
 */
public class ProfessionExpLoseEvent extends AbstractProfessionEvent {
    private static HandlerList handlerList = new HandlerList();
    private double exp;

    protected ProfessionExpLoseEvent(UserProfessionData data) {
        super(data);
    }

    public ProfessionExpLoseEvent(UserProfessionData data, double exp) {
        this(data);
        this.exp = exp;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public double getExp() {
        return exp;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

}
