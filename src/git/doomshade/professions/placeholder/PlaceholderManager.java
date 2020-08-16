package git.doomshade.professions.placeholder;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.ISetup;

import java.util.logging.Level;

/**
 * @author Doomshade
 * @version 1.0
 */
public class PlaceholderManager implements ISetup {
    private static final PlaceholderManager instance = new PlaceholderManager();
    private ProfessionPlaceholders placeholders = null;

    private PlaceholderManager() {
    }

    public static PlaceholderManager getInstance() {
        return instance;
    }

    @Override
    public void setup() {
        if (placeholders != null) return;

        placeholders = new ProfessionPlaceholders();
        if (!placeholders.register()) {
            String msg = "Failed to register placeholders";
            Professions.log(msg, Level.CONFIG);
            Professions.log(msg, Level.WARNING);
        }
    }
}
