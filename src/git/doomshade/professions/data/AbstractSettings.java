package git.doomshade.professions.data;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.ISetup;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;

public abstract class AbstractSettings implements ISetup {
    protected static FileConfiguration config;
    private static Professions plugin = Professions.getInstance();

    static {
        loadConfig();
    }

    AbstractSettings() {
    }

    static void loadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    protected void printError(String section, Object value) {
        Professions.log("Your configuration file is outdated!", Level.CONFIG);
        Professions.log(String.format("Missing \"%s\" section!", section), Level.CONFIG);
        if (value == null)
            Professions.log("Using default values.", Level.CONFIG);
        else
            Professions.log(String.format("Using %s as default value.", value.toString()), Level.CONFIG);
    }

    protected final boolean isSection(String section, Object value) {
        boolean isSection = getDefaultSection().isConfigurationSection(section);
        if (!isSection) {
            printError(getDefaultSection().getCurrentPath() + "." + section, value);
        }
        return isSection;
    }

    protected ConfigurationSection getDefaultSection() {
        return config;
    }


    protected final boolean isSection(String section) {
        return isSection(section, null);
    }
}
