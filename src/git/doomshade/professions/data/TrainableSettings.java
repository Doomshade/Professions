package git.doomshade.professions.data;

import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.profession.ITrainable;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static git.doomshade.professions.utils.Strings.ITrainableEnum.VAR_TRAINABLE_COST;

/**
 * Settings for trainable item types
 *
 * @author Doomshade
 * @version 1.0
 */
public class TrainableSettings extends AbstractProfessionSettings {
    private static final String TRAINABLE_SECTION = "trainable",
            TRAINED = "trained",
            NOT_TRAINED = "not-trained",
            CANNOT_TRAIN = "cannot-train";

    private final ArrayList<String> trainedLore, notTrainedLore, unableToTrainLore;

    TrainableSettings() {
        trainedLore = new ArrayList<>();
        notTrainedLore = new ArrayList<>();
        unableToTrainLore = new ArrayList<>();
    }

    @Override
    public void setup() throws ConfigurationException {
        super.setup();
        ConfigurationSection section = getDefaultSection();
        ConfigurationSection trainableSection = section.getConfigurationSection(TRAINABLE_SECTION);
        if (trainableSection != null) {
            initLore(trainableSection, TRAINED, trainedLore);
            initLore(trainableSection, NOT_TRAINED, notTrainedLore);
            initLore(trainableSection, CANNOT_TRAIN, unableToTrainLore);
        } else {
            printError(TRAINABLE_SECTION, null);
        }
    }

    private void initLore(ConfigurationSection trainableSection, String section, ArrayList<String> lore) {
        if (trainableSection.isList(section)) {
            lore.addAll(trainableSection.getStringList(section));
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
            }
        } else {
            printError(TRAINABLE_SECTION + "." + section, null);
        }
    }

    @Override
    public void cleanup() {
        trainedLore.clear();
        notTrainedLore.clear();
    }

    public List<String> getNotTrainedLore(ITrainable trainable) {
        return replaceStrings(trainable, notTrainedLore);
    }

    public List<String> getTrainedLore(ITrainable trainable) {
        return replaceStrings(trainable, trainedLore);
    }


    public List<String> getUnableToTrainLore(ITrainable trainable) {
        return replaceStrings(trainable, unableToTrainLore);
    }

    @NotNull
    private List<String> replaceStrings(ITrainable trainable, ArrayList<String> lore) {
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, lore.get(i).replaceAll(VAR_TRAINABLE_COST.s, String.valueOf(trainable.getCost())));
        }
        return lore;
    }
}
