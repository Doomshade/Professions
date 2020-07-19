package git.doomshade.professions.data;

import git.doomshade.professions.exceptions.ConfigurationException;
import org.bukkit.configuration.ConfigurationSection;

import java.io.Serializable;

/**
 * Profession specific settings. This class implements {@link Serializable} - all fields and inner classes MUST be {@link Serializable}, too!
 *
 * @author Doomshade
 * @version 1.0
 */
public abstract class AbstractProfessionSettings extends AbstractSettings {
    private transient static final String SECTION = "profession";
    private transient static ConfigurationSection section = config.getConfigurationSection(SECTION);

    @Override
    protected ConfigurationSection getDefaultSection() {
        return section;
    }

    /**
     * Try to set the section first, then check if it exists
     *
     * @throws ConfigurationException if the section doesn't exist
     */
    @Override
    public void setup() throws ConfigurationException {
        section = config.getConfigurationSection(SECTION);
        super.setup();
    }

    @Override
    protected void printError(String section, Object value) {
        super.printError(SECTION + "." + section, value);
    }
}
