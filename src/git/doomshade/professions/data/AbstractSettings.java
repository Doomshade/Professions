package git.doomshade.professions.data;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.ISetup;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

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
        plugin.sendConsoleMessage("Your configuration file is outdated!");
        plugin.sendConsoleMessage(String.format("Missing \"%s\" section!", section));
        if (value == null)
            plugin.sendConsoleMessage("Using default values.");
        else
            plugin.sendConsoleMessage(String.format("Using %s as default value.", value.toString()));
    }

    protected boolean isSection(String section, Object value) {
        boolean isSection = getDefaultSection().isConfigurationSection(section);
        if (!isSection) {
            printError(section, value);
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
