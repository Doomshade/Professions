package git.doomshade.professions.data;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class ItemSettings extends AbstractSettings {
    private static final String SECTION = "items", LORE = "default-lore";
    private List<String> defaultLore = new ArrayList<>();

    ItemSettings() {

    }

    @Override
    public void setup() throws Exception {
        // TODO Auto-generated method stub
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
}
