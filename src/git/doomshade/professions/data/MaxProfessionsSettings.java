package git.doomshade.professions.data;

import git.doomshade.professions.Profession;
import git.doomshade.professions.exceptions.ConfigurationException;
import org.bukkit.configuration.ConfigurationSection;

public class MaxProfessionsSettings extends AbstractProfessionSettings {

    private int maxPrimaryProfessions = 1, maxSecondaryProfessions = 1;

    @Override
    public void setup() throws ConfigurationException {

        super.setup();
        final String SECTION = "max-professions";

        ConfigurationSection section = getDefaultSection();

        ConfigurationSection maxSection = section.getConfigurationSection(SECTION);
        if (maxSection != null) {
            this.maxPrimaryProfessions = maxSection.getInt("primary", 1);
            this.maxSecondaryProfessions = maxSection.getInt("secondary", 1);
        } else {
            printError(SECTION, 1);
        }
    }

    public int getMaxProfessions(Profession.ProfessionType type) {
        return type == Profession.ProfessionType.PRIMARY ? maxPrimaryProfessions : maxSecondaryProfessions;
    }
}
