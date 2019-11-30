package git.doomshade.professions.data;

import com.google.common.collect.ImmutableList;
import git.doomshade.professions.exceptions.ConfigurationException;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class DefaultsSettings extends AbstractProfessionSettings {

    private static final String DEFAULTS = "defaults", SORTED_BY = "sorted-by";
    private List<String> sortedBy = new ArrayList<>();

    DefaultsSettings() {
    }

    @Override
    public void setup() throws ConfigurationException {
        super.setup();

        ConfigurationSection section = getDefaultSection();

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

    @Override
    public void cleanup() {
        sortedBy = new ArrayList<>();
    }
}
