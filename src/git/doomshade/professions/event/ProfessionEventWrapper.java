package git.doomshade.professions.event;

import git.doomshade.professions.profession.Profession;
import git.doomshade.professions.profession.types.ItemType;

/**
 * This is a workaround class for {@link ProfessionEvent} - this does NOT extend the {@link org.bukkit.event.Event} class thus this prevents the {@link org.bukkit.event.EventHandler}
 * from handling the event (just an insurance against possible handling of an event in {@link Profession} class other
 * than {@link Profession#handleEvent(ProfessionEvent)}. If the {@link org.bukkit.event.Event} class was an interface this would not be necessary.
 *
 * @param <T> the Item Type
 * @author Doomshade
 * @version 1.0
 */
public class ProfessionEventWrapper<T extends ItemType<?>> {
    public final ProfessionEvent<T> event;

    public ProfessionEventWrapper(ProfessionEvent<T> event) {
        this.event = event;
    }
}
