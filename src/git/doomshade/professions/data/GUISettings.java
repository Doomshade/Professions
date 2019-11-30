package git.doomshade.professions.data;

import git.doomshade.professions.exceptions.ConfigurationException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class GUISettings extends AbstractSettings {
    private static final String SECTION = "gui", PROFESSIONS_GUI_NAME = "professions-gui-name";
    private String professionsGuiName = "Professions";

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
    }

    public String getProfessionsGuiName() {
        return professionsGuiName;
    }

    @Override
    public String getSetupName() {
        return "GUI " + super.getSetupName();
    }
}
