package git.doomshade.professions.data;

import git.doomshade.professions.exceptions.ConfigurationException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

/**
 * GUI settings
 *
 * @author Doomshade
 * @version 1.0
 * @see git.doomshade.professions.commands.PlayerGuiCommand
 */
public class GUISettings extends AbstractSettings {
    private static final String SECTION = "gui",
            PROFESSIONS_GUI_NAME = "professions-gui-name",
            LEVEL_THRESHOLD = "show-level-threshold",
            INFORMATION_SIGN_NAME = "information-sign-name";
    private String professionsGuiName = "Professions", signName = "Information";
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

        professionsGuiName = setupString(section, PROFESSIONS_GUI_NAME, professionsGuiName);
        signName = setupString(section, INFORMATION_SIGN_NAME, signName);

        if (!section.isInt(LEVEL_THRESHOLD)) {
            printError(LEVEL_THRESHOLD, 3);
        } else {
            this.levelThreshold = section.getInt(LEVEL_THRESHOLD);
        }
    }

    private String setupString(ConfigurationSection section, String path, String defaultName) {

        String str = "";
        if (!section.isString(path)) {
            printError(path, defaultName);
        } else {
            str = section.getString(path);
        }
        if (!Objects.requireNonNull(str).isEmpty()) {
            str = ChatColor.translateAlternateColorCodes('&', str);
        }
        return str;
    }


    public String getProfessionsGuiName() {
        return professionsGuiName;
    }

    public int getLevelThreshold() {
        return levelThreshold;
    }

    public String getSignName() {
        return signName;
    }

    @Override
    public String getSetupName() {
        return "GUI " + super.getSetupName();
    }
}
