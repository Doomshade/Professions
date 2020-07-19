package git.doomshade.professions.data;

import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.utils.ISetup;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.Serializable;
import java.util.logging.Level;

/**
 * Plugin specific settings. This class implements {@link Serializable} -> all fields and inner classes MUST be {@link Serializable}, too!
 *
 * @author Doomshade
 * @version 1.0
 */
public abstract class AbstractSettings implements ISetup, Serializable {
    transient static final Level LEVEL = Level.WARNING;
    protected transient static FileConfiguration config;
    private transient static Professions plugin = Professions.getInstance();
    transient static boolean outdated = false;

    static {
        loadConfig();
    }

    AbstractSettings() {
    }

    private static void loadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    protected void printError(String section, Object value) {
        if (!outdated) {
            Professions.log("Your configuration file is outdated!", LEVEL);
            outdated = true;
        }
        Professions.log(String.format("Missing \"%s\" section!", section), LEVEL);
        if (value == null)
            Professions.log("Using default values.", LEVEL);
        else
            Professions.log(String.format("Using %s as default value.", value.toString()), LEVEL);
    }

    protected final boolean isSection(String section, Object value) {
        boolean isSection = getDefaultSection().isConfigurationSection(section);
        if (!isSection) {
            printError(getDefaultSection().getCurrentPath() + "." + section, value);
        }
        return isSection;
    }

    private void assertSectionExists() throws ConfigurationException {
        if (getDefaultSection() == null) {
            throw new ConfigurationException();
        }
    }

    @Override
    public void setup() throws ConfigurationException {
        loadConfig();
        assertSectionExists();
    }

    @Override
    public String getSetupName() {
        return "settings";
    }

    protected ConfigurationSection getDefaultSection() {
        return config;
    }


    protected final boolean isSection(String section) {
        return isSection(section, null);
    }


}
