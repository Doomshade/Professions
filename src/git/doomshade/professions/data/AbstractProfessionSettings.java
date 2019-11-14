package git.doomshade.professions.data;

import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;

public abstract class AbstractProfessionSettings extends AbstractSettings {
    private static final String SECTION = "profession";

    @Override
    @Nullable
    protected ConfigurationSection getDefaultSection() {
        if (config.isConfigurationSection(SECTION))
            return config.getConfigurationSection(SECTION);
        else {
            printError(SECTION, null);
            return null;
        }
    }

    @Override
    protected void printError(String section, Object value) {
        super.printError(SECTION + "." + section, value);
    }
}