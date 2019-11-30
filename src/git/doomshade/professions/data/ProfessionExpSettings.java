package git.doomshade.professions.data;

import git.doomshade.professions.enums.SkillupColor;
import git.doomshade.professions.exceptions.ConfigurationException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class ProfessionExpSettings extends AbstractProfessionSettings {

    private static final String
            EXP_SETTINGS = "exp-settings",
            COLOR = "color",
            CHANCE = "chance",
            COLOR_CHANGE_AFTER = "color-change-after";

    ProfessionExpSettings() {
    }

    @Override
    public void setup() throws ConfigurationException {
        super.setup();
        ConfigurationSection section = getDefaultSection();
        ConfigurationSection expSection = section.getConfigurationSection(EXP_SETTINGS);
        if (expSection != null) {
            for (String key : expSection.getKeys(false)) {
                ConfigurationSection colorSection = expSection.getConfigurationSection(key);
                for (SkillupColor skillupColor : SkillupColor.values()) {
                    if (skillupColor.name().equalsIgnoreCase(key)) {
                        skillupColor.setSkillupColor(ChatColor.getByChar(colorSection.getString(COLOR).charAt(0)), colorSection.getInt(COLOR_CHANGE_AFTER), colorSection.getDouble(CHANCE));
                    }
                }
            }
        } else {
            printError(EXP_SETTINGS, null);
        }
    }
}
