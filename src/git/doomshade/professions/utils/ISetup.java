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

    /**
     * A method in which the implementing class should clean up all the data. Called in {@link git.doomshade.professions.commands.ReloadCommand}
     */
    default void cleanup() throws Exception {

    }

    default String getSetupName() {
        return getClass().getSimpleName();
    }
}
