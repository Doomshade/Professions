package git.doomshade.professions.event;

import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.event.HandlerList;

public class ProfessionExpLoseEvent extends AbstractProfessionEvent {
    private static HandlerList handlerList = new HandlerList();
    private double exp;

    protected ProfessionExpLoseEvent(UserProfessionData data) {
        super(data);
        // TODO Auto-generated constructor stub
    }

    public ProfessionExpLoseEvent(UserProfessionData data, double exp) {
        // TODO Auto-generated constructor stub
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
        // TODO Auto-generated method stub
        return handlerList;
    }

}
