package git.doomshade.professions.data;

import git.doomshade.professions.Profession;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Class for custom profession settings
 *
 * @param <T> the profession
 */
public class ProfessionSettings<T extends Profession<?>> extends AbstractProfessionSettings {
    private final T profession;

    ProfessionSettings(T profession) {
        this.profession = profession;
    }

    @Override
    protected ConfigurationSection getDefaultSection() {
        ConfigurationSection section = super.getDefaultSection();

        if (section.isConfigurationSection(profession.getID())) {
            return section.getConfigurationSection(profession.getID());
        } else {
            printError(profession.getID(), null);
            throw new IllegalStateException();
        }
    }

    @Override
    public void setup() throws Exception {

    }
}
