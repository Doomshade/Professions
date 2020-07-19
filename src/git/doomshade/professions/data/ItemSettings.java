package git.doomshade.professions.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Item default settings for item types
 *
 * @author Doomshade
 * @version 1.0
 */
public class ItemSettings extends AbstractSettings {
    private static final String SECTION = "items", LORE = "default-lore";
    private List<String> defaultLore = new ArrayList<>();

    ItemSettings() {

    }

    @Override
    public void setup() {
        if (!isSection(SECTION)) {
            return;
        }
        ConfigurationSection section = config.getConfigurationSection(SECTION);
        if (section.isSet(LORE)) {
            this.defaultLore = section.getStringList(LORE);
        }
    }

    public List<String> getDefaultLore() {
        return defaultLore;
    }

    @Override
    public String getSetupName() {
        return "item " + super.getSetupName();
    }
}
