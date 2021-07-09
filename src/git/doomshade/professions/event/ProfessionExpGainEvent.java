package git.doomshade.professions.event;

import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a player receives exp
 *
 * @author Doomshade
 * @version 1.0
 */
public class ProfessionExpGainEvent extends AbstractProfessionEvent implements Cancellable {
    private static HandlerList handlerList = new HandlerList();
    private ItemType<?> source;
    private double exp;

    protected ProfessionExpGainEvent(UserProfessionData data) {
        super(data);
    }

    public ProfessionExpGainEvent(UserProfessionData data, ItemType<?> source, double exp) {
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

    public SkillupColor getSkillupColor() {
        return getUserProfessionData().getSkillupColor(getSource());
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

}
