package git.doomshade.professions.data;

import com.google.common.collect.ImmutableList;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
public class DefaultsSettings extends AbstractProfessionSettings {

    private static final String DEFAULTS = "defaults", SORTED_BY = "sorted-by";
    private List<String> sortedBy;

    DefaultsSettings() {
    }

    @Override
    public void setup() {
        sortedBy = new ArrayList<>();
        ConfigurationSection section = getDefaultSection();

        if (section == null) {
            return;
        }
        ConfigurationSection defaultsSection = section.getConfigurationSection(DEFAULTS);
        if (defaultsSection != null) {
            this.sortedBy = defaultsSection.getStringList(SORTED_BY);
        } else {
            printError(DEFAULTS, null);
        }
    }

    public ImmutableList<String> getSortedBy() {
        return ImmutableList.copyOf(sortedBy);
    }
}
