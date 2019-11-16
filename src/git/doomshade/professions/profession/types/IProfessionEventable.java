package git.doomshade.professions.profession.types;

import git.doomshade.professions.event.ProfessionEvent;

/**
 * Interface for this plugin's purposes, not the API's!
 *
 * @author Doomshade
 */
public interface IProfessionEventable {
    /**
     * Handles an event
     *
     * @param e the event called
     */
    <T extends ItemType<?>> void onEvent(ProfessionEvent<T> e);
}
