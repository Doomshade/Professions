package git.doomshade.professions.data;

import git.doomshade.professions.exceptions.ConfigurationException;
import org.bukkit.configuration.ConfigurationSection;

public abstract class AbstractProfessionSettings extends AbstractSettings {
    private static final String SECTION = "profession";
    private static ConfigurationSection section = config.getConfigurationSection(SECTION);

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
