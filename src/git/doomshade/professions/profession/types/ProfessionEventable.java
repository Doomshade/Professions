package git.doomshade.professions.profession.types;

import git.doomshade.professions.event.ProfessionEvent;

public interface ProfessionEventable {
    /**
     * Handles an event
     *
     * @param e the event called
     */
    <T extends ItemType<?>> void onEvent(ProfessionEvent<T> e);
}
