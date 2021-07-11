package git.doomshade.professions.placeholder;

import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ISetup;

import java.util.logging.Level;

/**
 * @author Doomshade
 * @version 1.0
 */
public class PlaceholderManager implements ISetup {
    private static final PlaceholderManager instance = new PlaceholderManager();
    private ProfessionPlaceholders placeholders = null;
    private static boolean registered = false;

    private PlaceholderManager() {
    }

    public static PlaceholderManager getInstance() {
        return instance;
    }

    public static boolean usesPlaceholders() {
        return registered;
    }

    @Override
    public void setup() {
        if (placeholders != null && registered) return;

        placeholders = new ProfessionPlaceholders();

        if (!(registered = placeholders.register())) {
            String msg = "Failed to register placeholders";
            ProfessionLogger.log(msg, Level.CONFIG);
            ProfessionLogger.log(msg, Level.WARNING);
        }
    }
}
