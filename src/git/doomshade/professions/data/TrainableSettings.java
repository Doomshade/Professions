package git.doomshade.professions.data;

import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.profession.types.ITrainable;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

import static git.doomshade.professions.utils.Strings.ITrainableEnum.VAR_TRAINABLE_COST;

public class TrainableSettings extends AbstractProfessionSettings {
    private static final String LEVEL_THRESHOLD = "level-threshold",
            TRAINABLE_SECTION = "trainable", TRAINED = "trained", NOT_TRAINED = "not-trained";

    private int levelThreshold = 3;
    private final ArrayList<String> trainedLore, notTrainedLore;

    TrainableSettings() {
        trainedLore = new ArrayList<>();
        notTrainedLore = new ArrayList<>();
    }

    @Override
    public void setup() throws ConfigurationException {
        super.setup();
        ConfigurationSection section = getDefaultSection();
        ConfigurationSection trainableSection = section.getConfigurationSection(TRAINABLE_SECTION);
        if (trainableSection != null) {
            levelThreshold = trainableSection.getInt(LEVEL_THRESHOLD);
            if (trainableSection.isList(TRAINED)) {
                trainedLore.addAll(trainableSection.getStringList(TRAINED));
                for (int i = 0; i < trainedLore.size(); i++) {
                    trainedLore.set(i, ChatColor.translateAlternateColorCodes('&', trainedLore.get(i)));
                }
            } else {
                printError(TRAINABLE_SECTION + "." + TRAINED, null);
            }
            if (trainableSection.isList(NOT_TRAINED)) {
                notTrainedLore.addAll(trainableSection.getStringList(NOT_TRAINED));
                for (int i = 0; i < notTrainedLore.size(); i++) {
                    notTrainedLore.set(i, ChatColor.translateAlternateColorCodes('&', notTrainedLore.get(i)));
                }
            } else {
                printError(TRAINABLE_SECTION + "." + NOT_TRAINED, null);
            }
        } else {
            printError(TRAINABLE_SECTION, null);
        }
    }

    @Override
    public void cleanup() {
        trainedLore.clear();
        notTrainedLore.clear();
    }

    public int getLevelThreshold() {
        return levelThreshold;
    }

    public List<String> getNotTrainedLore(ITrainable trainable) {
        for (int i = 0; i < notTrainedLore.size(); i++) {
            notTrainedLore.set(i, notTrainedLore.get(i).replaceAll(VAR_TRAINABLE_COST.s, String.valueOf(trainable.getCost())));
        }
        return notTrainedLore;
    }

    public List<String> getTrainedLore(ITrainable trainable) {
        for (int i = 0; i < trainedLore.size(); i++) {
            trainedLore.set(i, trainedLore.get(i).replaceAll(VAR_TRAINABLE_COST.s, String.valueOf(trainable.getCost())));
        }
        return trainedLore;
    }
}
