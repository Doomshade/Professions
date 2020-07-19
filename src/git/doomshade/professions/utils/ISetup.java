package git.doomshade.professions.utils;

import git.doomshade.professions.Professions;

/**
 * This interface is only for this plugin's purposes, not the API's, the {@link #setup()} method will not be called during {@link Professions#onEnable()} even if you register it!
 * <p>Note that both setup and cleanup methods could possibly return a boolean inducing the result, but perhaps it's too late for that (too lazy)</p>
 *
 * @author Doomshade
 * @version 1.0
 */
public interface ISetup {
    /**
     * A method in which the implementing class should put all needed data to memory. Called during {@link Professions#onEnable()}
     *
     * @throws Exception when needed
     */
    void setup() throws Exception;

    /**
     * A method in which the implementing class should clean up all the data. Called in {@link git.doomshade.professions.commands.ReloadCommand}
     *
     * @throws Exception when needed
     */
    default void cleanup() throws Exception {

    }

    /**
     * The default name of the class (similar to toString()). Used in logging.
     *
     * @return the default class name
     */
    default String getSetupName() {
        return getClass().getSimpleName();
    }
}
