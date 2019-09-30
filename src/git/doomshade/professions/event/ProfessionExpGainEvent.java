package git.doomshade.professions.event;

import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class ProfessionExpGainEvent extends AbstractProfessionEvent implements Cancellable {
    private static HandlerList handlerList = new HandlerList();
    private ItemType<?> source;
    private double exp;

    protected ProfessionExpGainEvent(UserProfessionData data) {
        super(data);
    }

    public ProfessionExpGainEvent(UserProfessionData data, ItemType<?> source,
                                  double exp) {
        // TODO Auto-generated constructor stub
        this(data);
        this.source = source;
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

    public ItemType<?> getSource() {
        return source;
    }

    @Override
    public HandlerList getHandlers() {
        // TODO Auto-generated method stub
        return handlerList;
    }

}
