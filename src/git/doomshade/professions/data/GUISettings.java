package git.doomshade.professions.data;

import git.doomshade.professions.exceptions.ConfigurationException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class GUISettings extends AbstractSettings {
    private static final String SECTION = "gui", PROFESSIONS_GUI_NAME = "professions-gui-name", LEVEL_THRESHOLD = "show-level-threshold";
    private String professionsGuiName = "Professions";
    private int levelThreshold = 3;

    GUISettings() {
        super();
    }


    @Override
    protected ConfigurationSection getDefaultSection() {
        ConfigurationSection section = super.getDefaultSection();

        if (!section.isConfigurationSection(SECTION)) {
            super.printError(SECTION, null);
            return null;
        }

        return section.getConfigurationSection(SECTION);
    }

    @Override
    protected void printError(String section, Object value) {
        super.printError(SECTION + "." + section, value);
    }

    // TODO update gui settings on reload (have to add a method in GUIApi)
    @Override
    public void setup() throws ConfigurationException {
        super.setup();
        ConfigurationSection section = getDefaultSection();
        if (!section.isString(PROFESSIONS_GUI_NAME)) {
            printError(PROFESSIONS_GUI_NAME, professionsGuiName);
        } else {
            this.professionsGuiName = section.getString(PROFESSIONS_GUI_NAME);
        }
        if (!this.professionsGuiName.isEmpty()) {
            this.professionsGuiName = ChatColor.translateAlternateColorCodes('&', this.professionsGuiName);
        }

        if (!section.isInt(LEVEL_THRESHOLD)) {
            printError(LEVEL_THRESHOLD, 3);
        } else {
            this.levelThreshold = section.getInt(LEVEL_THRESHOLD);
        }
    }


    public String getProfessionsGuiName() {
        return professionsGuiName;
    }

    public int getLevelThreshold() {
        return levelThreshold;
    }

    @Override
    public String getSetupName() {
        return "GUI " + super.getSetupName();
    }
}
