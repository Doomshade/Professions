package git.doomshade.professions.data;

import org.bukkit.configuration.ConfigurationSection;

public abstract class ProfessionSettings extends AbstractSettings {
    private static final String SECTION = "profession";

    @Override
    protected ConfigurationSection getDefaultSection() {
        if (config.isConfigurationSection(SECTION))
            return config.getConfigurationSection(SECTION);
        else {
            printError(SECTION, null);
            throw new IllegalStateException();
        }
    }

    @Override
    protected void printError(String section, Object value) {
        super.printError(SECTION + "." + section, value);
    }

    @Override
    protected boolean isSection(String section, Object value) {
        return super.isSection(SECTION + "." + section, value);
    }
}
