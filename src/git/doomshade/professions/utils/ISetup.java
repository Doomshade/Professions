package git.doomshade.professions.utils;

import git.doomshade.professions.Professions;

/**
 * This interface is only for this plugin's purposes, not the API's, the {@link #setup()} method will not be called during {@link Professions#onEnable()} even if you register it!
 *
 * @author Doomshade
 */
public interface ISetup {
    /**
     * A method in which the implementing class should put all needed data to memory. Called during {@link Professions#onEnable()}
     *
     * @throws Exception
     */
    void setup() throws Exception;
}
